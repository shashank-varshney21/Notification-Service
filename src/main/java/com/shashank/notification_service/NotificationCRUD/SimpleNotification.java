package com.shashank.notification_service.NotificationCRUD;

import com.shashank.notification_service.DTO.NotiMessageDto;
import com.shashank.notification_service.NotificationFactory.NotificationMessageFactory.INotificationMessageFactory;

public class SimpleNotification implements INotification{

    private final NotiMessageDto message;
    private final INotificationMessageFactory iNotificationMessageFactory;

    public SimpleNotification(String text, INotificationMessageFactory iNotificationMessageFactory) {
        this.iNotificationMessageFactory  = iNotificationMessageFactory;
        this.message = iNotificationMessageFactory.getNotificationMessage(text);
    }

    @Override
    public NotiMessageDto getContent() {
        return message;
    }
}
