package com.shashank.notification_service.NotificationObserver.Observer;

import com.shashank.notification_service.DTO.NotificationEvent;
import com.shashank.notification_service.NotificationCRUD.INotification;
import com.shashank.notification_service.NotificationObserver.Observable.ConcreteObservable;
import org.springframework.kafka.core.KafkaTemplate;

public class NotificationEngine implements IObserver{

    private final ConcreteObservable concreteObservable;

    private final String name;
    private final String email;
    private final String phone;
    private final INotification notification;

    private final KafkaTemplate<Long, NotificationEvent> kafkaTemplate;
    private final String kafkaTopic;

    public NotificationEngine(ConcreteObservable concreteObservable, String name, String email, String phone, INotification notification, KafkaTemplate<Long, NotificationEvent> kafkaTemplate, String kafkaTopic) {
        this.concreteObservable = concreteObservable;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.notification = notification;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTopic = kafkaTopic;
    }

    @Override
    public void update() {
        NotificationEvent notificationEvent = new NotificationEvent();
        notificationEvent.setName(name);
        notificationEvent.setEmail(email);
        notificationEvent.setPhone(phone);
        notificationEvent.setId(notification.getContent().getId());
        notificationEvent.setText(notification.getContent().getText());
        if(notification.getContent().getCreatedAt() != null) {
            notificationEvent.setTimestamp(notification.getContent().getCreatedAt());
        }

        //Here u can have the logic to store notification in user file etc according to business logic.

        //Pushing Notification Event as a new record in kafka topic.

        try{
            kafkaTemplate.send(kafkaTopic, notification.getContent().getId(), notificationEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            System.out.println("Kafka send failed: " + ex.getMessage());
                        } else {
                            System.out.println("Message sent successfully");
                        }
                    });
        }catch(Exception e) {
            e.printStackTrace();
        }
//        return ResponseEntity.status(HttpStatus.OK).body("Message Queued");
    }
}