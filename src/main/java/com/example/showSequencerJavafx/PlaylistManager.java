package com.example.showSequencerJavafx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class PlaylistManager {

    private int playListNumber = -1;
    private double initVolume = -1;
    private File playlistDirectory = null;
    private final ObservableList<PlaylistFile> playlistFiles = FXCollections.observableArrayList();
    private ChangeListener<Duration> playlistFileProgressListener;
    private final Controller controller;
    public SimpleDoubleProperty progress = new SimpleDoubleProperty(0);

    public PlaylistManager(Controller controller){
        this.controller = controller;
    }

    public ObservableList<PlaylistFile> getPlaylistFiles() {
        return playlistFiles;
    }

    public File getPlaylistDirectory() {
        return playlistDirectory;
    }

    public void setCurrentVolume(double volume) {
        for(PlaylistFile playlistFile : playlistFiles){
            if(playlistFile.getPlayer().getFadeProgress().get()==1){
                playlistFile.getPlayer().setVolume(volume/100);
            }
        }
    }

    public void setVolume(double volume) {
        if(initVolume<0) initVolume = controller.playlistVolumeSlider.getValue();
        controller.playlistVolumeSlider.setValue(volume);
    }

    public void resetVolume() {
        if(initVolume>=0) {
            controller.playlistVolumeSlider.setValue(initVolume);
            initVolume = -1;
        }
    }

    public void seekStart() {
        for(PlaylistFile playlistFile : playlistFiles){
            if(playlistFile.getPlayer().getStatus().equals(MediaPlayer.Status.PLAYING)){
                playlistFile.getPlayer().pauseFaded(controller.playlistVolumeSlider.getValue(), controller.MIN_FADE_TIME);
            }
        }
    }

    public void seek(double seekPercentage) {
        Optional<PlaylistFile> current = playlistFiles.stream().filter(x->x.getState().equals(PlaylistFile.STATE.PLAYING)||x.getState().equals(PlaylistFile.STATE.PAUSED)).findFirst();
        if (current.isEmpty()) return;

        Duration total = current.get().getPlayer().getTotalDuration();
        Duration seekTime = total.multiply(seekPercentage);
        current.get().getPlayer().seek(seekTime);
    }

    public void seekEnd() {
        for(PlaylistFile playlistFile : playlistFiles){
            if(playlistFile.getState().equals(States.STATE.PLAYING)){
                playlistFile.getPlayer().playFaded(controller.playlistVolumeSlider.getValue(), controller.MIN_FADE_TIME);
            }
        }
    }

    private void initiateTrack(double duration){
        controller.assertValidFiles();

        for(PlaylistFile file : playlistFiles){
            if(file.getState().equals(PlaylistFile.STATE.PLAYING) || file.getState().equals(PlaylistFile.STATE.PAUSED)) {
                file.getPlayer().stopFaded(controller.playlistVolumeSlider.getValue() ,duration);
                file.setState(PlaylistFile.STATE.STOPPED);
                file.setSelected(false);
                file.getPlayer().currentTimeProperty().removeListener(playlistFileProgressListener);
            }
        }

        PlaylistFile currentFile = playlistFiles.get(playListNumber);

        currentFile.getPlayer().playFaded(controller.playlistVolumeSlider.getValue(), duration);
        progress.bind(currentFile.getProgress());
        currentFile.setState(PlaylistFile.STATE.PLAYING);
        currentFile.setSelected(true);

        playlistFileProgressListener = (Observable, oldValue, newValue) -> {
            controller.playlistProgressBar.setProgress(newValue.toMillis()/currentFile.getPlayer().getTotalDuration().toMillis());
            controller.playlistCurrentDuration.setText(String.format("%2d",(int)Math.floor(currentFile.getPlayer().getCurrentTime().toMinutes()))  + ":" + String.format("%02d", (int)Math.floor(currentFile.getPlayer().getCurrentTime().toSeconds()%60)));
            controller.playlistTotalDuration.setText(String.format("%2d",(int)Math.floor(currentFile.getPlayer().getTotalDuration().toMinutes()))  + ":" + String.format("%02d", (int)Math.floor(currentFile.getPlayer().getMediaPlayer().getTotalDuration().toSeconds()%60)));
        };

        currentFile.getPlayer().currentTimeProperty().addListener(playlistFileProgressListener);
    }

    public void jumpTo(PlaylistFile selectedItem, double duration) {
        playListNumber = playlistFiles.indexOf(selectedItem);
        initiateTrack(duration);
    }


    public void start(double duration){
        playListNumber = -1;

        nextTrack(duration);
    }

    public void pausePlay(double duration){
        if(playlistFiles.stream().anyMatch(x->x.getState().equals(States.STATE.PAUSED))){
            play(duration);
        }else if(playlistFiles.stream().anyMatch(x->x.getState().equals(States.STATE.PLAYING))){
            pause(duration);
        }else{
            start(duration);
        }
    }


    public void play(double duration){
        for(PlaylistFile playlistFile : playlistFiles){
            if(playlistFile.getState().equals(PlaylistFile.STATE.PAUSED)) {
                playlistFile.getPlayer().playFaded(controller.playlistVolumeSlider.getValue(), duration);
                playlistFile.setState(PlaylistFile.STATE.PLAYING);
            }
        }
    }

    public void pause(double duration){
        pause(duration, ()->{});
    }

    public void pause(double duration, Runnable onFinished){
        for(PlaylistFile playlistFile : playlistFiles){
            if(playlistFile.getState().equals(PlaylistFile.STATE.PLAYING)) {
                playlistFile.getPlayer().pauseFaded(controller.playlistVolumeSlider.getValue(), duration, onFinished);
                playlistFile.setState(PlaylistFile.STATE.PAUSED);
            }
        }
    }

    public void stop(double duration){
        stop(duration, ()->{});
    }

    public void stop(double duration, Runnable onFinished){
        for(PlaylistFile file : playlistFiles){
            if(file.getState().equals(PlaylistFile.STATE.PLAYING) || file.getState().equals(PlaylistFile.STATE.PAUSED) || file.getProgress().get()>0) {
                progress.bind(file.getProgress());
                file.getPlayer().stopFaded(controller.playlistVolumeSlider.getValue(), duration, onFinished);
                file.setState(PlaylistFile.STATE.STOPPED);
                file.setSelected(false);
                file.getPlayer().currentTimeProperty().removeListener(playlistFileProgressListener);
            }
        }

        playListNumber = -1;

        controller.playlistTotalDuration.setText("0:00");
        controller.playlistCurrentDuration.setText("0:00");
        controller.playlistProgressBar.setProgress(0);
    }


    void nextTrack(double duration){
        if(controller.getPlaylistTable().getItems().isEmpty()) return;

        if(playListNumber>=controller.getPlaylistTable().getItems().size()-1) this.playListNumber=0;
        else {
            this.playListNumber++;
        }

        if (playlistFiles.get(playListNumber).isExcluded()) {
            nextTrack(duration);
            return;
        }

        initiateTrack(duration);
    }

    void prevTrack(double duration){
        if(controller.getPlaylistTable().getItems().isEmpty()) return;

        if(playListNumber<=0) playListNumber = playlistFiles.size()-1;
        else {
            playlistFiles.get(playListNumber).getPlayer().stopFaded(controller.playlistVolumeSlider.getValue(), duration);
            playListNumber--;
        }

        if(playlistFiles.get(playListNumber).isExcluded()) {
            prevTrack(duration);
            return;
        }

        initiateTrack(duration);
    }


    void setDirectory(File selectedDirectory){

        playlistDirectory = selectedDirectory;

        List<File> files = List.of(Objects.requireNonNull(selectedDirectory.listFiles()));
        List<File> musicFiles = files.stream().filter(x -> x.getName().endsWith(".mp3") || x.getName().endsWith(".wav") || x.getName().endsWith(".mpeg")).toList();

        playlistFiles.clear();

        int i = 1;
        for (File musicFile : musicFiles) {
            PlaylistFile newFile = new PlaylistFile(musicFile.getName(), i, false, musicFile.getAbsolutePath(), Paths.get(musicFile.getAbsolutePath()).toUri(), controller.getPlaylistTable(), controller, controller.playlistVolumeSlider);

            newFile.getPlayer().setOnEndOfMedia(()->{
                newFile.getPlayer().getMediaPlayer().stop();
                nextTrack(controller.PLAYLIST_FADE_TIME);
            });

            playlistFiles.add(newFile);
            i++;
        }

        controller.getPlaylistTable().setItems(playlistFiles);
    }



    void shuffle(){

        List<PlaylistFile> items = new ArrayList<>(controller.getPlaylistTable().getItems().stream().toList());
        Collections.shuffle(items);
        int i = 1;
        for (PlaylistFile item :items){
            item.setPlaylistOrder(i);
            i++;
        }

        items.sort(Comparator.comparing(PlaylistFile::getPlaylistOrder));
        playlistFiles.clear();
        playlistFiles.addAll(items);

        controller.getPlaylistTable().setItems(playlistFiles);

        controller.getPlaylistTable().refresh();

        stop(controller.MIN_FADE_TIME);
        controller.getPlaylistTable().scrollTo(0);
    }



}
