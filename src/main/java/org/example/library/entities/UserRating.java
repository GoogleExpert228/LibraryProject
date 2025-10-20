package org.example.library.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.library.enums.LoyaltyLevel;

@Data
@Table(name = "user_ratings")
@Entity
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
}
