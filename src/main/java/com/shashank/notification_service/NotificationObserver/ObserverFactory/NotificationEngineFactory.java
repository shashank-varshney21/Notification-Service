package com.shashank.notification_service.NotificationObserver.ObserverFactory;

import com.shashank.notification_service.DTO.NotificationEvent;
import com.shashank.notification_service.NotificationCRUD.INotification;
import com.shashank.notification_service.NotificationObserver.Observable.ConcreteObservable;
import com.shashank.notification_service.NotificationObserver.Observer.NotificationEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationEngineFactory implements IObserverFactory {
    private final KafkaTemplate<Long, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topic.notification}")
    private String kafkaTopic;

    public NotificationEngineFactory(KafkaTemplate<Long, NotificationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public NotificationEngine create(ConcreteObservable observable,
                                     String name,
                                     String email,
                                     Long phone,
                                     INotification notification) {

        return new NotificationEngine(
                observable,
                name,
                email,
                phone,
                notification,
                kafkaTemplate,
                kafkaTopic
        );
    }
}