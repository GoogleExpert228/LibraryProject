package org.example.library.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.library.entities.*;
import org.example.library.enums.*;
import org.example.library.services.LibraryFacade;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LibraryDashboardController {

    private final LibraryFacade facade = new LibraryFacade();

    // region User creation
    @FXML
    private TextField operatorUsernameField;
    @FXML
    private PasswordField operatorPasswordField;
    @FXML
    private TextField operatorFullNameField;
    @FXML
    private TextField operatorEmailField;
    @FXML
    private TextField readerUsernameField;
    @FXML
    private PasswordField readerPasswordField;
    @FXML
    private TextField readerFullNameField;
    @FXML
    private TextField readerEmailField;
    @FXML
    private ComboBox<User> readerSelectionBox;
    @FXML
    private ComboBox<User> ratingReaderCombo;
    @FXML
    private ComboBox<LoyaltyLevel> loyaltyLevelCombo;
    // endregion

    // region Forms
    @FXML
    private TextArea formContentArea;
    @FXML
    private ComboBox<User> formSubmittedByCombo;
    @FXML
    private ComboBox<User> formOperatorCombo;
    @FXML
    private ComboBox<FormRequest> formSelectionCombo;
    @FXML
    private ComboBox<FormStatus> formStatusCombo;
    // endregion

    // region Books
    @FXML
    private TextField bookInventoryField;
    @FXML
    private TextField bookTitleField;
    @FXML
    private TextField bookAuthorField;
    @FXML
    private TextField bookGenreField;
    @FXML
    private ComboBox<BookCondition> bookConditionCombo;
    @FXML
    private ComboBox<Book> archiveBookCombo;
    @FXML
    private ComboBox<Book> scrapBookCombo;
    // endregion

    // region Borrowing
    @FXML
    private ComboBox<User> borrowReaderCombo;
    @FXML
    private ComboBox<Book> borrowBookCombo;
    @FXML
    private ComboBox<BorrowType> borrowTypeCombo;
    @FXML
    private DatePicker borrowDueDatePicker;
    @FXML
    private ComboBox<Borrow> returnBorrowCombo;
    // endregion

    // region Notifications
    @FXML
    private ComboBox<NotificationType> notificationTypeCombo;
    @FXML
    private TextArea notificationMessageArea;
    @FXML
    private ComboBox<User> notificationRecipientCombo;
    // endregion

    // region Tables
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> userNameColumn;
    @FXML
    private TableColumn<User, Role> userRoleColumn;
    @FXML
    private TableColumn<User, UserStatus> userStatusColumn;
    @FXML
    private TableColumn<User, LocalDate> userRegistrationColumn;

    @FXML
    private TableView<FormRequest> formsTable;
    @FXML
    private TableColumn<FormRequest, LocalDate> formDateColumn;
    @FXML
    private TableColumn<FormRequest, FormStatus> formStatusColumn;
    @FXML
    private TableColumn<FormRequest, String> formContentColumn;

    @FXML
    private TableView<Book> booksTable;
    @FXML
    private TableColumn<Book, String> bookInventoryColumn;
    @FXML
    private TableColumn<Book, String> bookTitleColumn;
    @FXML
    private TableColumn<Book, BookCondition> bookConditionColumn;
    @FXML
    private TableColumn<Book, Boolean> bookAvailabilityColumn;

    @FXML
    private TableView<Borrow> borrowsTable;
    @FXML
    private TableColumn<Borrow, String> borrowReaderColumn;
    @FXML
    private TableColumn<Borrow, String> borrowBookColumn;
    @FXML
    private TableColumn<Borrow, LocalDate> borrowDueDateColumn;
    @FXML
    private TableColumn<Borrow, BorrowType> borrowTypeColumn;
    @FXML
    private TableColumn<Borrow, BorrowStatus> borrowStatusColumn;

    @FXML
    private TableView<Borrow> overdueTable;
    @FXML
    private TableColumn<Borrow, String> overdueReaderColumn;
    @FXML
    private TableColumn<Borrow, String> overdueBookColumn;
    @FXML
    private TableColumn<Borrow, LocalDate> overdueDueColumn;
    @FXML
    private TableColumn<Borrow, BorrowStatus> overdueStatusColumn;

    @FXML
    private TableView<UserRating> ratingsTable;
    @FXML
    private TableColumn<UserRating, String> ratingReaderColumn;
    @FXML
    private TableColumn<UserRating, LoyaltyLevel> ratingLevelColumn;

    @FXML
    private TableView<Notification> notificationsTable;
    @FXML
    private TableColumn<Notification, LocalDate> notificationDateColumn;
    @FXML
    private TableColumn<Notification, NotificationType> notificationTypeTableColumn;
    @FXML
    private TableColumn<Notification, String> notificationMessageColumn;
    @FXML
    private TableColumn<Notification, String> notificationRecipientColumn;
    // endregion

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        setupTables();
        setupChoiceBoxes();
        refreshAllData();
    }

    private void setupTables() {
        userNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        userRoleColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getRole()));
        userStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));
        userRegistrationColumn.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getRegistrationDate() != null
                        ? data.getValue().getRegistrationDate().toLocalDate()
                        : null));

        formDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSubmitDate()));
        formStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getStatus()));
        formContentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));

        bookInventoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInventoryNumber()));
        bookTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        bookConditionColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getCondition()));
        bookAvailabilityColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().isAvailable()));

        borrowReaderColumn.setCellValueFactory(data -> new SimpleStringProperty(userLabel(data.getValue().getReader())));
        borrowBookColumn.setCellValueFactory(data -> new SimpleStringProperty(bookLabel(data.getValue().getBook())));
        borrowDueDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDueDate()));
        borrowTypeColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getBorrowType()));
        borrowStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getBorrowStatus()));

        overdueReaderColumn.setCellValueFactory(data -> new SimpleStringProperty(userLabel(data.getValue().getReader())));
        overdueBookColumn.setCellValueFactory(data -> new SimpleStringProperty(bookLabel(data.getValue().getBook())));
        overdueDueColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDueDate()));
        overdueStatusColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getBorrowStatus()));

        ratingReaderColumn.setCellValueFactory(data -> new SimpleStringProperty(userLabel(data.getValue().getReader())));
        ratingLevelColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getRating()));

        notificationDateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTimeStamp()));
        notificationTypeTableColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getType()));
        notificationMessageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMessage()));
        notificationRecipientColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRecipient() != null ? userLabel(data.getValue().getRecipient()) : "-"));
    }

    private void setupChoiceBoxes() {
        configureUserCombo(readerSelectionBox);
        configureUserCombo(ratingReaderCombo);
        configureUserCombo(formSubmittedByCombo);
        configureUserCombo(formOperatorCombo);
        configureUserCombo(borrowReaderCombo);
        configureUserCombo(notificationRecipientCombo);

        configureBookCombo(archiveBookCombo);
        configureBookCombo(scrapBookCombo);
        configureBookCombo(borrowBookCombo);

        configureBorrowCombo(returnBorrowCombo);

        configureFormCombo(formSelectionCombo);

        bookConditionCombo.setItems(FXCollections.observableArrayList(BookCondition.values()));
        borrowTypeCombo.setItems(FXCollections.observableArrayList(BorrowType.values()));
        loyaltyLevelCombo.setItems(FXCollections.observableArrayList(LoyaltyLevel.values()));
        notificationTypeCombo.setItems(FXCollections.observableArrayList(NotificationType.values()));
        formStatusCombo.setItems(FXCollections.observableArrayList(FormStatus.values()));
    }

    private void refreshAllData() {
        refreshUsersData();
        refreshBooksData();
        refreshFormsData();
        refreshBorrowData();
        refreshRatingsData();
        refreshNotificationsData();
    }

    private void refreshUsersData() {
        List<User> users = facade.loadAllUsers().stream()
                .sorted(Comparator.comparing(User::getRole).thenComparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        ObservableList<User> data = FXCollections.observableArrayList(users);
        usersTable.setItems(data);
        readerSelectionBox.setItems(FXCollections.observableArrayList(facade.loadReaders()));
        ratingReaderCombo.setItems(FXCollections.observableArrayList(facade.loadReaders()));
        borrowReaderCombo.setItems(FXCollections.observableArrayList(facade.loadReaders()));
        formSubmittedByCombo.setItems(FXCollections.observableArrayList(facade.loadReaders()));
        formOperatorCombo.setItems(FXCollections.observableArrayList(facade.loadOperators()));
        notificationRecipientCombo.setItems(FXCollections.observableArrayList(facade.loadAllUsers()));
    }

    private void refreshBooksData() {
        List<Book> books = facade.loadAllBooks();
        booksTable.setItems(FXCollections.observableArrayList(books));
        archiveBookCombo.setItems(FXCollections.observableArrayList(books));
        scrapBookCombo.setItems(FXCollections.observableArrayList(books));
        borrowBookCombo.setItems(FXCollections.observableArrayList(facade.loadAvailableBooks()));
    }

    private void refreshFormsData() {
        List<FormRequest> forms = facade.loadAllForms();
        formsTable.setItems(FXCollections.observableArrayList(forms));
        formSelectionCombo.setItems(FXCollections.observableArrayList(forms));
    }

    private void refreshBorrowData() {
        List<Borrow> borrows = facade.loadAllBorrows();
        borrowsTable.setItems(FXCollections.observableArrayList(borrows));
        List<Borrow> active = borrows.stream()
                .filter(b -> b.getBorrowStatus() == BorrowStatus.ACTIVE)
                .collect(Collectors.toList());
        returnBorrowCombo.setItems(FXCollections.observableArrayList(active));
        overdueTable.setItems(FXCollections.observableArrayList(facade.loadOverdueBorrows()));
    }

    private void refreshRatingsData() {
        ratingsTable.setItems(FXCollections.observableArrayList(facade.loadRatings()));
    }

    private void refreshNotificationsData() {
        notificationsTable.setItems(FXCollections.observableArrayList(facade.loadNotifications()));
    }

    @FXML
    private void onCreateOperator() {
        if (hasBlank(operatorUsernameField.getText(), operatorPasswordField.getText(),
                operatorFullNameField.getText(), operatorEmailField.getText())) {
            showError("Попълнете всички полета за оператор.", null);
            return;
        }

        try {
            facade.createOperator(
                    operatorUsernameField.getText().trim(),
                    operatorPasswordField.getText(),
                    operatorFullNameField.getText().trim(),
                    operatorEmailField.getText().trim());
            clearInputs(operatorUsernameField, operatorPasswordField, operatorFullNameField, operatorEmailField);
            refreshUsersData();
            setStatus("Създаден е нов оператор.");
        } catch (Exception e) {
            showError("Неуспешно създаване на оператор.", e);
        }
    }

    @FXML
    private void onRegisterReader() {
        if (hasBlank(readerUsernameField.getText(), readerPasswordField.getText(),
                readerFullNameField.getText(), readerEmailField.getText())) {
            showError("Попълнете всички полета за читател.", null);
            return;
        }

        try {
            facade.registerReader(
                    readerUsernameField.getText().trim(),
                    readerPasswordField.getText(),
                    readerFullNameField.getText().trim(),
                    readerEmailField.getText().trim());
            clearInputs(readerUsernameField, readerPasswordField, readerFullNameField, readerEmailField);
            refreshUsersData();
            setStatus("Регистриран е нов читател.");
        } catch (Exception e) {
            showError("Неуспешна регистрация на читател.", e);
        }
    }

    @FXML
    private void onDeactivateReader() {
        User reader = readerSelectionBox.getValue();
        if (reader == null) {
            showError("Изберете читател за блокиране.", null);
            return;
        }

        try {
            facade.deactivateReader(reader.getId());
            refreshUsersData();
            setStatus("Читателят е блокиран.");
        } catch (Exception e) {
            showError("Неуспешно блокиране на читател.", e);
        }
    }

    @FXML
    private void onRemoveReader() {
        User reader = readerSelectionBox.getValue();
        if (reader == null) {
            showError("Изберете читател за отписване.", null);
            return;
        }

        try {
            facade.removeReader(reader.getId());
            refreshUsersData();
            setStatus("Читателят е изтрит.");
        } catch (Exception e) {
            showError("Неуспешно изтриване на читател.", e);
        }
    }

    @FXML
    private void onSubmitForm() {
        if (formSubmittedByCombo.getValue() == null || hasBlank(formContentArea.getText())) {
            showError("Изберете читател и съдържание на формуляра.", null);
            return;
        }

        try {
            Long readerId = formSubmittedByCombo.getValue().getId();
            Long operatorId = formOperatorCombo.getValue() != null ? formOperatorCombo.getValue().getId() : null;
            facade.submitReaderForm(formContentArea.getText().trim(), readerId, operatorId);
            formContentArea.clear();
            refreshFormsData();
            setStatus("Формулярът е подаден.");
        } catch (Exception e) {
            showError("Неуспешно подаване на формуляр.", e);
        }
    }

    @FXML
    private void onUpdateFormStatus() {
        FormRequest request = formSelectionCombo.getValue();
        FormStatus status = formStatusCombo.getValue();
        if (request == null || status == null) {
            showError("Изберете формуляр и статус.", null);
            return;
        }

        try {
            facade.updateFormStatus(request.getId(), status);
            refreshFormsData();
            setStatus("Статусът на формуляра е обновен.");
        } catch (Exception e) {
            showError("Неуспешно обновяване на формуляр.", e);
        }
    }

    @FXML
    private void onAddBook() {
        if (hasBlank(bookInventoryField.getText(), bookTitleField.getText(),
                bookAuthorField.getText(), bookGenreField.getText())) {
            showError("Попълнете всички полета за книга.", null);
            return;
        }

        try {
            facade.addBook(
                    bookInventoryField.getText().trim(),
                    bookTitleField.getText().trim(),
                    bookAuthorField.getText().trim(),
                    bookGenreField.getText().trim(),
                    bookConditionCombo.getValue());
            clearInputs(bookInventoryField, bookTitleField, bookAuthorField, bookGenreField);
            bookConditionCombo.getSelectionModel().clearSelection();
            refreshBooksData();
            setStatus("Книгата е добавена.");
        } catch (Exception e) {
            showError("Неуспешно добавяне на книга.", e);
        }
    }

    @FXML
    private void onArchiveBook() {
        Book book = archiveBookCombo.getValue();
        if (book == null) {
            showError("Изберете книга за архивиране.", null);
            return;
        }

        try {
            facade.archiveBook(book.getId());
            refreshBooksData();
            setStatus("Книгата е архивирана.");
        } catch (Exception e) {
            showError("Неуспешно архивиране на книга.", e);
        }
    }

    @FXML
    private void onScrapBook() {
        Book book = scrapBookCombo.getValue();
        if (book == null) {
            showError("Изберете книга за бракуване.", null);
            return;
        }

        try {
            facade.scrapBook(book.getId());
            refreshBooksData();
            setStatus("Книгата е бракувана.");
        } catch (Exception e) {
            showError("Неуспешно бракуване на книга.", e);
        }
    }

    @FXML
    private void onBorrowBook() {
        User reader = borrowReaderCombo.getValue();
        Book book = borrowBookCombo.getValue();
        BorrowType type = borrowTypeCombo.getValue();
        LocalDate dueDate = borrowDueDatePicker.getValue();

        if (reader == null || book == null || type == null || dueDate == null) {
            showError("Изберете читател, книга, тип и срок за връщане.", null);
            return;
        }

        try {
            facade.borrowBook(reader.getId(), book.getId(), type, dueDate);
            borrowDueDatePicker.setValue(null);
            refreshBooksData();
            refreshBorrowData();
            setStatus("Книгата е заета.");
        } catch (Exception e) {
            showError("Неуспешно заемане.", e);
        }
    }

    @FXML
    private void onReturnBook() {
        Borrow borrow = returnBorrowCombo.getValue();
        if (borrow == null) {
            showError("Изберете заем за връщане.", null);
            return;
        }

        try {
            facade.returnBook(borrow.getId());
            refreshBooksData();
            refreshBorrowData();
            setStatus("Книгата е върната.");
        } catch (Exception e) {
            showError("Неуспешно връщане.", e);
        }
    }

    @FXML
    private void onUpdateRating() {
        User reader = ratingReaderCombo.getValue();
        LoyaltyLevel level = loyaltyLevelCombo.getValue();
        if (reader == null || level == null) {
            showError("Изберете читател и рейтинг.", null);
            return;
        }

        try {
            facade.updateUserRating(reader.getId(), level);
            refreshRatingsData();
            setStatus("Рейтингът е обновен.");
        } catch (Exception e) {
            showError("Неуспешно обновяване на рейтинг.", e);
        }
    }

    @FXML
    private void onCreateNotification() {
        NotificationType type = notificationTypeCombo.getValue();
        String message = notificationMessageArea.getText();
        if (type == null || hasBlank(message)) {
            showError("Изберете тип и съобщение за известието.", null);
            return;
        }

        try {
            Long recipientId = notificationRecipientCombo.getValue() != null
                    ? notificationRecipientCombo.getValue().getId()
                    : null;
            facade.createNotification(type, message.trim(), recipientId);
            notificationMessageArea.clear();
            refreshNotificationsData();
            setStatus("Известието е изпратено.");
        } catch (Exception e) {
            showError("Неуспешно създаване на известие.", e);
        }
    }

    private void configureUserCombo(ComboBox<User> comboBox) {
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : userLabel(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : userLabel(item));
            }
        });
    }

    private void configureBookCombo(ComboBox<Book> comboBox) {
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : bookLabel(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Book item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : bookLabel(item));
            }
        });
    }

    private void configureBorrowCombo(ComboBox<Borrow> comboBox) {
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Borrow item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : borrowLabel(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Borrow item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : borrowLabel(item));
            }
        });
    }

    private void configureFormCombo(ComboBox<FormRequest> comboBox) {
        if (comboBox == null) {
            return;
        }
        comboBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(FormRequest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formLabel(item));
            }
        });
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FormRequest item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formLabel(item));
            }
        });
    }

    private String userLabel(User user) {
        return user == null ? "" : user.getFullName() + " (#" + user.getId() + ")";
    }

    private String bookLabel(Book book) {
        return book == null ? "" : book.getTitle() + " [" + book.getInventoryNumber() + "]";
    }

    private String borrowLabel(Borrow borrow) {
        if (borrow == null) {
            return "";
        }
        return borrow.getId() + ": " + bookLabel(borrow.getBook()) + " → " + userLabel(borrow.getReader());
    }

    private String formLabel(FormRequest formRequest) {
        if (formRequest == null) {
            return "";
        }
        return "#" + formRequest.getId() + " (" + formRequest.getStatus() + ")";
    }

    private boolean hasBlank(String... values) {
        if (values == null) {
            return true;
        }
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void clearInputs(TextInputControl... fields) {
        if (fields == null) {
            return;
        }
        for (TextInputControl field : fields) {
            if (field != null) {
                field.clear();
            }
        }
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void showError(String message, Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        if (throwable != null) {
            alert.setContentText(Objects.toString(throwable.getMessage(), ""));
        }
        alert.showAndWait();
        setStatus(message);
    }
}

