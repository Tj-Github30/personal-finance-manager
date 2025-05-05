// src/main/java/com/yourorg/finance/dao/CategoryDao.java
package com.yourorg.finance.dao;

import com.yourorg.finance.model.Category;
import com.yourorg.finance.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    public List<Category> findAll(int userId) throws SQLException {
        String sql = "SELECT id, user_id, name FROM categories WHERE user_id = ?;";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Category> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Category(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("name")
                    ));
                }
                return list;
            }
        }
    }

    public Category create(int userId, String name) throws SQLException {
        String sql = "INSERT INTO categories (user_id,name) VALUES (?,?);";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, name);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Category(keys.getInt(1), userId, name);
                } else {
                    throw new SQLException("No ID returned for new category");
                }
            }
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?;";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
