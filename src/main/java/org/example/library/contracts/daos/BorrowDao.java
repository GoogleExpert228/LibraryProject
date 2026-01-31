package org.example.library.contracts.daos;

import org.example.library.entities.Borrow;
import org.example.library.enums.BorrowStatus;

import java.time.LocalDate;
import java.util.List;

public interface BorrowDao extends GenericDao<Borrow, Long> {
    List<Borrow> findActiveByReader(Long readerId);
    List<Borrow> findOverdue(LocalDate today);
    List<Borrow> findByBookAndStatus(Long bookId, BorrowStatus status);
}






