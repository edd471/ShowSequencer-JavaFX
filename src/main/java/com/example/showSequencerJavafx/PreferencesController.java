package com.example.showSequencerJavafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import java.net.URL;
import java.util.*;

public class PreferencesController implements Initializable {

    private MainController mainController;
    private FaderManager faderManager;

    public MidiDevice device;
    public double minFadeTime, runScreenFadeTime, playlistFadeTime;
    public Color colorNone, colorPLAY, colorSTOP, colorVOLUME, colorSTOP_ALL, colorPLAYLIST_START, colorPLAYLIST_CONT, colorPLAYLIST_FADE;
    public ObservableList<Fader> tempFaderList = FXCollections.observableArrayList();

    @FXML
    private TextField txtMinFadeTime, txtRunScreenFadeTime, txtPlaylistFadeTime;
    @FXML
    private ColorPicker pickerNONE, pickerPLAY, pickerSTOP, pickerVOLUME, pickerSTOP_ALL, pickerPLAYLIST_START, pickerPLAYLIST_CONT, pickerPLAYLIST_FADE;
    @FXML
    private ComboBox<MidiDevice.Info> comboBoxMidiDevice;
    @FXML
    private TableView<Fader> faderConfigTable;
    @FXML
    private TableColumn<Fader, Integer> colFaderNum;
    @FXML
    private TableColumn<Fader, String> colFaderName;
    @FXML
    private TableColumn<Fader, Boolean> colFaderType;
    @FXML
    private TableColumn<Fader, Integer> colFaderValue;
    @FXML
    private TableColumn<Fader, Boolean> colIsVisible;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainController = PreferencesScreen.getMainController();
        faderManager = mainController.getFaderManager();

        tempFaderList = FXCollections.observableArrayList();
        tempFaderList.addAll(faderManager.getFaderList().stream().map(Fader::new).toList());

        faderConfigTable.setItems(tempFaderList);

        colFaderNum.setCellValueFactory(new PropertyValueFactory<>("faderNum"));

        colFaderName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colFaderName.setCellFactory(x-> new TextFieldTableCell<>(new StringConverter<>() {
            @Override
            public String toString(String s) {
                return s;
            }
            @Override
            public String fromString(String s) {
                return s;
            }
        }));
        colFaderName.setOnEditCommit(event->{
            Fader fader = event.getRowValue();
            fader.setName(event.getNewValue());
        });

        colFaderType.setCellValueFactory(new PropertyValueFactory<>("isMix"));
        colFaderType.setCellFactory(x -> new ComboBoxTableCell<>(new StringConverter<>() {
            @Override
            public String toString(Boolean aBoolean) {
                if (aBoolean) return "Mix";
                else return "Input";
            }

            @Override
            public Boolean fromString(String s) {
                return s.equals("Mix");
            }
        }, FXCollections.observableArrayList(Arrays.asList(true, false))));
        colFaderType.setOnEditCommit(event -> {
            Fader fader = event.getRowValue();
            fader.setIsMix(event.getNewValue());
        });

        colFaderValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colFaderValue.setCellFactory(col-> new TextFieldTableCell<>(new IntegerStringConverter()));
        colFaderValue.setOnEditCommit(event ->{
            Fader fader = event.getRowValue();
            if(event.getNewValue() == (int) event.getNewValue()){
                fader.setValue(event.getNewValue());
            }else{
                fader.setValue(event.getOldValue());
            }
        });

        colIsVisible.setCellValueFactory(x->x.getValue().getIsVisible());
        colIsVisible.setCellFactory(CheckBoxTableCell.forTableColumn(colIsVisible));
        colIsVisible.setOnEditCommit(event -> {
            Fader fader = event.getRowValue();
            fader.setIsVisible(event.getNewValue());
        });


        ObservableList<MidiDevice.Info> devices =  FXCollections.observableList(Arrays.stream(MidiSystem.getMidiDeviceInfo()).toList());
        List<MidiDevice.Info> toRemove = devices.stream().filter(info -> {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                return device.getMaxTransmitters() == 0;
            } catch (MidiUnavailableException e) {
                return true;
            }
        }).toList();

        devices = FXCollections.observableList(Arrays.stream(MidiSystem.getMidiDeviceInfo()).filter(toRemove::contains).toList());
        comboBoxMidiDevice.setItems(devices);

        if(faderManager.getDevice()!=null){
            comboBoxMidiDevice.getSelectionModel().select(faderManager.getDevice().getDeviceInfo());
            midiDeviceChosen();
        }


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
    protected void midiDeviceChosen(){
        try {
            faderManager.setDevice(MidiSystem.getMidiDevice(comboBoxMidiDevice.getSelectionModel().getSelectedItem()));
            device = MidiSystem.getMidiDevice(comboBoxMidiDevice.getSelectionModel().getSelectedItem());
            faderConfigTable.refresh();

        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Connection to MIDI device failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
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
