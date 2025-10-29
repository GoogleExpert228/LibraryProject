package org.example.library.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.library.enums.BorrowStatus;
import org.example.library.enums.BorrowType;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "borrows")
@AllArgsConstructor
@NoArgsConstructor
public class Borrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "reader_id")
    private User reader;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    @Enumerated(EnumType.STRING)
    private BorrowType borrowType;
    @Enumerated(EnumType.STRING)
    private BorrowStatus borrowStatus;
}
