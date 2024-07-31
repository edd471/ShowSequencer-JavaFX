package com.example.showSequencerJavafx;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import javafx.util.Duration;

public class MediaPlayerWrapper {


    private final MediaPlayer mediaPlayer;
    private final MainController mainController;
    private final SimpleDoubleProperty fadeProgress = new SimpleDoubleProperty(0);
    private final Slider slider;


    public MediaPlayerWrapper(Media media, MainController mainController, Slider slider) {
        this.mediaPlayer = new MediaPlayer(media);
        this.mainController = mainController;
        this.slider = slider;
    }

    public SimpleDoubleProperty getFadeProgress() {
        return fadeProgress;
    }

    public SimpleDoubleProperty fadeProgressProperty() {
        return fadeProgress;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }


    public void playFaded(double targetVol, double duration) {

        ExponentialFade grow = new ExponentialFade(false, Math.max(duration, mainController.MIN_FADE_TIME), 0, targetVol, mediaPlayer.toString(), mainController);
        ChangeListener<Number> volListener = (Obs, oldValue, newValue)-> {
            if(slider!=null) mediaPlayer.setVolume(((Double) newValue/100) * (slider.getValue()/100));
            else mediaPlayer.setVolume((Double) newValue/100);
            mainController.refreshTables();
        };
        grow.newVol.addListener(volListener);
        ChangeListener<Number> progressListener = (Obs, oldValue, newValue) -> fadeProgress.set((double)newValue);
        grow.progress.addListener(progressListener);
        grow.setOnFinished(()-> {grow.newVol.removeListener(volListener);
            grow.progress.removeListener(progressListener);
        });

        grow.timeline.play();

        mediaPlayer.setVolume(0);
        mediaPlayer.play();
    }

    public void pauseFaded(double startVol, double duration){
        pauseFaded(startVol, duration, ()->{});
    }

    public void pauseFaded(double startVol, double duration, Runnable onFinished){

        ExponentialFade grow = new ExponentialFade(false, Math.max(duration, mainController.MIN_FADE_TIME), startVol, 0, mediaPlayer.toString(), mainController);
        ChangeListener<Number> volListener = (Obs, oldValue, newValue)-> {
            if(slider!=null) mediaPlayer.setVolume(((Double) newValue/100) * (slider.getValue()/100));
            else mediaPlayer.setVolume((Double) newValue/100);
            mainController.refreshTables();
        };
        grow.newVol.addListener(volListener);
        ChangeListener<Number> progressListener = (Obs, oldValue, newValue) -> fadeProgress.set((double)newValue);
        grow.progress.addListener(progressListener);

        grow.setOnFinished(()-> {
            grow.newVol.removeListener(volListener);
            mediaPlayer.setVolume(0);
            mediaPlayer.pause();
            grow.progress.removeListener(progressListener);
            fadeProgress.set(0);
            onFinished.run();
            });

        grow.timeline.play();
    }

    public void stopFaded(double startVol, double duration){
        stopFaded(startVol, duration, ()->{});
    }

    public void stopFaded(double startVol, double duration, Runnable onFinished) {

        ExponentialFade grow = new ExponentialFade(true, Math.max(duration, mainController.MIN_FADE_TIME), startVol, 0, mediaPlayer.toString(), mainController);
        ChangeListener<Number> volListener = (Obs, oldValue, newValue)-> {
            if(slider!=null) mediaPlayer.setVolume(((Double) newValue/100) * (slider.getValue()/100));
            else mediaPlayer.setVolume((Double) newValue/100);
            mainController.refreshTables();
        };
        grow.newVol.addListener(volListener);
        ChangeListener<Number> progressListener = (Obs, oldValue, newValue) -> fadeProgress.set((double)newValue);
        grow.progress.addListener(progressListener);
        grow.setOnFinished(()-> {
            grow.newVol.removeListener(volListener);
            mediaPlayer.setVolume(0);
            mediaPlayer.pause();
            mediaPlayer.seek(Duration.ZERO);
            grow.progress.removeListener(progressListener);
            fadeProgress.set(0);
            onFinished.run();
        });

        grow.timeline.play();
    }


    public void seek(Duration seekTime) {
        mediaPlayer.seek(seekTime);
    }

    public void setVolume(double volume) {
        mediaPlayer.setVolume(volume);
    }

    public Duration getCurrentTime() {
        return mediaPlayer.getCurrentTime();
    }

    public Duration getTotalDuration() {
        return mediaPlayer.getTotalDuration();
    }

    public Media getMedia() {
        return mediaPlayer.getMedia();
    }

    public Object getStatus() {
        return mediaPlayer.getStatus();
    }

    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return mediaPlayer.currentTimeProperty();
    }

    public void setOnEndOfMedia(Runnable runnable) {
        mediaPlayer.setOnEndOfMedia(runnable);
    }
}
