package com.yourorg.finance.controller;

import com.yourorg.finance.model.User;
import com.yourorg.finance.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController extends BaseAuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField, confirmField;

    @FXML
    private void handleRegisterAction() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();
        String c = confirmField.getText();

        if (!p.equals(c)) {
            showAlert("Error", "Passwords do not match.");
            return;
        }

        try {
            User newUser = auth.register(u, p, "USER");
            // ← registration succeeded!

            // now swap back to the login screen:
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );
            Parent loginRoot = loader.load();

            // find the current stage by querying any node:
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
            stage.setTitle("PFM Login");

        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }


    @FXML
    private void handleBackToLogin(ActionEvent evt) throws IOException {
        Parent loginRoot = FXMLLoader.load(
                getClass().getResource("/fxml/login.fxml")
        );
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.getScene().setRoot(loginRoot);
        stage.setTitle("PFM Login");
    }


    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING, m);
        a.setHeaderText(t);
        a.showAndWait();
    }
}