package org.example.library.daos;

import org.example.library.entities.UserRating;

import java.util.Optional;

public interface UserRatingDao extends GenericDao<UserRating, Long> {
    Optional<UserRating> findByReaderId(Long readerId);
}


