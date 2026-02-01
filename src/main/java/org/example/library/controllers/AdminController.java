package org.example.library.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.example.library.HelloApplication;
import org.example.library.configs.UserSession;
import org.example.library.entities.Book;
import org.example.library.entities.Borrow;
import org.example.library.entities.FormRequest;
import org.example.library.entities.User;
import org.example.library.enums.*;
import org.example.library.services.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminController {

    private final LibraryFacade facade = new LibraryFacade();
    private User currentUser;
    private FilteredList<User> filteredUsers;

    // --- ПОТРЕБИТЕЛИ (Админ часть) ---
    @FXML
    private TextField operatorUsernameField;
    @FXML
    private PasswordField operatorPasswordField;
    @FXML
    private TextField operatorFullNameField;
    @FXML
    private TextField operatorEmailField;

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> userFullNameColumn;
    @FXML
    private TableColumn<User, String> userUsernameColumn;
    @FXML
    private TableColumn<User, Role> userRoleColumn;

    @FXML
    private TableColumn<User, LocalDate> userRegistrationDateColumn;
    @FXML
    private TableColumn<User, String> userEmailColumn;
    @FXML
    private TableColumn<User, UserStatus> userStatusColumn;

    // --- ЧИТАТЕЛИ (Операции) ---
    @FXML
    private TextField readerUsernameField;
    @FXML
    private PasswordField readerPasswordField;
    @FXML
    private TextField readerFullNameField;
    @FXML
    private TextField readerEmailField;
    @FXML
    private ComboBox<User> readerActionCombo;

    // --- КНИГИ ---
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
    private TableView<Book> booksTable;
    @FXML
    private TableColumn<Book, String> bookInvColumn;
    @FXML
    private TableColumn<Book, String> bookTitleColumn;
    @FXML
    private TableColumn<Book, BookCondition> bookConditionColumn;
    @FXML
    private TableColumn<Book, Boolean> bookAvailColumn;
    @FXML
    private ComboBox<Book> bookActionCombo;
    // --- ЗАЕМАНЕ (Выдача) ---
    @FXML
    private ComboBox<User> borrowReaderCombo;
    @FXML
    private ComboBox<Book> borrowBookCombo;
    @FXML
    private ComboBox<BorrowType> borrowTypeCombo;
    @FXML
    private DatePicker borrowDueDatePicker;
    @FXML
    private TableView<Borrow> borrowsTable;
    @FXML
    private TableColumn<Borrow, String> borrowReaderColumn;
    @FXML
    private TableColumn<Borrow, String> borrowBookColumn;
    @FXML
    private TableColumn<Borrow, LocalDate> borrowDueColumn;
    @FXML
    private TableColumn<Borrow, BorrowStatus> borrowStatusColumn;

    @FXML
    private ComboBox<Borrow> returnBorrowCombo;

    // --- ФОРМУЛЯРИ ---
    @FXML
    private TableView<FormRequest> formsTable;
    @FXML
    private TableColumn<FormRequest, LocalDate> formDateColumn;
    @FXML
    private TableColumn<FormRequest, FormStatus> formStatusColumn;
    @FXML
    private TableColumn<FormRequest, Book> formBookColumn;
    @FXML
    private ComboBox<FormRequest> formActionCombo;
    @FXML
    private ComboBox<FormStatus> formStatusCombo;

    @FXML
    public void initialize() {
        setupTables();
        setupCombos();
        refreshAllData();
    }

    private void setupTables() {
        // Users
        userFullNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        userUsernameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        userEmailColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        userRegistrationDateColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getRegistrationDate()));
        userRoleColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getRole()));
        userStatusColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));

        // Books
        bookInvColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInventoryNumber()));
        bookTitleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        bookConditionColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCondition()));
        bookAvailColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().isAvailable()));

        // Borrows
        borrowReaderColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReader().getUsername()));
        borrowBookColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBook().getTitle()));
        borrowDueColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        borrowStatusColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getBorrowStatus()));

        // Forms
        formDateColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSubmitDate()));
        formStatusColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        formBookColumn.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getBook()));
    }

    private void setupCombos() {
        bookConditionCombo.setItems(FXCollections.observableArrayList(BookCondition.values()));
        borrowTypeCombo.setItems(FXCollections.observableArrayList(BorrowType.values()));
        formStatusCombo.setItems(FXCollections.observableArrayList(FormStatus.values()));

        setComboFactory(readerActionCombo);
        setComboFactory(borrowReaderCombo);
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
        this.currentUser = UserSession.getInstance().getUser();

        List<User> users = ServiceFactory.service(UserService.class).loadAllUsers();
        ObservableList<User> masterData = FXCollections.observableArrayList(users);

        filteredUsers = new FilteredList<>(masterData, user -> true);

        SortedList<User> sortedData = new SortedList<>(filteredUsers);
        sortedData.comparatorProperty().bind(usersTable.comparatorProperty());

        usersTable.setItems(sortedData);
        readerActionCombo.setItems(sortedData);

        List<User> readers = ServiceFactory.service(UserService.class).loadReaders();
        borrowReaderCombo.setItems(FXCollections.observableArrayList(readers));

        // Books
        List<Book> books = ServiceFactory.service(BookService.class).loadAllBooks();
        booksTable.setItems(FXCollections.observableArrayList(books));
        bookActionCombo.setItems(FXCollections.observableArrayList(books));
        borrowBookCombo.setItems(FXCollections.observableArrayList(ServiceFactory.service(BookService.class).loadAvailableBooks()));

        // Borrows
        List<Borrow> borrows = ServiceFactory.service(BorrowService.class).loadAllBorrows();
        borrowsTable.setItems(FXCollections.observableArrayList(borrows));
        returnBorrowCombo.setItems(FXCollections.observableArrayList(
                borrows.stream().filter(b -> b.getBorrowStatus() == BorrowStatus.ACTIVE).collect(Collectors.toList())
        ));

        formsTable.setItems(FXCollections.observableArrayList(ServiceFactory.service(FormRequestService.class).loadAll()));
        formActionCombo.setItems(FXCollections.observableArrayList(ServiceFactory.service(FormRequestService.class).loadAll()));
    }

    // --- ACTIONS ---

    @FXML
    private void onCreateOperator() {
        try {
            ServiceFactory.service(UserService.class).createOperator(operatorUsernameField.getText(), operatorPasswordField.getText(),
                    operatorFullNameField.getText(), operatorEmailField.getText());
            refreshAllData();
            showStatus("Операторът е създаден успешно.");
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML
    private void onRegisterReader() {
        try {
            ServiceFactory.service(UserService.class).registerReader(readerUsernameField.getText(), readerPasswordField.getText(),
                    readerFullNameField.getText(), readerEmailField.getText());
            refreshAllData();
            showStatus("Читателят е регистриран.");
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML
    private void onRemoveUser() {
        if (readerActionCombo.getValue() != null) {
             ServiceFactory.service(UserService.class).removeReader(readerActionCombo.getValue().getId());
            refreshAllData();
            showStatus("Читателят е изтрит от системата.");
        }
    }

    @FXML
    private void onAddBook() {
        try {
           ServiceFactory.service(BookService.class).addBook(bookInventoryField.getText(), bookTitleField.getText(),
                    bookAuthorField.getText(), bookGenreField.getText(), bookConditionCombo.getValue());
            refreshAllData();
            showStatus("Книгата е добавена.");
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML
    private void onArchiveBook() {
        if (bookActionCombo.getValue() != null) {
            ServiceFactory.service(BookService.class).archiveBook(bookActionCombo.getValue().getId());
            refreshAllData();
            showStatus("Книгата е архивирана.");
        }
    }

    @FXML
    private void onApproveRequest() {
        // Получаем выбранную заявку из таблицы заявок оператора
        FormRequest selected = formsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Моля, изберете заявка за одобрение.");
            return;
        }

        if (selected.getStatus() != FormStatus.PENDING) {
            showStatus("Могат да се одобряват само заявки със статус PENDING.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Одобряване на заявка");
        dialog.setHeaderText("Параметри за: " + selected.getBook().getTitle());

        ButtonType approveButtonType = new ButtonType("Одобри", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(approveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<BorrowType> typeCombo = new ComboBox<>(FXCollections.observableArrayList(BorrowType.values()));
        typeCombo.setValue(BorrowType.TAKING_HOME); // По умолчанию "На дом"

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(14));

        // ЛОГИКА АВТОМАТИЧЕСКОЙ СМЕНЫ ДАТЫ
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == BorrowType.READING_ROOM) {
                datePicker.setValue(LocalDate.now());
            }
        });

        grid.add(new Label("Тип:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Срок:"), 0, 1);
        grid.add(datePicker, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(response -> {
            if (response == approveButtonType) {
                try {
                    ServiceFactory.service(FormRequestService.class).approveRequestAndBorrow(selected.getId(), typeCombo.getValue(), datePicker.getValue());
                    refreshAllData();
                    showStatus("Заявката е одобрена успешно.");
                } catch (Exception e) {
                    showError(e);
                }
            }
        });
    }

    @FXML
    private void onBorrowBook() {
        try {
            ServiceFactory.service(BorrowService.class).borrowBook(borrowReaderCombo.getValue().getId(), borrowBookCombo.getValue().getId(), borrowTypeCombo.getValue(), borrowDueDatePicker.getValue());
            refreshAllData();
            showStatus("Книгата е отдадена.");
        } catch (Exception e) {
            showError(e);
        }
    }

    @FXML
    private void onReturnBook() {
        // Предположим, вы выбираете запись о выдаче из комбобокса или таблицы
        Borrow selectedBorrow = borrowsTable.getSelectionModel().getSelectedItem();

        if (selectedBorrow == null) {
            showAlert("Моля, изберете заем за връщане.");
            return;
        }

        if (selectedBorrow.getBorrowStatus() != BorrowStatus.ACTIVE) {
            showStatus("Тази книга вече е върната.");
            return;
        }

        ChoiceDialog<BookCondition> dialog = new ChoiceDialog<>(BookCondition.GOOD, BookCondition.values());
        dialog.setTitle("Връщане на книга");
        dialog.setHeaderText("Изберете състояние на книгата: " + selectedBorrow.getBook().getTitle());
        dialog.setContentText("Състояние:");

        Optional<BookCondition> result = dialog.showAndWait();
        result.ifPresent(condition -> {
            try {
                ServiceFactory.service(BorrowService.class).returnBook(selectedBorrow.getId(), condition);
                refreshAllData();
                showStatus("Книгата е върната успешно със статус: " + condition);
            } catch (Exception e) {
                showError(e);
            }
        });
    }

    @FXML
    private void onUpdateForm() {
        if (formActionCombo.getValue() != null && formStatusCombo.getValue() != null) {
            ServiceFactory.service(FormRequestService.class).updateStatus(formActionCombo.getValue().getId(), formStatusCombo.getValue());
            refreshAllData();
            showStatus("Статусът на формуляра е обновен.");
        }
    }

    @FXML
    public void onLogout(ActionEvent event) throws IOException {
        UserSession.cleanUserSession();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 400, 350);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Библиотека - Вход");
        stage.centerOnScreen();
    }

    private void showStatus(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.show();
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.show();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }
}