package com.yourorg.finance.model;

public class Budget {
    private int id, userId;
    private String category;
    private double limit;

    public Budget(int id, int userId, String category, double limit) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.limit = limit;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public String getCategory() { return category; }
    public double getLimit() { return limit; }
    public void setLimit(double limit) {
        this.limit = limit;
    }
}
