package com.example.showSequencerJavafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import javax.sound.midi.MidiDevice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**Class to manage all faders and store Midi Device being used
 */
public class FaderManager {

    private MidiDevice device = null;
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
    public FaderManager(){
        for (int i = 0; i < 32; i++) {
            faderList.add(new Fader(i+1, "", false, i+1, true));
        }
    }

    /**Getter for fader list
     * @return Observable List of faders
     */
    public ObservableList<Fader> getFaderList(){return faderList;}

    /**Setter for midi device. Sets all faders' midi device to new device.
     * @param device Midi Device
     */
    public void setDevice(MidiDevice device){
        this.device = device;
        for(Fader fader: faderList){
            fader.setDevice(device);
        }
    }

    /**Getter for fader device
     * @return Midi Device
     */
    public MidiDevice getDevice(){
        return this.device;
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
        if(faderList.isEmpty() || device==null) return;

        Runnable sysExTask = () -> {

            try{
                if(!dBs.stream().allMatch(Objects::isNull)){
                    device.open();
                    for (int i = 0; i < 32; i++) {
                        if(dBs.get(i)==null) continue;
                        faderList.get(i).run(dbConversionMap.get(dBs.get(i)));
                    }

                    device.close();
                }

            }catch (Exception e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error Sending Midi Command");
                alert.setContentText(e.getLocalizedMessage());
                alert.showAndWait();
            }
        };

        executorService.submit(sysExTask);


    }

}
