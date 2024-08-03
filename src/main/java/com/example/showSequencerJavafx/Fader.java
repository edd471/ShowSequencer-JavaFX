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


            List<Byte> message = getBytes(dbValue);

            SysexMessage msg = new SysexMessage();
            byte[] byteArray = convertListToByteArray(message);
            msg.setMessage(byteArray, message.size());

            device.getReceiver().send(msg, -1);



        } catch (Exception e) {
            System.out.println(e);
        }



    }

    public static byte[] convertListToByteArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    private List<Byte> getBytes(int dbValue) {

        byte[] typeBytes;

        if(!isMix){
            typeBytes = new byte[] {(byte) 0x3E, (byte) 0x12,(byte) 0x01,(byte) 0x00,(byte) 0x33,(byte) 0x00,(byte) 0x00}; //KInput Fader
        }else{
            typeBytes = new byte[] {(byte) 0x3E, (byte) 0x12,(byte) 0x01,(byte) 0x00,(byte) 0x4E,(byte) 0x00,(byte) 0x00}; //kMixFader Fader
        }

        List<Byte> message = new ArrayList<>();

        message.add((byte) 0xF0); // Parameter Change
        message.add((byte) 0x43);

        message.add((byte) 0x01); // Midi Channel

        for (byte typeByte : typeBytes) {
            message.add(typeByte);  //Type
        }

        byte[] inputByte = intToTwoBytes(faderNum);

        message.add(inputByte[1]); // Input Num

        byte[] dbByte = intToTwoBytes(dbValue);

        message.add(dbByte[0]); // dB Value
        message.add(dbByte[1]);

        message.add((byte) 0xF7); // Termination

        return message;
    }

    public static byte[] intToTwoBytes(int number) {

        byte highByte = (byte) ((number >> 7) & 0x7F); // Extract the high 7 bits
        byte lowByte = (byte) (number & 0x7F);          // Extract the low 7 bits
        return new byte[]{highByte, lowByte};
    }

}
