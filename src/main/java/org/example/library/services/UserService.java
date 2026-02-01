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
import org.example.library.security.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UserService implements LibraryService {

    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao = new UserDaoImpl();

    public void createInitialAdmin() {
        log.info("Checking for initial admin user");

        if (userDao.findAll(User.class).isEmpty()) {
            log.warn("No users found, creating initial admin");

            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(PasswordHasher.hash("admin"));
            admin.setFullName("Главен Администратор");
            admin.setEmail("admin@library.bg");
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRegistrationDate(LocalDate.from(LocalDateTime.now()));

            userDao.save(admin);
            log.info("Initial admin created (username=admin)");
        } else {
            log.debug("Users already exist, admin creation skipped");
        }
    }

    public User authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);

        User user = userDao.findAll(User.class).stream()
                .filter(u -> u.getUsername().equals(username))
                .filter(u -> PasswordHasher.verify(password, u.getPassword()))
                .findFirst()
                .orElse(null);

        if (user == null) {
            log.warn("Authentication failed for username={}", username);
        } else {
            log.info("Authentication successful (userId={}, role={})",
                    user.getId(), user.getRole());
        }

        return user;
    }

    public Operator createOperator(String username,
                                   String password,
                                   String fullName,
                                   String email) {

        log.info("Creating operator: {}", username);

        Operator operator = new Operator();
        populateBaseUser(
                operator,
                username,
                PasswordHasher.hash(password),
                fullName,
                email,
                Role.OPERATOR
        );

        Operator saved = (Operator) userDao.save(operator);
        log.info("Operator created (id={})", saved.getId());

        return saved;
    }

    public Reader registerReader(String username,
                                 String password,
                                 String fullName,
                                 String email) {

        log.info("Registering reader: {}", username);

        Reader reader = new Reader();
        populateBaseUser(
                reader,
                username,
                PasswordHasher.hash(password),
                fullName,
                email,
                Role.READER
        );

        reader.setApprovalDate(LocalDate.now());

        Reader saved = (Reader) userDao.save(reader);
        log.info("Reader registered (id={})", saved.getId());

        return saved;
    }

    public void removeReader(Long readerId) {
        log.info("Removing reader with id={}", readerId);

        userDao.findById(User.class, readerId)
                .filter(u -> u.getRole() == Role.READER)
                .ifPresentOrElse(
                        u -> {
                            userDao.delete(u);
                            log.info("Reader removed (id={})", readerId);
                        },
                        () -> log.warn(
                                "User not removed (id={}, not a reader or not found)",
                                readerId
                        )
                );
    }

    public List<User> loadAllUsers() {
        log.debug("Loading all users");
        return userDao.findAll(User.class);
    }

    public List<User> loadReaders() {
        log.debug("Loading readers");
        return userDao.findByRole(Role.READER);
    }

    public List<User> loadOperators() {
        log.debug("Loading operators");
        return userDao.findByRole(Role.OPERATOR);
    }

    public User requireUser(Long id) {
        log.debug("Requiring user with id={}", id);

        return userDao.findById(User.class, id)
                .orElseThrow(() -> {
                    log.error("User not found: {}", id);
                    return new IllegalArgumentException("User not found: " + id);
                });
    }

    private void populateBaseUser(User user,
                                  String username,
                                  String password,
                                  String fullName,
                                  String email,
                                  Role role) {

        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setRegistrationDate(LocalDate.from(LocalDateTime.now()));
    }
}
