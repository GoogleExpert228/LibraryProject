package org.example.library.daos.impl;

import org.example.library.daos.UserRatingDao;
import org.example.library.entities.UserRating;
import org.hibernate.Session;

import java.util.Optional;

public class UserRatingDaoImpl extends GenericHibernateDao<UserRating, Long> implements UserRatingDao {
    @Override
    public Optional<UserRating> findByReaderId(Long readerId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from UserRating ur where ur.reader.id = :rid", UserRating.class)
                    .setParameter("rid", readerId)
                    .uniqueResultOptional();
        }
    }
}


