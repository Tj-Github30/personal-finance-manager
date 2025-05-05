package com.yourorg.finance.dao;

import com.yourorg.finance.model.Transaction;
import com.yourorg.finance.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDao {
    public List<Transaction> findByUser(int userId) throws SQLException {
        String sql = "SELECT id, user_id, date, description, category, amount " +
                "FROM transactions WHERE user_id = ? ORDER BY date DESC;";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Transaction> list = new ArrayList<>();
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setId(rs.getInt("id"));
                    t.setUserId(rs.getInt("user_id"));
                    t.setDate(LocalDate.parse(rs.getString("date")));
                    t.setDescription(rs.getString("description"));
                    t.setCategory(rs.getString("category"));
                    t.setAmount(rs.getDouble("amount"));
                    list.add(t);
                }
                return list;
            }
        }
    }
    /** Inserts a new transaction into the DB and sets its generated ID */
//    public Transaction create(Transaction tx) throws SQLException {
//        String sql = """
//      INSERT INTO transactions(user_id, date, description, category, amount)
//      VALUES (?, ?, ?, ?, ?);
//      """;
//
//        // 1) Flip sign for expenses
//        double amt = tx.getAmount();
//            // if it’s not Income, force it negative:
//        if (!"Income".equalsIgnoreCase(tx.getCategory())) {
//            amt = -Math.abs(amt);
//        }
//
//        try (Connection conn = ConnectionManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            ps.setInt(1, tx.getUserId());
//            ps.setString(2, tx.getDate().toString());
//            ps.setString(3, tx.getDescription());
//            ps.setString(4, tx.getCategory());
//            ps.setDouble(5, amt);
//            ps.executeUpdate();
//
//            try (ResultSet keys = ps.getGeneratedKeys()) {
//                if (keys.next()) {
//                    tx.setId(keys.getInt(1));
//                }
//            }
//
//            // Also update the in‑memory object so it reflects the stored sign
//            tx.setAmount(amt);
//            return tx;
//        }
//    }
    public Transaction create(Transaction tx) throws SQLException {
        String sql = """
      INSERT INTO transactions(user_id, date, description, category, amount)
      VALUES (?, ?, ?, ?, ?);
      """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tx.getUserId());
            ps.setString(2, tx.getDate().toString());
            ps.setString(3, tx.getDescription());
            ps.setString(4, tx.getCategory());

            // ▷ ensure expense categories are negative, income stays positive
            double raw = tx.getAmount();
            double signed = "Income".equalsIgnoreCase(tx.getCategory())
                    ? Math.abs(raw)
                    : -Math.abs(raw);
            ps.setDouble(5, signed);

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) tx.setId(keys.getInt(1));
            }
            return tx;
        }
    }


//    /** Updates an existing transaction record */
//    public boolean update(Transaction tx) throws SQLException {
//        String sql = """
//      UPDATE transactions
//         SET date = ?, description = ?, category = ?, amount = ?
//       WHERE id = ?;
//      """;
//
//        // 1) Flip sign for expenses
//        double amt = tx.getAmount();
//        if (!"Income".equalsIgnoreCase(tx.getCategory())) {
//            amt = -Math.abs(amt);
//        }
//
//        try (Connection conn = ConnectionManager.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//            ps.setString(1, tx.getDate().toString());
//            ps.setString(2, tx.getDescription());
//            ps.setString(3, tx.getCategory());
//            ps.setDouble(4, amt);
//            ps.setInt(5, tx.getId());
//            boolean ok = ps.executeUpdate() == 1;
//
//            // Keep your in‑memory object in sync
//            if (ok) {
//                tx.setAmount(amt);
//            }
//            return ok;
//        }
//    }
    /** Updates an existing transaction record */
    public boolean update(Transaction tx) throws SQLException {
        String sql = """
      UPDATE transactions
         SET date = ?, description = ?, category = ?, amount = ?
       WHERE id = ?;
      """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tx.getDate().toString());
            ps.setString(2, tx.getDescription());
            ps.setString(3, tx.getCategory());

            // ▷ same sign‐coercion logic here
            double raw = tx.getAmount();
            double signed = "Income".equalsIgnoreCase(tx.getCategory())
                    ? Math.abs(raw)
                    : -Math.abs(raw);
            ps.setDouble(4, signed);

            ps.setInt(5, tx.getId());
            return ps.executeUpdate() == 1;
        }
    }


    /** Deletes a transaction by its ID */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?;";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

}
