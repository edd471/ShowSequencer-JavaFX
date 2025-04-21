package com.example.showSequencerJavafx;

import javax.sound.midi.*;

public class IdentityResponseListener implements Receiver {

    private boolean received = false;
    private SysexMessage lastMessage = null;

    public IdentityResponseListener(){

    }

    @Override
    public void send(MidiMessage message, long timeStamp) {
        if (message instanceof SysexMessage sysex) {
            received = true;
            lastMessage = sysex;
        }
    }

    @Override
    public void close() {
        // Optional: handle cleanup
    }

    public boolean hasReceivedReply() {
        return received;
    }

    public SysexMessage getLastMessage() {
        return lastMessage;
    }

    public void reset() {
        received = false;
        lastMessage = null;
    }
}