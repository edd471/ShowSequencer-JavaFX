package com.example.showSequencerJavafx;

import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**Class instantiated for each individual cue. Performs action when ran depending on type.*/
public class Cue implements States{

    private final TableView<Cue> tableView;
    private String cueNum;
    private String cueName;
    private final SimpleObjectProperty<PlaylistFile> cueFile = new SimpleObjectProperty<>(null);
    private final MainController.COMMAND cueCommand;
    private double cueAuto;
    private SimpleDoubleProperty cueVol = new SimpleDoubleProperty(0);
    private double cueTime;
    private STATE state = STATE.STOPPED;
    private final MainController mainController;
    private final CuesManager cuesManager;
    private final PlaylistManager playlistManager;
    private final SimpleDoubleProperty progress = new SimpleDoubleProperty(0);
    private boolean selected = false;
    private ArrayList<Double> faderValues = new ArrayList<>();

    /**Constructs Cue Object, Assigns Values and Refreshes Table
     * @param cueNum User entered cue number
     * @param cueName User entered cue name
     * @param cueAuto Time to autoplay the next cue if not null
     * @param cueCommand Type of cue functionality
     * @param cueFile PlaylistFile object to be controlled by this instance
     * @param cueVol Volume of audio playback
     * @param cueTime Time for faders to increase/decrease volume
     * @param tableView Table containing the cue
     * @param mainController Controller class creating the cue*/
    public Cue(String cueNum, String cueName, double cueAuto, MainController.COMMAND cueCommand,
               PlaylistFile cueFile, double cueVol, double cueTime, TableView<Cue> tableView,
               MainController mainController) {
        this.cueNum = cueNum;
        this.cueName = cueName;
        this.cueCommand = cueCommand;
        this.cueAuto = cueAuto;
        this.cueVol.set(cueVol);
        this.cueTime = cueTime;
        this.tableView = tableView;
        this.mainController = mainController;
        this.cuesManager = mainController.getCuesManager();
        this.playlistManager = mainController.getPlaylistManager();

        for(int i=0; i<32; i++){
            faderValues.add(null);
        }

        setCueFile(cueFile);
        mainController.refreshTables();
    }


    /**Copy Constructor for Cue Class
     * @param cue Cue to be copied.*/
    public Cue(Cue cue) {
        this.mainController = cue.mainController;
        this.cuesManager = mainController.getCuesManager();
        this.playlistManager = mainController.getPlaylistManager();

        this.cueNum = cue.cueNum;
        this.cueName = cue.cueName;
        this.cueCommand = cue.cueCommand;
        this.cueAuto = cue.cueAuto;
        this.cueVol = new SimpleDoubleProperty(cue.cueVol.get());
        this.cueTime = cue.cueTime;
        this.tableView = cue.tableView;
        this.setState(STATE.STOPPED);
        this.progress.set(0);
        this.selected = false;
        this.faderValues = new ArrayList<>(cue.faderValues);

        setCueFile(cue.cueFile.get());
        mainController.refreshTables();
    }


    /**Getter for selected
     * @return Selected State that determines GUI selection indicator*/
    public boolean isSelected(){return selected;}


    /**Setter for selected
     * @param selected Selected State that determines GUI selection indicator*/
    public void setSelected(boolean selected){this.selected = selected;}


    /**Getter for fader values
     * @return List of values assigned to faders*/
    public ArrayList<Double> getFaderValues(){
        return faderValues;
    }

    @Override
    public SimpleDoubleProperty getProgress() {
        return progress;
    }

    /**Setter for progress value
     * @param progress Progress value for GUI progress bar*/
    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    /**Getter for cue number
     * @return User entered cue number*/
    public String getCueNum() {
        return cueNum;
    }

    /**Setter for cue number
     * @param cueNum User entered cue number*/
    public void setCueNum(String cueNum) {
        this.cueNum = cueNum;
    }

    /**Getter for cue name
     * @return User entered cue name
     */
    public String getCueName() {
        return cueName;
    }

    /**Getter for cue name
     * @param cueName User entered cue name*/
    public void setCueName(String cueName) {
        this.cueName = cueName;
    }

    /**Getter for cue command
     * @return Type of cue*/
    public MainController.COMMAND getCueCommand() {
        return cueCommand;
    }

    /**Getter for cue file
     * @return PlaylistFile for cue audio*/
    public PlaylistFile getCueFile() {
        return cueFile.get();
    }


