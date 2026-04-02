package com.shashank.notification_service.NotificationCRUD.NotificationDecorators;

import com.shashank.notification_service.DTO.NotiMessageDto;
import com.shashank.notification_service.NotificationCRUD.INotification;

import java.time.LocalDateTime;

public class TimestampNotification implements INotificationDecorator {

    private final INotification simpleNotification;

    public TimestampNotification(INotification simpleNotification) {
        this.simpleNotification = simpleNotification;
    }

    @Override
    public NotiMessageDto getContent() {
        NotiMessageDto original = simpleNotification.getContent();

        NotiMessageDto copy = new NotiMessageDto(original.getText());
        copy.setCreatedAt(LocalDateTime.now());

        return copy;
    }
}
