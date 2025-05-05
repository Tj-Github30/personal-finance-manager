package com.yourorg.finance.controller;

import com.yourorg.finance.dao.BudgetDao;
import com.yourorg.finance.dao.TransactionDao;
import com.yourorg.finance.model.Budget;
import com.yourorg.finance.model.Transaction;
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

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    // ——— Pie + Line charts —————————————————————
    @FXML
    private PieChart pieChart;
    @FXML
    private LineChart<Number, Number> lineChart;

    // ——— Summary cards ————————————————————————
    @FXML
    private Label totalBalanceLabel;
    @FXML
    private Label monthlyExpensesLabel;
    @FXML
    private Label goalsProgressLabel;

    // ——— Recent transactions table —————————————————
    @FXML
    private TableView<Transaction> recentTable;
    @FXML
    private TableColumn<Transaction, LocalDate> dtCol;
    @FXML
    private TableColumn<Transaction, String> descCol;
    @FXML
    private TableColumn<Transaction, String> catCol;
    @FXML
    private TableColumn<Transaction, Double> amtCol;
    @FXML private VBox budgetsBox;

//    @FXML private Label goalsProgressLabel;


    private final TransactionDao txDao = new TransactionDao();
    private final int currentUserId = 1;

    @FXML
    public void initialize() {
        // 1) Wire up the “recent” table columns
        dtCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        amtCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        // re‑apply your color cell‑factory:
        amtCol.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount==null) {
                    setText(null);
                    setStyle("");
                } else {
                    Transaction tx = getTableView().getItems().get(getIndex());
                    boolean isIncome = "Income".equalsIgnoreCase(tx.getCategory());
                    if (isIncome) {
                        setText(String.format("$%.2f", amount));
                        setStyle("-fx-text-fill: green;");
                    } else {
                        setText(String.format("-$%.2f", Math.abs(amount)));
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        // 2) Subscribe to “transaction/budget changed” events

        EventBus.get().subscribe(topic -> {
            if ("transactions:changed".equals(topic) ||
                    "budgets:changed".equals(topic)) {
                Platform.runLater(this::refreshDashboard);
            }
        });

        //3) Initial render
        refreshDashboard();

    }
    private void refreshDashboard() {
        try {
            // ─── A) Load all transactions ─────────────────────────────────
            List<Transaction> all = txDao.findByUser(currentUserId);

            // A1) Total balance
            double totalBalance = all.stream()
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            // A2) Monthly expenses (non‑income only)
            double monthlyExpenses = all.stream()
                    .filter(t -> t.getDate().getMonth() == LocalDate.now().getMonth())
                    .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                    .mapToDouble(t -> Math.abs(t.getAmount()))
                    .sum();

            // Update summary cards
            totalBalanceLabel.setText(String.format("$%.2f", totalBalance));
            monthlyExpensesLabel.setText(String.format("$%.2f", monthlyExpenses));

            // ─── B) Pie chart ───────────────────────────────────────────────
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            all.stream()
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.summingDouble(Transaction::getAmount)
                    ))
                    .forEach((cat, sum) ->
                            pieData.add(new PieChart.Data(cat, Math.abs(sum)))
                    );
            pieChart.setData(pieData);

            // ─── C) Line chart ──────────────────────────────────────────────
            NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
            NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
            xAxis.setLabel("Day");
            yAxis.setLabel("Amount");

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Spending");
            all.stream()
                    .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                    .sorted(Comparator.comparing(Transaction::getDate))
                    .forEach(t ->
                            series.getData().add(new XYChart.Data<>(
                                    t.getDate().getDayOfMonth(),
                                    Math.abs(t.getAmount())
                            ))
                    );
            lineChart.getData().setAll(series);

            // ─── D) Recent transactions ─────────────────────────────────────
            List<Transaction> recent = all.stream()
                    .sorted(Comparator.comparing(Transaction::getDate).reversed())
                    .limit(5)
                    .collect(Collectors.toList());
            recentTable.setItems(FXCollections.observableArrayList(recent));

            // ─── E) Goals progress ──────────────────────────────────────────
            // 1) total budget allocated
            List<Budget> budgets = new BudgetDao().findByUser(currentUserId);
            double totalBudgeted = budgets.stream()
                    .mapToDouble(Budget::getLimit)
                    .sum();

            // 2) total spent this month
            double totalSpent = all.stream()
                    .filter(t -> t.getDate().getMonth() == LocalDate.now().getMonth())
                    .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                    .mapToDouble(t -> Math.abs(t.getAmount()))
                    .sum();

            // 3) compute percent (safe against divide‐by‐zero)
            String progressText;
            if (totalBudgeted <= 0) {
                progressText = "0%";
            } else {
                int pct = (int) Math.round((totalSpent / totalBudgeted) * 100);
                progressText = pct + "%";
            }
            goalsProgressLabel.setText(progressText);
            // ─── F) Per‐budget progress bars ─────────────────────────────────

            budgetsBox.getChildren().clear();
            for (Budget b : budgets) {
                double limit = b.getLimit();
                double spent = all.stream()
                        .filter(t -> t.getDate().getMonth() == LocalDate.now().getMonth())
                        .filter(t -> t.getCategory().equalsIgnoreCase(b.getCategory()))
                        .mapToDouble(t -> Math.abs(t.getAmount()))
                        .sum();

                double ratio = limit > 0 ? Math.min(1.0, spent / limit) : 0;

                Label catLabel = new Label(b.getCategory());
                ProgressBar bar   = new ProgressBar(ratio);
                bar.setPrefWidth(150);
                Label pctLabel   = new Label(String.format("%d%%", (int)(ratio * 100)));

                HBox row = new HBox(8, catLabel, bar, pctLabel);
                budgetsBox.getChildren().add(row);
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            // optionally: show an Alert here
        }
//        // ─── F) Per‐budget progress bars ─────────────────────────────────
//        List<Budget> budgets;
//        try {
//            budgets = new BudgetDao().findByUser(currentUserId);
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//            budgets = List.of();  // fallback to empty
//        }
//
//        budgetsBox.getChildren().clear();
//        for (Budget b : budgets) {
//            double limit = b.getLimit();
//            double spent = all.stream()
//                    .filter(t -> t.getDate().getMonth() == LocalDate.now().getMonth())
//                    .filter(t -> t.getCategory().equalsIgnoreCase(b.getCategory()))
//                    .mapToDouble(t -> Math.abs(t.getAmount()))
//                    .sum();
//
//            double ratio = limit > 0 ? Math.min(1.0, spent / limit) : 0;
//
//            Label catLabel = new Label(b.getCategory());
//            ProgressBar bar   = new ProgressBar(ratio);
//            bar.setPrefWidth(150);
//            Label pctLabel   = new Label(String.format("%d%%", (int)(ratio * 100)));
//
//            HBox row = new HBox(8, catLabel, bar, pctLabel);
//            budgetsBox.getChildren().add(row);
//        }
    }

}