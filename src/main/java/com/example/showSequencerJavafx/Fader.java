package com.example.showSequencerJavafx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.*;

public class Fader {

    private int faderNum;
    private String name;
    private boolean isMix;
    private int channelNumber;

    public Fader(int faderNum, String name, boolean isMix, int channelNumber){
        this.faderNum = faderNum;
        this.name = name;
        this.isMix = isMix;
        this.channelNumber = channelNumber;
    }

    public void run() throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {

        MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
        System.out.println(Arrays.toString(info));

        MidiDevice device = MidiSystem.getMidiDevice(info[info.length-2]);
        System.out.println(device.getDeviceInfo());

        device.open();

        List<Byte> message = getBytes();


        SysexMessage msg = new SysexMessage();
        byte[] byteArray = convertListToByteArray(message);
        msg.setMessage(byteArray, message.size());

        device.getReceiver().send(msg, -1);

        device.close();

    }

    public static byte[] convertListToByteArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    private List<Byte> getBytes() {
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
            message.add(typeByte);
        }

        message.add((byte) 0x00); // Input Num
        message.add((byte) 0x00);

        message.add((byte) 0xF7); // Termination

        return message;
    }


    public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
        new Fader(0, "Bill", false, 0).run();


    }

}
