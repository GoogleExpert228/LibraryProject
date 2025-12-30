package org.example.library.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.library.enums.LoyaltyLevel;

@Data
@Entity
@Table(name = "user_ratings")
@AllArgsConstructor
@NoArgsConstructor
public class UserRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "reader_id")
    private User reader;
    @Enumerated(EnumType.STRING)
    private LoyaltyLevel rating;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReader() {
        return reader;
    }

    public void setReader(User reader) {
        this.reader = reader;
    }

    public LoyaltyLevel getRating() {
        return rating;
    }

    public void setRating(LoyaltyLevel rating) {
        this.rating = rating;
    }
}
