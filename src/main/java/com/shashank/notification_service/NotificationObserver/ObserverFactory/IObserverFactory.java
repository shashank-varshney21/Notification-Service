package com.shashank.notification_service.NotificationObserver.ObserverFactory;

import com.shashank.notification_service.NotificationCRUD.INotification;
import com.shashank.notification_service.NotificationObserver.Observable.ConcreteObservable;
import com.shashank.notification_service.NotificationObserver.Observer.NotificationEngine;
import org.springframework.stereotype.Component;

public interface IObserverFactory {
    NotificationEngine create(ConcreteObservable observable, String name, String email, String phone, INotification notification);
}
