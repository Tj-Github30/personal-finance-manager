package com.yourorg.finance;

import com.yourorg.finance.controller.WelcomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Load your initial FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
        Parent root = loader.load();

        // 2) Pass stage to controller if needed
        WelcomeController wc = loader.getController();
        wc.setPrimaryStage(stage);

        // 3) Compute 60% screen
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width = bounds.getWidth() * 0.6;
        double height = bounds.getHeight() * 0.6;

        // 4) Create Scene
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());



        // 7) Set stage
        stage.setTitle("PFM Welcome");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
