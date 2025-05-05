package com.yourorg.finance.controller;

import com.yourorg.finance.dao.TransactionDao;
import com.yourorg.finance.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    // ——— Pie + Line charts —————————————————————
    @FXML private PieChart pieChart;
    @FXML private LineChart<Number, Number> lineChart;

    // ——— Summary cards ————————————————————————
    @FXML private Label totalBalanceLabel;
    @FXML private Label monthlyExpensesLabel;
    @FXML private Label goalsProgressLabel;

    // ——— Recent transactions table —————————————————
    @FXML private TableView<Transaction> recentTable;
    @FXML private TableColumn<Transaction, LocalDate> dtCol;
    @FXML private TableColumn<Transaction, String> descCol;
    @FXML private TableColumn<Transaction, String> catCol;
    @FXML private TableColumn<Transaction, Double> amtCol;

    private final TransactionDao txDao = new TransactionDao();
    private final int currentUserId = 1;  // TODO: wire up the real logged‑in user

    @FXML
    public void initialize() {
        // 1) Wire up the “recent” table columns
        dtCol  .setCellValueFactory(new PropertyValueFactory<>("date"));
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        catCol .setCellValueFactory(new PropertyValueFactory<>("category"));
        amtCol .setCellValueFactory(new PropertyValueFactory<>("amount"));

        // 2) Load & render everything
        refreshDashboard();
    }

    private void refreshDashboard() {
        try {
            List<Transaction> all = txDao.findByUser(currentUserId);

            // A) Total balance (sum of all amounts)
            double totalBalance = all.stream()
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            // B) This‑month expenses (negatives only, absolute value)
            double monthlyExpenses = all.stream()
                    .filter(t -> t.getDate().getMonth() == LocalDate.now().getMonth())
                    // treat positive amounts as expenses if category != Income
                    .mapToDouble(t -> {
                        double amt = t.getAmount();
                        return "Income".equalsIgnoreCase(t.getCategory()) ? 0
                                : Math.abs(amt);
                    })
                    .sum();
//            System.out.println("Monthly Expenses: "+ monthlyExpenses);
            // Update cards
            totalBalanceLabel   .setText(String.format("$%.2f", totalBalance));
            monthlyExpensesLabel.setText(String.format("$%.2f", monthlyExpenses));
            goalsProgressLabel  .setText("0%");  // placeholder until budgets are hooked up

            // C) Pie chart by category (abs values)
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            all.stream()
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.summingDouble(Transaction::getAmount)
                    ))
                    .forEach((category, sum) ->
                            pieData.add(new PieChart.Data(category, Math.abs(sum)))
                    );
            pieChart.setData(pieData);

            // D) Line chart: daily spending (negatives only)
            NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
            NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
            xAxis.setLabel("Day");
            yAxis.setLabel("Amount");

            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Spending");

            // D) Line chart: daily spending (negatives only)
            all.stream()
                    // only build the series from non‑income items
                    .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                    .sorted(Comparator.comparing(Transaction::getDate))
                    .forEach(t -> series.getData().add(new XYChart.Data<>(
                            t.getDate().getDayOfMonth(),
                            Math.abs(t.getAmount())
                    )));


            lineChart.getData().setAll(series);

            // E) Recent transactions: last 5, newest first
            List<Transaction> recent = all.stream()
                    .sorted(Comparator.comparing(Transaction::getDate).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

            // ─────────────────────────────────────────────────────
            // Apply custom cell factory BEFORE setting items:
            amtCol.setCellFactory(column -> new TableCell<Transaction, Double>() {
                @Override
                protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
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

            // Now populate the table with your recent list
            recentTable.setItems(FXCollections.observableArrayList(recent));

        } catch (SQLException ex) {
            ex.printStackTrace();
            // optionally show an Alert here
        }

    }
}
