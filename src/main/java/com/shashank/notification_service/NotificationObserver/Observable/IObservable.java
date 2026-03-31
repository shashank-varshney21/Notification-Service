package com.shashank.notification_service.NotificationObserver.Observable;

import com.shashank.notification_service.DTO.ObserverDto;
import com.shashank.notification_service.NotificationCRUD.INotification;
import org.springframework.http.ResponseEntity;

public interface IObservable {

    void addObserver(ObserverDto userDto);

    void removeObserver(ObserverDto userDto);

    void notifyObservers(INotification notification);
}
