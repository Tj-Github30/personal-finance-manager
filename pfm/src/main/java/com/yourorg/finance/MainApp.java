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
        // load your initial FXML exactly as you have it
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/welcome.fxml"));
        Parent root = loader.load();
        WelcomeController wc = loader.getController();
        wc.setPrimaryStage(stage);

        // get the primary screen’s visual bounds
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double width  = bounds.getWidth()  * 0.6;
        double height = bounds.getHeight() * 0.6;

        stage.setTitle("PFM Welcome");
        stage.setScene(new Scene(root, width, height));
        stage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
