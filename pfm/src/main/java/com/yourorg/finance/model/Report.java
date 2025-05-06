package com.yourorg.finance.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Report{
    private final SimpleStringProperty category;
    private final SimpleDoubleProperty budgeted;
    private final SimpleDoubleProperty spent;
    private final SimpleDoubleProperty variance;

    public Report(String category, double budgeted, double spent, double variance) {
        this.category  = new SimpleStringProperty(category);
        this.budgeted  = new SimpleDoubleProperty(budgeted);
        this.spent     = new SimpleDoubleProperty(spent);
        this.variance  = new SimpleDoubleProperty(variance);
    }

    public String getCategory()     { return category.get(); }
    public double getBudgeted()     { return budgeted.get(); }
    public double getSpent()        { return spent.get(); }
    public double getVariance()     { return variance.get(); }

    public SimpleStringProperty categoryProperty() { return category; }
    public SimpleDoubleProperty budgetedProperty() { return budgeted; }
    public SimpleDoubleProperty spentProperty()    { return spent; }
    public SimpleDoubleProperty varianceProperty() { return variance; }
}
