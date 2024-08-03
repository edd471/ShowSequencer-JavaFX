package com.example.showSequencerJavafx;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.*;

import javax.sound.midi.*;

public class Fader {


    private int faderNum;
    private String name;
    private boolean isMix;
    private int value;
    private MidiDevice device;
    private final SimpleBooleanProperty isVisible = new SimpleBooleanProperty();

    public void setDevice(MidiDevice device){
        this.device = device;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setFaderNum(int faderNum) {
        this.faderNum = faderNum;
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}

    public int getFaderNum() {
        return faderNum;
    }

    public SimpleBooleanProperty getIsVisible(){
        return isVisible;
    }

    public void setIsVisible(boolean b){
        this.isVisible.set(b);
    }


    public boolean getIsMix() {
        return isMix;
    }

    public void setIsMix(boolean isMix){
        this.isMix = isMix;
    }


    public MidiDevice getDevice() {
        return device;
    }

    public Fader(int faderNum, String name, boolean isMix, int value, boolean isVisible){
        this.faderNum = faderNum;
        this.name = name;
        this.isMix = isMix;
        this.value = value;
        this.isVisible.set(isVisible);

    }

    public void run(int dbValue){
        if(device==null) return;

        try {

            String message = getBytes(dbValue);

            SysexMessage msg = new SysexMessage();

            int len = message.length();
            byte[] ans = new byte[len / 2];

            for (int i = 0; i < len; i += 2) {
                // using left shift operator on every character
                ans[i / 2] = (byte) ((Character.digit(message.charAt(i), 16) << 4)
                        + Character.digit(message.charAt(i+1), 16));
            }


            msg.setMessage(ans, ans.length);

            System.out.println(message);

            device.getReceiver().send(msg, -1);


        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private String getBytes(int dbValue) {

        String typeBytes = "";

        if(!isMix){
            typeBytes = "3E120100330000"; //KInput Fader
        }else{
            typeBytes = "3E1201004E0000"; //kMixFader Fader
        }

        String message = "";

        message = message + "F043"; //Parameter Change

        message = message + "10"; // Midi Channel

        message = message + typeBytes;

        message = message + intToNBytes(value-1, 2); // Input Num

        //message = message + intToNBytes(dbValue, 5);

        message = message + "000000077F";

        message = message + "F7"; // Termination

        return message;
    }

    public static String intToNBytes(int number, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of bytes must be positive");
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < n; i++) {
            int currentByte = number & 0xFF; // Extract 8 bits
            result.insert(0, String.format("%02X", currentByte)); // Convert to hex and prepend to the result
            number >>= 8; // Shift right by 8 bits
        }

        return result.toString();
    }
}
