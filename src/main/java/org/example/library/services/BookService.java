package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.BookDao;
import org.example.library.daos.impl.BookDaoImpl;
import org.example.library.entities.Book;
import org.example.library.enums.BookCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class BookService implements LibraryService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookDao bookDao = new BookDaoImpl();

    public Book addBook(String inventoryNumber,
                        String title,
                        String author,
                        String genre,
                        BookCondition condition) {

        log.info("Adding book: {} by {}", title, author);

        Book book = new Book();
        book.setInventoryNumber(inventoryNumber);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setCondition(condition != null ? condition : BookCondition.GOOD);
        book.setRegistrationDate(LocalDate.now());
        book.setAvailable(true);

        Book saved = bookDao.save(book);

        log.info("Book saved with id={}", saved.getId());
        return saved;
    }

    public Book archiveBook(Long bookId) {
        log.warn("Archiving book id={}", bookId);

        Book book = requireBook(bookId);
        book.setArchivedDate(LocalDate.now());
        book.setCondition(BookCondition.ARCHIVED);
        book.setAvailable(false);

        return bookDao.update(book);
    }

    public Book scrapBook(Long bookId) {
        log.warn("Scrapping book id={}", bookId);

        Book book = requireBook(bookId);
        book.setCondition(BookCondition.DAMAGED);
        book.setAvailable(false);

        return bookDao.update(book);
    }

    public List<Book> loadAllBooks() {
        log.debug("Loading all books");
        return bookDao.findAll(Book.class);
    }

    public List<Book> loadAvailableBooks() {
        log.debug("Loading available books");
        return bookDao.findAvailable();
    }

    public Book requireBook(Long id) {
        return bookDao.findById(Book.class, id)
                .orElseThrow(() -> {
                    log.error("Book not found: {}", id);
                    return new IllegalArgumentException("Book not found: " + id);
                });
    }
}
