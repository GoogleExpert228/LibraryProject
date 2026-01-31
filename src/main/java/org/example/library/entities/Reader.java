package org.example.library.entities;

import jakarta.persistence.*;
import lombok.*;

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

    @ToString.Exclude
    @OneToMany(mappedBy = "reader")
    private List<Borrow> borrows;

    @ToString.Exclude
    @OneToOne(mappedBy = "reader", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserRating userRating;

    public LocalDate getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDate approvalDate) {
        this.approvalDate = approvalDate;
    }

    public List<Borrow> getBorrows() {
        return borrows;
    }

    public void setBorrows(List<Borrow> borrows) {
        this.borrows = borrows;
    }

    public UserRating getUserRating() {
        return userRating;
    }

    public void setUserRating(UserRating userRating) {
        this.userRating = userRating;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("\"").append(getFullName()).append("\": ")
                .append("(").append(getUsername()).append(")")
                .toString();


    }
}
