package com.example.showSequencerJavafx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**Class to manage all faders and store Midi Device being used
 */
public class FaderManager {

    private MidiAlivePing midiAlivePing;
    private final Shape statusCircle;
    private MidiDevice receiverDevice = null;
    private MidiDevice transmitterDevice = null;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private ObservableList<Fader> faderList = FXCollections.observableArrayList();
    public final Map<Double, String> dbConversionMap = new HashMap<>(){{
        put((double) -41, "0000000000");
        put((double) -40, "000000015F");
        put((double) -30, "0000000243");
        put((double) -20, "0000000327");
        put((double) -15, "000000040B");
        put((double) -10, "000000046F");
        put( -7.5, "0000000521");
        put((double) -5, "0000000553");
        put((double) -3, "000000057B");
        put((double) 0, "0000000637");
        put((double) 3, "0000000673");
        put((double) 5, "000000071B");
        put((double) 10, "000000077F");
    }};

    /**
     * Constructor for fader manage. Populates fader list with default values
     */
    public FaderManager(Shape statusCircle) {
        this.statusCircle = statusCircle;
        for (int i = 0; i < 32; i++) {
            faderList.add(new Fader(i+1, "", false, i+1, true));
        }
    }

    /**Getter for fader list
     * @return Observable List of faders
     */
    public ObservableList<Fader> getFaderList(){return faderList;}

    /**Setter for midi device. Sets all faders' midi device to new device.
     * @param receiverDevice Midi Device
     */
    public void setReceiverDevice(MidiDevice receiverDevice) {
        if (receiverDevice == null) return;
        if (midiAlivePing != null) {
            midiAlivePing.stop();
            statusCircle.setFill(Color.GRAY);
        }
        if (this.receiverDevice != null) {
            this.receiverDevice.close();
        }
        if (this.transmitterDevice != null) {
            this.transmitterDevice.close();
        }
        try{
            this.receiverDevice = receiverDevice;
            this.receiverDevice.open();
            for(Fader fader: faderList){
                fader.setDevice(receiverDevice);
            }

            if(receiverDevice.getMaxTransmitters() != 0) transmitterDevice = receiverDevice;
            else {
                for(MidiDevice.Info info: MidiSystem.getMidiDeviceInfo()){
                    if(MidiSystem.getMidiDevice(info).getMaxTransmitters() != 0 && Objects.equals(info.getName(), receiverDevice.getDeviceInfo().getName())){
                        transmitterDevice = MidiSystem.getMidiDevice(info);
                        break;
                    }
                }
            }

            this.transmitterDevice.open();
            midiAlivePing = new MidiAlivePing(statusCircle ,this);
            midiAlivePing.start();

        }catch (Exception e){
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error Opening Midi Device");
                alert.setContentText(e.getLocalizedMessage());
                alert.showAndWait();
            });
        }
    }

    public void reconnect() {
        statusCircle.setFill(Color.GRAY);
        try {
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                if (device.getMaxTransmitters() == 0 && info == device.getDeviceInfo()) {
                    setReceiverDevice(device);
                }
            }
        }catch (Exception e){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error Opening Midi Device");
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
        });
    }
    }

    /**Getter for fader device
     * @return Midi Device
     */
    public MidiDevice getReceiverDevice(){
        return this.receiverDevice;
    }

    public MidiDevice getTransmitterDevice(){
        return this.transmitterDevice;
    }

    /**Setter for fader list.
     * @param faderList List of faders
     */
    public void setFaderList(ArrayList<Fader> faderList){
        this.faderList = FXCollections.observableArrayList(faderList);
    }

    /**Runs faders using a double to generate a command string to pass
     * to each fader.
     * @param dBs List of doubles, each corresponding to a fader.
     */
    public void runFaders(ArrayList<Double> dBs){
        if(faderList.isEmpty() || receiverDevice ==null) return;

        Runnable sysExTask = () -> {
            try{
                if(!dBs.stream().allMatch(Objects::isNull)){
                    for (int i = 0; i < 32; i++) {
                        if(dBs.get(i)==null) continue;
                        faderList.get(i).run(dbConversionMap.get(dBs.get(i)));
                    }
                }

            }catch (Exception e){
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Error Sending Midi Command");
                    alert.setContentText(e.getLocalizedMessage());
                    alert.showAndWait();
                });
            }
        };

        executorService.submit(sysExTask);

    }

}
