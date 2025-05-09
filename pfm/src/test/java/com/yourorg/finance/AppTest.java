package com.yourorg.finance;

import com.yourorg.finance.model.Transaction;
import com.yourorg.finance.service.TransactionService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

public class AppTest {

    @Test
    void testAddTransaction() {
        TransactionService service = new TransactionService();
        Transaction t = new Transaction(1, 101, LocalDate.now(), "Groceries", "Food", 120.0);

        service.addTransaction(t);
        List<Transaction> transactions = service.getAllTransactions();

        assertEquals(1, transactions.size());
        assertEquals("Groceries", transactions.get(0).getDescription());
        assertEquals(120.0, transactions.get(0).getAmount(), 0.001);
        assertEquals("Food", transactions.get(0).getCategory());
    }

    @Test
    void testGetTotalForCategory() {
        TransactionService service = new TransactionService();

        service.addTransaction(new Transaction(1, 101, LocalDate.now(), "Rent", "Housing", 1000.0));
        service.addTransaction(new Transaction(2, 101, LocalDate.now(), "Water", "Housing", 100.0));
        service.addTransaction(new Transaction(3, 101, LocalDate.now(), "Movie", "Entertainment", 20.0));

        double housingTotal = service.getTotalForCategory("Housing");
        double foodTotal = service.getTotalForCategory("Food");

        assertEquals(1100.0, housingTotal, 0.001);
        assertEquals(0.0, foodTotal, 0.001);
    }

    @Test
    void testTransactionWithZeroAmount() {
        TransactionService service = new TransactionService();
        Transaction t = new Transaction(4, 101, LocalDate.now(), "Freebie", "Other", 0.0);

        service.addTransaction(t);
        List<Transaction> transactions = service.getAllTransactions();

        assertEquals(1, transactions.size());
        assertEquals(0.0, transactions.get(0).getAmount(), 0.001);
    }

    @Test
    void testTransactionsAcrossUsers() {
        TransactionService service = new TransactionService();

        service.addTransaction(new Transaction(5, 101, LocalDate.now(), "Groceries", "Food", 100.0));
        service.addTransaction(new Transaction(6, 202, LocalDate.now(), "Flight", "Travel", 500.0));

        List<Transaction> transactions = service.getAllTransactions();

        assertEquals(2, transactions.size());
        assertEquals(101, transactions.get(0).getUserId());
        assertEquals(202, transactions.get(1).getUserId());
    }

    @Test
    void testLargeAmountTransaction() {
        TransactionService service = new TransactionService();
        double largeAmount = 1_000_000.00;

        service.addTransaction(new Transaction(7, 303, LocalDate.now(), "House Sale", "Income", largeAmount));

        double incomeTotal = service.getTotalForCategory("Income");

        assertEquals(largeAmount, incomeTotal, 0.001);
    }

    @Test
    void testGetTransactionsByUserId() {
        TransactionService service = new TransactionService();

        service.addTransaction(new Transaction(8, 999, LocalDate.now(), "Groceries", "Food", 50.0));
        service.addTransaction(new Transaction(9, 888, LocalDate.now(), "Travel", "Transport", 75.0));

        List<Transaction> userTransactions = service.getTransactionsByUserId(999);
        assertEquals(1, userTransactions.size());
        assertEquals("Groceries", userTransactions.get(0).getDescription());
    }

    @Test
    void testGetTransactionsByDateRange() {
        TransactionService service = new TransactionService();
        LocalDate now = LocalDate.now();

        service.addTransaction(new Transaction(10, 101, now.minusDays(5), "Old", "Misc", 10.0));
        service.addTransaction(new Transaction(11, 101, now, "Today", "Misc", 20.0));
        service.addTransaction(new Transaction(12, 101, now.plusDays(5), "Future", "Misc", 30.0));

        List<Transaction> inRange = service.getTransactionsByDateRange(now.minusDays(1), now.plusDays(1));
        assertEquals(1, inRange.size());
        assertEquals("Today", inRange.get(0).getDescription());
    }

    @Test
    void testGetTransactionsSortedByAmountDesc() {
        TransactionService service = new TransactionService();

        service.addTransaction(new Transaction(13, 101, LocalDate.now(), "Small", "Misc", 10.0));
        service.addTransaction(new Transaction(14, 101, LocalDate.now(), "Large", "Misc", 100.0));
        service.addTransaction(new Transaction(15, 101, LocalDate.now(), "Medium", "Misc", 50.0));

        List<Transaction> sorted = service.getTransactionsSortedByAmountDesc();
        assertEquals("Large", sorted.get(0).getDescription());
        assertEquals("Medium", sorted.get(1).getDescription());
        assertEquals("Small", sorted.get(2).getDescription());
    }

    @Test
    void testSearchByDescription() {
        TransactionService service = new TransactionService();

        service.addTransaction(new Transaction(16, 101, LocalDate.now(), "Spotify Subscription", "Entertainment", 9.99));
        service.addTransaction(new Transaction(17, 101, LocalDate.now(), "Netflix Subscription", "Entertainment", 12.99));

        List<Transaction> results = service.searchByDescription("netflix");
        assertEquals(1, results.size());
        assertEquals("Netflix Subscription", results.get(0).getDescription());
    }

    @Test
    void testUpdateTransactionById() {
        TransactionService service = new TransactionService();
        Transaction original = new Transaction(18, 101, LocalDate.now(), "Old", "Misc", 10.0);
        service.addTransaction(original);

        Transaction updated = new Transaction(18, 101, LocalDate.now(), "Updated", "Misc", 99.0);
        boolean success = service.updateTransaction(18, updated);

        assertTrue(success);
        assertEquals("Updated", service.getAllTransactions().get(0).getDescription());
    }

    @Test
    void testDeleteTransactionById() {
        TransactionService service = new TransactionService();
        service.addTransaction(new Transaction(19, 101, LocalDate.now(), "To Delete", "Misc", 20.0));

        boolean deleted = service.deleteTransaction(19);

        assertTrue(deleted);
        assertEquals(0, service.getAllTransactions().size());
    }
}
