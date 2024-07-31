package com.example.showSequencerJavafx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.media.Media;

import java.net.URI;

public class PlaylistFile implements States{


    private final TableView<PlaylistFile> tableView;
    private final String fileName;
    private int playlistOrder;
    private boolean excluded;
    private STATE state = STATE.STOPPED;
    private final MediaPlayerWrapper mediaPlayerWrapper;
    public final MainController mainController;
    public final Slider slider;
    private boolean selected = false;
    public STATE getState() {
        return state;
    }

    public boolean isSelected(){return selected;}

    public void setState(STATE state) {
        this.state = state;
        tableView.refresh();
    }

    private final String filePath;

    public SimpleDoubleProperty getProgress(){
        return mediaPlayerWrapper.getFadeProgress();
    }

    public String getFileName() {
        return fileName;
    }

    public void setSelected(boolean selected) {this.selected = selected;}

    public void setPlaylistOrder(int playlistOrder) {
        this.playlistOrder = playlistOrder;
    }

    public int getPlaylistOrder() {
        return playlistOrder;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public MediaPlayerWrapper getPlayer(){
        return mediaPlayerWrapper;
    }

    public PlaylistFile(String fileName, int playlistOrder, boolean excluded, String filePath, URI uri, TableView<PlaylistFile> tableView, MainController mainController, Slider slider) {
        this.excluded = excluded;
        this.playlistOrder = playlistOrder;
        this.fileName = fileName;
        this.filePath = filePath;
        this.tableView = tableView;
        this.mainController = mainController;
        this.slider = slider;

        Media media = new Media(uri.toString());
        this.mediaPlayerWrapper = new MediaPlayerWrapper(media, mainController, this.slider);

    }

    public PlaylistFile(PlaylistFile playlistFile){
        this.excluded = playlistFile.excluded;
        this.playlistOrder = playlistFile.playlistOrder;
        this.fileName = playlistFile.fileName;
        this.filePath = playlistFile.filePath;
        this.tableView = playlistFile.tableView;
        this.state = playlistFile.state;
        this.mainController = playlistFile.mainController;
        this.slider = playlistFile.slider;
        this.selected = false;

        this.mediaPlayerWrapper = new MediaPlayerWrapper(playlistFile.mediaPlayerWrapper.getMedia(), mainController, this.slider);

    }


}
