package org.example.library.daos.impl;

import org.example.library.entities.Book;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenericHibernateDaoTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<Book> query;

    private TestGenericHibernateDao testDao;
    private Book testBook;

    @BeforeEach
    void setUp() throws Exception {
        // Create DAO instance using helper to avoid static initialization
        testDao = DaoTestHelper.createInstanceWithoutInitialization(TestGenericHibernateDao.class);
        // Use helper to set the sessionFactory field
        DaoTestHelper.setSessionFactory(testDao, sessionFactory);
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
    }

    @Test
    void testSave_Success() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        Book result = testDao.save(testBook);

        assertNotNull(result);
        assertEquals(testBook, result);
        verify(session).persist(testBook);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void testSave_Exception_Rollback() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        doThrow(new RuntimeException("Database error")).when(session).persist(any());

        assertThrows(RuntimeException.class, () -> testDao.save(testBook));
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(session).close();
    }

    @Test
    void testUpdate_Success() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.merge(testBook)).thenReturn(testBook);

        Book result = testDao.update(testBook);

        assertNotNull(result);
        assertEquals(testBook, result);
        verify(session).merge(testBook);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void testUpdate_Exception_Rollback() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        doThrow(new RuntimeException("Database error")).when(session).merge(any());

        assertThrows(RuntimeException.class, () -> testDao.update(testBook));
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(session).close();
    }

    @Test
    void testDelete_Success() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);

        testDao.delete(testBook);

        verify(session).remove(testBook);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void testDelete_Exception_Rollback() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        doThrow(new RuntimeException("Database error")).when(session).remove(any());

        assertThrows(RuntimeException.class, () -> testDao.delete(testBook));
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(session).close();
    }

    @Test
    void testDeleteById_EntityExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.find(Book.class, 1L)).thenReturn(testBook);

        testDao.deleteById(Book.class, 1L);

        verify(session).find(Book.class, 1L);
        verify(session).remove(testBook);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void testDeleteById_EntityNotExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.find(Book.class, 1L)).thenReturn(null);

        testDao.deleteById(Book.class, 1L);

        verify(session).find(Book.class, 1L);
        verify(session, never()).remove(any());
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDeleteById_Exception_Rollback() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
        doThrow(new RuntimeException("Database error")).when(session).find(any(Class.class), any());

        assertThrows(RuntimeException.class, () -> testDao.deleteById(Book.class, 1L));
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(session).close();
    }

    @Test
    void testFindById_EntityExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.find(Book.class, 1L)).thenReturn(testBook);

        Optional<Book> result = testDao.findById(Book.class, 1L);

        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());
        verify(session).find(Book.class, 1L);
        verify(session).close();
    }

    @Test
    void testFindById_EntityNotExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.find(Book.class, 1L)).thenReturn(null);

        Optional<Book> result = testDao.findById(Book.class, 1L);

        assertFalse(result.isPresent());
        verify(session).find(Book.class, 1L);
        verify(session).close();
    }

    @Test
    void testFindAll_Success() {
        List<Book> books = Arrays.asList(testBook, new Book());
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.list()).thenReturn(books);

        List<Book> result = testDao.findAll(Book.class);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from Book", Book.class);
        verify(query).list();
        verify(session).close();
    }

    // Concrete implementation for testing abstract class
    private static class TestGenericHibernateDao extends GenericHibernateDao<Book, Long> {
        // This class is used only for testing the abstract GenericHibernateDao
    }
}

