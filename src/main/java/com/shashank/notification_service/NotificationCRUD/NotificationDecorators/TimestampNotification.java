package com.shashank.notification_service.NotificationCRUD.NotificationDecorators;

import com.shashank.notification_service.DTO.NotiMessageDto;
import com.shashank.notification_service.NotificationFactory.NotificationMessageFactory.INotificationMessageFactory;

public class TimestampNotification implements INotificationDecorator {

    private final NotiMessageDto message;
    private final INotificationMessageFactory iNotificationMessageFactory;

    public TimestampNotification(String text, INotificationMessageFactory iNotificationMessageFactory) {
        this.iNotificationMessageFactory = iNotificationMessageFactory;
        this.message = iNotificationMessageFactory.getNotificationMessage(text);
    }
    @Override
    public NotiMessageDto getContent() {
        return message;
    }
}
