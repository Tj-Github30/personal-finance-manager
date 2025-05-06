package com.yourorg.finance.controller;

import com.yourorg.finance.dao.BudgetDao;
import com.yourorg.finance.dao.TransactionDao;
import com.yourorg.finance.model.Budget;
import com.yourorg.finance.model.Report;
import com.yourorg.finance.model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsController {
    @FXML private ComboBox<String> monthFilter;
    @FXML private ComboBox<String> yearFilter;
    @FXML private Button       generateBtn;

    @FXML private TableView<Report> reportTable;
    @FXML private TableColumn<Report,String> categoryCol;
    @FXML private TableColumn<Report,Double> budgetCol;
    @FXML private TableColumn<Report,Double> spentCol;
    @FXML private TableColumn<Report,Double> varianceCol;

    @FXML private BarChart<String,Number> varianceChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis   yAxis;

    private final TransactionDao txDao   = new TransactionDao();
    private final BudgetDao      budgetDao = new BudgetDao();
    private final int userId = 1; // TODO: wire real user

    @FXML
    public void initialize() {
        // populate month/year combos (reuse your existing lists)
        monthFilter.getItems().addAll(
                "All","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
        );
        yearFilter.getItems().addAll("All","2023","2024","2025");
        monthFilter.getSelectionModel().select("All");
        yearFilter.getSelectionModel().select("All");

        // wire up table columns
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        budgetCol  .setCellValueFactory(new PropertyValueFactory<>("budgeted"));
        spentCol   .setCellValueFactory(new PropertyValueFactory<>("spent"));
        varianceCol.setCellValueFactory(new PropertyValueFactory<>("variance"));
        // 2) Strip any stray blank columns
        reportTable.getColumns().removeIf(c -> c.getText()==null || c.getText().isBlank());

        // 3) Proportional resize policy
        reportTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        categoryCol.prefWidthProperty().bind(reportTable.widthProperty().multiply(0.10)); // 10%
        budgetCol .prefWidthProperty().bind(reportTable.widthProperty().multiply(0.40)); // 40%
        spentCol  .prefWidthProperty().bind(reportTable.widthProperty().multiply(0.25)); // 25%
        varianceCol  .prefWidthProperty().bind(reportTable.widthProperty().multiply(0.25)); // 25%

        // axis labels
        xAxis.setLabel("Category");
        yAxis.setLabel("Variance");



        generateBtn.setOnAction(e -> onGenerate());
    }

    private void onGenerate() {
        try {
            // 1) filter transactions by selected month/year
            List<Transaction> allTx = txDao.findByUser(userId);
            YearMonth filterYm = parseYearMonth(monthFilter.getValue(), yearFilter.getValue());

            List<Transaction> txs = allTx.stream()
                    .filter(t -> {
                        if (filterYm == null) return true;
                        YearMonth ym = YearMonth.from(t.getDate());
                        return ym.equals(filterYm);
                    })
                    .collect(Collectors.toList());

            // 2) load budgets
            List<Budget> budgets = budgetDao.findByUser(userId);

            // 3) group spending by category
            Map<String,Double> spentByCat = txs.stream()
                    .filter(t -> !"Income".equalsIgnoreCase(t.getCategory()))
                    .collect(Collectors.groupingBy(
                            Transaction::getCategory,
                            Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                    ));

            // 4) build report rows
            ObservableList<Report> rows = FXCollections.observableArrayList();
            for (Budget b : budgets) {
                double spent    = spentByCat.getOrDefault(b.getCategory(), 0.0);
                double variance = b.getLimit() - spent;
                rows.add(new Report(b.getCategory(), b.getLimit(), spent, variance));
            }
            reportTable.setItems(rows);

            // 5) bar chart: variance per category
            XYChart.Series<String,Number> series = new XYChart.Series<>();
            series.setName("Budget Variance");
            for (Report r : rows) {
                series.getData().add(new XYChart.Data<>(r.getCategory(), r.getVariance()));
            }
            varianceChart.getData().setAll(series);

        } catch (SQLException ex) {
            ex.printStackTrace();
            // TODO: show an Alert
        }
    }

    private YearMonth parseYearMonth(String m, String y) {
        if ("All".equals(m) || "All".equals(y)) return null;
        int mm = List.of(
                "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
        ).indexOf(m) + 1;
        int yy = Integer.parseInt(y);
        return YearMonth.of(yy, mm);
    }
}
