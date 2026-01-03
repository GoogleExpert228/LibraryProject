package org.example.library.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.library.services.LoginService;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    private final LoginService loginService = new LoginService();

    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        String error = loginService.login(
                usernameField.getText(),
                passwordField.getText(),
                stage
        );

        if (error != null) {
            errorLabel.setText(error);
        }
    }
}