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
import javafx.scene.Node;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    // Use the AuthService we built
    private final AuthService authService = new AuthService();

@FXML
private void handleLoginAction(ActionEvent event) {
    System.out.println("🔑 Login button clicked — username="
            + usernameField.getText() + " password=" + passwordField.getText());
    String user = usernameField.getText().trim();
    String pass = passwordField.getText();

    try {
        User authenticated = authService.login(user, pass);
        if (authenticated != null) {
            // Load main.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main.fxml")
            );
            Parent mainRoot = loader.load();

            // Optional: pass the logged-in user to MainController
            // MainController mainCtrl = loader.getController();
            // mainCtrl.setCurrentUser(authenticated);

            // Replace the current window's scene
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setTitle("PFM Dashboard");
            stage.setScene(new Scene(mainRoot, 1200, 800));
            stage.show();
        } else {
            // login failed → show error alert
            Alert err = new Alert(Alert.AlertType.ERROR, "Invalid username or password.");
            err.setHeaderText(null);
            err.showAndWait();
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
