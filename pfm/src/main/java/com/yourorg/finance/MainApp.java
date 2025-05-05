package com.yourorg.finance;

import com.yourorg.finance.util.ConnectionManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 1) Smoke-test the DB connection
        try (var conn = ConnectionManager.getConnection()) {
            System.out.println("✅ Connected to database: " + conn.getMetaData().getURL());
        }

        // 2) Show a simple window so JavaFX thread stays alive
        Label label = new Label("Hello, Personal Finance Manager!");
        Scene scene = new Scene(label, 800, 600);

        stage.setTitle("PFM Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
