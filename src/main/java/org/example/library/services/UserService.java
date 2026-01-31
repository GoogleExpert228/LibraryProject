package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.UserDao;
import org.example.library.daos.impl.UserDaoImpl;
import org.example.library.entities.Admin;
import org.example.library.entities.Operator;
import org.example.library.entities.Reader;
import org.example.library.entities.User;
import org.example.library.enums.Role;
import org.example.library.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserService implements LibraryService {
    private final UserDao userDao = new UserDaoImpl();

    public void createInitialAdmin() {
        if (userDao.findAll(User.class).isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setFullName("Главен Администратор");
            admin.setEmail("admin@library.bg");
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRegistrationDate(LocalDateTime.now());

            userDao.save(admin);
        }
    }

    public User authenticate(String username, String password) {
        return userDao.findAll(User.class).stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public Operator createOperator(String username, String password, String fullName, String email) {
        Operator operator = new Operator();
        populateBaseUser(operator, username, password, fullName, email, Role.OPERATOR);
        return (Operator) userDao.save(operator);
    }

    public Reader registerReader(String username, String password, String fullName, String email) {
        Reader reader = new Reader();
        populateBaseUser(reader, username, password, fullName, email, Role.READER);
        reader.setApprovalDate(LocalDate.now());
        return (Reader) userDao.save(reader);
    }

    public void deactivateReader(Long readerId) {
        userDao.findById(User.class, readerId)
                .filter(u -> u.getRole() == Role.READER)
                .ifPresent(u -> {
                    u.setStatus(UserStatus.BLOCKED);
                    userDao.update(u);
                });
    }

    public void removeReader(Long readerId) {
        userDao.findById(User.class, readerId)
                .filter(u -> u.getRole() == Role.READER)
                .ifPresent(userDao::delete);
    }

    public List<User> loadAllUsers() {
        return userDao.findAll(User.class);
    }

    public List<User> loadReaders() {
        return userDao.findByRole(Role.READER);
    }

    public List<User> loadOperators() {
        return userDao.findByRole(Role.OPERATOR);
    }

    public User requireUser(Long id) {
        return userDao.findById(User.class, id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    private void populateBaseUser(User user, String username, String password,
                                  String fullName, String email, Role role) {
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setRegistrationDate(LocalDateTime.now());
    }
}