    /**Setter for cue file. Creates new PlaylistFile for PLAY cues
     * or references PlaylistFile of PLAY cue of same track name
     * @param cueFile Playlist File to assign to cue*/
    public void setCueFile(PlaylistFile cueFile) {
        stop();
        if(cueFile==null) {
            this.cueFile.set(null);
            return;
        }

        if(getCueCommand().equals(MainController.COMMAND.PLAY)){
            this.cueFile.set(new PlaylistFile(cueFile));
            this.getCueFile().getPlayer().setVolume(getCueVol().get() / 100);
        }else{
            //Find last PLAY cue that matches the PlaylistFile name previous to current cue
            ObservableList<Cue> revCues = FXCollections.observableArrayList(cuesManager.getCues());
            Collections.reverse(revCues);

            Cue cue = revCues.stream()
                        .filter(c->c.getCueFile()!=null && c.getCueFile().getFileName().equals(cueFile.getFileName()) &&
                                   c.getCueCommand().equals(MainController.COMMAND.PLAY) &&
                                   cuesManager.getCues().indexOf(c)< cuesManager.getCues().indexOf(this)).findFirst().orElse(null);

            if(cue==null) {
                this.cueFile.set(null);
            } else{
                this.cueFile.set(cue.cueFile.get());

                //Listener to the "linked" PLAY cue to remove this PlaylistFile if the
                //associated PlaylistFile is changed
                cue.cueFile.addListener((obs, oldValue, newValue) -> {
                    if(oldValue.equals(this.getCueFile())) {this.cueFile.set(null); mainController.refreshTables();}
                });
            }
        }
    }


    /**Getter for cueAuto
     * @return Value of time for autoplay of next cue*/
    public double getCueAuto() {
        return cueAuto;
    }

    /**Setter for cueAuto
     * @param cueAuto Value of time for autoplay of next cue*/
    public void setCueAuto(double cueAuto) {
        this.cueAuto = cueAuto;
    }

    /**Getter for cueVol
     * @return Volume for audio playback*/
    public SimpleDoubleProperty getCueVol() {
        return cueVol;
    }

    /**Setter for cueVol. Sets the PlaylistFile player volume according to
     * provided value and GUI volume slider
     * @param cueVol Volume for audio playback*/
    public void setCueVol(double cueVol) {
        this.cueVol.set(cueVol);
        if(getCueFile()!=null){
            this.getCueFile().getPlayer().setVolume((mainController.cueListVolumeSlider.getValue()/100)*(getCueVol().get()/100));
        }
    }

    /**Getter for cueTime
     * @return Time for faders to increase/decrease volume*/
    public double getCueTime() {
        return cueTime;
    }

    /**Setter for cueTime
     * @param cueTime Time for faders to increase/decrease volume*/
    public void setCueTime(double cueTime) {
        this.cueTime = cueTime;
    }

    /**Setter for cue State
     * @param state Playing state of cue*/
    public void setState(STATE state) {
        this.state = state;
        mainController.refreshTables();
        mainController.refreshRunScreen();
    }

    /**Getter for cue state
     * @return Playing state of cue*/
    public STATE getState(){
        return state;
    }

