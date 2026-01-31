package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.NotificationDao;
import org.example.library.daos.impl.NotificationDaoImpl;
import org.example.library.entities.Notification;
import org.example.library.enums.NotificationType;

import java.time.LocalDate;
import java.util.List;

public class NotificationService implements LibraryService {
    NotificationDao notificationDao = new NotificationDaoImpl();

    public Notification createNotification(NotificationType type, String message, Long recipientId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setTimeStamp(LocalDate.now());

        if (recipientId != null) {
            notification.setRecipient(ServiceFactory.service(UserService.class).requireUser(recipientId));
        }

        return notificationDao.save(notification);
    }

    public List<Notification> loadNotifications() {
        return notificationDao.findAll(Notification.class);
    }
}
