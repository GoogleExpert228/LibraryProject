package org.example.library.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.library.configs.UserSession;
import org.example.library.contracts.LibraryService;
import org.example.library.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginService implements LibraryService {

    private static final Logger log =
            LoggerFactory.getLogger(LoginService.class);

    public String login(String username, String password, Stage stage) {

        log.info("Login attempt for username={}", username);

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {

            log.warn("Login failed: empty username or password");
            return "Моля, въведете име и парола.";
        }

        User user = ServiceFactory
                .service(UserService.class)
                .authenticate(username, password);

        if (user == null) {
            log.warn("Login failed: invalid credentials for username={}", username);
            return "Грешно потребителско име или парола!";
        }

        UserSession.getInstance(user);
        log.info("User {} logged in successfully (role={})",
                username, user.getRole());

        try {
            openUIByRole(user, stage);
            return null;
        } catch (IOException e) {
            log.error("Failed to load UI for user {}", username, e);
            return "Грешка при зареждане на интерфейса.";
        }
    }

    private void openUIByRole(User user, Stage stage) throws IOException {

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
            default -> {
                log.error("Unknown user role: {}", user.getRole());
                throw new IllegalStateException(
                        "Unknown role: " + user.getRole()
                );
            }
        }

        log.debug("Loading UI: {} ({})", fxmlPath, title);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/library/" + fxmlPath)
        );

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();

        log.info("UI loaded successfully for role={}", user.getRole());
    }
}
