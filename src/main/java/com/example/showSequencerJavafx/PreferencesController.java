package com.example.showSequencerJavafx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferencesController implements Initializable {

    private MainController mainController;

    public double minFadeTime, runScreenFadeTime, playlistFadeTime;
    public Color colorNone, colorPLAY, colorSTOP, colorVOLUME, colorSTOP_ALL, colorPLAYLIST_START, colorPLAYLIST_CONT, colorPLAYLIST_FADE;

    @FXML
    private TextField txtMinFadeTime, txtRunScreenFadeTime, txtPlaylistFadeTime;
    @FXML
    private ColorPicker pickerNONE, pickerPLAY, pickerSTOP, pickerVOLUME, pickerSTOP_ALL, pickerPLAYLIST_START, pickerPLAYLIST_CONT, pickerPLAYLIST_FADE;
    @FXML
    private TableView<Fader> faderConfigTable;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainController = PreferencesScreen.getMainController();

        txtMinFadeTime.setText(Double.toString(mainController.MIN_FADE_TIME));
        minFadeTime = mainController.MIN_FADE_TIME;
        txtRunScreenFadeTime.setText(Double.toString(mainController.RUNSCREEN_FADE_TIME));
        runScreenFadeTime = mainController.RUNSCREEN_FADE_TIME;
        txtPlaylistFadeTime.setText(Double.toString(mainController.PLAYLIST_FADE_TIME));
        playlistFadeTime = mainController.RUNSCREEN_FADE_TIME;

        pickerNONE.setValue(mainController.commandColorMap.get(MainController.COMMAND.NONE));
        colorNone = mainController.commandColorMap.get(MainController.COMMAND.NONE);
        pickerPLAY.setValue(mainController.commandColorMap.get(MainController.COMMAND.PLAY));
        colorPLAY = mainController.commandColorMap.get(MainController.COMMAND.PLAY);
        pickerSTOP.setValue(mainController.commandColorMap.get(MainController.COMMAND.STOP));
        colorSTOP = mainController.commandColorMap.get(MainController.COMMAND.STOP);
        pickerVOLUME.setValue(mainController.commandColorMap.get(MainController.COMMAND.VOLUME));
        colorVOLUME = mainController.commandColorMap.get(MainController.COMMAND.VOLUME);
        pickerSTOP_ALL.setValue(mainController.commandColorMap.get(MainController.COMMAND.STOP_ALL));
        colorSTOP_ALL = mainController.commandColorMap.get(MainController.COMMAND.STOP_ALL);
        pickerPLAYLIST_START.setValue(mainController.commandColorMap.get(MainController.COMMAND.PLAYLIST_START));
        colorPLAYLIST_START = mainController.commandColorMap.get(MainController.COMMAND.PLAYLIST_START);
        pickerPLAYLIST_CONT.setValue(mainController.commandColorMap.get(MainController.COMMAND.PLAYLIST_CONT));
        colorPLAYLIST_CONT = mainController.commandColorMap.get(MainController.COMMAND.PLAYLIST_CONT);
        pickerPLAYLIST_FADE.setValue(mainController.commandColorMap.get(MainController.COMMAND.PLAYLIST_FADE));
        colorPLAYLIST_FADE = mainController.commandColorMap.get(MainController.COMMAND.PLAYLIST_FADE);
    }


    @FXML
    protected void close(){
        PreferencesScreen.getStage().close();
    }

    @FXML
    protected void saveAndClose(){
        try{
            minFadeTime= Double.parseDouble(txtMinFadeTime.getText());
            runScreenFadeTime = Double.parseDouble(txtRunScreenFadeTime.getText());
            playlistFadeTime = Double.parseDouble(txtPlaylistFadeTime.getText());

            colorNone = pickerNONE.getValue();
            colorPLAY = pickerPLAY.getValue();
            colorSTOP = pickerSTOP.getValue();
            colorVOLUME = pickerVOLUME.getValue();
            colorSTOP_ALL = pickerSTOP_ALL.getValue();
            colorPLAYLIST_START = pickerPLAYLIST_START.getValue();
            colorPLAYLIST_CONT = pickerPLAYLIST_CONT.getValue();
            colorPLAYLIST_FADE = pickerPLAYLIST_FADE.getValue();

            mainController.setPreferences(this);

            PreferencesScreen.getStage().close();
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
        }

    }


}
