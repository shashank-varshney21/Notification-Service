package com.shashank.notification_service.NotificationStrategy.KafkaConsumers;

import com.shashank.notification_service.DTO.NotificationEvent;
import com.shashank.notification_service.NotificationStrategy.CoreServices.EmailService;
import com.shashank.notification_service.NotificationStrategy.INotificationStrategy;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailStrategy implements INotificationStrategy {

    private final EmailService emailService;

    @Override
    @KafkaListener(topics = "notification", groupId = "notification-service")
    @Retry(name = "notificationRetry", fallbackMethod = "sendNotificationFallback")
    public void sendNotification(NotificationEvent event) {
        try{
            emailService.sendSimpleEmail(event.getEmail(), event.getText());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    //Above method consumes kafka topic

    public void sendNotificationFallback(NotificationEvent event, Throwable throwable) {
        log.info("Fallback occurred due to : {}", throwable.getMessage());
    }
}