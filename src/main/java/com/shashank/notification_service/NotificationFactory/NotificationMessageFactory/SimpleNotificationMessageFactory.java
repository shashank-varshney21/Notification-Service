package com.shashank.notification_service.NotificationFactory.NotificationMessageFactory;

import com.shashank.notification_service.DTO.NotiMessageDto;
import com.shashank.notification_service.NotificationFactory.INotificationFactory;
import org.springframework.stereotype.Component;

@Component("SimpleNotificationMessageFactory")
public class SimpleNotificationMessageFactory implements INotificationMessageFactory {

    @Override
    public NotiMessageDto getNotificationMessage(String text) {
        return new NotiMessageDto(text);
    }
}
