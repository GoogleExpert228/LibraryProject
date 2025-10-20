package org.example.library.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.library.enums.NotificationType;

import java.time.LocalDate;

@Data
@Table(name = "notifications")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private LocalDate timeStamp;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User recipient;
}
