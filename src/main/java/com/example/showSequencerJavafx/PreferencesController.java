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
    public Color colorINF, colorN40, colorN30, colorN20, colorN15, colorN10, colorN75, colorN5, colorN3, color0, color3, color5, color10;
    public ObservableList<Fader> tempFaderList = FXCollections.observableArrayList();

    @FXML
    private TextField txtMinFadeTime, txtRunScreenFadeTime, txtPlaylistFadeTime;
    @FXML
    private ColorPicker pickerNONE, pickerPLAY, pickerSTOP, pickerVOLUME, pickerSTOP_ALL, pickerPLAYLIST_START, pickerPLAYLIST_CONT, pickerPLAYLIST_FADE;
    @FXML
    private ColorPicker pickerINF, pickerN40, pickerN30, pickerN20, pickerN15, pickerN10, pickerN75, pickerN5, pickerN3,picker0, picker3, picker5, picker10;
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

        pickerINF.setValue(mainController.dBColorMap.get((double) -41));
        colorINF = mainController.dBColorMap.get((double)-41);
        pickerN40.setValue(mainController.dBColorMap.get((double)-40));
        colorN40 = mainController.dBColorMap.get((double)-40);
        pickerN30.setValue(mainController.dBColorMap.get((double)-30));
        colorN30 = mainController.dBColorMap.get((double)-30);
        pickerN20.setValue(mainController.dBColorMap.get((double)-20));
        colorN20 = mainController.dBColorMap.get((double)-20);
        pickerN15.setValue(mainController.dBColorMap.get((double)-15));
        colorN15 = mainController.dBColorMap.get((double)-15);
        pickerN10.setValue(mainController.dBColorMap.get((double)-10));
        colorN10 = mainController.dBColorMap.get((double)-10);
        pickerN75.setValue(mainController.dBColorMap.get(-7.5));
        colorN75 = mainController.dBColorMap.get(-7.5);
        pickerN5.setValue(mainController.dBColorMap.get((double)-5));
        colorN5 = mainController.dBColorMap.get((double)-5);
        pickerN3.setValue(mainController.dBColorMap.get((double)-3));
        colorN3 = mainController.dBColorMap.get((double)-3);
        picker0.setValue(mainController.dBColorMap.get((double)0));
        color0 = mainController.dBColorMap.get((double)0);
        picker3.setValue(mainController.dBColorMap.get((double)3));
        color3 = mainController.dBColorMap.get((double)3);
        picker5.setValue(mainController.dBColorMap.get((double)5));
        color5 = mainController.dBColorMap.get((double)5);
        picker10.setValue(mainController.dBColorMap.get((double)10));
        color10 = mainController.dBColorMap.get((double)10);


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

            colorINF = pickerINF.getValue();
            colorN40 = pickerN40.getValue();
            colorN30 = pickerN30.getValue();
            colorN20 = pickerN20.getValue();
            colorN15 = pickerN15.getValue();
            colorN10 = pickerN10.getValue();
            colorN75 = pickerN75.getValue();
            colorN5 = pickerN5.getValue();
            colorN3 = pickerN3.getValue();
            color0 = picker0.getValue();
            color3 = picker3.getValue();
            color5 = picker5.getValue();
            color10 = picker10.getValue();

            mainController.setPreferences(this);

            PreferencesScreen.getStage().close();
        }catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
        }

    }


}
