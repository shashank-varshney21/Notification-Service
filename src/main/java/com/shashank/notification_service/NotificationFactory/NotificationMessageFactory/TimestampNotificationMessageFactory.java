package com.shashank.notification_service.NotificationFactory.NotificationMessageFactory;

import com.shashank.notification_service.DTO.NotiMessageDto;
import com.shashank.notification_service.NotificationFactory.INotificationFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TimestampNotificationMessageFactory implements INotificationMessageFactory {
    @Override
    public NotiMessageDto getNotificationMessage(String text) {
        LocalDateTime now = LocalDateTime.now();
        return new NotiMessageDto(text, now);
    }
}
