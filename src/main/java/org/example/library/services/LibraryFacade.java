package org.example.library.services;

import org.example.library.contracts.daos.*;
import org.example.library.daos.impl.*;
import org.example.library.entities.*;
import org.example.library.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Facade that orchestrates the core library use-cases expected by the GUI.
 * Keeps the JavaFX layer decoupled from the Hibernate DAOs while reusing the
 * existing persistence logic.
 */
public class LibraryFacade {

    private final UserDao userDao = new UserDaoImpl();
    private final BookDao bookDao = new BookDaoImpl();
    private final BorrowDao borrowDao = new BorrowDaoImpl();
    private final FormRequestDao formRequestDao = new FormRequestDaoImpl();
    private final NotificationDao notificationDao = new NotificationDaoImpl();
    private final UserRatingDao userRatingDao = new UserRatingDaoImpl();

    public void createInitialAdmin() {
        List<User> users = userDao.findAll(User.class);
        if (users.isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setFullName("Главен Администратор");
            admin.setEmail("admin@library.bg");
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRegistrationDate(LocalDateTime.now());

            userDao.save(admin);
            System.out.println(">>> Успешно създаден служебен администратор");
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
        populateBaseUser(operator, username, password, fullName, email, Role.OPERATOR, UserStatus.ACTIVE);
        return (Operator) userDao.save(operator);
    }

    public Reader registerReader(String username, String password, String fullName, String email) {
        Reader reader = new Reader();
        populateBaseUser(reader, username, password, fullName, email, Role.READER, UserStatus.ACTIVE);
        reader.setApprovalDate(LocalDate.now());
        return (Reader) userDao.save(reader);
    }

    public void deactivateReader(Long readerId) {
        userDao.findById(User.class, readerId)
                .filter(user -> user.getRole() == Role.READER)
                .ifPresent(user -> {
                    user.setStatus(UserStatus.BLOCKED);
                    userDao.update(user);
                });
    }

    public void removeReader(Long readerId) {
        userDao.findById(User.class, readerId)
                .filter(user -> user.getRole() == Role.READER)
                .ifPresent(userDao::delete);
    }

    public FormRequest submitReaderForm(String content, Long submittedByUserId, Long createdByOperatorId) {
        User submittedBy = requireUser(submittedByUserId);
        FormRequest formRequest = new FormRequest();
        formRequest.setContent(content);
        formRequest.setSubmitDate(LocalDate.now());
        formRequest.setStatus(FormStatus.PENDING);
        formRequest.setSubmittedBy(submittedBy);

        if (createdByOperatorId != null) {
            formRequest.setCreatedBy(requireUser(createdByOperatorId));
        }

        return formRequestDao.save(formRequest);
    }

    public FormRequest updateFormStatus(Long formId, FormStatus newStatus) {
        FormRequest formRequest = requireForm(formId);
        formRequest.setStatus(newStatus);
        return formRequestDao.update(formRequest);
    }

    public Book addBook(String inventoryNumber, String title, String author, String genre, BookCondition condition) {
        Book book = new Book();
        book.setInventoryNumber(inventoryNumber);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setCondition(condition != null ? condition : BookCondition.GOOD);
        book.setRegistrationDate(LocalDate.now());
        book.setArchivedDate(null);
        book.setAvailable(true);
        return bookDao.save(book);
    }

    public Book archiveBook(Long bookId) {
        Book book = requireBook(bookId);
        book.setArchivedDate(LocalDate.now());
        book.setCondition(BookCondition.ARCHIVED);
        book.setAvailable(false);
        return bookDao.update(book);
    }

    public Book scrapBook(Long bookId) {
        Book book = requireBook(bookId);
        book.setCondition(BookCondition.DAMAGED);
        book.setAvailable(false);
        return bookDao.update(book);
    }

    public Borrow borrowBook(Long readerId, Long bookId, BorrowType borrowType, LocalDate dueDate) {
        if (dueDate == null || !dueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Срокът за връщане трябва да е в бъдеще.");
        }

        User reader = requireUser(readerId);
        if (reader.getRole() != Role.READER) {
            throw new IllegalStateException("Само читатели могат да заемат книги.");
        }

        Book book = requireBook(bookId);
        if (!book.isAvailable()) {
            throw new IllegalStateException("Книгата вече е заета.");
        }

        Borrow borrow = new Borrow();
        borrow.setReader(reader);
        borrow.setBook(book);
        borrow.setBorrowType(borrowType != null ? borrowType : BorrowType.READING_ROOM);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(dueDate);
        borrow.setBorrowStatus(BorrowStatus.ACTIVE);

        Borrow saved = borrowDao.save(borrow);

        book.setAvailable(false);
        bookDao.update(book);

        return saved;
    }

    public Borrow returnBook(Long borrowId) {
        Borrow borrow = requireBorrow(borrowId);
        borrow.setReturnDate(LocalDate.now());
        borrow.setBorrowStatus(BorrowStatus.RETURNED);
        Borrow updated = borrowDao.update(borrow);

        Book book = borrow.getBook();
        book.setAvailable(true);
        bookDao.update(book);

        return updated;
    }

    public Notification createNotification(NotificationType type, String message, Long recipientId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setTimeStamp(LocalDate.now());

        if (recipientId != null) {
            notification.setRecipient(requireUser(recipientId));
        }

        return notificationDao.save(notification);
    }

    public UserRating updateUserRating(Long readerId, LoyaltyLevel loyaltyLevel) {
        User reader = requireUser(readerId);
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

    public List<User> loadAllUsers() {
        return userDao.findAll(User.class);
    }

    public List<User> loadReaders() {
        return userDao.findByRole(Role.READER);
    }

    public List<User> loadOperators() {
        return userDao.findByRole(Role.OPERATOR);
    }

    public List<Book> loadAllBooks() {
        return bookDao.findAll(Book.class);
    }

    public List<Book> loadAvailableBooks() {
        return bookDao.findAvailable();
    }

    public List<FormRequest> loadAllForms() {
        return formRequestDao.findAll(FormRequest.class);
    }

    public List<Borrow> loadAllBorrows() {
        return borrowDao.findAll(Borrow.class);
    }

    public List<Borrow> loadOverdueBorrows() {
        return borrowDao.findOverdue(LocalDate.now());
    }

    public List<Notification> loadNotifications() {
        return notificationDao.findAll(Notification.class);
    }

    public List<UserRating> loadRatings() {
        return userRatingDao.findAll(UserRating.class);
    }

    private void populateBaseUser(User target, String username, String password, String fullName, String email,
                                  Role role, UserStatus status) {
        target.setUsername(username);
        target.setPassword(password);
        target.setFullName(fullName);
        target.setEmail(email);
        target.setRole(role);
        target.setStatus(status);
        target.setRegistrationDate(LocalDateTime.now());
    }

    private User requireUser(Long id) {
        return userDao.findById(User.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Потребител с ID " + id + " не е намерен."));
    }

    private Book requireBook(Long id) {
        return bookDao.findById(Book.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Книга с ID " + id + " не е намерена."));
    }

    private Borrow requireBorrow(Long id) {
        return borrowDao.findById(Borrow.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Заем с ID " + id + " не е намерен."));
    }

    private FormRequest requireForm(Long id) {
        return formRequestDao.findById(FormRequest.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Формуляр с ID " + id + " не е намерен."));
    }
}



