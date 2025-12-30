package org.example.library.daos.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.example.library.daos.UserDao;
import org.example.library.entities.User;
import org.example.library.enums.Role;
import org.example.library.enums.UserStatus;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl extends GenericHibernateDao<User, Long> implements UserDao {

    @Override
    public Optional<User> findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<? extends User> root = cq.from(User.class);

            cq.select(root)
                    .where(cb.equal(root.get("username"), username));

            return session.createQuery(cq).uniqueResultOptional();
        }
    }

    @Override
    public List<User> findByRole(Role role) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<? extends User> root = cq.from(User.class);

            cq.select(root)
                    .where(cb.equal(root.get("role"), role));

            return session.createQuery(cq).getResultList();
        }
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cq = cb.createQuery(User.class);
            Root<? extends User> root = cq.from(User.class);

            cq.select(root)
                    .where(cb.equal(root.get("status"), status));

            return session.createQuery(cq).getResultList();
        }
    }
}
