package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.UserRatingDao;
import org.example.library.daos.impl.UserRatingDaoImpl;
import org.example.library.entities.User;
import org.example.library.entities.UserRating;
import org.example.library.enums.LoyaltyLevel;

import java.util.List;
import java.util.Optional;

public class UserRatingService implements LibraryService {
    private final UserRatingDao userRatingDao = new UserRatingDaoImpl();
    public UserRating updateUserRating(Long readerId, LoyaltyLevel loyaltyLevel) {
        User reader = ServiceFactory.service(UserService.class).requireUser(readerId);
        Optional<UserRating> existing = userRatingDao.findByReaderId(readerId);

        if (existing.isPresent()) {
            UserRating rating = existing.get();
            rating.setRating(loyaltyLevel);
            return userRatingDao.update(rating);
        }

        UserRating rating = new UserRating();
        rating.setReader(reader);
        rating.setRating(loyaltyLevel);
        return userRatingDao.save(rating);
    }

    public List<UserRating> loadRatings() {
        return userRatingDao.findAll(UserRating.class);
    }

}
