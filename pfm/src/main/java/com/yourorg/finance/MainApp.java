package com.yourorg.finance;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("PFM Dashboard");
        stage.setScene(new Scene(new Label("Hello, PFM!"), 800, 600));
        stage.show();
    }
    public static void main(String[] args) { launch(); }
}
