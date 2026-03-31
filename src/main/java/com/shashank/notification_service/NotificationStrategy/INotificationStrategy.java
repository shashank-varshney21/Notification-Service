package com.shashank.notification_service.NotificationStrategy;

import com.shashank.notification_service.DTO.NotificationEvent;
import org.springframework.http.ResponseEntity;

public interface INotificationStrategy {
    void sendNotification(NotificationEvent event);
}
