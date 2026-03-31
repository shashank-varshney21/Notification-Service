package com.shashank.notification_service.NotificationFactory.NotificationMessageFactory;

import com.shashank.notification_service.DTO.NotiMessageDto;

public interface INotificationMessageFactory {
    NotiMessageDto getNotificationMessage(String text);
}
