package com.shashank.notification_service.NotificationFactory;

import com.shashank.notification_service.NotificationCRUD.SimpleNotification;
import com.shashank.notification_service.NotificationFactory.NotificationMessageFactory.INotificationMessageFactory;
import org.springframework.stereotype.Component;

@Component
public class SimpleNotificationFactory implements INotificationFactory{

    @Override
    public SimpleNotification getNotification(String text, INotificationMessageFactory iNotificationMessageFactory) {
        return new SimpleNotification(text, iNotificationMessageFactory);
    }
}