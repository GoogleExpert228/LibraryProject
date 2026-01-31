package org.example.library.daos.impl;

import org.example.library.contracts.daos.BookDao;
import org.example.library.entities.Book;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BookDaoImpl extends GenericHibernateDao<Book, Long> implements BookDao {
    @Override
    public Optional<Book> findByInventoryNumber(String inventoryNumber) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Book b where b.inventoryNumber = :inv", Book.class)
                    .setParameter("inv", inventoryNumber)
                    .uniqueResultOptional();
        }
    }

    @Override
    public List<Book> findAvailable() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Book b where b.isAvailable = true and b.archivedDate is null", Book.class)
                    .list();
        }
    }

    @Override
    public List<Book> findArchived() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Book b where b.archivedDate is not null", Book.class)
                    .list();
        }
    }

    @Override
    public List<Book> registeredBefore(LocalDate date) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Book b where b.registrationDate < :d", Book.class)
                    .setParameter("d", date)
                    .list();
        }
    }
}
