package com.yourorg.finance.model;

import java.time.LocalDate;

public class Transaction {
    private int id;
    private int userId;
    private LocalDate date;
    private String description;
    private String category;
    private double amount;

    public Transaction() {}

    public Transaction(int id, int userId, LocalDate date, String description,
                       String category, double amount) {
        this.id = id; this.userId = userId; this.date = date;
        this.description = description; this.category = category; this.amount = amount;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
