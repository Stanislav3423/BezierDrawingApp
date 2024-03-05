package com.example.bezierdrawingapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DrawingApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(DrawingApplication.class.getResource("MainWindowDrawingBezierCurveApp.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 650);
        stage.setTitle("Drawing Bezier Curve");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}