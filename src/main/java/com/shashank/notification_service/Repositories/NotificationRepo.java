package com.shashank.notification_service.Repositories;

import com.shashank.notification_service.Entites.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
}
