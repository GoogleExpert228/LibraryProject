package org.example.library.controllers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
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
import org.example.library.entities.Reader;
import org.example.library.entities.User;
import org.example.library.services.LibraryFacade;
import org.example.library.services.LoginService;
import org.example.library.services.ServiceFactory;
import org.example.library.services.UserService;

import java.io.IOException;
import java.util.Set;

public class LoginController {

    private final LoginService loginService = new LoginService();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private Label usernameErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label emailErrorLabel;
    private final LibraryFacade facade = new LibraryFacade();

    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene().getWindow();

        String error = ServiceFactory.service(LoginService.class).login(
                usernameField.getText(),
                passwordField.getText(),
                stage
        );
        if (error != null) {
            errorLabel.setText(error);
        }
    }

    @FXML
    private void handleOpenRegisterView(ActionEvent event) {
        try {
            ((Node) event.getSource()).getScene().getWindow().hide();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/library/register_view.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Регистриране");
            loginStage.setScene(new Scene(root));
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Грешка: Не е възможно да се зареди прозорецът за вход. Проверете пътя към FXML файла..");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        usernameErrorLabel.setText("");
        passwordErrorLabel.setText("");
        emailErrorLabel.setText("");
        usernameField.setStyle("");
        passwordField.setStyle("");
        emailField.setStyle("");

        // 2. Create the entity from UI input
        User user = new Reader();
        user.setUsername(usernameField.getText());
        user.setPassword(passwordField.getText());
        user.setEmail(emailField.getText());

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            for (ConstraintViolation<User> violation : violations) {
                String propertyPath = violation.getPropertyPath().toString();

                if (propertyPath.equals("username")) {
                    usernameErrorLabel.setText(violation.getMessage());
                    usernameField.setStyle("-fx-border-color: red;");
                } else if (propertyPath.equals("password")) {
                    passwordErrorLabel.setText(violation.getMessage());
                    passwordField.setStyle("-fx-border-color: red;");
                } else if (propertyPath.equals("email")) {
                    emailErrorLabel.setText(violation.getMessage());
                    emailField.setStyle("-fx-border-color: red;");
                }
            }
        } else {
            ServiceFactory.service(UserService.class).registerReader(usernameField.getText(), passwordField.getText(), fullNameField.getText(), emailField.getText());
            System.out.println("Читател " + usernameField.getText() + " успешно създан!");
            handleCancelRegister(event);
        }
    }

    public void handleCancelRegister(ActionEvent actionEvent) {
        try {
            ((Node) actionEvent.getSource()).getScene().getWindow().hide();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/library/login-view.fxml"));
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Вход в системата");
            loginStage.setScene(new Scene(root));
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Грешка: Не е възможно да се зареди прозорецът за вход. Проверете пътя към FXML файла..");
        }
    }
}