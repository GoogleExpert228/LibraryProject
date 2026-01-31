package org.example.library.daos.impl;

import org.example.library.entities.Reader;
import org.example.library.entities.User;
import org.example.library.enums.Role;
import org.example.library.enums.UserStatus;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<User> query;

    private UserDaoImpl userDao;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // Create DAO instance using helper to avoid static initialization
        userDao = DaoTestHelper.createInstanceWithoutInitialization(UserDaoImpl.class);
        // Use helper to set the sessionFactory field
        DaoTestHelper.setSessionFactory(userDao, sessionFactory);

        testUser = new Reader();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.READER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRegistrationDate(LocalDateTime.now());
    }

    @Test
    void testFindByUsername_UserExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("username", "testuser")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(testUser));

        Optional<User> result = userDao.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(session).createQuery("from User u where u.username = :username", User.class);
        verify(query).setParameter("username", "testuser");
        verify(query).uniqueResultOptional();
        verify(session).close();
    }

    @Test
    void testFindByUsername_UserNotExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("username", "nonexistent")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        Optional<User> result = userDao.findByUsername("nonexistent");

        assertFalse(result.isPresent());
        verify(session).createQuery("from User u where u.username = :username", User.class);
        verify(query).setParameter("username", "nonexistent");
        verify(query).uniqueResultOptional();
        verify(session).close();
    }

    @Test
    void testFindByRole_Success() {
        User user1 = new Reader();
        user1.setRole(Role.READER);
        User user2 = new Reader();
        user2.setRole(Role.READER);
        List<User> users = Arrays.asList(user1, user2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("role", Role.READER)).thenReturn(query);
        when(query.list()).thenReturn(users);

        List<User> result = userDao.findByRole(Role.READER);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from User u where u.role = :role", User.class);
        verify(query).setParameter("role", Role.READER);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByRole_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("role", Role.ADMIN)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<User> result = userDao.findByRole(Role.ADMIN);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from User u where u.role = :role", User.class);
        verify(query).setParameter("role", Role.ADMIN);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByStatus_Success() {
        User user1 = new Reader();
        user1.setStatus(UserStatus.ACTIVE);
        User user2 = new Reader();
        user2.setStatus(UserStatus.ACTIVE);
        List<User> users = Arrays.asList(user1, user2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("status", UserStatus.ACTIVE)).thenReturn(query);
        when(query.list()).thenReturn(users);

        List<User> result = userDao.findByStatus(UserStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from User u where u.status = :status", User.class);
        verify(query).setParameter("status", UserStatus.ACTIVE);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByStatus_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(User.class))).thenReturn(query);
        when(query.setParameter("status", UserStatus.BLOCKED)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<User> result = userDao.findByStatus(UserStatus.BLOCKED);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from User u where u.status = :status", User.class);
        verify(query).setParameter("status", UserStatus.BLOCKED);
        verify(query).list();
        verify(session).close();
    }
}

