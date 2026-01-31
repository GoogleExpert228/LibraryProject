package org.example.library.services;

import org.example.library.daos.*;
import org.example.library.daos.impl.*;
import org.example.library.entities.*;
import org.example.library.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class LibraryFacade {

    private final UserDao userDao = new UserDaoImpl();
    private final BookDao bookDao = new BookDaoImpl();
    private final BorrowDao borrowDao = new BorrowDaoImpl();
    private final FormRequestDao formRequestDao = new FormRequestDaoImpl();

    public void createInitialAdmin() {
        List<User> users = userDao.findAll(User.class);
        if (users.isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("administrator");
            admin.setFullName("Главен Администратор");
            admin.setEmail("admin@library.bg");
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRegistrationDate(LocalDate.now());

            userDao.save(admin);
            System.out.println(">>> Успешно създаден служебен администратор");
        }
    }

    public User authenticate(String username, String password) {
        return userDao.findAll(User.class).stream()
                .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password)
                        && u.getStatus() == UserStatus.ACTIVE)
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

    public void removeReader(Long readerId) {
        List<Borrow> readerBorrows = borrowDao.findAllByReader(readerId);

        for (Borrow b : readerBorrows) {
            borrowDao.delete(b);
        }

        User readerUser = userDao.findById(User.class, readerId).orElse(null);
        if (readerUser != null) {
            List<FormRequest> readerForms = formRequestDao.findBySubmittedBy(readerUser);
            for (FormRequest fr : readerForms) {
                formRequestDao.delete(fr);
            }
        }

        userDao.findById(User.class, readerId)
                .filter(user -> user.getRole() == Role.READER || user.getRole() == Role.OPERATOR)
                .ifPresent(userDao::delete);
    }

    public FormRequest submitReaderForm(Long submittedByUserId, Book book) {
        User submittedBy = requireUser(submittedByUserId);
        FormRequest formRequest = new FormRequest();
        formRequest.setBook(book);
        formRequest.setSubmitDate(LocalDate.now());
        formRequest.setStatus(FormStatus.PENDING);
        formRequest.setSubmittedBy(submittedBy);

        return formRequestDao.save(formRequest);
    }

    public void approveRequestAndBorrow(Long requestId, BorrowType type, LocalDate dueDate) {
        FormRequest request = requireForm(requestId);

        if (request.getStatus() != FormStatus.PENDING) {
            throw new IllegalStateException("Заявка вече е обработена.");
        }

        borrowBook(request.getSubmittedBy().getId(), request.getBook().getId(), type, dueDate);

        request.setStatus(FormStatus.ACTIVE);
        formRequestDao.update(request);
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

    public Borrow borrowBook(Long readerId, Long bookId, BorrowType borrowType, LocalDate dueDate) {
        if (dueDate == null || !dueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Срокът за връщане трябва да е в бъдеще.");
        }

        Reader reader = (Reader) requireUser(readerId);
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

    public Borrow returnBook(Long borrowId, BookCondition returnCondition) {
        Borrow borrow = requireBorrow(borrowId);
        borrow.setReturnDate(LocalDate.now());
        borrow.setBorrowStatus(BorrowStatus.RETURNED);

        Book book = borrow.getBook();
        book.setCondition(returnCondition);

        if (returnCondition == BookCondition.DAMAGED) {
            book.setAvailable(false);
            book.setArchivedDate(LocalDate.now());
        } else {
            book.setAvailable(true);
        }

        bookDao.update(book);
        return borrowDao.update(borrow);
    }

    public List<User> loadAllUsers() {
        return userDao.findAll(User.class);
    }

    public List<User> loadReaders() {
        return userDao.findByRole(Role.READER);
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

    private void populateBaseUser(User target, String username, String password, String fullName, String email,
                                  Role role, UserStatus status) {
        target.setUsername(username);
        target.setPassword(password);
        target.setFullName(fullName);
        target.setEmail(email);
        target.setRole(role);
        target.setStatus(status);
        target.setRegistrationDate(LocalDate.now());
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

    public void changeUserStatus(Long id, UserStatus userStatus) {
        User user = requireUser(id);
        user.setStatus(userStatus);
        userDao.update(user);
    }

    public void requestReturn(Long borrowId) {
        Borrow borrow = borrowDao.findById(Borrow.class, borrowId)
                .orElseThrow(() -> new IllegalArgumentException("Заем не е намерен"));

        borrow.setBorrowStatus(BorrowStatus.RETURN_PENDING);
        borrowDao.update(borrow);
    }

    public Borrow finalizeReturn(Long borrowId, BookCondition returnCondition) {
        Borrow borrow = requireBorrow(borrowId);
        borrow.setReturnDate(LocalDate.now());
        borrow.setBorrowStatus(BorrowStatus.RETURNED);

        Book book = borrow.getBook();
        book.setCondition(returnCondition);

        if (returnCondition == BookCondition.DAMAGED || returnCondition == BookCondition.LOST) {
            book.setAvailable(false);
        } else {
            book.setAvailable(true);
        }

        bookDao.update(book);
        return borrowDao.update(borrow);
    }
}



