package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.BorrowDao;
import org.example.library.daos.impl.BorrowDaoImpl;
import org.example.library.entities.Book;
import org.example.library.entities.Borrow;
import org.example.library.entities.User;
import org.example.library.enums.BorrowStatus;
import org.example.library.enums.BorrowType;

import java.time.LocalDate;
import java.util.List;

public class BorrowService implements LibraryService {
    private final BorrowDao borrowDao = new BorrowDaoImpl();
    private final BookService bookService = new BookService();
    private final UserService userService = new UserService();

    public Borrow borrowBook(Long readerId, Long bookId, BorrowType type, LocalDate dueDate) {
        if (dueDate == null || !dueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Invalid due date");
        }

        User reader = userService.requireUser(readerId);
        Book book = bookService.requireBook(bookId);

        if (!book.isAvailable()) {
            throw new IllegalStateException("Book already borrowed");
        }

        Borrow borrow = new Borrow();
        borrow.setReader(reader);
        borrow.setBook(book);
        borrow.setBorrowType(type != null ? type : BorrowType.READING_ROOM);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setDueDate(dueDate);
        borrow.setBorrowStatus(BorrowStatus.ACTIVE);

        Borrow saved = borrowDao.save(borrow);

        book.setAvailable(false);
        return saved;
    }

    public Borrow returnBook(Long borrowId) {
        Borrow borrow = requireBorrow(borrowId);
        borrow.setReturnDate(LocalDate.now());
        borrow.setBorrowStatus(BorrowStatus.RETURNED);
        return borrowDao.update(borrow);
    }

    public List<Borrow> loadAllBorrows() {
        return borrowDao.findAll(Borrow.class);
    }

    public List<Borrow> loadOverdueBorrows() {
        return borrowDao.findOverdue(LocalDate.now());
    }

    public Borrow requireBorrow(Long id) {
        return borrowDao.findById(Borrow.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Borrow not found: " + id));
    }
}