    /**Executes when cue is reached from above based on type of cue and params of cue
     * @param withAuto False ignores the autoplay feature
     */
    public void run(boolean withAuto){

        if(cueAuto>=0 && withAuto){  //Run next cue after delay
            PauseTransition autoDelay = new PauseTransition(Duration.seconds(Math.max(cueAuto, 0.01)));
            autoDelay.setOnFinished(event -> mainController.cueListNext());
            autoDelay.play();
        }

        if(cuesManager.getCurrentCueNum()>0){  //Set previous cue state to STOPPED if not PLAY cue
            Cue prevCue = cuesManager.getCues().get(cuesManager.getCurrentCueNum()-1);
            if(!prevCue.getCueCommand().equals(MainController.COMMAND.PLAY)) {
                prevCue.setState(STATE.STOPPED);
            }
        }

        switch (this.getCueCommand()){
            case NONE:{
                //Do nothing for NONE Command
                break;
            }
            case PLAY:{
                //Set end of media to stop playlistFile and set State
                //Bind the progress of playlistFile fade progress to cue fade progress
                //Play the playlistFile

                if(getCueFile()==null) return;

                getCueFile().getPlayer().setOnEndOfMedia(()->{
                    setState(STATE.STOPPED);
                    getCueFile().getPlayer().stopFaded(cueVol.get(), mainController.MIN_FADE_TIME, ()->getCueFile().getPlayer().getMediaPlayer().stop());
                });

                progress.bind(getCueFile().getPlayer().getFadeProgress());

                getCueFile().getPlayer().playFaded(cueVol.get(), Math.max(cueTime, mainController.MIN_FADE_TIME));

                break;
            }
            case STOP:{
                //Fade the PLAY cue with the same playlistFile as this cue if it is still playing

                List<Cue> prevCues = cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum());
                for(Cue cue : prevCues){
                    if(cue.getCueFile()!=null && cue.getCueFile().equals(getCueFile()) &&
                       (cue.getState().equals(STATE.PLAYING) || cue.getProgress().get()>0)) {
                        cue.fade(cueTime);
                    }
                }

                break;
            }
            case VOLUME: {
                //Create fade with start and end volumes and time to fade
                //Create listener to change the GUI volume slider
                //Create listener to bind fade progress to cue progress
                //Play the fade

                ExponentialFade fade = new ExponentialFade(cueTime,
                                                            mainController.cueListVolumeSlider.getValue(),
                                                            cueVol.get(), "||CUELIST||", mainController);

                ChangeListener<Number> listener = (Obs, oldValue, newValue)-> mainController.cueListVolumeSlider.setValue((Double) newValue);
                fade.newVol.addListener(listener);

                ChangeListener<Number> progressListener = (Obs, oldValue, newValue)-> {setProgress((Double) newValue); mainController.refreshTables();};
                fade.progress.addListener(progressListener);

                fade.setOnFinished(()->{fade.newVol.removeListener(listener); fade.progress.set(0); fade.progress.removeListener(progressListener);});

                fade.timeline.play();

                break;

            }
            case STOP_ALL: {
                cuesManager.stop(cueTime);
                break;
            }
            case PLAYLIST_START: {
                //If the playlist is still fading, fade it and run again once fade is finished
                //Otherwise, shuffle, bind progress, enable control panel, set volume and play playlist
                if(playlistManager.progress.get()>0) playlistManager.stop(mainController.MIN_FADE_TIME, ()->run(false));
                else{
                    playlistManager.shuffle();
                    progress.bind(playlistManager.progress);
                    mainController.setPlaylistControlPanelDisabled(false);
                    playlistManager.setVolume(cueVol.get());
                    playlistManager.nextTrack(cueTime);
                }
                break;
            }
            case PLAYLIST_CONT: {
                //Bind progress, enable control panel, set volume and play playlist
                progress.bind(playlistManager.progress);
                mainController.setPlaylistControlPanelDisabled(false);
                playlistManager.setVolume(cueVol.get());
                playlistManager.nextTrack(cueTime);
                break;
            }
            case PLAYLIST_FADE: {
                //Disable control panel and pause the playlist, resetting the volume once faded
                mainController.setPlaylistControlPanelDisabled(true);
                playlistManager.pause(cueTime, playlistManager::resetVolume);
                break;
            }
        }
        this.setState(STATE.PLAYING);
    }

    /**Executes when cue is backtracked based on cue type and params*/
    public void backTrack(){

        switch (this.getCueCommand()){
            case NONE:{
                //Do nothing for type NONE
                break;
            }
            case PLAY:{
                //Stop the playlistFile
                if(getCueFile()!=null){
                    getCueFile().getPlayer().stopFaded(cueVol.get(), cueTime);
                }
                break;
            }
            case STOP:{
                //Run the PLAY cue of the same playlistFile
                for(Cue cue : cuesManager.getCues()){
                    if(cue.getCueFile()!=null && cue.getCueFile().equals(getCueFile()) && cue!=this &&
                            !cue.getCueFile().getPlayer().getMediaPlayer().getStatus().equals(MediaPlayer.Status.STOPPED)){

                        cue.run(false);
                    }
                }
                break;
            }
            case VOLUME: {
                //Find the most recent VOLUME cue value or default to the initial volume
                //Create fade from current volume to most recent volume

                List<Cue> prevCues = cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum());
                List<Cue> volumeCues = new ArrayList<>();
                for(Cue cue : prevCues){
                    if(cue.getCueCommand().equals(MainController.COMMAND.VOLUME)){
                        volumeCues.add(cue);
                    }
                }

                double targetVol = cuesManager.getInitialCueVolume();

                if (!volumeCues.isEmpty()) {
                    targetVol = volumeCues.get(volumeCues.size()-1).cueVol.get();
                }

                ExponentialFade fade = new ExponentialFade(cueTime, mainController.cueListVolumeSlider.getValue(),
                                                            targetVol, "||CUELIST||", mainController);

                ChangeListener<Number> listener = (Obs, oldValue, newValue)-> mainController.cueListVolumeSlider.setValue((Double) newValue);
                fade.newVol.addListener(listener);

                ChangeListener<Number> progressListener = (Obs, oldValue, newValue)-> {setProgress((Double) newValue); mainController.refreshTables();};
                fade.progress.addListener(progressListener);

                fade.setOnFinished(() -> {fade.newVol.removeListener(listener);  progress.set(0); fade.progress.removeListener(progressListener);});

                fade.timeline.play();


                break;
            }
            case STOP_ALL: {
                //Find and run cues that should be resumed

                List<Cue> prevCues = new ArrayList<>(List.copyOf(cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum())));
                List<Cue> cuesToResume = new ArrayList<>();
                for(Cue cue : prevCues){
                    if(cue.getCueFile()!=null && cue.getCueCommand().equals(MainController.COMMAND.PLAY)
                            && !cue.getCueFile().getPlayer().getMediaPlayer().getStatus().equals(MediaPlayer.Status.STOPPED)) {
                        cuesToResume.add(cue);
                    }else if(cue.getCueFile()!=null && cue.getCueCommand().equals(MainController.COMMAND.STOP)) {
                        cuesToResume.stream().filter(x -> x.getCueFile().equals(cue.getCueFile())).findFirst().ifPresent(cuesToResume::remove);
                    }else if(cue.getCueCommand().equals(MainController.COMMAND.STOP_ALL)){
                        cuesToResume.clear();
                    }
                }

                cuesToResume.forEach(cue->cue.run(false));

                break;
            }
            case PLAYLIST_START: {
                //Disable control panel, stop playlist

                mainController.setPlaylistControlPanelDisabled(true);
                playlistManager.stop(Math.max(cueTime, mainController.MIN_FADE_TIME), playlistManager::resetVolume);
                break;
            }
            case PLAYLIST_CONT: {
                //Disable control panel, pause playlist

                mainController.setPlaylistControlPanelDisabled(true);
                playlistManager.pause(Math.max(cueTime, mainController.MIN_FADE_TIME), playlistManager::resetVolume);
                break;
            }
            case PLAYLIST_FADE: {
                //Find previous cue that is PLAYLIST_START or PLAYLIST_CONT
                //Play the playlist using volume of previous cue

                List<Cue> prevCues = new ArrayList<>(List.copyOf(cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum())));
                List<Cue> playlistCues = new ArrayList<>();
                Cue prevPlaylistCue = null;

                for(Cue cue : prevCues){
                    if(cue.getCueCommand().equals(MainController.COMMAND.PLAYLIST_CONT) || cue.getCueCommand().equals(MainController.COMMAND.PLAYLIST_START)){
                        playlistCues.add(cue);
                    }
                }

                if(!playlistCues.isEmpty()){
                    prevPlaylistCue = playlistCues.get(playlistCues.size()-1);
                }

                mainController.setPlaylistControlPanelDisabled(false);
                if(cuesManager.getCurrentCueNum()>0){
                    Cue prevCue = cuesManager.getCues().get(cuesManager.getCurrentCueNum()-1);
                    if(!prevCue.getCueCommand().equals(MainController.COMMAND.PLAYLIST_START)
                            && !prevCue.getCueCommand().equals(MainController.COMMAND.PLAYLIST_CONT) && prevPlaylistCue!=null){
                        playlistManager.setVolume(prevPlaylistCue.cueVol.get());
                        playlistManager.play(Math.max(cueTime, mainController.MIN_FADE_TIME));
                    }
                }
                break;
            }
        }
        this.setState(STATE.STOPPED);
    }

    /**Executes when cue is reached from below based on type of cue and params of cue*/
    public void replay(){

        switch (this.getCueCommand()) {
            case NONE, STOP, VOLUME, STOP_ALL, PLAYLIST_CONT, PLAYLIST_FADE: {
                //Implementation unchanged from Run function
                run(false);
                break;
            }
            case PLAY: {
                //Stop the playlistFile and run cue once fade is complete
                if(getCueFile()!=null){
                    getCueFile().getPlayer().stopFaded(cueVol.get(), 0, ()->run(false));
                }
                break;
            }
            case PLAYLIST_START: {
                //Stop the playlist and run the cue once fade is complete
                playlistManager.stop(mainController.MIN_FADE_TIME, ()->run(false));
                break;
            }
        }
    }

    /**Fades the audio of cue and pauses player
     * @param duration Time to fade*/
    public void fade(Double duration){
        if(getCueFile()!=null && getCueCommand().equals(MainController.COMMAND.PLAY) &&
           getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.PLAYING)) {

            getCueFile().getPlayer().pauseFaded(cueVol.get(), duration);
        }
        setState(STATE.STOPPED);
    }


    /**Stops the cue and stops the player*/
    public void stop(){
        if(getCueFile()!=null && getCueCommand().equals(MainController.COMMAND.PLAY) &&
           !getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.STOPPED)) {

            getCueFile().getPlayer().stopFaded(cueVol.get(), mainController.MIN_FADE_TIME);
        }
        setState(STATE.STOPPED);
    }

    /**Pause/Play the cue based on current state
     * @param duration Time to fade*/
    public void pausePlay(double duration){
        if(getCueFile()==null) return;
        if(getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.PLAYING)) {
            getCueFile().getPlayer().pauseFaded(cueVol.get(), duration);
        }else if(getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.PAUSED)){
            getCueFile().getPlayer().playFaded(cueVol.get(), duration);
        }
    }


}
