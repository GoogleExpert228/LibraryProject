package org.example.library.daos;

import org.example.library.entities.Book;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookDao extends GenericDao<Book, Long> {
    Optional<Book> findByInventoryNumber(String inventoryNumber);
    List<Book> findAvailable();
    List<Book> findArchived();
    List<Book> registeredBefore(LocalDate date);
}






