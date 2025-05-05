package com.yourorg.finance.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML private StackPane contentPane;

    @FXML
    private void onDashboard() {
        contentPane.getChildren().setAll(new Label("Dashboard View"));
    }

    @FXML
    private void onTransactions() {
        contentPane.getChildren().setAll(new Label("Transactions View"));
    }

    @FXML
    private void onBudgets() {
        contentPane.getChildren().setAll(new Label("Budgets View"));
    }

    @FXML
    private void onReports() {
        contentPane.getChildren().setAll(new Label("Reports View"));
    }

    @FXML
    private void onReminders() {
        contentPane.getChildren().setAll(new Label("Reminders View"));
    }

    @FXML
    private void onSettings() {
        contentPane.getChildren().setAll(new Label("Settings View"));
    }
}
