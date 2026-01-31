package org.example.library.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.library.HelloApplication;
import org.example.library.entities.*;
import org.example.library.enums.*;
import org.example.library.services.LibraryFacade;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class OperatorController {

    private final LibraryFacade facade = new LibraryFacade();
    private User currentUser;

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;

    // --- ЧИТАТЕЛИ (Управление) ---
    @FXML private TextField readerUsernameField;
    @FXML private PasswordField readerPasswordField;
    @FXML private TextField readerFullNameField;
    @FXML private TextField readerEmailField;

    @FXML private ComboBox<User> readerActionCombo; // Для блокировки/удаления
    @FXML private TableView<User> readersTable;
    @FXML private TableColumn<User, String> readerNameCol;
    @FXML private TableColumn<User, String> readerUserCol;
    @FXML private TableColumn<User, UserStatus> readerStatusCol;

    // --- КНИГИ ---
    @FXML private TextField bookInventoryField;
    @FXML private TextField bookTitleField;
    @FXML private TextField bookAuthorField;
    @FXML private TextField bookGenreField;
    @FXML private ComboBox<BookCondition> bookConditionCombo;
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> bookInvColumn;
    @FXML private TableColumn<Book, String> bookTitleColumn;
    @FXML private TableColumn<Book, BookCondition> bookConditionColumn;
    @FXML private TableColumn<Book, Boolean> bookAvailColumn;

    @FXML private ComboBox<Book> bookActionCombo;

    // --- ЗАЕМАНЕ ---
    @FXML private ComboBox<User> borrowReaderCombo;
    @FXML private ComboBox<Book> borrowBookCombo;
    @FXML private ComboBox<BorrowType> borrowTypeCombo;
    @FXML private DatePicker borrowDueDatePicker;
    @FXML private TableView<Borrow> borrowsTable;
    @FXML private TableColumn<Borrow, String> borrowReaderColumn;
    @FXML private TableColumn<Borrow, String> borrowBookColumn;
    @FXML private TableColumn<Borrow, LocalDate> borrowDueColumn;
    @FXML private TableColumn<Borrow, BorrowStatus> borrowStatusColumn;

    @FXML private ComboBox<Borrow> returnBorrowCombo;

    // --- ФОРМУЛЯРИ ---
    @FXML private TableView<FormRequest> formsTable;
    @FXML private TableColumn<FormRequest, LocalDate> formDateColumn;
    @FXML private TableColumn<FormRequest, FormStatus> formStatusColumn;
    @FXML private TableColumn<FormRequest, String> formContentColumn;
    @FXML private ComboBox<FormRequest> formActionCombo;
    @FXML private ComboBox<FormStatus> formStatusCombo;

    // --- ИЗВЕСТИЯ ---
    @FXML private ComboBox<NotificationType> notifTypeCombo;
    @FXML private TextArea notifMsgArea;
    @FXML private ComboBox<User> notifRecipientCombo;

    @FXML
    public void initialize() {
        setupTables();
        setupCombos();
        refreshAllData();
    }

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Оператор: " + user.getFullName());
    }

    private void setupTables() {
        // Readers Table
        readerNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        readerUserCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        readerStatusCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));

        // Books
        bookInvColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInventoryNumber()));
        bookTitleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        bookConditionColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCondition()));
        bookAvailColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().isAvailable()));

        // Borrows
        borrowReaderColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReader().getFullName()));
        borrowBookColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBook().getTitle()));
        borrowDueColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        borrowStatusColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getBorrowStatus()));

        // Forms
        formDateColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSubmitDate()));
        formStatusColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        formContentColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContent()));
    }

    private void setupCombos() {
        bookConditionCombo.setItems(FXCollections.observableArrayList(BookCondition.values()));
        borrowTypeCombo.setItems(FXCollections.observableArrayList(BorrowType.values()));
        notifTypeCombo.setItems(FXCollections.observableArrayList(NotificationType.values()));
        formStatusCombo.setItems(FXCollections.observableArrayList(FormStatus.values()));

        setComboFactory(readerActionCombo);
        setComboFactory(borrowReaderCombo);
        setComboFactory(notifRecipientCombo);
    }

    private <T> void setComboFactory(ComboBox<T> combo) {
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        combo.setCellFactory(p -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
    }

    private void refreshAllData() {
        // Readers
        List<User> readers = facade.loadReaders();
        readersTable.setItems(FXCollections.observableArrayList(readers));
        readerActionCombo.setItems(FXCollections.observableArrayList(readers));
        borrowReaderCombo.setItems(FXCollections.observableArrayList(readers));
        notifRecipientCombo.setItems(FXCollections.observableArrayList(readers)); // Operators notify readers

        // Books
        List<Book> books = facade.loadAllBooks();
        booksTable.setItems(FXCollections.observableArrayList(books));
        bookActionCombo.setItems(FXCollections.observableArrayList(books));
        borrowBookCombo.setItems(FXCollections.observableArrayList(facade.loadAvailableBooks()));

        // Borrows
        List<Borrow> borrows = facade.loadAllBorrows();
        borrowsTable.setItems(FXCollections.observableArrayList(borrows));
        returnBorrowCombo.setItems(FXCollections.observableArrayList(
                borrows.stream().filter(b -> b.getBorrowStatus() == BorrowStatus.ACTIVE).collect(Collectors.toList())
        ));

        // Forms
        formsTable.setItems(FXCollections.observableArrayList(facade.loadAllForms()));
        formActionCombo.setItems(FXCollections.observableArrayList(facade.loadAllForms()));
    }

    // --- ACTIONS ---

    @FXML
    private void onRegisterReader() {
        if (readerUsernameField.getText().isEmpty()) {
            showStatus("Моля, въведете данни за читател.");
            return;
        }
        try {
            facade.registerReader(readerUsernameField.getText(), readerPasswordField.getText(),
                    readerFullNameField.getText(), readerEmailField.getText());
            refreshAllData();
            showStatus("Читателят е регистриран успешно.");
            clearFields(readerUsernameField, readerPasswordField, readerFullNameField, readerEmailField);
        } catch (Exception e) { showError(e); }
    }

    @FXML
    private void onBlockReader() {
        if (readerActionCombo.getValue() != null) {
            facade.deactivateReader(readerActionCombo.getValue().getId());
            refreshAllData();
            showStatus("Читателят е блокиран.");
        }
    }

    @FXML
    private void onRemoveReader() {
        if (readerActionCombo.getValue() != null) {
            facade.removeReader(readerActionCombo.getValue().getId());
            refreshAllData();
            showStatus("Читателят е изтрит от системата.");
        }
    }

    @FXML
    private void onAddBook() {
        try {
            facade.addBook(bookInventoryField.getText(), bookTitleField.getText(),
                    bookAuthorField.getText(), bookGenreField.getText(), bookConditionCombo.getValue());
            refreshAllData();
            showStatus("Книгата е добавена.");
        } catch (Exception e) { showError(e); }
    }

    @FXML
    private void onArchiveBook() {
        if (bookActionCombo.getValue() != null) {
            facade.archiveBook(bookActionCombo.getValue().getId());
            refreshAllData();
            showStatus("Книгата е архивирана.");
        }
    }

    @FXML
    private void onBorrowBook() {
        try {
            facade.borrowBook(borrowReaderCombo.getValue().getId(), borrowBookCombo.getValue().getId(),
                    borrowTypeCombo.getValue(), borrowDueDatePicker.getValue());
            refreshAllData();
            showStatus("Книгата е отдадена.");
        } catch (Exception e) { showError(e); }
    }

    @FXML
    private void onReturnBook() {
        if (returnBorrowCombo.getValue() != null) {
            facade.returnBook(returnBorrowCombo.getValue().getId());
            refreshAllData();
            showStatus("Книгата е върната.");
        }
    }

    @FXML
    private void onUpdateForm() {
        if (formActionCombo.getValue() != null && formStatusCombo.getValue() != null) {
            facade.updateFormStatus(formActionCombo.getValue().getId(), formStatusCombo.getValue());
            refreshAllData();
            showStatus("Статусът е обновен.");
        }
    }

    @FXML
    private void onSendNotification() {
        try {
            Long recipientId = notifRecipientCombo.getValue() != null ? notifRecipientCombo.getValue().getId() : null;
            facade.createNotification(notifTypeCombo.getValue(), notifMsgArea.getText(), recipientId);
            showStatus("Известието е изпратено.");
            notifMsgArea.clear();
        } catch (Exception e) { showError(e); }
    }

    @FXML
    public void onLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 350);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Библиотека - Вход");
        stage.centerOnScreen();
    }

    private void showStatus(String msg) { statusLabel.setText(msg); }
    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.show();
    }
    private void clearFields(TextInputControl... fields) { for(TextInputControl f : fields) f.clear(); }
}