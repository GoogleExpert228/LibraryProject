package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.BookDao;
import org.example.library.contracts.daos.BorrowDao;
import org.example.library.daos.impl.BookDaoImpl;
import org.example.library.daos.impl.BorrowDaoImpl;
import org.example.library.entities.Book;
import org.example.library.entities.Borrow;
import org.example.library.entities.Reader;
import org.example.library.entities.User;
import org.example.library.enums.BookCondition;
import org.example.library.enums.BorrowStatus;
import org.example.library.enums.BorrowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class BorrowService implements LibraryService {

    private static final Logger log = LoggerFactory.getLogger(BorrowService.class);

    private final BorrowDao borrowDao = new BorrowDaoImpl();
    private final BookDao bookDao = new BookDaoImpl();

    public Borrow borrowBook(Long readerId,
                             Long bookId,
                             BorrowType type,
                             LocalDate dueDate) {

        log.info("Borrow request: readerId={}, bookId={}", readerId, bookId);

        if (dueDate == null || !dueDate.isAfter(LocalDate.now())) {
            log.error("Invalid due date: {}", dueDate);
            throw new IllegalArgumentException("Invalid due date");
        }

        User reader = ServiceFactory
                .service(UserService.class)
                .requireUser(readerId);

        Book book = ServiceFactory
                .service(BookService.class)
                .requireBook(bookId);

        if (!book.isAvailable()) {
            log.warn("Book {} is not available", bookId);
            throw new IllegalStateException("Book already borrowed");
        }

        Borrow borrow = new Borrow();
        borrow.setReader((Reader) reader);
        borrow.setBook(book);
        borrow.setBorrowType(type != null ? type : BorrowType.READING_ROOM);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(dueDate);
        borrow.setBorrowStatus(BorrowStatus.ACTIVE);

        Borrow saved = borrowDao.save(borrow);

        book.setAvailable(false);
        bookDao.update(book);

        log.info("Book {} borrowed successfully (borrowId={})",
                bookId, saved.getId());

        return saved;
    }

    public Borrow returnBook(Long borrowId, BookCondition returnCondition) {
        log.info("Return book request: borrowId={}", borrowId);

        Borrow borrow = requireBorrow(borrowId);
        borrow.setReturnDate(LocalDate.now());
        borrow.setBorrowStatus(BorrowStatus.RETURNED);

        Book book = borrow.getBook();
        book.setCondition(returnCondition);

        if (returnCondition == BookCondition.DAMAGED) {
            log.warn("Book {} returned damaged", book.getId());
            book.setAvailable(false);
            book.setArchivedDate(LocalDate.now());
        } else {
            book.setAvailable(true);
        }

        bookDao.update(book);
        Borrow updated = borrowDao.update(borrow);

        log.info("Borrow {} successfully returned", borrowId);
        return updated;
    }

    public List<Borrow> loadAllBorrows() {
        log.debug("Loading all borrows");
        return borrowDao.findAll(Borrow.class);
    }

    public List<Borrow> loadOverdueBorrows() {
        log.debug("Loading overdue borrows");
        return borrowDao.findOverdue(LocalDate.now());
    }

    public Borrow requireBorrow(Long id) {
        return borrowDao.findById(Borrow.class, id)
                .orElseThrow(() -> {
                    log.error("Borrow not found: {}", id);
                    return new IllegalArgumentException("Borrow not found: " + id);
                });
    }

    public Borrow finalizeReturn(Long borrowId,
                                 BookCondition returnCondition) {

        log.info("Finalizing return: borrowId={}", borrowId);

        Borrow borrow = requireBorrow(borrowId);
        borrow.setReturnDate(LocalDate.now());
        borrow.setBorrowStatus(BorrowStatus.RETURNED);

        Book book = borrow.getBook();
        book.setCondition(returnCondition);

        if (returnCondition == BookCondition.DAMAGED
                || returnCondition == BookCondition.LOST) {

            log.warn("Book {} finalized as {}", book.getId(), returnCondition);
            book.setAvailable(false);
        } else {
            book.setAvailable(true);
        }

        bookDao.update(book);
        return borrowDao.update(borrow);
    }

    public void requestReturn(Long borrowId) {
        log.info("Return request submitted for borrowId={}", borrowId);

        Borrow borrow = borrowDao.findById(Borrow.class, borrowId)
                .orElseThrow(() -> {
                    log.error("Borrow not found for return request: {}", borrowId);
                    return new IllegalArgumentException("Заем не е намерен");
                });

        borrow.setBorrowStatus(BorrowStatus.RETURN_PENDING);
        borrowDao.update(borrow);

        log.info("Borrow {} marked as RETURN_PENDING", borrowId);
    }
}
