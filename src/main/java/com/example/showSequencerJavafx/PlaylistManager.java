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
    private final MainController mainController;
    public SimpleDoubleProperty progress = new SimpleDoubleProperty(0);

    public PlaylistManager(MainController mainController){
        this.mainController = mainController;
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
        if(initVolume<0) initVolume = mainController.playlistVolumeSlider.getValue();
        mainController.playlistVolumeSlider.setValue(volume);
    }

    public void resetVolume() {
        if(initVolume>=0) {
            mainController.playlistVolumeSlider.setValue(initVolume);
            initVolume = -1;
        }
    }

    public void seekStart() {
        for(PlaylistFile playlistFile : playlistFiles){
            if(playlistFile.getPlayer().getStatus().equals(MediaPlayer.Status.PLAYING)){
                playlistFile.getPlayer().pauseFaded(mainController.playlistVolumeSlider.getValue(), mainController.MIN_FADE_TIME);
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
                playlistFile.getPlayer().playFaded(mainController.playlistVolumeSlider.getValue(), mainController.MIN_FADE_TIME);
            }
        }
    }

    private void initiateTrack(double duration){
        mainController.assertValidFiles();

        for(PlaylistFile file : playlistFiles){
            file.setSelected(false);
            if(file.getState().equals(PlaylistFile.STATE.PLAYING) || file.getState().equals(PlaylistFile.STATE.PAUSED)) {
                file.getPlayer().stopFaded(mainController.playlistVolumeSlider.getValue() ,duration);
                file.setState(PlaylistFile.STATE.STOPPED);
                file.getPlayer().currentTimeProperty().removeListener(playlistFileProgressListener);
            }
        }

        PlaylistFile currentFile = playlistFiles.get(playListNumber);

        currentFile.getPlayer().playFaded(mainController.playlistVolumeSlider.getValue(), duration);
        progress.bind(currentFile.getProgress());
        currentFile.setState(PlaylistFile.STATE.PLAYING);
        currentFile.setSelected(true);

        playlistFileProgressListener = (Observable, oldValue, newValue) -> {
            mainController.playlistProgressBar.setProgress(newValue.toMillis()/currentFile.getPlayer().getTotalDuration().toMillis());
            mainController.playlistCurrentDuration.setText(String.format("%2d",(int)Math.floor(currentFile.getPlayer().getCurrentTime().toMinutes()))  + ":" + String.format("%02d", (int)Math.floor(currentFile.getPlayer().getCurrentTime().toSeconds()%60)));
            mainController.playlistTotalDuration.setText(String.format("%2d",(int)Math.floor(currentFile.getPlayer().getTotalDuration().toMinutes()))  + ":" + String.format("%02d", (int)Math.floor(currentFile.getPlayer().getMediaPlayer().getTotalDuration().toSeconds()%60)));
        };

        currentFile.getPlayer().currentTimeProperty().addListener(playlistFileProgressListener);
    }

    public void jumpTo(PlaylistFile selectedItem, double duration) {
        playListNumber = playlistFiles.indexOf(selectedItem);
        initiateTrack(duration);
    }


    public void pausePlay(double duration){
        if(playlistFiles.stream().anyMatch(x->x.getState().equals(States.STATE.PLAYING))){
            pause(duration);
        }else if(playlistFiles.stream().anyMatch(x->x.getState().equals(States.STATE.PAUSED))){
            play(duration);
        }else if(playListNumber>=0){
            initiateTrack(duration);
        }else{
            nextTrack(duration);
        }
    }


    public void play(double duration){
        for(PlaylistFile playlistFile : playlistFiles){
            if(playlistFile.getState().equals(PlaylistFile.STATE.PAUSED)) {
                playlistFile.getPlayer().playFaded(mainController.playlistVolumeSlider.getValue(), duration);
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
                playlistFile.getPlayer().pauseFaded(mainController.playlistVolumeSlider.getValue(), duration, onFinished);
                playlistFile.setState(PlaylistFile.STATE.PAUSED);
            }
        }
    }

    public void stop(double duration){
        stop(duration, ()->{});
    }

    public void stop(double duration, Runnable onFinished){
        if(playlistFiles.stream().noneMatch(x->x.getState().equals(States.STATE.PLAYING)||x.getState().equals(States.STATE.PAUSED))) return;

        for(PlaylistFile file : playlistFiles){
            if(file.getState().equals(PlaylistFile.STATE.PLAYING) || file.getState().equals(PlaylistFile.STATE.PAUSED) || file.getProgress().get()>0) {
                progress.bind(file.getProgress());
                file.getPlayer().stopFaded(mainController.playlistVolumeSlider.getValue(), duration, onFinished);
                file.setState(PlaylistFile.STATE.STOPPED);
                file.setSelected(false);
                file.getPlayer().currentTimeProperty().removeListener(playlistFileProgressListener);
            }
        }

        playListNumber++;
        playlistFiles.get(playListNumber).setSelected(true);

        mainController.playlistTotalDuration.setText("0:00");
        mainController.playlistCurrentDuration.setText("0:00");
        mainController.playlistProgressBar.setProgress(0);
    }


    void nextTrack(double duration){
        if(mainController.getPlaylistTable().getItems().isEmpty()) return;

        if(playListNumber>= mainController.getPlaylistTable().getItems().size()-1) this.playListNumber=0;
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
        if(mainController.getPlaylistTable().getItems().isEmpty()) return;

        if(playListNumber<=0) playListNumber = playlistFiles.size()-1;
        else {
            playlistFiles.get(playListNumber).getPlayer().stopFaded(mainController.playlistVolumeSlider.getValue(), duration);
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
            PlaylistFile newFile = new PlaylistFile(musicFile.getName(), i, false, musicFile.getAbsolutePath(), Paths.get(musicFile.getAbsolutePath()).toUri(), mainController.getPlaylistTable(), mainController, mainController.playlistVolumeSlider);

            newFile.getPlayer().setOnEndOfMedia(()->{
                newFile.getPlayer().getMediaPlayer().stop();
                nextTrack(mainController.PLAYLIST_FADE_TIME);
            });

            playlistFiles.add(newFile);
            i++;
        }

        mainController.getPlaylistTable().setItems(playlistFiles);
    }



    void shuffle(){

        List<PlaylistFile> items = new ArrayList<>(mainController.getPlaylistTable().getItems().stream().toList());
        Collections.shuffle(items);
        int i = 1;
        for (PlaylistFile item :items){
            item.setPlaylistOrder(i);
            i++;
        }

        items.sort(Comparator.comparing(PlaylistFile::getPlaylistOrder));
        playlistFiles.clear();
        playlistFiles.addAll(items);

        mainController.getPlaylistTable().setItems(playlistFiles);

        mainController.getPlaylistTable().refresh();

        stop(mainController.MIN_FADE_TIME);
        mainController.getPlaylistTable().scrollTo(0);
    }



}
