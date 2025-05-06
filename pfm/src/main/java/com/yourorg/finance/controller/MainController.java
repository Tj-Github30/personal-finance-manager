package com.yourorg.finance.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML private StackPane contentPane;

    @FXML
    public void initialize() {
        // load the dashboard view as soon as the main window comes up
        onDashboard();
    }

    @FXML
    private void onDashboard() {
        try {
            // explicitly load as a Parent (which is a Node)
            Parent dash = FXMLLoader.load(
                    getClass().getResource("/fxml/dashboard.fxml")
            );

            // now you can pass it directly to setAll(...)
            contentPane.getChildren().setAll(dash);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onTransactions() {
        try {
            Parent txView = FXMLLoader.load(
                    getClass().getResource("/fxml/transactions.fxml")
            );
            contentPane.getChildren().setAll(txView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void onBudgets() throws Exception {
        try {
            Parent txView = FXMLLoader.load(
                    getClass().getResource("/fxml/budgets.fxml")
            );
            contentPane.getChildren().setAll(txView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void onReports() {
        try {
            Parent txView = FXMLLoader.load(
                    getClass().getResource("/fxml/reports.fxml")
            );
            contentPane.getChildren().setAll(txView);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
