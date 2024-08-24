package com.example.showSequencerJavafx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.media.Media;

import java.net.URI;

/**
 * Class to contain media player wrapper and associated variables.
 */
public class PlaylistFile implements States{

    private final TableView<PlaylistFile> tableView;
    private final String fileName;
    private int playlistOrder;
    private boolean excluded;
    private STATE state = STATE.STOPPED;
    private final MediaPlayerWrapper mediaPlayerWrapper;
    private boolean selected = false;

    public final MainController mainController;
    public final Slider slider;


    @Override
    public boolean isSelected(){return selected;}

    /**Setter for playlistFile state
     * @param state State of playlistFile
     */
    public void setState(STATE state) {
        this.state = state;
        tableView.refresh();
    }

    /**Getter for playlistFile state
     * @return State of playlistFile
     */
    public STATE getState() {
        return state;
    }

    /**Getter for fade progress
     * @return Double property containing progress percentage of fade.
     */
    public SimpleDoubleProperty getProgress(){
        return mediaPlayerWrapper.getFadeProgress();
    }

    /**Getter for media file name
     * @return Name of media file
     */
    public String getFileName() {
        return fileName;
    }

    /**Setter for selected
     * @param selected True if selected, false otherwise.
     */
    public void setSelected(boolean selected) {this.selected = selected;}

    /**Setter for the order position of the playlistFile
     * @param playlistOrder Order position of playlistFile
     */
    public void setPlaylistOrder(int playlistOrder) {
        this.playlistOrder = playlistOrder;
    }

    /**Getter for playlistOrder
     * @return Order position of playlistFile
     */
    public int getPlaylistOrder() {
        return playlistOrder;
    }

    /**Getter for excluded
     * @return True if excluded, false otherwise.
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**Setter for excluded
     * @param excluded True if excluded, false otherwise.
     */
    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    /**Getter for media player wrapper
     * @return Media player Wrapper
     */
    public MediaPlayerWrapper getPlayer(){
        return mediaPlayerWrapper;
    }

    /**Constructor for playlistFile
     * @param fileName Name of media file
     * @param playlistOrder Order position of playlistFile
     * @param excluded True if excluded, false otherwise
     * @param uri Path to media file
     * @param tableView Table containing playlistFile
     * @param mainController Main controller for GUI
     * @param slider Slider controlling playlistFile volume
     */
    public PlaylistFile(String fileName, int playlistOrder, boolean excluded, URI uri, TableView<PlaylistFile> tableView, MainController mainController, Slider slider) {
        this.excluded = excluded;
        this.playlistOrder = playlistOrder;
        this.fileName = fileName;
        this.tableView = tableView;
        this.mainController = mainController;
        this.slider = slider;

        Media media = new Media(uri.toString());
        this.mediaPlayerWrapper = new MediaPlayerWrapper(media, mainController, this.slider);

    }

    /**Copy Constructor
     * @param playlistFile PlaylistFile to copy
     */
    public PlaylistFile(PlaylistFile playlistFile){
        this.excluded = playlistFile.excluded;
        this.playlistOrder = playlistFile.playlistOrder;
        this.fileName = playlistFile.fileName;
        this.tableView = playlistFile.tableView;
        this.state = playlistFile.state;
        this.mainController = playlistFile.mainController;
        this.slider = playlistFile.slider;
        this.selected = false;

        this.mediaPlayerWrapper = new MediaPlayerWrapper(playlistFile.mediaPlayerWrapper.getMedia(), mainController, this.slider);

    }


}
