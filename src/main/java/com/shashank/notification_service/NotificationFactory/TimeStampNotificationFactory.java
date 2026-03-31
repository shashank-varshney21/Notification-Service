package com.shashank.notification_service.NotificationFactory;

import com.shashank.notification_service.NotificationCRUD.NotificationDecorators.TimestampNotification;
import com.shashank.notification_service.NotificationFactory.NotificationMessageFactory.INotificationMessageFactory;
import org.springframework.stereotype.Component;

@Component
public class TimeStampNotificationFactory implements INotificationFactory{

    @Override
    public TimestampNotification getNotification(String text, INotificationMessageFactory iNotificationMessageFactory) {
        return new TimestampNotification(text, iNotificationMessageFactory);
    }
}
