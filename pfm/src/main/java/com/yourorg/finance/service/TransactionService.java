package com.yourorg.finance.service;

import com.yourorg.finance.model.Transaction;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TransactionService {

    private final List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public List<Transaction> getAllTransactions() {
        return transactions;
    }

    public double getTotalForCategory(String category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // 1. Filter by user ID
    public List<Transaction> getTransactionsByUserId(int userId) {
        return transactions.stream()
                .filter(t -> t.getUserId() == userId)
                .collect(Collectors.toList());
    }

    // 2. Filter by date range
    public List<Transaction> getTransactionsByDateRange(LocalDate start, LocalDate end) {
        return transactions.stream()
                .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    // 3. Sort by amount (highest to lowest)
    public List<Transaction> getTransactionsSortedByAmountDesc() {
        return transactions.stream()
                .sorted(Comparator.comparingDouble(Transaction::getAmount).reversed())
                .collect(Collectors.toList());
    }

    // 4. Search by keyword in description
    public List<Transaction> searchByDescription(String keyword) {
        return transactions.stream()
                .filter(t -> t.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    // 5. Update transaction by ID
    public boolean updateTransaction(int id, Transaction updated) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId() == id) {
                transactions.set(i, updated);
                return true;
            }
        }
        return false;
    }

    // 6. Delete transaction by ID
    public boolean deleteTransaction(int id) {
        return transactions.removeIf(t -> t.getId() == id);
    }
}
