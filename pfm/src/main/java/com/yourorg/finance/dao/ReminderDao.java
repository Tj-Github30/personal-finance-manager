package com.yourorg.finance.dao;

import com.yourorg.finance.model.Reminder;
import com.yourorg.finance.model.Reminder.Interval;
import com.yourorg.finance.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderDao {

    /** Creates the reminders table if it doesn’t already exist. */
//    public void initSchema() throws SQLException {
//        String sql = """
//            CREATE TABLE IF NOT EXISTS reminders (
//              id                   INTEGER PRIMARY KEY AUTOINCREMENT,
//              user_id              INTEGER NOT NULL,
//              message              TEXT    NOT NULL,
//              trigger_at           TEXT    NOT NULL,
//              recurring            INTEGER NOT NULL,
//              interval             TEXT    NOT NULL,
//              repeat_interval_ms   INTEGER NOT NULL,
//              FOREIGN KEY(user_id) REFERENCES users(id)
//            );
//        """;
//        try (Connection conn = ConnectionManager.getConnection();
//             Statement st = conn.createStatement()) {
//            st.execute(sql);
//        }
//    }

    /** Insert a new reminder. Populates r.id with the generated key. */
    public Reminder create(Reminder r) throws SQLException {
        String sql = """
            INSERT INTO reminders
              (user_id, message, trigger_at, recurring, interval, repeat_interval_ms)
            VALUES (?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getUserId());
            ps.setString(2, r.getMessage());
            ps.setString(3, r.getTriggerAt().toString());
            ps.setBoolean(4, r.isRecurring());
            ps.setString(5, r.getInterval().name());
            ps.setLong(6, r.getRepeatIntervalMs());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    r.setId(rs.getInt(1));
                }
            }
            return r;
        }
    }

    /** Update an existing reminder. */
    public boolean update(Reminder r) throws SQLException {
        String sql = """
            UPDATE reminders
               SET message = ?,
                   trigger_at = ?,
                   recurring = ?,
                   interval = ?,
                   repeat_interval_ms = ?
             WHERE id = ?;
        """;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getMessage());
            ps.setString(2, r.getTriggerAt().toString());
            ps.setBoolean(3, r.isRecurring());
            ps.setString(4, r.getInterval().name());
            ps.setLong(5, r.getRepeatIntervalMs());
            ps.setInt(6, r.getId());

            return ps.executeUpdate() == 1;
        }
    }

    /** Delete a reminder by its ID. */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM reminders WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /** Find all reminders for a given user. */
    public List<Reminder> findByUser(int userId) throws SQLException {
        String sql = """
            SELECT id, user_id, message, trigger_at, recurring, interval, repeat_interval_ms
              FROM reminders
             WHERE user_id = ?
             ORDER BY trigger_at
        """;
        List<Reminder> list = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reminder r = new Reminder(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("message"),
                            LocalDateTime.parse(rs.getString("trigger_at")),
                            rs.getBoolean("recurring"),
                            Interval.valueOf(rs.getString("interval")),
                            rs.getLong("repeat_interval_ms")
                    );
                    list.add(r);
                }
            }
        }
        return list;
    }
}
