package com.yourorg.finance.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class DashboardController {
    @FXML private PieChart categoryChart;
    @FXML private LineChart<Number,Number> trendChart;

    @FXML
    public void initialize() {
        // --- Populate PieChart ---
        var pieData = FXCollections.observableArrayList(
                new PieChart.Data("Food", 40),
                new PieChart.Data("Rent", 30),
                new PieChart.Data("Other", 30)
        );
        categoryChart.setData(pieData);

        // Add tooltips to pie slices
        for (PieChart.Data d : pieData) {
            Tooltip t = new Tooltip(String.format("%s: %.2f", d.getName(), d.getPieValue()));
            t.setShowDelay(Duration.millis(100));
            Tooltip.install(d.getNode(), t);
        }

        // --- Populate LineChart ---
        var series = new XYChart.Series<Number,Number>();
        series.setName("Spending");
        for (int day = 1; day <= 7; day++) {
            series.getData().add(new XYChart.Data<>(day, Math.random()*100));
        }
        trendChart.getData().add(series);

        // After the chart is rendered, attach tooltips to each data symbol
        series.getData().forEach(data -> {
            Tooltip t = new Tooltip(
                    String.format("Day %d: %.2f",
                            data.getXValue().intValue(),
                            data.getYValue().doubleValue())
            );
            t.setShowDelay(Duration.millis(100));
            // data.getNode() is the graphical symbol (circle) for each point
            Tooltip.install(data.getNode(), t);
        });
    }
}
