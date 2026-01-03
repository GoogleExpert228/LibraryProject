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
import org.example.library.enums.BorrowStatus;
import org.example.library.enums.FormStatus;
import org.example.library.enums.NotificationType;
import org.example.library.services.LibraryFacade;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ReaderController {

    private final LibraryFacade facade = new LibraryFacade();
    private User currentUser;

    @FXML private Label welcomeLabel;
    @FXML private Label ratingLabel;

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
    @FXML private TableColumn<FormRequest, String> requestContentCol;
    @FXML private TableColumn<FormRequest, FormStatus> requestStatusCol;

    // --- ИЗВЕСТИЯ ---
    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, LocalDate> notifDateCol;
    @FXML private TableColumn<Notification, NotificationType> notifTypeCol;
    @FXML private TableColumn<Notification, String> notifMsgCol;

    @FXML
    public void initialize() {
        setupTables();
    }

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Здравейте, " + user.getFullName());

        // Загружаем рейтинг пользователя
        facade.loadRatings().stream()
                .filter(r -> r.getReader().getId().equals(currentUser.getId()))
                .findFirst()
                .ifPresentOrElse(
                        r -> ratingLabel.setText("Вашият рейтинг: " + r.getRating()),
                        () -> ratingLabel.setText("Вашият рейтинг: Няма данни")
                );

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
        requestContentCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getContent()));
        requestStatusCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStatus()));

        // Notifications
        notifDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTimeStamp()));
        notifTypeCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getType()));
        notifMsgCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMessage()));
    }

    private void refreshData() {
        if (currentUser == null) return;

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

        // 4. Известия (фильтр по получателю)
        List<Notification> myNotifs = facade.loadNotifications().stream()
                .filter(n -> n.getRecipient() != null && n.getRecipient().getId().equals(currentUser.getId()))
                .sorted(Comparator.comparing(Notification::getTimeStamp).reversed())
                .collect(Collectors.toList());
        notificationsTable.setItems(FXCollections.observableArrayList(myNotifs));
    }

    @FXML
    private void onSubmitRequest() {
        String content = requestContentArea.getText();
        if (content == null || content.trim().isEmpty()) {
            showAlert("Моля, въведете текст на заявката.");
            return;
        }

        try {
            // null для createdByOperatorId, так как создал сам читатель
            facade.submitReaderForm(content, currentUser.getId(), null);
            requestContentArea.clear();
            refreshData();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Вашата заявка е изпратена успешно.");
            alert.show();
        } catch (Exception e) {
            showAlert("Грешка при изпращане: " + e.getMessage());
        }
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

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.show();
    }
}