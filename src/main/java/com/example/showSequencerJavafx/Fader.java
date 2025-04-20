package com.example.showSequencerJavafx;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Alert;

import javax.sound.midi.*;

/**
 * Fader class to store information for a fader channel in the fader desk.
 * Sends command to fader desk to set desk fader to new volume.
 */
public class Fader {

    private final int faderNum;
    private String name;
    private boolean isMix;
    private int value;
    private MidiDevice device;
    private final SimpleBooleanProperty isVisible = new SimpleBooleanProperty();

    /**Setter for Midi Device
     * @param device Midi device to set
     */
    public void setDevice(MidiDevice device) {
        this.device = device;
    }

    /**Getter for value
     * @return The channel number of the fader
     */
    public int getValue() {
        return value;
    }

    /**Setter for value
     * @param value The channel number of the fader
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**Getter for name
     * @return The name of the fader
     */
    public String getName() {
        return name;
    }

    /**Setter for name
     * @param name The name of the fader
     */
    public void setName(String name) {
        this.name = name;
    }

    /**Getter for fader num
     * @return The fader number
     */
    public int getFaderNum() {
        return faderNum;
    }

    /**Getter for isVisible
     * @return Boolean property containing true if visible, contains false otherwise
     */
    public SimpleBooleanProperty getIsVisible() {
        return isVisible;
    }

    /**Setter for isVisible
     * @param b True if visible, false otherwise
     */
    public void setIsVisible(boolean b) {
        this.isVisible.set(b);
    }

    /**Getter for isMix
     * @return True if mix, false otherwise
     */
    public boolean getIsMix() {
        return isMix;
    }

    /**Setter for isMix
     * @param isMix True if mix, false otherwise
     */
    public void setIsMix(boolean isMix) {
        this.isMix = isMix;
    }


    /**Constructor for Fader. Sets variables.
     * @param faderNum Number of fader in fader list
     * @param name User input name of fader
     * @param isMix True if mix, false otherwise
     * @param value Channel number of fader
     * @param isVisible True if visible, false otherwise
     */
    public Fader(int faderNum, String name, boolean isMix, int value, boolean isVisible) {
        this.faderNum = faderNum;
        this.name = name;
        this.isMix = isMix;
        this.value = value;
        this.isVisible.set(isVisible);
    }

    /**Copy constructor
     * @param fader Fader to copy
     */
    public Fader(Fader fader){
        this.faderNum = fader.faderNum;
        this.name = fader.name;
        this.isMix = fader.isMix;
        this.isVisible.set(fader.getIsVisible().get());
        this.value = fader.value;
        this.device = fader.device;
    }

    /**Constructs and sends message to Midi Device to change a channels dB volume
     * @param dbValue String containing byte code to change fader desk channel
     *                to dB value
     */
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
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error Sending Midi Command");
                alert.setContentText(e.getLocalizedMessage());
                alert.showAndWait();
            });
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

        message = message + intToTwoBytes(value - 1); // Input Num

        message = message + dbValue; // dB Gain

        message = message + "F7"; // Termination

        return message;
    }

    private static String intToTwoBytes(int number) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 2; i++) {
            int currentByte = number & 0xFF; // Extract 8 bits
            result.insert(0, String.format("%02X", currentByte)); // Convert to hex and prepend to the result
            number >>= 8; // Shift right by 8 bits
        }

        return result.toString();
    }


}


