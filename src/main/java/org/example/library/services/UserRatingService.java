package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.UserRatingDao;
import org.example.library.daos.impl.UserRatingDaoImpl;
import org.example.library.entities.User;
import org.example.library.entities.UserRating;
import org.example.library.enums.LoyaltyLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserRatingService implements LibraryService {

    private static final Logger log =
            LoggerFactory.getLogger(UserRatingService.class);

    private final UserRatingDao userRatingDao = new UserRatingDaoImpl();

    public UserRating updateUserRating(Long readerId,
                                       LoyaltyLevel loyaltyLevel) {

        log.info("Updating user rating: readerId={}, loyaltyLevel={}",
                readerId, loyaltyLevel);

        User reader =
                ServiceFactory.service(UserService.class)
                        .requireUser(readerId);

        Optional<UserRating> existing =
                userRatingDao.findByReaderId(readerId);

        if (existing.isPresent()) {
            UserRating rating = existing.get();
            rating.setRating(loyaltyLevel);

            UserRating updated = userRatingDao.update(rating);
            log.info("User rating updated (ratingId={})", updated.getId());

            return updated;
        }

        log.debug("No rating found for readerId={}, creating new", readerId);

        UserRating rating = new UserRating();
        rating.setReader(reader);
        rating.setRating(loyaltyLevel);

        UserRating saved = userRatingDao.save(rating);
        log.info("User rating created (ratingId={})", saved.getId());

        return saved;
    }

    public List<UserRating> loadRatings() {
        log.debug("Loading all user ratings");
        return userRatingDao.findAll(UserRating.class);
    }
}