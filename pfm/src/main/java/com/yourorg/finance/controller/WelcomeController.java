package com.yourorg.finance.controller;

import com.yourorg.finance.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class WelcomeController {
    @FXML private Button loginBtn, registerBtn;
    private Stage primaryStage;

    /** called by MainApp *after* FXMLLoader.load() */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        wireButtons();
    }

    /** moved out of initialize() */
    private void wireButtons() {
        loginBtn.setOnAction(e -> load("/fxml/login.fxml","PFM Login"));
        registerBtn.setOnAction(e -> load("/fxml/register.fxml","PFM Register"));
    }

    @FXML
    public void initialize() {
        // no-op
    }

    private void load(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // pass along the same stage & AuthService
            Object ctrl = loader.getController();
            if (ctrl instanceof BaseAuthController bac) {
                bac.setAuthService(AuthService.getInstance());
                bac.setPrimaryStage(primaryStage);
            }

            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle(title);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}