package com.yourorg.finance.controller;

import com.yourorg.finance.model.User;
import com.yourorg.finance.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    private User currentUser;

    @FXML public void initialize() {
//        Parent welcome = FXMLLoader.load(getClass().getResource("/fxml/welcome.fxml"));
//        contentPane.getChildren().setAll(welcome);
        loadView("/fxml/welcome.fxml", "Welcome");
    }

    public void setCurrentUser(User u) {
        this.currentUser = u;
        // optionally update your pageTitle with their name:
        pageTitle.setText("Dashboard — " + u.getUsername());
    }

    @FXML
    void onDashboard() {
        loadView("/fxml/dashboard.fxml", "Dashboard");
    }

    @FXML private void onTransactions() {
        loadView("/fxml/transactions.fxml", "Transactions");
    }

    @FXML private void onBudgets() {
        loadView("/fxml/budgets.fxml", "Budgets");
    }

    @FXML private void onReports() {
        loadView("/fxml/reports.fxml", "Reports");
    }

    @FXML private void onReminders() {
        loadView("/fxml/reminders.fxml", "Reminders");
    }

    @FXML private void onLogout() {
        try {
            AuthService.getInstance().logout();
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.getScene().setRoot(loginRoot);
            stage.setTitle("Please Log In");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath, String title) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(view);
            pageTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
