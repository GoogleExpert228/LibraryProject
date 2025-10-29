package org.example.library.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "readers")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Reader extends User {
    private LocalDate approvalDate;

    @OneToMany(mappedBy = "reader")
    private List<Borrow> borrows;

    @OneToOne(mappedBy = "reader")
    private UserRating userRating;
}
