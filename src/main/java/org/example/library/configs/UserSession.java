package org.example.library.configs;

import org.example.library.entities.User;

public class UserSession {
    private static UserSession instance;

    private User currentUser;

    private UserSession(User user) {
        this.currentUser = user;
    }

    public static void getInstance(User user) {
        if (instance == null) {
            instance = new UserSession(user);
        }
    }

    public static UserSession getInstance() {
        return instance;
    }

    public User getUser() {
        return currentUser;
    }

    public static void cleanUserSession() {
        instance = null;
    }
}
