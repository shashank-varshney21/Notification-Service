package com.shashank.notification_service.NotificationFactory;

import com.shashank.notification_service.NotificationCRUD.INotification;
import com.shashank.notification_service.NotificationCRUD.SimpleNotification;
import com.shashank.notification_service.NotificationFactory.NotificationMessageFactory.INotificationMessageFactory;

public interface INotificationFactory {
     INotification getNotification(String text, INotificationMessageFactory iNotificationMessageFactory);
}
