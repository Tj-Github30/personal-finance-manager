package com.yourorg.finance.dao;

import com.yourorg.finance.model.User;
import com.yourorg.finance.util.ConnectionManager;

import java.sql.*;

public class UserDao {
    public void create(User user) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, role) VALUES(?,?,?);";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?;";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setUsername(rs.getString("username"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setRole(rs.getString("role"));
                    return u;
                } else {
                    return null;
                }
            }
        }
    }
}
