package com.example.showSequencerJavafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PreferencesScreen extends Application {

    private static Stage thisStage;

    private static MainController mainController;

    @Override
    public void start(Stage stage) throws IOException {
        thisStage = stage;


        FXMLLoader fxmlLoader = new FXMLLoader(ShowSequencer.class.getResource("Preferences.fxml"));
        BorderPane root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Preferences");
        stage.setScene(scene);
        stage.show();
        stage.setMinHeight(stage.getHeight());
        stage.setMinWidth(stage.getWidth());
        stage.setMaxHeight(stage.getHeight());
        stage.setMaxWidth(stage.getWidth());


    }

    public void open(MainController mainController) {
        PreferencesScreen.mainController = mainController;
        try{
            start(new Stage());
        }catch (Exception e){
            System.out.println(e);
        }

    }

    public static Stage getStage(){
        return thisStage;
    }

    public static MainController getMainController(){
        return mainController;
    }


}