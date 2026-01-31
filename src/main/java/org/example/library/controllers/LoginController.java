package org.example.library.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.library.entities.User;
import org.example.library.services.LibraryFacade;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final LibraryFacade facade = new LibraryFacade();

    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Моля, въведете име и парола.");
            return;
        }

        User user = facade.authenticate(username, password);

        if (user != null) {
            try {
                loadUIByRole(user, event);
            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Грешка при зареждане на интерфейса: " + e.getMessage());
            }
        } else {
            errorLabel.setText("Грешно потребителско име или парола!");
        }
    }

    private void loadUIByRole(User user, ActionEvent event) throws IOException {
        String fxmlPath;
        String title;

        switch (user.getRole()) {
            case ADMIN -> {
                fxmlPath = "admin-view.fxml";
                title = "Административен панел";
            }
            case OPERATOR -> {
                fxmlPath = "operator-view.fxml";
                title = "Операторски панел";
            }
            case READER -> {
                fxmlPath = "reader-view.fxml";
                title = "Библиотека - Читател";
            }
            default -> throw new IllegalArgumentException("Unknown role: " + user.getRole());
        }

        // Загружаем View
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/library/" + fxmlPath));
        Parent root = loader.load();


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();
    }
}