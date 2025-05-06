package com.yourorg.finance.controller;

import com.yourorg.finance.service.AuthService;
import javafx.stage.Stage;

/** Shared by LoginController & RegisterController */
public abstract class BaseAuthController {
    protected AuthService auth = AuthService.getInstance();;
    protected Stage        primaryStage;
    public void setAuthService(AuthService auth) { this.auth = auth; }
    public void setPrimaryStage(Stage stage) { this.primaryStage = stage; }
}

