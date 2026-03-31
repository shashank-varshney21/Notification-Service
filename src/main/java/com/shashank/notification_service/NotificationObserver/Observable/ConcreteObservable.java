package com.shashank.notification_service.NotificationObserver.Observable;

import com.shashank.notification_service.DTO.ObserverDto;
import com.shashank.notification_service.Entites.Observer;
import com.shashank.notification_service.NotificationCRUD.INotification;
import com.shashank.notification_service.NotificationObserver.Observer.NotificationEngine;
import com.shashank.notification_service.NotificationObserver.ObserverFactory.IObserverFactory;
import com.shashank.notification_service.Repositories.ObserverRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcreteObservable implements IObservable{

    //Important thing to know, In Observer pattern -> concrete Observable class maintains a List of Observers (concrete).
    //but code do not look like that. Here we maintain a composition of Observer Repository. So that whenever we
    //want List of observers we can have it as - List<Observers> list = repository.findAll. Likewise we can
    //remove it from list.

    private final ObserverRepository observerRepository;
    private final ModelMapper modelMapper;
    private final IObserverFactory factory;

    @Override
    public void addObserver(ObserverDto observerDto) {
        Observer newObserver = modelMapper.map(observerDto, Observer.class);
        observerRepository.save(newObserver);
//        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Observer saved successfully");
    }

    @Override
    public void removeObserver(ObserverDto observerDto) {
        Observer observer = modelMapper.map(observerDto, Observer.class);
        observerRepository.deleteById(observer.getId());
//        return ResponseEntity.status(HttpStatus.OK).body("Observer deleted successfully");
    }

    @Override
    public void notifyObservers(INotification notification) {
        List<Observer> list = observerRepository.findAll();
        for (Observer o : list) {
            NotificationEngine notificationEngine = factory.create(this, o.getName(), o.getEmail(), o.getPhone(), notification);
            notificationEngine.update();
        }
//        return ResponseEntity.status(HttpStatus.OK).body("Observers Notified Successfully");
    }
}
