package com.example.showSequencerJavafx;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

import javax.sound.midi.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MidiAlivePing {

    private final MidiDevice transmitDevice;
    private final MidiDevice receiverDevice;
    private final FaderManager faderManager;
    private final Shape statusCircle;
    private final ScheduledExecutorService usbStatus;
    private Transmitter transmitter;

    AtomicBoolean usbConnected = new AtomicBoolean(false);
    AtomicReference<MidiDevice.Info> lastConnected = new AtomicReference<>(null);
    IdentityResponseListener identityListener;

    public MidiAlivePing(Shape statusCircle, FaderManager faderManager) {
        this.faderManager = faderManager;
        this.transmitDevice = faderManager.getTransmitterDevice();
        this.receiverDevice = faderManager.getReceiverDevice();
        this.statusCircle = statusCircle;

        usbStatus = Executors.newSingleThreadScheduledExecutor();

    }

    public void start() throws MidiUnavailableException {
        identityListener = new IdentityResponseListener();
        transmitter = transmitDevice.getTransmitter();
        transmitter.setReceiver(identityListener);
        usbStatus.scheduleAtFixedRate(() -> {
            try {
                if (receiverDevice == null){
                    statusCircle.setFill(Color.GRAY);
                    usbConnected.set(false);
                }
                else if (isDeviceStillAvailable(receiverDevice)) {
                    if(!usbConnected.get() && lastConnected.get() != null){
                        faderManager.reconnect();
                        usbConnected.set(true);
                    }

                    // Reset listener before sending
                    identityListener.reset();


                    // Send Identity Request
                    byte[] identityRequest = new byte[] {
                            (byte) 0xF0, 0x43, 0x30, 0x3E, 0x12, 0x01, 0x02, 0x2E, 0x00, 0x00, 0x00, 0x00, (byte) 0xF7
                    };
                    SysexMessage request = new SysexMessage();
                    request.setMessage(identityRequest, identityRequest.length);
                    receiverDevice.getReceiver().send(request, -1);

                    // Wait briefly to allow a response (non-blocking delay)
                    Executors.newSingleThreadScheduledExecutor().schedule(() -> Platform.runLater(() -> {
                        if (identityListener.hasReceivedReply()) {
                            statusCircle.setFill(Color.GREEN);
                        } else {
                            statusCircle.setFill(Color.ORANGE);
                        }
                    }), 300, TimeUnit.MILLISECONDS); // You can tweak this delay


                } else {
                    lastConnected.set(faderManager.getReceiverDevice().getDeviceInfo());
                    statusCircle.setFill(Color.RED);
                    usbConnected.set(false);
                }

            }catch (Exception e){
                statusCircle.setFill(Color.RED);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    public void stop(){
        usbStatus.shutdown();
        identityListener.close();
    }

    boolean isDeviceStillAvailable(MidiDevice device) {
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (info.getName().equals(device.getDeviceInfo().getName())) {
                return true;
            }
        }
        return false;
    }

}

