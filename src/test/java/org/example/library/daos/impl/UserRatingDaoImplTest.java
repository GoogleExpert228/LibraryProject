package org.example.library.daos.impl;

import org.example.library.entities.UserRating;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRatingDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<UserRating> query;

    private UserRatingDaoImpl userRatingDao;
    private UserRating testUserRating;

    @BeforeEach
    void setUp() throws Exception {
        // Create DAO instance using helper to avoid static initialization
        userRatingDao = DaoTestHelper.createInstanceWithoutInitialization(UserRatingDaoImpl.class);
        // Use helper to set the sessionFactory field
        DaoTestHelper.setSessionFactory(userRatingDao, sessionFactory);

        testUserRating = new UserRating();
        testUserRating.setId(1L);
    }

    @Test
    void testFindByReaderId_RatingExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(UserRating.class))).thenReturn(query);
        when(query.setParameter("rid", 1L)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(testUserRating));

        Optional<UserRating> result = userRatingDao.findByReaderId(1L);

        assertTrue(result.isPresent());
        assertEquals(testUserRating, result.get());
        verify(session).createQuery("from UserRating ur where ur.reader.id = :rid", UserRating.class);
        verify(query).setParameter("rid", 1L);
        verify(query).uniqueResultOptional();
        verify(session).close();
    }

    @Test
    void testFindByReaderId_RatingNotExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(UserRating.class))).thenReturn(query);
        when(query.setParameter("rid", 999L)).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        Optional<UserRating> result = userRatingDao.findByReaderId(999L);

        assertFalse(result.isPresent());
        verify(session).createQuery("from UserRating ur where ur.reader.id = :rid", UserRating.class);
        verify(query).setParameter("rid", 999L);
        verify(query).uniqueResultOptional();
        verify(session).close();
    }

    @Test
    void testFindByReaderId_MultipleCalls() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(UserRating.class))).thenReturn(query);
        when(query.setParameter(anyString(), anyLong())).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(testUserRating));

        Optional<UserRating> result1 = userRatingDao.findByReaderId(1L);
        Optional<UserRating> result2 = userRatingDao.findByReaderId(2L);

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        verify(session, times(2)).createQuery("from UserRating ur where ur.reader.id = :rid", UserRating.class);
        verify(query, times(2)).uniqueResultOptional();
        verify(session, times(2)).close();
    }
}

