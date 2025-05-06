package com.yourorg.finance.controller;

import com.yourorg.finance.dao.BudgetDao;
import com.yourorg.finance.dao.TransactionDao;
import com.yourorg.finance.model.Budget;
import com.yourorg.finance.model.Transaction;
import com.yourorg.finance.model.User;
import com.yourorg.finance.service.AuthService;
import com.yourorg.finance.util.EventBus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DashboardController {

    @FXML private PieChart pieChart;
    @FXML private LineChart<Number, Number> lineChart;

    @FXML private Label totalBalanceLabel;
    @FXML private Label monthlyExpensesLabel;
    @FXML private Label goalsProgressLabel;

    @FXML private TableView<Transaction> recentTable;
    @FXML private TableColumn<Transaction, LocalDate> dtCol;
    @FXML private TableColumn<Transaction, String> descCol;
    @FXML private TableColumn<Transaction, String> catCol;
    @FXML private TableColumn<Transaction, Double> amtCol;

    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private VBox budgetsBox;
    @FXML private Button exportCsvBtn;
    @FXML private TableView<Transaction> table;

    private final TransactionDao txDao = new TransactionDao();
    private int currentUserId() {
        User u = AuthService.getInstance().getCurrentUser();
        return u != null ? u.getId() : -1;
    }


    @FXML
    public void initialize() {
        // 1) columns
        dtCol .setCellValueFactory(new PropertyValueFactory<>("date"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        catCol .setCellValueFactory(new PropertyValueFactory<>("category"));
        amtCol .setCellValueFactory(new PropertyValueFactory<>("amount"));
        amtCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amt, boolean empty) {
                super.updateItem(amt, empty);
                if (empty || amt == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction tx = getTableView().getItems().get(getIndex());
                    boolean income = "Income".equalsIgnoreCase(tx.getCategory());
                    if (income) {
                        setText(String.format("$%.2f", amt));
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setText(String.format("-$%.2f", Math.abs(amt)));
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        // ─── 2) Strip blank columns & proportional resize ──────────────────
        recentTable.getColumns().removeIf(c -> c.getText()==null || c.getText().isBlank());
        recentTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dtCol .prefWidthProperty().bind(recentTable.widthProperty().multiply(0.10));
        descCol.prefWidthProperty().bind(recentTable.widthProperty().multiply(0.40));
        catCol .prefWidthProperty().bind(recentTable.widthProperty().multiply(0.25));
        amtCol .prefWidthProperty().bind(recentTable.widthProperty().multiply(0.25));

        // 3) filters
        monthFilter.getItems().setAll(
                "All","Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
        );
        yearFilter.getItems().setAll("All","2023","2024","2025");
        monthFilter.getSelectionModel().select("All");
        yearFilter .getSelectionModel().select("All");
        monthFilter.setOnAction(e -> refreshDashboard());
        yearFilter .setOnAction(e -> refreshDashboard());

        // 4) listen for changes
        EventBus.get().subscribe(topic -> {
            if (topic.equals("transactions:changed") ||
                    topic.equals("budgets:changed")) {
                Platform.runLater(this::refreshDashboard);
            }
        });

        // 5) first load
        refreshDashboard();
        exportCsvBtn.setOnAction(e -> exportCsv());
    }

    private void refreshDashboard() {
        try {
            // fetch all
            List<Transaction> all = txDao.findByUser(currentUserId());

            // apply month/year filter
            String m = monthFilter.getValue();
            String y = yearFilter.getValue();
            Stream<Transaction> stream = all.stream();

            if (!"All".equals(m)) {
                int mi = List.of(
                        "Jan","Feb","Mar","Apr","May","Jun",
                        "Jul","Aug","Sep","Oct","Nov","Dec"
                ).indexOf(m) + 1;
                stream = stream.filter(tx -> tx.getDate().getMonthValue() == mi);
            }
            if (!"All".equals(y)) {
                int yi = Integer.parseInt(y);
                stream = stream.filter(tx -> tx.getDate().getYear() == yi);
            }
            List<Transaction> filtered = stream.collect(Collectors.toList());

            // ─ A) Cards ───────────────────────────────
            double balance = filtered.stream()
                    .mapToDouble(Transaction::getAmount).sum();
            double expenses = filtered.stream()
                    .filter(tx -> !"Income".equalsIgnoreCase(tx.getCategory()))
                    .mapToDouble(tx -> Math.abs(tx.getAmount())).sum();
            totalBalanceLabel.setText(String.format("$%.2f", balance));
            monthlyExpensesLabel.setText(String.format("$%.2f", expenses));

            // ─ B) Pie ─────────────────────────────────
            var pieData = FXCollections.<PieChart.Data>observableArrayList();
            filtered.stream()
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.summingDouble(Transaction::getAmount)
                    ))
                    .forEach((cat, sum) ->
                            pieData.add(new PieChart.Data(cat, Math.abs(sum)))
                    );
            pieChart.setData(pieData);

            // ─ C) Line ────────────────────────────────
            NumberAxis x = (NumberAxis)lineChart.getXAxis();
            NumberAxis yAxis = (NumberAxis)lineChart.getYAxis();
            x.setLabel("Day");  yAxis.setLabel("Amount");
            XYChart.Series<Number,Number> series = new XYChart.Series<>();
            series.setName("Spending");
            filtered.stream()
                    .filter(tx -> !"Income".equalsIgnoreCase(tx.getCategory()))
                    .sorted(Comparator.comparing(Transaction::getDate))
                    .forEach(tx -> series.getData().add(
                            new XYChart.Data<>(
                                    tx.getDate().getDayOfMonth(),
                                    Math.abs(tx.getAmount())
                            )
                    ));
            lineChart.getData().setAll(series);

            // ─ D) Recent ──────────────────────────────
            var recent = filtered.stream()
                    .sorted(Comparator.comparing(Transaction::getDate).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
            recentTable.setItems(FXCollections.observableArrayList(recent));

            // ─ E) Goals % ─────────────────────────────
            List<Budget> budgets = new BudgetDao().findByUser(currentUserId());
            double totalBudget = budgets.stream()
                    .mapToDouble(Budget::getLimit).sum();
            double spent        = filtered.stream()
                    .filter(tx -> !"Income".equalsIgnoreCase(tx.getCategory()))
                    .mapToDouble(tx -> Math.abs(tx.getAmount())).sum();
            String pct = totalBudget <= 0
                    ? "0%"
                    : Math.round((spent/totalBudget)*100) + "%";
            goalsProgressLabel.setText(pct);

            // ─ F) Per‑budget bars ──────────────────────
            budgetsBox.getChildren().clear();
            for (Budget b : budgets) {
                double lim   = b.getLimit();
                double used  = filtered.stream()
                        .filter(tx ->
                                tx.getCategory().equalsIgnoreCase(b.getCategory()))
                        .mapToDouble(tx -> Math.abs(tx.getAmount()))
                        .sum();
                double ratio = lim>0 ? Math.min(1.0, used/lim) : 0;

                Label name = new Label(b.getCategory());
                ProgressBar bar = new ProgressBar(ratio);
                bar.setPrefWidth(150);
                Label label = new Label((int)(ratio*100)+"%");

                budgetsBox.getChildren().add(new HBox(8, name, bar, label));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            // optionally show an alert
        }
    }
    private void exportCsv() {
        // 1) Fetch whichever set of transactions you want:
        List<Transaction> rows;
        try {
            rows = txDao.findByUser(currentUserId());
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
        String filename = "Dashboard - " + namePart + ".csv";

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
