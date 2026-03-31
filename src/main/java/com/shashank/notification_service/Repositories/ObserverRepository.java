package com.shashank.notification_service.Repositories;

import com.shashank.notification_service.Entites.Observer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObserverRepository extends JpaRepository<Observer, Long> {
}
