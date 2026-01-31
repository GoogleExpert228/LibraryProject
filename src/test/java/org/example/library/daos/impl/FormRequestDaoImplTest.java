package org.example.library.daos.impl;

import org.example.library.entities.FormRequest;
import org.example.library.entities.User;
import org.example.library.enums.FormStatus;
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
class FormRequestDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<FormRequest> query;

    private FormRequestDaoImpl formRequestDao;
    private FormRequest testFormRequest;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // Create DAO instance using helper to avoid static initialization
        formRequestDao = DaoTestHelper.createInstanceWithoutInitialization(FormRequestDaoImpl.class);
        // Use helper to set the sessionFactory field
        DaoTestHelper.setSessionFactory(formRequestDao, sessionFactory);

        testUser = new User() {};
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testFormRequest = new FormRequest();
        testFormRequest.setId(1L);
        testFormRequest.setContent("Test request content");
        testFormRequest.setStatus(FormStatus.PENDING);
        testFormRequest.setSubmitDate(LocalDate.now());
        testFormRequest.setSubmittedBy(testUser);
    }

    @Test
    void testFindByStatus_Success() {
        FormRequest request1 = new FormRequest();
        request1.setStatus(FormStatus.PENDING);
        FormRequest request2 = new FormRequest();
        request2.setStatus(FormStatus.PENDING);
        List<FormRequest> requests = Arrays.asList(request1, request2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(FormRequest.class))).thenReturn(query);
        when(query.setParameter("st", FormStatus.PENDING)).thenReturn(query);
        when(query.list()).thenReturn(requests);

        List<FormRequest> result = formRequestDao.findByStatus(FormStatus.PENDING);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from FormRequest fr where fr.status = :st", FormRequest.class);
        verify(query).setParameter("st", FormStatus.PENDING);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindByStatus_EmptyList() {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(FormRequest.class))).thenReturn(query);
        when(query.setParameter("st", FormStatus.REJECTED)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<FormRequest> result = formRequestDao.findByStatus(FormStatus.REJECTED);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from FormRequest fr where fr.status = :st", FormRequest.class);
        verify(query).setParameter("st", FormStatus.REJECTED);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testSubmittedBetween_Success() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        FormRequest request1 = new FormRequest();
        request1.setSubmitDate(startDate.plusDays(2));
        FormRequest request2 = new FormRequest();
        request2.setSubmitDate(startDate.plusDays(5));
        List<FormRequest> requests = Arrays.asList(request1, request2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(FormRequest.class))).thenReturn(query);
        when(query.setParameter("start", startDate)).thenReturn(query);
        when(query.setParameter("end", endDate)).thenReturn(query);
        when(query.list()).thenReturn(requests);

        List<FormRequest> result = formRequestDao.submittedBetween(startDate, endDate);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery(
                "from FormRequest fr where fr.submitDate between :start and :end", FormRequest.class);
        verify(query).setParameter("start", startDate);
        verify(query).setParameter("end", endDate);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testSubmittedBetween_EmptyList() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(FormRequest.class))).thenReturn(query);
        when(query.setParameter("start", startDate)).thenReturn(query);
        when(query.setParameter("end", endDate)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<FormRequest> result = formRequestDao.submittedBetween(startDate, endDate);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery(
                "from FormRequest fr where fr.submitDate between :start and :end", FormRequest.class);
        verify(query).setParameter("start", startDate);
        verify(query).setParameter("end", endDate);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testSubmittedBetween_SameStartAndEnd() {
        LocalDate date = LocalDate.now();
        FormRequest request = new FormRequest();
        request.setSubmitDate(date);
        List<FormRequest> requests = List.of(request);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(FormRequest.class))).thenReturn(query);
        when(query.setParameter("start", date)).thenReturn(query);
        when(query.setParameter("end", date)).thenReturn(query);
        when(query.list()).thenReturn(requests);

        List<FormRequest> result = formRequestDao.submittedBetween(date, date);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(session).createQuery(
                "from FormRequest fr where fr.submitDate between :start and :end", FormRequest.class);
        verify(query).setParameter("start", date);
        verify(query).setParameter("end", date);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindBySubmittedBy_Success() {
        FormRequest request1 = new FormRequest();
        request1.setSubmittedBy(testUser);
        FormRequest request2 = new FormRequest();
        request2.setSubmittedBy(testUser);
        List<FormRequest> requests = Arrays.asList(request1, request2);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(FormRequest.class))).thenReturn(query);
        when(query.setParameter("user", testUser)).thenReturn(query);
        when(query.list()).thenReturn(requests);

        List<FormRequest> result = formRequestDao.findBySubmittedBy(testUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(session).createQuery("from FormRequest fr where fr.submittedBy = :user", FormRequest.class);
        verify(query).setParameter("user", testUser);
        verify(query).list();
        verify(session).close();
    }

    @Test
    void testFindBySubmittedBy_EmptyList() {
        User anotherUser = new User() {};
        anotherUser.setId(2L);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.createQuery(anyString(), eq(FormRequest.class))).thenReturn(query);
        when(query.setParameter("user", anotherUser)).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        List<FormRequest> result = formRequestDao.findBySubmittedBy(anotherUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(session).createQuery("from FormRequest fr where fr.submittedBy = :user", FormRequest.class);
        verify(query).setParameter("user", anotherUser);
        verify(query).list();
        verify(session).close();
    }
}

