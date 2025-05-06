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

    private final TransactionDao txDao = new TransactionDao();
    private final int currentUserId = 1;

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

        // 2) filters
        monthFilter.getItems().setAll(
                "All","Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
        );
        yearFilter.getItems().setAll("All","2023","2024","2025");
        monthFilter.getSelectionModel().select("All");
        yearFilter .getSelectionModel().select("All");
        monthFilter.setOnAction(e -> refreshDashboard());
        yearFilter .setOnAction(e -> refreshDashboard());

        // 3) listen for changes
        EventBus.get().subscribe(topic -> {
            if (topic.equals("transactions:changed") ||
                    topic.equals("budgets:changed")) {
                Platform.runLater(this::refreshDashboard);
            }
        });

        // 4) first load
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            // fetch all
            List<Transaction> all = txDao.findByUser(currentUserId);

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
            List<Budget> budgets = new BudgetDao().findByUser(currentUserId);
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
}
