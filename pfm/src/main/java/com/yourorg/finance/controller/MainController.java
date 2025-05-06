package com.yourorg.finance.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainController {
    @FXML private StackPane contentPane;

    @FXML
    public void initialize() {
        onDashboard();
    }

    @FXML private void onDashboard()    { loadView("/fxml/dashboard.fxml"); }
    @FXML private void onTransactions() { loadView("/fxml/transactions.fxml"); }
    @FXML private void onBudgets()      { loadView("/fxml/budgets.fxml"); }
    @FXML private void onReports()      { loadView("/fxml/reports.fxml"); }
    @FXML private void onReminders()    { loadView("/fxml/reminders.fxml"); }

    private void loadView(String path) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(path));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
