package com.yourorg.finance.controller;

import com.yourorg.finance.dao.TransactionDao;
import com.yourorg.finance.model.Transaction;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.util.Duration;
import javafx.scene.control.Tooltip;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DashboardController {
    @FXML private PieChart categoryChart;
    @FXML private LineChart<Number,Number> trendChart;
    @FXML private Label totalBalanceLabel;
    @FXML private Label monthlyExpensesLabel;
    @FXML private Label goalsProgressLabel;

    private final TransactionDao dao = new TransactionDao();
    private final int currentUserId = 1; // TODO: wire in real logged-in user

    @FXML
    public void initialize() {
        List<Transaction> allTx;
        try {
            allTx = dao.findByUser(currentUserId);
        } catch (SQLException e) {
            e.printStackTrace();
            // you could show an Alert here
            return;
        }

        // 1) Compute totals
        double totalIncome = allTx.stream()
                .filter(t -> "Income".equalsIgnoreCase(t.getCategory()))
                .mapToDouble(Transaction::getAmount).sum();
        double totalExpenses = allTx.stream()
                .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                .mapToDouble(Transaction::getAmount).sum();
        double balance = totalIncome - totalExpenses;

        YearMonth now = YearMonth.now();
        List<Transaction> thisMonth = allTx.stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(now))
                .collect(Collectors.toList());
        double monthExp = thisMonth.stream()
                .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                .mapToDouble(Transaction::getAmount).sum();

        // 2) Update labels
        totalBalanceLabel.setText(String.format("$%.2f", balance));
        monthlyExpensesLabel.setText(String.format("$%.2f", monthExp));
        goalsProgressLabel.setText("0%"); // hook in budgets later

        // 3) Pie chart (this month’s categories)
        Map<String, Double> byCat = thisMonth.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        var pieData = byCat.entrySet().stream()
                .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                .toList();

        categoryChart.setData(FXCollections.observableArrayList(pieData));
        pieData.forEach(d -> {
            Tooltip t = new Tooltip(
                    String.format("%s: %.2f", d.getName(), d.getPieValue())
            );
            t.setShowDelay(Duration.millis(50));
            Tooltip.install(d.getNode(), t);
        });

        // 4) Line chart (daily spend this month)
        Map<Integer, Double> byDay = thisMonth.stream()
                .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                .collect(Collectors.groupingBy(
                        tx -> tx.getDate().getDayOfMonth(),
                        TreeMap::new,
                        Collectors.summingDouble(Transaction::getAmount)
                ));

        XYChart.Series<Number,Number> series = new XYChart.Series<>();
        series.setName("Spending");
        byDay.forEach((day, amt) ->
                series.getData().add(new XYChart.Data<>(day, amt))
        );
        trendChart.getData().setAll(series);
        series.getData().forEach(data -> {
            Tooltip t = new Tooltip(
                    String.format("Day %d: %.2f",
                            data.getXValue().intValue(),
                            data.getYValue().doubleValue())
            );
            t.setShowDelay(Duration.millis(50));
            Tooltip.install(data.getNode(), t);
        });
    }
}
