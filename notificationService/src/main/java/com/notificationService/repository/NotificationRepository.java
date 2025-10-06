package com.notificationService.repository;

import com.notificationService.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>{

}
