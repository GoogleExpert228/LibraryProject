package org.example.library.daos.impl;

import org.example.library.entities.Borrow;
import org.example.library.enums.BorrowStatus;
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
class BorrowDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<Borrow> query;

    private BorrowDaoImpl borrowDao;
    private Borrow testBorrow;

    @BeforeEach
    void setUp() throws Exception {
        // Create DAO instance using helper to avoid static initialization
        borrowDao = DaoTestHelper.createInstanceWithoutInitialization(BorrowDaoImpl.class);
        // Use helper to set the sessionFactory field
        DaoTestHelper.setSessionFactory(borrowDao, sessionFactory);

        testBorrow = new Borrow();
        testBorrow.setId(1L);
        testBorrow.setBorrowStatus(BorrowStatus.ACTIVE);
        testBorrow.setBorrowDate(LocalDate.now());
        testBorrow.setDueDate(LocalDate.now().plusDays(14));
    }

    @Test
    void testFindActiveByReader_Success() {
        Borrow borrow1 = new Borrow();
        borrow1.setBorrowStatus(BorrowStatus.ACTIVE);
        Borrow borrow2 = new Borrow();
        borrow2.setBorrowStatus(BorrowStatus.ACTIVE);
        List<Borrow> borrows = Arrays.asList(borrow1, borrow2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Borrow.class))).thenReturn(query);
        when(query.setParameter("rid", 1L)).thenReturn(query);
        when(query.setParameter("st", BorrowStatus.ACTIVE)).thenReturn(query);
        when(query.list()).thenReturn(borrows);

        List<Borrow> result = borrowDao.findActiveByReader(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery(
                "from Borrow br where br.reader.id = :rid and br.borrowStatus = :st", Borrow.class);
        verify(query).setParameter("rid", 1L);
        verify(query).setParameter("st", BorrowStatus.ACTIVE);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindActiveByReader_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Borrow.class))).thenReturn(query);
        when(query.setParameter("rid", 1L)).thenReturn(query);
        when(query.setParameter("st", BorrowStatus.ACTIVE)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Borrow> result = borrowDao.findActiveByReader(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery(
                "from Borrow br where br.reader.id = :rid and br.borrowStatus = :st", Borrow.class);
        verify(query).setParameter("rid", 1L);
        verify(query).setParameter("st", BorrowStatus.ACTIVE);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindOverdue_Success() {
        LocalDate today = LocalDate.now();
        Borrow borrow1 = new Borrow();
        borrow1.setBorrowStatus(BorrowStatus.ACTIVE);
        borrow1.setDueDate(today.minusDays(5));
        borrow1.setReturnDate(null);
        Borrow borrow2 = new Borrow();
        borrow2.setBorrowStatus(BorrowStatus.ACTIVE);
        borrow2.setDueDate(today.minusDays(10));
        borrow2.setReturnDate(null);
        List<Borrow> borrows = Arrays.asList(borrow1, borrow2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Borrow.class))).thenReturn(query);
        when(query.setParameter("st", BorrowStatus.ACTIVE)).thenReturn(query);
        when(query.setParameter("today", today)).thenReturn(query);
        when(query.list()).thenReturn(borrows);

        List<Borrow> result = borrowDao.findOverdue(today);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery(
                "from Borrow br where br.borrowStatus = :st and br.dueDate < :today and br.returnDate is null",
                Borrow.class);
        verify(query).setParameter("st", BorrowStatus.ACTIVE);
        verify(query).setParameter("today", today);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindOverdue_EmptyList() {
        LocalDate today = LocalDate.now();

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Borrow.class))).thenReturn(query);
        when(query.setParameter("st", BorrowStatus.ACTIVE)).thenReturn(query);
        when(query.setParameter("today", today)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Borrow> result = borrowDao.findOverdue(today);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery(
                "from Borrow br where br.borrowStatus = :st and br.dueDate < :today and br.returnDate is null",
                Borrow.class);
        verify(query).setParameter("st", BorrowStatus.ACTIVE);
        verify(query).setParameter("today", today);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByBookAndStatus_Success() {
        Borrow borrow1 = new Borrow();
        borrow1.setBorrowStatus(BorrowStatus.ACTIVE);
        Borrow borrow2 = new Borrow();
        borrow2.setBorrowStatus(BorrowStatus.ACTIVE);
        List<Borrow> borrows = Arrays.asList(borrow1, borrow2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Borrow.class))).thenReturn(query);
        when(query.setParameter("bid", 1L)).thenReturn(query);
        when(query.setParameter("st", BorrowStatus.ACTIVE)).thenReturn(query);
        when(query.list()).thenReturn(borrows);

        List<Borrow> result = borrowDao.findByBookAndStatus(1L, BorrowStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery(
                "from Borrow br where br.book.id = :bid and br.borrowStatus = :st", Borrow.class);
        verify(query).setParameter("bid", 1L);
        verify(query).setParameter("st", BorrowStatus.ACTIVE);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByBookAndStatus_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(Borrow.class))).thenReturn(query);
        when(query.setParameter("bid", 1L)).thenReturn(query);
        when(query.setParameter("st", BorrowStatus.RETURNED)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<Borrow> result = borrowDao.findByBookAndStatus(1L, BorrowStatus.RETURNED);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery(
                "from Borrow br where br.book.id = :bid and br.borrowStatus = :st", Borrow.class);
        verify(query).setParameter("bid", 1L);
        verify(query).setParameter("st", BorrowStatus.RETURNED);
        verify(query).list();
        verify(session).close();
    }
}

