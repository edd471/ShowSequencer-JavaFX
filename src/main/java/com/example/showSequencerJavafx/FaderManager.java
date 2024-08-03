package com.example.showSequencerJavafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.sound.midi.MidiDevice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FaderManager {

    private MidiDevice device = null;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();;

    private ObservableList<Fader> faderList = FXCollections.observableArrayList();
    public final Map<Double, Integer> dbConversionMap = new HashMap<>(){{
        put((double) -41, 0);
        put((double) -40, 223);
        put((double) -30, 323);
        put((double) -20, 423);
        put((double) -15, 523);
        put((double) -10, 623);
        put((double) -7.5, 673);
        put((double) -5, 723);
        put((double) -3, 764);
        put((double) 0, 823);
        put((double) 3, 883);
        put((double) 5, 923);
        put((double) 10, 1023);
    }};

    public ObservableList<Fader> getFaderList(){return faderList;}


    public void setDevice(MidiDevice device){
        this.device = device;
        for(Fader fader: faderList){
            fader.setDevice(device);
        }
    }
    public MidiDevice getDevice(){
        return this.device;
    }

    public void setFaderList(MidiDevice device){
        if(device==null){return;}
        faderList.clear();
        for (int i = 0; i < 32; i++) {
            faderList.add(new Fader(i+1, "", false, i+1, true));
        }
    }

    public void setFaderList(ArrayList<Fader> faderList){
        this.faderList = FXCollections.observableArrayList(faderList);
    }

    public void runFaders(ArrayList<Double> dBs){
        if(faderList.isEmpty()) return;

        Runnable sysExTask = () -> {
            try{
                device.open();

                for (int i = 0; i < 32; i++) {
                    faderList.get(i).run(dbConversionMap.get(dBs.get(i)));
                }

                device.close();
            }catch (Exception e){
                System.out.println(e);
            }
        };

        executorService.submit(sysExTask);


    }

}
