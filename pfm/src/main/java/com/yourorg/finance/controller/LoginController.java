package com.yourorg.finance.controller;

import com.yourorg.finance.model.User;
import com.yourorg.finance.service.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController extends BaseAuthController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLoginAction() {
        try {
            User u = auth.login(usernameField.getText().trim(),
                    passwordField.getText());
            if (u == null) {
                showAlert("Login Failed","Invalid credentials.");
                return;
            }

            // 1) Load the main layout
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml")
            );
            Parent mainRoot = loader.load();

            // 2) Wire up controller
            MainController mc = loader.getController();
            mc.setCurrentUser(u);

            // 3) Swap scenes
            Stage st = (Stage) usernameField.getScene().getWindow();
            st.setScene(new Scene(mainRoot, st.getWidth(), st.getHeight()));
            st.setTitle("PFM Dashboard");

            // 4) *Immediately* show the Dashboard tab
            mc.onDashboard();

        } catch (Exception ex) {
            showAlert("Error",ex.getMessage());
        }
    }


    @FXML
    private void handleShowRegister() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/register.fxml")
        );
        Parent regRoot = loader.load();
        // If your RegisterController needs the authService, inject it here:
        RegisterController rc = loader.getController();
        rc.setAuthService(AuthService.getInstance());   // or however you supply it

        // swap the root
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.getScene().setRoot(regRoot);
        stage.setTitle("PFM Register");
    }


    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING, m);
        a.setHeaderText(t);
        a.showAndWait();
    }
}
