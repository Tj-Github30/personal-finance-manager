package com.yourorg.finance.controller;

import com.yourorg.finance.dao.BudgetDao;
import com.yourorg.finance.dao.CategoryDao;
import com.yourorg.finance.dao.TransactionDao;
import com.yourorg.finance.model.Budget;
import com.yourorg.finance.model.Category;
import com.yourorg.finance.model.Transaction;
import com.yourorg.finance.util.EventBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BudgetController {
    @FXML private TableView<Budget> table;
    @FXML private TableColumn<Budget, String> catCol;
    @FXML private TableColumn<Budget, Double> limitCol;
    @FXML private Button addBtn, editBtn, delBtn;
    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> yearFilter;


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
        // initialize filters:
        monthFilter.getItems().setAll(
                "All","Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
        );
        yearFilter.getItems().setAll("All","2023","2024","2025");
        monthFilter.getSelectionModel().select("All");
        yearFilter.getSelectionModel().select("All");

        // whenever filter changes, reload the table:
        monthFilter.setOnAction(e -> refreshTable());
        yearFilter .setOnAction(e -> refreshTable());

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
            // get raw budgets (these are static entries)
            List<Budget> allBudgets = dao.findByUser(currentUserId);

            // but we only *display* budgets that have spending in the selected window
            // (or alternatively, always display but show zero consumption…)
            String m = monthFilter.getValue();
            String y = yearFilter.getValue();

            // fetch all transactions once:
            List<Transaction> allTx = new TransactionDao().findByUser(currentUserId);
            Stream<Budget> stream = allBudgets.stream();

            // if month != All, parse to number:
            if (!"All".equals(m)) {
                int month = List.of(
                        "Jan","Feb","Mar","Apr","May","Jun",
                        "Jul","Aug","Sep","Oct","Nov","Dec"
                ).indexOf(m) + 1;
                stream = stream.filter(budget ->
                        allTx.stream().anyMatch(tx ->
                                tx.getCategory().equalsIgnoreCase(budget.getCategory())
                                        && tx.getDate().getMonthValue() == month
                                        && ( "All".equals(y) || tx.getDate().getYear() == Integer.parseInt(y) )
                        )
                );
            }
            // if year != All but month==All:
            else if (!"All".equals(y)) {
                int year = Integer.parseInt(y);
                stream = stream.filter(budget ->
                        allTx.stream().anyMatch(tx ->
                                tx.getCategory().equalsIgnoreCase(budget.getCategory())
                                        && tx.getDate().getYear() == year
                        )
                );
            }

            // collect & show
            List<Budget> filtered = stream.collect(Collectors.toList());
            data.setAll(filtered);

        } catch (SQLException ex) {
            showAlert("DB Error", ex.getMessage());
        }
    }


    // in BudgetController.showDialog(...)
    @FXML
    private void showDialog(Budget b) {
        boolean isNew = (b == null);

        // --- 1) Setup dialog ---
        Dialog<Budget> dlg = new Dialog<>();
        dlg.setTitle(isNew ? "New Budget" : "Edit Budget");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- 2) Load existing categories from CategoryDao ---
        List<Category> cats;
        try {
            cats = new CategoryDao().findAll(currentUserId);
        } catch (SQLException ex) {
            cats = List.of();  // fallback to empty
        }
        ObservableList<Category> catList = FXCollections.observableArrayList(cats);
        ComboBox<Category> catBox = new ComboBox<>(catList);
        catBox.setPrefWidth(200);

        // if editing, pre‑select current category
        if (!isNew) {
            catList.stream()
                    .filter(c -> c.getName().equals(b.getCategory()))
                    .findFirst()
                    .ifPresent(catBox::setValue);
        } else if (!catList.isEmpty()) {
            catBox.getSelectionModel().selectFirst();
        }

        // “+” button to create a new category inline
        Button addCatBtn = new Button("+");
        addCatBtn.setOnAction(evt -> {
            TextInputDialog input = new TextInputDialog();
            input.setTitle("New Category");
            input.setHeaderText("Enter new category name:");
            input.setContentText("Name:");
            input.showAndWait().ifPresent(name -> {
                try {
                    Category created = new CategoryDao().create(currentUserId, name.trim());
                    catList.add(created);
                    catBox.getSelectionModel().select(created);
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR,
                            "Failed to create category:\n" + e.getMessage(),
                            ButtonType.OK).showAndWait();
                }
            });
        });

        // --- 3) Amount field ---
        TextField limitField = new TextField(isNew ? "" : String.valueOf(b.getLimit()));

        // --- 4) Layout in GridPane ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Category:"),  0, 0);
        grid.add(new HBox(5, catBox, addCatBtn), 1, 0);

        grid.add(new Label("Limit:"),     0, 1);
        grid.add(limitField,              1, 1);

        dlg.getDialogPane().setContent(grid);

        // --- 5) Convert result back to Budget ---
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    double lim = Double.parseDouble(limitField.getText().trim());
                    String categoryName = catBox.getValue().getName();
                    return new Budget(
                            isNew ? 0 : b.getId(),
                            currentUserId,
                            categoryName,
                            lim
                    );
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.WARNING,
                            "Limit must be a valid number.",
                            ButtonType.OK).showAndWait();
                }
            }
            return null;
        });

        // --- 6) Show & persist ---
        dlg.showAndWait().ifPresent(nb -> {
            try {
                if (isNew) dao.create(nb);
                else       dao.update(nb);
                refreshTable();
                EventBus.get().publish("budgets:changed");
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR,
                        "DB Error: " + ex.getMessage(),
                        ButtonType.OK).showAndWait();
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
