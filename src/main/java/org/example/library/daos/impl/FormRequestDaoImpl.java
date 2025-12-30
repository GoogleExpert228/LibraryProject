package org.example.library.daos.impl;

import org.example.library.daos.FormRequestDao;
import org.example.library.entities.FormRequest;
import org.example.library.entities.User;
import org.example.library.enums.FormStatus;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class FormRequestDaoImpl extends GenericHibernateDao<FormRequest, Long> implements FormRequestDao {
    @Override
    public List<FormRequest> findByStatus(FormStatus status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from FormRequest fr where fr.status = :st", FormRequest.class)
                    .setParameter("st", status)
                    .list();
        }
    }

    @Override
    public List<FormRequest> submittedBetween(LocalDate startInclusive, LocalDate endInclusive) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from FormRequest fr where fr.submitDate between :start and :end",
                            FormRequest.class)
                    .setParameter("start", startInclusive)
                    .setParameter("end", endInclusive)
                    .list();
        }
    }

    @Override
    public List<FormRequest> findBySubmittedBy(User user) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from FormRequest fr where fr.submittedBy = :user", FormRequest.class)
                    .setParameter("user", user)
                    .list();
        }
    }
}




