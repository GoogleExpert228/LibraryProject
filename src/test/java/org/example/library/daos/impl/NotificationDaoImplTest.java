package org.example.library.daos.impl;

import org.example.library.entities.Notification;
import org.example.library.enums.NotificationType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<Notification> query;

    private NotificationDaoImpl notificationDao;
    private Notification testNotification;

    @BeforeEach
    void setUp() throws Exception {
        // Create DAO instance using helper to avoid static initialization
        notificationDao = DaoTestHelper.createInstanceWithoutInitialization(NotificationDaoImpl.class);
        // Use helper to set the sessionFactory field
        DaoTestHelper.setSessionFactory(notificationDao, sessionFactory);

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setMessage("Test notification");
        testNotification.setType(NotificationType.BOOK_OVERDUE);
        testNotification.setTimeStamp(LocalDate.now());
    }

    @Test
    void testFindByRecipient_Success() {
        Notification notification1 = new Notification();
        Notification notification2 = new Notification();
        List<Notification> notifications = Arrays.asList(notification1, notification2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Notification.class))).thenReturn(query);
        when(query.setParameter("uid", 1L)).thenReturn(query);
        when(query.list()).thenReturn(notifications);

        List<Notification> result = notificationDao.findByRecipient(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from Notification n where n.recipient.id = :uid", Notification.class);
        verify(query).setParameter("uid", 1L);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByRecipient_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Notification.class))).thenReturn(query);
        when(query.setParameter("uid", 1L)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Notification> result = notificationDao.findByRecipient(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from Notification n where n.recipient.id = :uid", Notification.class);
        verify(query).setParameter("uid", 1L);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByType_Success() {
        Notification notification1 = new Notification();
        notification1.setType(NotificationType.BOOK_OVERDUE);
        Notification notification2 = new Notification();
        notification2.setType(NotificationType.BOOK_OVERDUE);
        List<Notification> notifications = Arrays.asList(notification1, notification2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Notification.class))).thenReturn(query);
        when(query.setParameter("tp", NotificationType.BOOK_OVERDUE)).thenReturn(query);
        when(query.list()).thenReturn(notifications);

        List<Notification> result = notificationDao.findByType(NotificationType.BOOK_OVERDUE);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from Notification n where n.type = :tp", Notification.class);
        verify(query).setParameter("tp", NotificationType.BOOK_OVERDUE);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByType_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Notification.class))).thenReturn(query);
        when(query.setParameter("tp", NotificationType.NEW_READER_REQUEST)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Notification> result = notificationDao.findByType(NotificationType.NEW_READER_REQUEST);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from Notification n where n.type = :tp", Notification.class);
        verify(query).setParameter("tp", NotificationType.NEW_READER_REQUEST);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByType_AllTypes() {
        for (NotificationType type : NotificationType.values()) {
            Notification notification = new Notification();
            notification.setType(type);
            List<Notification> notifications = List.of(notification);

            when(sessionFactory.openSession()).thenReturn(session);
            when(session.createQuery(anyString(), eq(Notification.class))).thenReturn(query);
            when(query.setParameter("tp", type)).thenReturn(query);
            when(query.list()).thenReturn(notifications);

            List<Notification> result = notificationDao.findByType(type);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(type, result.get(0).getType());
            verify(query).setParameter("tp", type);
            verify(query).list();
            verify(session).close();

            reset(sessionFactory, session, query);
        }
    }
}

