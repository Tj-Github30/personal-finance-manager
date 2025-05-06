package com.yourorg.finance.controller;

import com.yourorg.finance.dao.ReminderDao;
import com.yourorg.finance.model.Reminder;
import com.yourorg.finance.service.ReminderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class RemindersController {
    @FXML private TableView<Reminder> table;
    @FXML private TableColumn<Reminder,String> msgCol;
    @FXML private TableColumn<Reminder,LocalDateTime> timeCol;
    @FXML private TableColumn<Reminder,Boolean> recCol;
    @FXML private TableColumn<Reminder,Long> intervalCol;
    @FXML private Button addBtn, editBtn, delBtn;
    @FXML private ComboBox<String> intervalBox;


    private final ReminderDao dao = new ReminderDao();
    private final ReminderService service = new ReminderService();
    private final ObservableList<Reminder> data = FXCollections.observableArrayList();
    private final int currentUserId = 1;

    @FXML
    public void initialize() throws SQLException {
        // 1) Column wiring
        msgCol     .setCellValueFactory(new PropertyValueFactory<>("message"));
        timeCol    .setCellValueFactory(new PropertyValueFactory<>("triggerAt"));
        recCol     .setCellValueFactory(new PropertyValueFactory<>("recurring"));
        intervalCol.setCellValueFactory(new PropertyValueFactory<>("repeatIntervalMs"));

        // 2) Strip stray blank columns & set proportional resize
        table.getColumns().removeIf(c -> c.getText()==null || c.getText().isBlank());
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        msgCol     .prefWidthProperty().bind(table.widthProperty().multiply(0.40)); // 40%
        timeCol    .prefWidthProperty().bind(table.widthProperty().multiply(0.25)); // 25%
        recCol     .prefWidthProperty().bind(table.widthProperty().multiply(0.15)); // 15%
        intervalCol.prefWidthProperty().bind(table.widthProperty().multiply(0.20)); // 20%

        // 3) Populate & schedule
        table.setItems(data);
        loadAll();
        // prepare the ComboBox for the dialog
        intervalBox = new ComboBox<>();
        intervalBox.getItems().setAll("NONE","HOURLY","DAILY","MONTHLY","YEARLY");

        service.start(currentUserId);

        // 4) wiring
        addBtn.setOnAction(e -> showDialog(null));
        editBtn.setOnAction(e -> {
            Reminder sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) showDialog(sel);
            else showAlert("Select one","Please select a reminder.");
        });
        delBtn.setOnAction(e -> {
            Reminder sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    dao.delete(sel.getId());
                    loadAll();
                } catch(SQLException ex) {
                    showAlert("DB Error", ex.getMessage());
                }
            }
        });
    }

    private void loadAll() {
        try {
            List<Reminder> list = dao.findByUser(currentUserId);
            data.setAll(list);
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showDialog(Reminder r) {
        boolean isNew = (r == null);

        // 1) Create dialog
        Dialog<Reminder> dlg = new Dialog<>();
        dlg.setTitle(isNew ? "New Reminder" : "Edit Reminder");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 2) Build form controls
        TextField msgField = new TextField(isNew ? "" : r.getMessage());
        DatePicker datePicker = new DatePicker(
                isNew ? LocalDate.now() : r.getTriggerAt().toLocalDate());
        Spinner<Integer> hourSp = new Spinner<>(0, 23,
                isNew ? LocalDateTime.now().getHour() : r.getTriggerAt().getHour());
        Spinner<Integer> minSp  = new Spinner<>(0, 59,
                isNew ? LocalDateTime.now().getMinute() : r.getTriggerAt().getMinute());
        CheckBox recBox = new CheckBox("Recurring");
        recBox.setSelected(!isNew && r.isRecurring());

        // 3) Interval ComboBox instead of raw ms
        ComboBox<Reminder.Interval> intervalBox = new ComboBox<>();
        intervalBox.getItems().setAll(Reminder.Interval.values());
        intervalBox.getSelectionModel().select(
                isNew ? Reminder.Interval.NONE : r.getInterval()
        );

        // 4) Layout in a GridPane
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Message:"), 0, 0);
        grid.add(msgField,               1, 0);
        grid.add(new Label("Date:"),    0, 1);
        grid.add(datePicker,            1, 1);
        grid.add(new Label("Hour:"),    0, 2);
        grid.add(hourSp,                1, 2);
        grid.add(new Label("Minute:"),  0, 3);
        grid.add(minSp,                 1, 3);
        grid.add(recBox,                1, 4);
        grid.add(new Label("Repeat every:"), 0, 5);
        grid.add(intervalBox,               1, 5);
        dlg.getDialogPane().setContent(grid);

        // 5) Convert result into a Reminder
        dlg.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                LocalDateTime at = datePicker.getValue()
                        .atTime(hourSp.getValue(), minSp.getValue());
                boolean recurring = recBox.isSelected();
                Reminder.Interval iv = intervalBox.getValue();

                long repeatMs;
                switch (iv) {
                    case HOURLY:  repeatMs = Duration.ofHours(1).toMillis();  break;
                    case DAILY:   repeatMs = Duration.ofDays(1).toMillis();   break;
                    case MONTHLY: repeatMs = Duration.ofDays(30).toMillis();  break;
                    case YEARLY:  repeatMs = Duration.ofDays(365).toMillis(); break;
                    default:      repeatMs = 0L;
                }

                return new Reminder(
                        isNew ? 0 : r.getId(),
                        currentUserId,
                        msgField.getText().trim(),
                        at,
                        recurring,
                        iv,
                        repeatMs
                );
            }
            return null;
        });

        // 6) Show and persist
        Optional<Reminder> result = dlg.showAndWait();
        result.ifPresent(nr -> {
            try {
                if (isNew) dao.create(nr);
                else       dao.update(nr);

                service.schedule(nr);
                loadAll();
            } catch (SQLException ex) {
                showAlert("DB Error", ex.getMessage());
            }
        });
    }


    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING, m, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(t);
        a.showAndWait();
    }
}
