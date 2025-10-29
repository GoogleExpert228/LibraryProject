package org.example.library.daos;

import org.example.library.entities.FormRequest;
import org.example.library.enums.FormStatus;

import java.time.LocalDate;
import java.util.List;

public interface FormRequestDao extends GenericDao<FormRequest, Long> {
    List<FormRequest> findByStatus(FormStatus status);
    List<FormRequest> submittedBetween(LocalDate startInclusive, LocalDate endInclusive);
}


