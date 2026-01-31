package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.FormRequestDao;
import org.example.library.daos.impl.FormRequestDaoImpl;
import org.example.library.entities.FormRequest;
import org.example.library.enums.FormStatus;

import java.time.LocalDate;
import java.util.List;

public class FormRequestService implements LibraryService {
    private final FormRequestDao dao = new FormRequestDaoImpl();
    private final UserService userService = new UserService();

    public FormRequest submit(String content, Long submittedById, Long createdById) {
        FormRequest form = new FormRequest();
        form.setContent(content);
        form.setSubmitDate(LocalDate.now());
        form.setStatus(FormStatus.PENDING);
        form.setSubmittedBy(userService.requireUser(submittedById));

        if (createdById != null) {
            form.setCreatedBy(userService.requireUser(createdById));
        }

        return dao.save(form);
    }

    public FormRequest updateStatus(Long id, FormStatus status) {
        FormRequest form = requireForm(id);
        form.setStatus(status);
        return dao.update(form);
    }

    public List<FormRequest> loadAll() {
        return dao.findAll(FormRequest.class);
    }

    private FormRequest requireForm(Long id) {
        return dao.findById(FormRequest.class, id)
                .orElseThrow(() -> new IllegalArgumentException("Form not found: " + id));
    }
}
