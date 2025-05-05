package com.yourorg.finance;

import com.yourorg.finance.model.User;
import com.yourorg.finance.service.AuthService;
import com.yourorg.finance.util.ConnectionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApp extends Application {
//    @Override
//    public void start(Stage stage) throws Exception {
//        // 1) Smoke-test the DB connection
//        try (var conn = ConnectionManager.getConnection()) {
//            System.out.println("✅ Connected to database: " + conn.getMetaData().getURL());
//        }
//// ==== BEGIN TEMPORARY SMOKE-TEST — remove when you add real login UI ====
//
////        AuthService auth = new AuthService();
////
////        // 2) Only register if the test user doesn't already exist
////        User existing = auth.login("testuser", "password123");
////        if (existing == null) {
////            User newUser = auth.register("testuser", "password123", "USER");
////            System.out.println("Registered: " + newUser.getUsername() + " (id=" + newUser.getId() + ")");
////        } else {
////            System.out.println("User already registered: " + existing.getUsername() + " (id=" + existing.getId() + ")");
////        }
////
////        // 3) Attempt login
////        User login = auth.login("testuser", "password123");
////        System.out.println("Login success: " + (login != null));
//
//        // 4) Show a simple window so JavaFX thread stays alive
//        Label label = new Label("Hello, Personal Finance Manager!");
//        Scene scene = new Scene(label, 800, 600);
//
//        stage.setTitle("PFM Dashboard");
//        stage.setScene(scene);
//        stage.show();
//    }
    @Override
    public void start(Stage stage) throws Exception {
        // Ensure our test user always exists
        AuthService auth = new AuthService();
        try {
            auth.register("testuser", "password123", "USER");
            System.out.println("✅ testuser registered");
        } catch (Exception ignore) {
            // user probably already existed
        }

        // Now load the real login screen
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        stage.setTitle("PFM Login");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
