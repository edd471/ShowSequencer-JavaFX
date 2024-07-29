package com.example.showSequencerJavafx;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ShowSequencer extends Application {

    private static Stage thisStage;

    public static StringBinding fontSizeBinding;

    @Override
    public void start(Stage stage) throws IOException {
        thisStage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(ShowSequencer.class.getResource("MainMenu.fxml"));
        BorderPane root = fxmlLoader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styleSheet.css")).toExternalForm());
        stage.setTitle("Untitled");
        stage.setScene(scene);
        stage.show();
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());

        // Maximum font size
        double maxFontSize = 16; // Example maximum font size in pixels

        // Bind the root's font size to the stage's width property with a maximum value
        fontSizeBinding = Bindings.createStringBinding(() -> {
            double calculatedFontSize1 = stage.getWidth() / 120;
            double calculatedFontSize2 = stage.getHeight() / 60;
            return "-fx-font-size: " + Math.min(Math.min(calculatedFontSize1, calculatedFontSize2), maxFontSize) + "px;";
        }, stage.widthProperty());

        root.styleProperty().bind(fontSizeBinding);

        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // Trigger binding update when window is restored
                fontSizeBinding.invalidate();
            }
        });
    }

    public static Stage getStage(){
        return thisStage;
    }

    public static void main(String[] args) {
        launch();
    }
}