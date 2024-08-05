package com.example.showSequencerJavafx;

import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TableView;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;


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


    public Cue(String cueNum, String cueName, double cueAuto, MainController.COMMAND cueCommand, PlaylistFile cueFile, double cueVol, double cueTime, TableView<Cue> tableView, MainController mainController) {
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

    public boolean isSelected(){return selected;}

    public void setSelected(boolean selected){this.selected = selected;}


    public ArrayList<Double> getFaderValues(){
        return faderValues;
    }

    public String getCueNum() {
        return cueNum;
    }

    @Override
    public SimpleDoubleProperty getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public void setCueNum(String cueNum) {
        this.cueNum = cueNum;
    }

    public String getCueName() {
        return cueName;
    }

    public void setCueName(String cueName) {
        this.cueName = cueName;
    }

    public MainController.COMMAND getCueCommand() {
        return cueCommand;
    }

    public PlaylistFile getCueFile() {
        return cueFile.get();
    }

    public void setCueFile(PlaylistFile cueFile) {
        stop();
        if(cueFile==null) {
            this.cueFile.set(null);
        }
        else{
            if(getCueCommand().equals(MainController.COMMAND.PLAY)){
                this.cueFile.set(new PlaylistFile(cueFile));
                this.getCueFile().getPlayer().setVolume(getCueVol().get() / 100);


            }else{
                for(Cue cue : cuesManager.getCues()){
                    if(cue.getCueFile()!=null && cue.getCueFile().getFileName().equals(cueFile.getFileName()) && cue.getCueCommand().equals(MainController.COMMAND.PLAY) && cuesManager.getCues().indexOf(cue)< cuesManager.getCues().indexOf(this)){
                        this.cueFile.set(cue.cueFile.get());

                        ChangeListener<PlaylistFile> listener = (obs, oldValue, newValue) -> {if(oldValue.equals(this.getCueFile())) {this.cueFile.set(null); mainController.refreshTables();}};
                        cue.cueFile.addListener(listener);

                    }
                }
            }
        }
    }

    public double getCueAuto() {
        return cueAuto;
    }

    public void setCueAuto(double cueAuto) {
        this.cueAuto = cueAuto;
    }

    public SimpleDoubleProperty getCueVol() {
        return cueVol;
    }

    public void setCueVol(double cueVol) {
        this.cueVol.set(cueVol);
        if(getCueFile()!=null){
            this.getCueFile().getPlayer().setVolume((mainController.cueListVolumeSlider.getValue()/100)*(getCueVol().get()/100));
        }
    }

    public double getCueTime() {
        return cueTime;
    }

    public void setCueTime(double cueTime) {
        this.cueTime = cueTime;
    }

    public void setState(STATE state) {
        this.state = state;
        mainController.refreshTables();
        mainController.refreshRunScreen();
    }

    public STATE getState(){
        return state;
    }

    public void run(){
        run(true);
    }

    public void run(boolean withAuto){



        if(cueAuto>=0 && withAuto){
            PauseTransition autoDelay = new PauseTransition(Duration.seconds(Math.max(cueAuto, 0.01)));
            autoDelay.setOnFinished(event -> mainController.cueListNext());
            autoDelay.play();
        }

        if(cuesManager.getCurrentCueNum()>0){
            Cue prevCue = cuesManager.getCues().get(cuesManager.getCurrentCueNum()-1);
            if(!prevCue.getCueCommand().equals(MainController.COMMAND.PLAY)) {
                prevCue.setState(STATE.STOPPED);
            }
        }

        switch (this.getCueCommand()){
            case NONE:{
                break;
            }
            case PLAY:{

                if(getCueFile()==null) return;

                getCueFile().getPlayer().setOnEndOfMedia(()->{
                    setState(STATE.STOPPED);
                    getCueFile().getPlayer().stopFaded(cueVol.get(), mainController.MIN_FADE_TIME, ()->getCueFile().getPlayer().getMediaPlayer().stop());
                });

                progress.bind(getCueFile().getPlayer().fadeProgressProperty());

                getCueFile().getPlayer().playFaded(cueVol.get(), Math.max(cueTime, mainController.MIN_FADE_TIME));

                break;
            }
            case STOP:{
                List<Cue> prevCues = cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum());
                for(Cue cue : prevCues){
                    if(cue.getCueFile()!=null && cue.getCueFile().equals(getCueFile()) && (cue.getState().equals(STATE.PLAYING) || cue.getProgress().get()>0)) {
                        cue.fade(cueTime);
                    }
                }

                break;
            }
            case VOLUME: {

                double prevVol = mainController.cueListVolumeSlider.getValue();

                ExponentialFade fade = new ExponentialFade(false, cueTime, prevVol, cueVol.get(), "||CUELIST||", mainController);

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
                if(playlistManager.progress.get()>0) playlistManager.stop(mainController.MIN_FADE_TIME, ()->run(false));
                else{
                    playlistManager.shuffle();
                    progress.bind(playlistManager.progress);
                    mainController.setPlaylistControlPanelDisabled(false);
                    playlistManager.setVolume(cueVol.get());
                    playlistManager.start(cueTime);
                }
                break;
            }
            case PLAYLIST_CONT: {
                progress.bind(playlistManager.progress);
                mainController.setPlaylistControlPanelDisabled(false);
                playlistManager.setVolume(cueVol.get());
                playlistManager.nextTrack(cueTime);
                break;
            }
            case PLAYLIST_FADE: {
                mainController.setPlaylistControlPanelDisabled(true);
                playlistManager.pause(cueTime, playlistManager::resetVolume);
                break;
            }
        }
        this.setState(STATE.PLAYING);
    }

    public void backTrack(){

        switch (this.getCueCommand()){
            case NONE:{
                break;
            }
            case PLAY:{
                if(getCueFile()!=null){
                    getCueFile().getPlayer().stopFaded(cueVol.get(), cueTime);
                }
                break;
            }
            case STOP:{

                for(Cue cue : cuesManager.getCues()){
                    if(cue.getCueFile()!=null && cue.getCueFile().equals(getCueFile()) && cue!=this && !cue.getCueFile().getPlayer().getMediaPlayer().getStatus().equals(MediaPlayer.Status.STOPPED)){
                        cue.run(false);
                    }
                }

                break;

            }
            case VOLUME: {
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

                ExponentialFade fade = new ExponentialFade(false, cueTime, mainController.cueListVolumeSlider.getValue(), targetVol, "||CUELIST||", mainController);

                ChangeListener<Number> listener = (Obs, oldValue, newValue)-> mainController.cueListVolumeSlider.setValue((Double) newValue);
                fade.newVol.addListener(listener);

                ChangeListener<Number> progressListener = (Obs, oldValue, newValue)-> {setProgress((Double) newValue); mainController.refreshTables();};
                fade.progress.addListener(progressListener);

                fade.setOnFinished(() -> {fade.newVol.removeListener(listener);  progress.set(0); fade.progress.removeListener(progressListener);});

                fade.timeline.play();


                break;
            }
            case STOP_ALL: {

                List<Cue> prevCues = new ArrayList<>(List.copyOf(cuesManager.getCues().subList(0, cuesManager.getCurrentCueNum())));
                List<Cue> cuesToResume = new ArrayList<>();
                for(Cue cue : prevCues){
                    if(cue.getCueFile()!=null && cue.getCueCommand().equals(MainController.COMMAND.PLAY) && !cue.getCueFile().getPlayer().getMediaPlayer().getStatus().equals(MediaPlayer.Status.STOPPED)) {
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
                mainController.setPlaylistControlPanelDisabled(true);
                playlistManager.stop(Math.max(cueTime, mainController.MIN_FADE_TIME));
                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(mainController.MIN_FADE_TIME + 0.05), event -> playlistManager.resetVolume()));
                delay.play();
                break;
            }
            case PLAYLIST_CONT: {
                mainController.setPlaylistControlPanelDisabled(true);
                playlistManager.pause(Math.max(cueTime, mainController.MIN_FADE_TIME));
                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(mainController.MIN_FADE_TIME + 0.05), event -> playlistManager.resetVolume()));
                delay.play();
                break;
            }
            case PLAYLIST_FADE: {
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
                    if(!prevCue.getCueCommand().equals(MainController.COMMAND.PLAYLIST_START) && !prevCue.getCueCommand().equals(MainController.COMMAND.PLAYLIST_CONT) && prevPlaylistCue!=null){
                        playlistManager.setVolume(prevPlaylistCue.cueVol.get());
                        playlistManager.play(Math.max(cueTime, mainController.MIN_FADE_TIME));
                    }
                }
                break;
            }
        }
        this.setState(STATE.STOPPED);
    }

    public void replay(){

        switch (this.getCueCommand()) {
            case NONE, STOP, VOLUME, STOP_ALL, PLAYLIST_CONT, PLAYLIST_FADE: {
                run(false);
                break;
            }
            case PLAY: {
                if(getCueFile()!=null){
                    getCueFile().getPlayer().stopFaded(cueVol.get(), 0, ()->run(false));
                }
                break;
            }
            case PLAYLIST_START: {
                playlistManager.stop(mainController.MIN_FADE_TIME, ()->run(false));
                break;
            }
        }
    }

    public void fade(Double duration){
        if(getCueFile()!=null && getCueCommand().equals(MainController.COMMAND.PLAY) && getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.PLAYING)){
            getCueFile().getPlayer().pauseFaded(cueVol.get(), duration);
        }
        setState(STATE.STOPPED);
    }


    public void stop(){
        if(getCueFile()!=null && getCueCommand().equals(MainController.COMMAND.PLAY) && !getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.STOPPED)) {
            getCueFile().getPlayer().stopFaded(cueVol.get(), mainController.MIN_FADE_TIME);
        }
        setState(STATE.STOPPED);
    }

    public void pausePlay(double duration){
        if(getCueFile()==null) return;
        if(getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.PLAYING)) {
            getCueFile().getPlayer().pauseFaded(cueVol.get(), duration);
        }else if(getCueFile().getPlayer().getStatus().equals(MediaPlayer.Status.PAUSED)){
            getCueFile().getPlayer().playFaded(cueVol.get(), duration);
        }
    }


}
