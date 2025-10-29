package org.example.library.daos.impl;

import org.example.library.daos.NotificationDao;
import org.example.library.entities.Notification;
import org.example.library.enums.NotificationType;
import org.hibernate.Session;

import java.util.List;

public class NotificationDaoImpl extends GenericHibernateDao<Notification, Long> implements NotificationDao {
    @Override
    public List<Notification> findByRecipient(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Notification n where n.recipient.id = :uid", Notification.class)
                    .setParameter("uid", userId)
                    .list();
        }
    }

    @Override
    public List<Notification> findByType(NotificationType type) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Notification n where n.type = :tp", Notification.class)
                    .setParameter("tp", type)
                    .list();
        }
    }
}


