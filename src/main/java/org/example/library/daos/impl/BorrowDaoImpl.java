package org.example.library.daos.impl;

import org.example.library.daos.BorrowDao;
import org.example.library.entities.Borrow;
import org.example.library.enums.BorrowStatus;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

public class BorrowDaoImpl extends GenericHibernateDao<Borrow, Long> implements BorrowDao {
    @Override
    public List<Borrow> findActiveByReader(Long readerId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from Borrow br where br.reader.id = :rid and br.borrowStatus = :st", Borrow.class)
                    .setParameter("rid", readerId)
                    .setParameter("st", BorrowStatus.ACTIVE)
                    .list();
        }
    }

    @Override
    public List<Borrow> findOverdue(LocalDate today) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from Borrow br where br.borrowStatus = :st and br.dueDate < :today and br.returnDate is null",
                            Borrow.class)
                    .setParameter("st", BorrowStatus.ACTIVE)
                    .setParameter("today", today)
                    .list();
        }
    }

    @Override
    public List<Borrow> findByBookAndStatus(Long bookId, BorrowStatus status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from Borrow br where br.book.id = :bid and br.borrowStatus = :st", Borrow.class)
                    .setParameter("bid", bookId)
                    .setParameter("st", status)
                    .list();
        }
    }

    @Override
    public List<Borrow> findAllByReader(Long readerId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "from Borrow br where br.reader.id = :rid", Borrow.class)
                    .setParameter("rid", readerId)
                    .list();
        }    }
}






