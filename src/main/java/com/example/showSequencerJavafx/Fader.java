package com.example.showSequencerJavafx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;

import javax.sound.midi.*;

public class Fader {


    private final int faderNum;
    private String name;
    private boolean isMix;
    private int value;
    private MidiDevice device;
    private final SimpleBooleanProperty isVisible = new SimpleBooleanProperty();

    public void setDevice(MidiDevice device) {
        this.device = device;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFaderNum() {
        return faderNum;
    }

    public SimpleBooleanProperty getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean b) {
        this.isVisible.set(b);
    }


    public boolean getIsMix() {
        return isMix;
    }

    public void setIsMix(boolean isMix) {
        this.isMix = isMix;
    }


    public Fader(int faderNum, String name, boolean isMix, int value, boolean isVisible) {
        this.faderNum = faderNum;
        this.name = name;
        this.isMix = isMix;
        this.value = value;
        this.isVisible.set(isVisible);
    }

    public Fader(Fader fader){
        this.faderNum = fader.faderNum;
        this.name = fader.name;
        this.isMix = fader.isMix;
        this.isVisible.set(fader.getIsVisible().get());
        this.value = fader.value;
        this.device = fader.device;
    }

    public void run(String dbValue) {
        if (device == null) return;

        try {

            String message = getBytes(dbValue);

            SysexMessage msg = new SysexMessage();

            int len = message.length();
            byte[] ans = new byte[len / 2];

            for (int i = 0; i < len; i += 2) {
                // using left shift operator on every character
                ans[i / 2] = (byte) ((Character.digit(message.charAt(i), 16) << 4)
                        + Character.digit(message.charAt(i + 1), 16));
            }

            msg.setMessage(ans, ans.length);

            device.getReceiver().send(msg, -1);


        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error Sending Midi Command");
            alert.setContentText(e.getLocalizedMessage());
            alert.showAndWait();
        }

    }

    private String getBytes(String dbValue) {

        String typeBytes;

        if (!isMix) {
            typeBytes = "3E120100330000"; //KInput Fader
        } else {
            typeBytes = "3E1201004E0000"; //kMixFader Fader
        }

        String message = "";

        message = message + "F043"; //Parameter Change

        message = message + "10"; // Midi Channel

        message = message + typeBytes;

        message = message + intToNBytes(value - 1, 2); // Input Num

        message = message + dbValue; // dB Gain

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


