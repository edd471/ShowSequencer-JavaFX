package com.example.showSequencerJavafx;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import javafx.util.Duration;

/**
 * Wrapper class to add fading functionality to the mediaPlayer class.
 */
public class MediaPlayerWrapper {

    private final MediaPlayer mediaPlayer;
    private final MainController mainController;
    private final SimpleDoubleProperty fadeProgress = new SimpleDoubleProperty(0);
    private final Slider slider;

    /**Constructor for media player wrapper. Sets variables
     * @param media Media object to be controlled
     * @param mainController Main controller for GUI
     * @param slider Slider that controls media volume (Nullable)
     */
    public MediaPlayerWrapper(Media media, MainController mainController, Slider slider) {
        this.mediaPlayer = new MediaPlayer(media);
        this.mainController = mainController;
        this.slider = slider;
    }

    /**Getter for fade progress
     * @return Double property containing percentage progress.
     */
    public SimpleDoubleProperty getFadeProgress() {
        return fadeProgress;
    }

    /**Getter for media player
     * @return Media player being wrapped
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**Plays media player and fades volume to target value in duration.
     * @param targetVol Volume to play media player at.
     * @param duration Duration for fade to target volume.
     */
    public void playFaded(double targetVol, double duration) {

        ExponentialFade grow = new ExponentialFade(Math.max(duration, mainController.MIN_FADE_TIME), 0,
                                                   targetVol, mediaPlayer.toString(), mainController);
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

    /**Default call for pauseFaded. Passes empty runnable to pauseFaded
     * @param startVol Start volume for fade.
     * @param duration Duration of fade.
     */
    public void pauseFaded(double startVol, double duration){
        pauseFaded(startVol, duration, ()->{});
    }

    /**Pauses media player after fading volume down over given duration.
     * @param startVol Start volume for fade.
     * @param duration Duration of fade.
     * @param onFinished Runnable to be run at end of fade.
     */
    public void pauseFaded(double startVol, double duration, Runnable onFinished){

        ExponentialFade grow = new ExponentialFade(Math.max(duration, mainController.MIN_FADE_TIME), startVol,
                                          0, mediaPlayer.toString(), mainController);
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

    /**Default call for stopFaded. Passes empty runnable to stopFaded
     * @param startVol Start volume for fade.
     * @param duration Duration of fade.
     */
    public void stopFaded(double startVol, double duration){
        stopFaded(startVol, duration, ()->{});
    }

    /**Stops media player after fading volume down over given duration.
     * @param startVol Start volume for fade.
     * @param duration Duration of fade.
     * @param onFinished Runnable to be run at end of fade.
     */
    public void stopFaded(double startVol, double duration, Runnable onFinished) {

        ExponentialFade grow = new ExponentialFade(Math.max(duration, mainController.MIN_FADE_TIME), startVol,
                                          0, mediaPlayer.toString(), mainController);
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

    /**Default function of wrapped media player.
     * @param seekTime Time variable to seek media player to.
     */
    public void seek(Duration seekTime) {
        mediaPlayer.seek(seekTime);
    }

    /**Default function of wrapped media player.
     * @param volume Volume to set media player to.
     */
    public void setVolume(double volume) {
        mediaPlayer.setVolume(volume);
    }

    /**Default function of wrapped media player.
     * @return Duration played of media.
     */
    public Duration getCurrentTime() {
        return mediaPlayer.getCurrentTime();
    }

    /**Default function of wrapped media player.
     * @return Total duration of media.
     */
    public Duration getTotalDuration() {
        return mediaPlayer.getTotalDuration();
    }

    /**Default function of wrapped media player.
     * @return Media Object.
     */
    public Media getMedia() {
        return mediaPlayer.getMedia();
    }

    /**Default function of wrapped media player.
     * @return Playing Status of media player.
     */
    public Object getStatus() {
        return mediaPlayer.getStatus();
    }

    /**Default function of wrapped media player.
     * @return Property containing current duration played of media.
     */
    public ReadOnlyObjectProperty<Duration> currentTimeProperty() {
        return mediaPlayer.currentTimeProperty();
    }

    /**Default function of wrapped media player.
     * @param runnable Runnable to be run on end of media.
     */
    public void setOnEndOfMedia(Runnable runnable) {
        mediaPlayer.setOnEndOfMedia(runnable);
    }
}
