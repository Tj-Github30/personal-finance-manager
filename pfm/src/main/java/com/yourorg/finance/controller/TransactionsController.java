package com.yourorg.finance.controller;

import com.yourorg.finance.dao.CategoryDao;
import com.yourorg.finance.dao.TransactionDao;
import com.yourorg.finance.model.Category;
import com.yourorg.finance.model.Transaction;
import com.yourorg.finance.model.User;
import com.yourorg.finance.service.AuthService;
import com.yourorg.finance.util.EventBus;          // <— import EventBus
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionsController {
    @FXML private TableView<Transaction> table;
    @FXML private TableColumn<Transaction, LocalDate> dateCol;
    @FXML private TableColumn<Transaction, String> descCol;
    @FXML private TableColumn<Transaction, String> catCol;
    @FXML private TableColumn<Transaction, Double> amtCol;
    @FXML private Button addBtn, editBtn, delBtn;
    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private Button exportCsvBtn;

    private final TransactionDao dao    = new TransactionDao();
    private final CategoryDao   catDao = new CategoryDao();
    private int currentUserId() {
        User u = AuthService.getInstance().getCurrentUser();
        return u != null ? u.getId() : -1;
    }
    // TODO: wire this up properly

    /** The single backing list for the TableView */
    private final ObservableList<Transaction> data =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1) Column wiring
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        catCol .setCellValueFactory(new PropertyValueFactory<>("category"));
        amtCol .setCellValueFactory(new PropertyValueFactory<>("amount"));
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

        // 1b) Custom cell factory for amount: expenses in red (-), income in green
        amtCol.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction tx = getTableView().getItems().get(getIndex());
                    boolean isIncome = "Income".equalsIgnoreCase(tx.getCategory());
                    double display = isIncome ? amount : -amount;
                    if (isIncome) {
                        setText(String.format("$%.2f", display));
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setText(String.format("-$%.2f", Math.abs(display)));
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        // 2) Bind our one list to the table
        table.setItems(data);

        // 3) Remove any stray blank columns, set proportional resize
        table.getColumns().removeIf(c -> c.getText()==null || c.getText().isBlank());
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dateCol.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        descCol.prefWidthProperty().bind(table.widthProperty().multiply(0.40));
        catCol .prefWidthProperty().bind(table.widthProperty().multiply(0.25));
        amtCol .prefWidthProperty().bind(table.widthProperty().multiply(0.25));

        // 4) Load initial data
        refreshTable();

        // 5) Wire buttons
        addBtn .setOnAction(e -> showTransactionDialog(null));
        editBtn.setOnAction(e -> {
            Transaction sel = table.getSelectionModel().getSelectedItem();
            if (sel!=null) showTransactionDialog(sel);
            else showAlert("No selection","Please select a transaction to edit.");
        });
        delBtn.setOnAction(e -> {
            Transaction sel = table.getSelectionModel().getSelectedItem();
            if (sel!=null) handleDelete(sel);
            else showAlert("No selection","Please select a transaction to delete.");
        });
        exportCsvBtn.setOnAction(e -> exportCsv());
    }

    /** Reloads all transactions for the current user, applying month/year filters */
    public void refreshTable() {
        try {
            // 1) Load everything
            List<Transaction> allTx = dao.findByUser(currentUserId());

            // 2) Read selections
            String m = monthFilter.getValue();
            String y = yearFilter.getValue();

            // 3) Stream‐filter by month/year if not “All”
            Stream<Transaction> stream = allTx.stream();
            if (m != null && !"All".equals(m)) {
                // map “Jan”→1, “Feb”→2, …
                int monthIdx = List.of(
                        "Jan","Feb","Mar","Apr","May","Jun",
                        "Jul","Aug","Sep","Oct","Nov","Dec"
                ).indexOf(m) + 1;
                stream = stream.filter(tx ->
                        tx.getDate().getMonthValue() == monthIdx
                );
            }
            if (y != null && !"All".equals(y)) {
                int year = Integer.parseInt(y);
                stream = stream.filter(tx ->
                        tx.getDate().getYear() == year
                );
            }

            // 4) Collect & bind
            List<Transaction> filtered = stream.collect(Collectors.toList());
            data.setAll(filtered);
            table.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("DB Error", e.getMessage());
        }
    }


    /** Shows a dialog to add or edit a transaction */
    private void showTransactionDialog(Transaction tx) {
        boolean isNew = (tx==null);

        // --- 1) Setup dialog ---
        Dialog<Transaction> dlg = new Dialog<>();
        dlg.setTitle(isNew ? "New Transaction" : "Edit Transaction");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // --- 2) Build form controls ---
        DatePicker datePicker = new DatePicker(isNew ? LocalDate.now() : tx.getDate());
        TextField descField  = new TextField(isNew ? "" : tx.getDescription());

        // --- 3) Load categories ---
        List<Category> cats;
        try {
            cats = catDao.findAll(currentUserId());
        } catch (SQLException e) {
            e.printStackTrace();
            cats = List.of();
        }
        ObservableList<Category> catList = FXCollections.observableArrayList(cats);
        ComboBox<Category> catBox = new ComboBox<>(catList);
        if (isNew) {
            if (!catList.isEmpty()) catBox.setValue(catList.get(0));
        } else {
            catList.stream()
                    .filter(c->c.getName().equals(tx.getCategory()))
                    .findFirst()
                    .ifPresent(catBox::setValue);
        }

        // --- 4) “+” button to add a new category on the fly ---
        Button addCatBtn = new Button("+");
        addCatBtn.setOnAction(evt -> {
            TextInputDialog input = new TextInputDialog();
            input.setTitle("New Category");
            input.setHeaderText("Enter category name:");
            input.setContentText("Name:");
            input.showAndWait().ifPresent(name -> {
                try {
                    Category created = catDao.create(currentUserId(), name.trim());
                    catList.add(created);
                    catBox.setItems(catList);
                    catBox.setValue(created);
                } catch (SQLException ex) {
                    new Alert(Alert.AlertType.ERROR,
                            "Cannot create category: "+ex.getMessage())
                            .showAndWait();
                }
            });
        });

        // --- 5) Layout everything in a GridPane ---
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Date:"),        0, 0);
        grid.add(datePicker,                1, 0);

        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField,                 1, 1);

        grid.add(new Label("Category:"),    0, 2);
        grid.add(new HBox(5, catBox, addCatBtn), 1, 2);

        grid.add(new Label("Amount:"),      0, 3);
        TextField amtField = new TextField(isNew ? "" : String.valueOf(tx.getAmount()));
        grid.add(amtField,                  1, 3);

        dlg.getDialogPane().setContent(grid);

        // --- 6) When OK clicked, convert inputs back to a Transaction ---
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    double amt = Double.parseDouble(amtField.getText().trim());
                    return new Transaction(
                            isNew ? 0 : tx.getId(),
                            currentUserId(),
                            datePicker.getValue(),
                            descField.getText().trim(),
                            catBox.getValue().getName(),
                            amt
                    );
                } catch (NumberFormatException nf) {
                    showAlert("Invalid input","Amount must be a number.");
                }
            }
            return null;
        });

        // --- 7) Persist & refresh ---
        Optional<Transaction> result = dlg.showAndWait();
        result.ifPresent(t -> {
            try {
                if (isNew) dao.create(t);
                else        dao.update(t);

                // re‑populate the local table immediately
                refreshTable();

                // tell the Dashboard to refresh its cards/charts
                EventBus.get().publish("transactions:changed");

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert("DB Error", ex.getMessage());
            }
        });
    }

    /** Deletes a transaction after confirmation */
    private void handleDelete(Transaction tx) {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete transaction on " + tx.getDate() + "?",
                ButtonType.YES, ButtonType.NO);
        conf.setHeaderText(null);
        conf.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try {
                    dao.delete(tx.getId());
                    refreshTable();
                    EventBus.get().publish("transactions:changed");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showAlert("DB Error", ex.getMessage());
                }
            }
        });
    }

    /** Utility to show a simple alert */
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void exportCsv() {
        // 1) Fetch whichever set of transactions you want:
        List<Transaction> rows;
        try {
            rows = dao.findByUser(currentUserId());
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage())
                    .showAndWait();
            return;
        }
        if (rows.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No transactions to export")
                    .showAndWait();
            return;
        }
        // 2) build filename from filters
        String month = monthFilter.getValue();
        String year  = yearFilter.getValue();
        if (month == null || month.equals("All"))  month = "";
        if (year  == null || year .equals("All"))  year  = "";
        String namePart = Stream.of(month, year)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
        if (namePart.isBlank()) namePart = "All Transactions";
        String filename = "Transactions - " + namePart + ".csv";

        // 3) Ask user where to save
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Transactions CSV");
        chooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName(filename);
        File file = chooser.showSaveDialog(exportCsvBtn.getScene().getWindow());
        if (file == null) return;

        // 4) Write out
        try (PrintWriter out = new PrintWriter(file)) {
            out.println("Date,Description,Category,Amount");
            for (Transaction t : rows) {
                String desc = t.getDescription().replace("\"", "\"\"");
                out.printf("%s,\"%s\",%s,%.2f%n",
                        t.getDate(), desc, t.getCategory(), t.getAmount());
            }
            new Alert(Alert.AlertType.INFORMATION,
                    "Exported " + rows.size() + " transactions to:\n" + file)
                    .showAndWait();
        } catch (IOException io) {
            new Alert(Alert.AlertType.ERROR,
                    "Failed to write CSV:\n" + io.getMessage())
                    .showAndWait();
        }
    }
}
