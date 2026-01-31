package org.example.library.daos.impl;

import org.example.library.entities.Book;
import org.example.library.enums.BookCondition;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<Book> query;

    private BookDaoImpl bookDao;
    private Book testBook;

    @BeforeEach
    void setUp() throws Exception {
        // Create DAO instance using helper to avoid static initialization
        bookDao = DaoTestHelper.createInstanceWithoutInitialization(BookDaoImpl.class);
        // Use helper to set the sessionFactory field
        DaoTestHelper.setSessionFactory(bookDao, sessionFactory);

        testBook = new Book();
        testBook.setId(1L);
        testBook.setInventoryNumber("INV-001");
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setCondition(BookCondition.GOOD);
        testBook.setAvailable(true);
        testBook.setRegistrationDate(LocalDate.now());
    }

    @Test
    void testFindByInventoryNumber_BookExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.setParameter("inv", "INV-001")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.of(testBook));

        Optional<Book> result = bookDao.findByInventoryNumber("INV-001");

        assertTrue(result.isPresent());
        assertEquals(testBook, result.get());
        verify(session).createQuery("from Book b where b.inventoryNumber = :inv", Book.class);
        verify(query).setParameter("inv", "INV-001");
        verify(query).uniqueResultOptional();
        verify(session).close();
    }

    @Test
    void testFindByInventoryNumber_BookNotExists() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.setParameter("inv", "INV-999")).thenReturn(query);
        when(query.uniqueResultOptional()).thenReturn(Optional.empty());

        Optional<Book> result = bookDao.findByInventoryNumber("INV-999");

        assertFalse(result.isPresent());
        verify(session).createQuery("from Book b where b.inventoryNumber = :inv", Book.class);
        verify(query).setParameter("inv", "INV-999");
        verify(query).uniqueResultOptional();
        verify(session).close();
    }

    @Test
    void testFindAvailable_Success() {
        Book book1 = new Book();
        book1.setAvailable(true);
        book1.setArchivedDate(null);
        Book book2 = new Book();
        book2.setAvailable(true);
        book2.setArchivedDate(null);
        List<Book> books = Arrays.asList(book1, book2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.list()).thenReturn(books);

        List<Book> result = bookDao.findAvailable();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from Book b where b.isAvailable = true and b.archivedDate is null", Book.class);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindAvailable_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Book> result = bookDao.findAvailable();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from Book b where b.isAvailable = true and b.archivedDate is null", Book.class);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindArchived_Success() {
        Book book1 = new Book();
        book1.setArchivedDate(LocalDate.now());
        Book book2 = new Book();
        book2.setArchivedDate(LocalDate.now().minusDays(10));
        List<Book> books = Arrays.asList(book1, book2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.list()).thenReturn(books);

        List<Book> result = bookDao.findArchived();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from Book b where b.archivedDate is not null", Book.class);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindArchived_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Book> result = bookDao.findArchived();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from Book b where b.archivedDate is not null", Book.class);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testRegisteredBefore_Success() {
        LocalDate cutoffDate = LocalDate.now();
        Book book1 = new Book();
        book1.setRegistrationDate(cutoffDate.minusDays(10));
        Book book2 = new Book();
        book2.setRegistrationDate(cutoffDate.minusDays(5));
        List<Book> books = Arrays.asList(book1, book2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.setParameter("d", cutoffDate)).thenReturn(query);
        when(query.list()).thenReturn(books);

        List<Book> result = bookDao.registeredBefore(cutoffDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from Book b where b.registrationDate < :d", Book.class);
        verify(query).setParameter("d", cutoffDate);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testRegisteredBefore_EmptyList() {
        LocalDate cutoffDate = LocalDate.now();

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Book.class))).thenReturn(query);
        when(query.setParameter("d", cutoffDate)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Book> result = bookDao.registeredBefore(cutoffDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from Book b where b.registrationDate < :d", Book.class);
        verify(query).setParameter("d", cutoffDate);
        verify(query).list();
        verify(session).close();
    }
}

