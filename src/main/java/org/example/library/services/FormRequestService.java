package org.example.library.services;

import org.example.library.contracts.LibraryService;
import org.example.library.contracts.daos.FormRequestDao;
import org.example.library.daos.impl.FormRequestDaoImpl;
import org.example.library.entities.Book;
import org.example.library.entities.FormRequest;
import org.example.library.entities.User;
import org.example.library.enums.BorrowType;
import org.example.library.enums.FormStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

public class FormRequestService implements LibraryService {

    private static final Logger log =
            LoggerFactory.getLogger(FormRequestService.class);

    private final FormRequestDao dao = new FormRequestDaoImpl();

    public FormRequest submit(String content,
                              Long submittedById,
                              Long createdById) {

        log.info("Submitting form request by userId={}", submittedById);

        FormRequest form = new FormRequest();
        form.setSubmitDate(LocalDate.now());
        form.setStatus(FormStatus.PENDING);
        form.setSubmittedBy(
                ServiceFactory.service(UserService.class)
                        .requireUser(submittedById)
        );

        if (createdById != null) {
            log.debug("Form request created by admin/operator id={}", createdById);
            form.setCreatedBy(
                    ServiceFactory.service(UserService.class)
                            .requireUser(createdById)
            );
        }

        FormRequest saved = dao.save(form);
        log.info("Form request submitted successfully (id={})", saved.getId());

        return saved;
    }

    public FormRequest updateStatus(Long id, FormStatus status) {
        log.info("Updating form request status: id={}, newStatus={}", id, status);

        FormRequest form = requireForm(id);
        form.setStatus(status);

        return dao.update(form);
    }

    public List<FormRequest> loadAll() {
        log.debug("Loading all form requests");
        return dao.findAll(FormRequest.class);
    }

    private FormRequest requireForm(Long id) {
        return dao.findById(FormRequest.class, id)
                .orElseThrow(() -> {
                    log.error("Form request not found: {}", id);
                    return new IllegalArgumentException("Form not found: " + id);
                });
    }

    public void approveRequestAndBorrow(Long requestId,
                                        BorrowType type,
                                        LocalDate dueDate) {

        log.info("Approving form request id={}", requestId);

        FormRequest request = requireForm(requestId);

        if (request.getStatus() != FormStatus.PENDING) {
            log.warn("Form request {} already processed (status={})",
                    requestId, request.getStatus());
            throw new IllegalStateException("Заявка вече е обработена.");
        }

        ServiceFactory.service(BorrowService.class)
                .borrowBook(
                        request.getSubmittedBy().getId(),
                        request.getBook().getId(),
                        type,
                        dueDate
                );

        request.setStatus(FormStatus.ACTIVE);
        dao.update(request);

        log.info("Form request {} approved and borrow created", requestId);
    }

    public FormRequest submitReaderForm(Long submittedByUserId, Book book) {
        log.info("Submitting reader form: userId={}, bookId={}",
                submittedByUserId,
                book != null ? book.getId() : null
        );

        User submittedBy =
                ServiceFactory.service(UserService.class)
                        .requireUser(submittedByUserId);

        FormRequest formRequest = new FormRequest();
        formRequest.setBook(book);
        formRequest.setSubmitDate(LocalDate.now());
        formRequest.setStatus(FormStatus.PENDING);
        formRequest.setSubmittedBy(submittedBy);

        FormRequest saved = dao.save(formRequest);
        log.info("Reader form submitted successfully (id={})", saved.getId());

        return saved;
    }
}
