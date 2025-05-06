package com.yourorg.finance.service;

import com.yourorg.finance.dao.ReminderDao;
import com.yourorg.finance.model.Reminder;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;

public class ReminderService {
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();
    private final ReminderDao dao = new ReminderDao();

    /** Call once after login to schedule existing reminders */
    public void start(int userId) throws SQLException {
        for (Reminder r : dao.findByUser(userId)) {
            schedule(r);
        }
    }

    /** Schedule one reminder (and recurring if set) */
    public void schedule(Reminder r) {
        long delay = Duration.between(LocalDateTime.now(), r.getTriggerAt()).toMillis();
        Runnable show = () -> Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, r.getMessage(), ButtonType.OK);
            a.setHeaderText("Reminder");
            a.showAndWait();
        });

        if (r.isRecurring()) {
            scheduler.scheduleAtFixedRate(
                    show,
                    Math.max(delay, 0),
                    r.getRepeatIntervalMs(),
                    TimeUnit.MILLISECONDS
            );
        } else {
            scheduler.schedule(
                    show,
                    Math.max(delay, 0),
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /** Shut down on app exit */
    public void shutdown() {
        scheduler.shutdownNow();
    }
}
