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
import org.example.library.configs.UserSession;
import org.example.library.entities.*;
import org.example.library.enums.BorrowStatus;
import org.example.library.enums.FormStatus;
import org.example.library.services.LibraryFacade;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ReaderController {

    private final LibraryFacade facade = new LibraryFacade();
    private User currentUser;
    // --- КАТАЛОГ ---
    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> bookTitleCol;
    @FXML private TableColumn<Book, String> bookAuthorCol;
    @FXML private TableColumn<Book, String> bookGenreCol;
    @FXML private TableColumn<Book, String> bookConditionCol;
    @FXML private TableColumn<Book, String> bookStatusCol;

    // --- МОИ ЗАЕМИ ---
    @FXML private TableView<Borrow> myBorrowsTable;
    @FXML private TableColumn<Borrow, String> borrowBookCol;
    @FXML private TableColumn<Borrow, LocalDate> borrowDateCol;
    @FXML private TableColumn<Borrow, LocalDate> borrowDueCol;
    @FXML private TableColumn<Borrow, BorrowStatus> borrowStatusCol;

    // --- ЗАЯВКИ ---
    @FXML private TextArea requestContentArea;
    @FXML private TableView<FormRequest> myRequestsTable;
    @FXML private TableColumn<FormRequest, LocalDate> requestDateCol;
    @FXML private TableColumn<FormRequest, Book> requestBookCol;
    @FXML private TableColumn<FormRequest, FormStatus> requestStatusCol;

    @FXML private ComboBox<Book> requestBookCombo;

    @FXML
    public void initialize() {
        setupTables();
        refreshData();
    }

    private void setupTables() {
        // Books
        bookTitleCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        bookAuthorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthor()));
        bookGenreCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenre()));
        bookConditionCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCondition().toString()));
        bookStatusCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isAvailable() ? "Налична" : "Заета"));

        // Borrows
        borrowBookCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBook().getTitle()));
        borrowDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getBorrowDate()));
        borrowDueCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDueDate()));
        borrowStatusCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getBorrowStatus()));

        // Requests
        requestDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSubmitDate()));
        requestStatusCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));
        requestBookCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getBook()));
    }

    private void refreshData() {
        this.currentUser = UserSession.getInstance().getUser();
        // 1. Каталог (все книги)
        booksTable.setItems(FXCollections.observableArrayList(facade.loadAllBooks()));

        // 2. Заеми (фильтр по текущему пользователю)
        List<Borrow> myBorrows = facade.loadAllBorrows().stream()
                .filter(b -> b.getReader().getId().equals(currentUser.getId()))
                .sorted(Comparator.comparing(Borrow::getBorrowDate).reversed())
                .collect(Collectors.toList());
        myBorrowsTable.setItems(FXCollections.observableArrayList(myBorrows));

        // 3. Заявки (фильтр по текущему пользователю)
        List<FormRequest> myRequests = facade.loadAllForms().stream()
                .filter(f -> f.getSubmittedBy().getId().equals(currentUser.getId()))
                .sorted(Comparator.comparing(FormRequest::getSubmitDate).reversed())
                .collect(Collectors.toList());
        myRequestsTable.setItems(FXCollections.observableArrayList(myRequests));

        if (requestBookCombo != null) {
            requestBookCombo.setItems(FXCollections.observableArrayList(facade.loadAvailableBooks()));
            requestBookCombo.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void onSubmitRequest() {
        Book selectedBook = requestBookCombo.getValue();

        try {
            // null для createdByOperatorId, так как создал сам читатель
            facade.submitReaderForm(currentUser.getId(), selectedBook);
            refreshData();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Вашата заявка е изпратена успешно.");
            alert.show();
        } catch (Exception e) {
            showAlert("Грешка при изпращане: " + e.getMessage());
        }
    }

    @FXML
    private void onReturnBook() {
        Borrow selected = myBorrowsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Моля, изберете заем за връщане от таблицата.");
            return;
        }

        if (selected.getBorrowStatus() != BorrowStatus.ACTIVE) {
            showAlert("Този заем вече е приключен.");
            return;
        }

        try {
            // Вызываем новый метод фасада для создания запроса на возврат
            facade.requestReturn(selected.getId());
            refreshData();
            showStatus("Заявката за връщане е изпратена успешно. Моля, предайте книгата на оператор.");
        } catch (Exception e) {
            showError(e);
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

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }
    private void showStatus(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.show();
    }
    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.show();
    }
}