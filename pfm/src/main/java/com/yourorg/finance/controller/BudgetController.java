package com.yourorg.finance.controller;

import com.yourorg.finance.dao.BudgetDao;
import com.yourorg.finance.model.Budget;
import com.yourorg.finance.model.Transaction;
import com.yourorg.finance.util.EventBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BudgetController {
    @FXML private TableView<Budget> table;
    @FXML private TableColumn<Budget, String> catCol;
    @FXML private TableColumn<Budget, Double> limitCol;
    @FXML private Button addBtn, editBtn, delBtn;

    private final BudgetDao dao = new BudgetDao();
    private final int currentUserId = 1; // TODO: replace with real user
//    private final ObservableList<Budget> data = FXCollections.observableArrayList();
    private final ObservableList<Budget> data =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1) Column wiring: category ⇢ category, limit_amount ⇢ limit
        catCol  .setCellValueFactory(new PropertyValueFactory<>("category"));
        limitCol.setCellValueFactory(new PropertyValueFactory<>("limit"));

        // 2) Bind data & load
        table.setItems(data);


        // 3) strip out any stray columns (empty header OR no factory)
        table.getColumns().removeIf(c ->
                c.getText() == null
                        || c.getText().isBlank()
                        || c.getCellValueFactory() == null
        );
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        catCol.prefWidthProperty().bind(table.widthProperty().multiply(0.60));
        limitCol.prefWidthProperty().bind(table.widthProperty().multiply(0.40));

        refreshTable();

        // Hook up buttons
        addBtn.setOnAction(e -> showDialog(null));
        editBtn.setOnAction(e -> {
            Budget sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                showDialog(sel);
            } else {
                showAlert("Select one", "Please select a budget to edit.");
            }
        });
        delBtn.setOnAction(e -> {
            Budget sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    dao.delete(sel.getId());
                    refreshTable();
                    EventBus.get().publish("budgets:changed");
                    System.out.println("🏷  [BudgetController] published budgets:changed");
                } catch (SQLException ex) {
                    showAlert("DB Error", ex.getMessage());
                }
            } else {
                showAlert("Select one", "Please select a budget to delete.");
            }
        });

        // 4) Subscribe so dashboard updates automatically
        EventBus.get().subscribe(topic -> {
            if ("budgets:changed".equals(topic)) {
                javafx.application.Platform.runLater(this::refreshTable);
            }
        });
    }

    private void refreshTable() {
        try {
            List<Budget> list = dao.findByUser(currentUserId);
            data.setAll(list);
        } catch (SQLException ex) {
            showAlert("DB Error", ex.getMessage());
        }
    }

    private void showDialog(Budget b) {
        boolean isNew = (b == null);

        Dialog<Budget> dlg = new Dialog<>();
        dlg.setTitle(isNew ? "New Budget" : "Edit Budget");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField catField   = new TextField(isNew ? "" : b.getCategory());
        TextField limitField = new TextField(isNew ? "" : String.valueOf(b.getLimit()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Category:"), 0, 0);
        grid.add(catField,                1, 0);
        grid.add(new Label("Limit:"),    0, 1);
        grid.add(limitField,              1, 1);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    double lim = Double.parseDouble(limitField.getText().trim());
                    return new Budget(
                            isNew ? 0 : b.getId(),
                            currentUserId,
                            catField.getText().trim(),
                            lim
                    );
                } catch (NumberFormatException ex) {
                    showAlert("Invalid", "Limit must be a number.");
                }
            }
            return null;
        });

        Optional<Budget> res = dlg.showAndWait();
        res.ifPresent(nb -> {
            try {
                if (isNew) dao.create(nb);
                else       dao.update(nb);
                refreshTable();
                EventBus.get().publish("budgets:changed");
                System.out.println("🏷  [BudgetController] published budgets:changed");
            } catch (SQLException ex) {
                showAlert("DB Error", ex.getMessage());
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
