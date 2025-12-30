package org.example.library.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "operators")
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Operator extends User {
}
