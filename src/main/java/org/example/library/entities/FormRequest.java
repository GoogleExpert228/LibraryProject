package org.example.library.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.library.enums.FormStatus;

import java.time.LocalDate;

@Data
@Table(name = "form_requests")
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FormRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private LocalDate submitDate;
    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @ManyToOne
    @JoinColumn(name = "created_by_operator_id")
    private User createdBy;
}
