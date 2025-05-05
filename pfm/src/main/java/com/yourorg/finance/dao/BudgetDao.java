package com.yourorg.finance.dao;

import com.yourorg.finance.model.Budget;
import com.yourorg.finance.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDao {

    public Budget create(Budget b) throws SQLException {
        String sql = "INSERT INTO budgets(user_id, category, limit_amount) VALUES (?, ?, ?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, b.getUserId());
            ps.setString(2, b.getCategory());
            ps.setDouble(3, b.getLimit());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    b.setId(rs.getInt(1));
                }
            }
            return b;
        }
    }

    public boolean update(Budget b) throws SQLException {
        String sql = """
            UPDATE budgets
               SET category     = ?,
                   limit_amount = ?
             WHERE id = ?
            """;
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, b.getCategory());
            ps.setDouble(2, b.getLimit());
            ps.setInt(3, b.getId());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Budget> findByUser(int userId) throws SQLException {
        List<Budget> list = new ArrayList<>();
        String sql = """
            SELECT id,
                   category,
                   limit_amount
              FROM budgets
             WHERE user_id = ?
            """;
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Budget(
                            rs.getInt("id"),
                            userId,
                            rs.getString("category"),
                            rs.getDouble("limit_amount")
                    ));
                }
            }
        }
        return list;
    }
}
