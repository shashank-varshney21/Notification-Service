package com.shashank.notification_service.Services;

import com.shashank.notification_service.DTO.NotiMessageDto;
import com.shashank.notification_service.DTO.RequestObserverDto;
import com.shashank.notification_service.Entites.Notification;
import com.shashank.notification_service.Entites.Observer;
import com.shashank.notification_service.NotificationCRUD.INotification;
import com.shashank.notification_service.NotificationCRUD.NotificationDecorators.TimestampNotification;
import com.shashank.notification_service.NotificationFactory.INotificationFactory;
import com.shashank.notification_service.NotificationFactory.NotificationMessageFactory.INotificationMessageFactory;
import com.shashank.notification_service.NotificationObserver.Observable.IObservable;
import com.shashank.notification_service.Repositories.NotificationRepo;
import com.shashank.notification_service.Repositories.ObserverRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final INotificationFactory SimpleNotificationFactory;
    private final INotificationMessageFactory SimpleNotificationMessageFactory;
    private final IObservable ConcreteObservable;
    private final NotificationRepo notificationRepo;
    private final ModelMapper modelMapper;
    private final ObserverRepository observerRepository;

    public NotiMessageDto createSimpleNotification(String text) {
        INotification notification = SimpleNotificationFactory.getNotification(text, SimpleNotificationMessageFactory);
        //Also save this notification
        Notification notificationToBeSave = modelMapper.map(notification.getContent(), Notification.class);
        notificationRepo.save(notificationToBeSave);
        return notification.getContent();
    }

    public NotiMessageDto createTimestampNotification(String text) {
        // Returning object with 'new' because this is how it is done in decorator pattern.
        INotification notification = new TimestampNotification(SimpleNotificationFactory.getNotification(text, SimpleNotificationMessageFactory));
        //Also save this notification
        Notification notificationToBeSave = modelMapper.map(notification.getContent(), Notification.class);
        notificationRepo.save(notificationToBeSave);
        return notification.getContent();
    }

    public ResponseEntity<String> sendNotification(NotiMessageDto notiMessageDto) {
        INotification notification = SimpleNotificationFactory.getNotification(notiMessageDto.getText(), SimpleNotificationMessageFactory);
        ConcreteObservable.notifyObservers(notification);
        return ResponseEntity.status(HttpStatus.OK).body("Notifications queued");
    }

    public ResponseEntity<String> createObserver(RequestObserverDto requestObserverDto) {
        Observer observer = modelMapper.map(requestObserverDto, Observer.class);
        observerRepository.save(observer);
        return ResponseEntity.status(HttpStatus.OK).body("SUCCESS");
    }
}