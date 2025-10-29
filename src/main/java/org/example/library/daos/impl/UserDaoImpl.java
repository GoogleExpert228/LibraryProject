package org.example.library.daos.impl;

import org.example.library.daos.UserDao;
import org.example.library.entities.User;
import org.example.library.enums.Role;
import org.example.library.enums.UserStatus;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl extends GenericHibernateDao<User, Long> implements UserDao {
    @Override
    public Optional<User> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> q = session.createQuery(
                    "from User u where u.username = :username", User.class);
            q.setParameter("username", username);
            return q.uniqueResultOptional();
        }
    }

    @Override
    public List<User> findByRole(Role role) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User u where u.role = :role", User.class)
                    .setParameter("role", role)
                    .list();
        }
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from User u where u.status = :status", User.class)
                    .setParameter("status", status)
                    .list();
        }
    }
}


