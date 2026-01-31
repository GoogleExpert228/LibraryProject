package org.example.library.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.library.configs.UserSession;
import org.example.library.entities.User;

import java.io.IOException;

public class LoginService {

    private final LibraryFacade facade = new LibraryFacade();

    public String login(String username, String password, Stage stage) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return "Моля, въведете име и парола.";
        }

        User user = facade.authenticate(username, password);

        if (user == null) {
            return "Грешно потребителско име или парола!";
        }

        UserSession.getInstance(user);

        try {
            openUIByRole(user, stage);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
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
            default -> throw new IllegalStateException(
                    "Unknown role: " + user.getRole()
            );
        }

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/library/" + fxmlPath)
        );

        Parent root = loader.load();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();
    }
}