package com.yourorg.finance;

import com.yourorg.finance.controller.LoginController;
import com.yourorg.finance.controller.WelcomeController;
import com.yourorg.finance.model.User;
import com.yourorg.finance.service.AuthService;
import com.yourorg.finance.util.ConnectionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

    private final AuthService auth = new AuthService();

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Load your initial FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
        Parent root = loader.load();

        // Give the controller the stage if it needs it
        WelcomeController wc = loader.getController();
        wc.setPrimaryStage(stage);

        // 2) Compute 60% of the primary screen’s usable area
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width  = bounds.getWidth()  * 0.6;
        double height = bounds.getHeight() * 0.6;

        // 3) Create the scene at that size, attach your stylesheet
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(
                getClass().getResource("/css/styles.css")
                        .toExternalForm()
        );

        // 4) Show it
        stage.setTitle("PFM Welcome");
        stage.setScene(scene);
        stage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
