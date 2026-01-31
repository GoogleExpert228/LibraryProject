package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.BookDao;
import org.example.library.daos.impl.BookDaoImpl;
import org.example.library.entities.Book;
import org.example.library.enums.BookCondition;

import java.time.LocalDate;
import java.util.List;

public class BookService implements LibraryService {
    private final BookDao bookDao = new BookDaoImpl();

    public Book addBook(String inventoryNumber, String title, String author, String genre, BookCondition condition) {
        Book book = new Book();
        book.setInventoryNumber(inventoryNumber);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        book.setCondition(condition != null ? condition : BookCondition.GOOD);
        book.setRegistrationDate(LocalDate.now());
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

    public List<Book> loadAllBooks() {
        return bookDao.findAll(Book.class);
    }

    public List<Book> loadAvailableBooks() {
        return bookDao.findAvailable();
    }

    public Book requireBook(Long id) {
        return bookDao.findById(Book.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
    }
}
