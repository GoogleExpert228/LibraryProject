package org.example.library.daos;

import org.example.library.entities.Notification;
import org.example.library.enums.NotificationType;

import java.util.List;

public interface NotificationDao extends GenericDao<Notification, Long> {
    List<Notification> findByRecipient(Long userId);
    List<Notification> findByType(NotificationType type);
}


