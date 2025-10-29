package org.example.library.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.library.enums.BookCondition;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "books")
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(unique = true, nullable = false)
    private String inventoryNumber;
    private String title;
    private String author;
    private String genre;
    @Enumerated(EnumType.STRING)
    private BookCondition condition;
    private LocalDate registrationDate;
    private LocalDate archivedDate;
    private boolean isAvailable;
}
