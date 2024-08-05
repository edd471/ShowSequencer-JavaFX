package com.example.showSequencerJavafx;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CuesManager {


    private int currentCueNum;
    private final ObservableList<Cue> cues = FXCollections.observableArrayList();
    private final ArrayList<Cue> cueClipboard = new ArrayList<>();
    private final ObservableList<PlaylistFile> SFXFiles = FXCollections.observableArrayList();
    private File sfxDirectory;
    private double initialCueVolume;
    private final MainController mainController;
    private final PlaylistManager playlistManager;
    private final FaderManager faderManager;



    public CuesManager(MainController mainController){
        this.mainController = mainController;
        this.playlistManager = mainController.getPlaylistManager();
        this.faderManager = mainController.getFaderManager();

        this.currentCueNum = -1;
        this.cues.clear();
        this.cueClipboard.clear();
        this.SFXFiles.clear();
        this.sfxDirectory = null;
        this.initialCueVolume = -1;
    }


    public int getCurrentCueNum(){
        return currentCueNum;
    }

    public void setCurrentCueVolume(double i) {
        for(Cue cue : cues){
            if(cue.getCueFile()!=null && cue.getCueCommand().equals(MainController.COMMAND.PLAY) && cue.getCueFile().getPlayer().getFadeProgress().get()==1){
                cue.getCueFile().getPlayer().setVolume((cue.getCueVol().get()/100)*(i/100));
            }
        }
    }

    public double getInitialCueVolume() {
        return initialCueVolume;
    }

    public void setInitialCueVolume(double i) {
        initialCueVolume = i;
    }

    public ObservableList<PlaylistFile> getSFXFiles() {
        return SFXFiles;
    }

    public ArrayList<Cue> getCueClipboard() {
        return cueClipboard;
    }

    public ObservableList<Cue> getCues() {
        return cues;
    }

    public void setSFXDirectory(File selectedDirectory){
        sfxDirectory = selectedDirectory;

        List<File> files = List.of(Objects.requireNonNull(selectedDirectory.listFiles()));
        List<File> musicFiles = files.stream().filter(x -> x.getName().endsWith(".mp3") || x.getName().endsWith(".wav") || x.getName().endsWith(".mpeg")).toList();

        SFXFiles.clear();
        int i = 1;
        for (File musicFile : musicFiles) {
            PlaylistFile newFile = new PlaylistFile(musicFile.getName(), i, false, musicFile.getAbsolutePath(), Paths.get(musicFile.getAbsolutePath()).toUri(), mainController.getPlaylistTable(), mainController, mainController.cueListVolumeSlider);
            SFXFiles.add(newFile);
            i++;
        }
    }

    public File getSFXDirectory() {
        return sfxDirectory;
    }

    public void pasteCue(int selectedIndex) {
        getCues().set(selectedIndex, new Cue(cueClipboard.get(0)));
    }

    public void pasteCueAsNew(int selectionStart) {
        int currentSelection = selectionStart;
        for (Cue cue : cueClipboard){
            cues.add(currentSelection, new Cue(cue));
            currentSelection++;
        }
    }

    public void copyCues(ObservableList<Cue> selectedItems) {
        cueClipboard.clear();
        for(Cue cue : selectedItems){
            getCueClipboard().add(new Cue(cue));
        }
    }

    public void addCue() {

        Cue newCue = new Cue("0.0", "", -1, MainController.COMMAND.NONE, null, 75, 0, mainController.getCueAudioTable(), mainController);
        if(mainController.getCueAudioTable().getSelectionModel().getSelectedIndex()>=0){
            cues.add(mainController.getCueAudioTable().getSelectionModel().getSelectedIndex()+1,newCue);
        } else {
            cues.add(newCue);
        }
    }

    public void removeCue(List<Cue> selectedCues) {
        for (Cue cue : selectedCues) {
            cue.stop();
            cues.remove(cue);
        }
    }

    public void next() {
        if(currentCueNum<cues.size()-1){
            if(currentCueNum ==-1) setInitialCueVolume(mainController.cueListVolumeSlider.getValue());

            if(currentCueNum >=0){cues.get(currentCueNum).setSelected(false);}

            currentCueNum++;

            if(currentCueNum>=0) {
                cues.get(currentCueNum).setSelected(true);
                cues.get(currentCueNum).run();
                faderManager.runFaders(cues.get(currentCueNum).getFaderValues());
            }

        }
    }

    public void previous() {
        if(currentCueNum >0){
            cues.get(currentCueNum).setSelected(false);
            cues.get(currentCueNum).backTrack();
            currentCueNum--;

            cues.get(currentCueNum).setSelected(true);
            cues.get(currentCueNum).replay();
            faderManager.runFaders(getBacktrackFaderDb(currentCueNum));

        }else{
            mainController.cueListReset();
        }
    }

    public ArrayList<Double> getBacktrackFaderDb (int currentCueNum){
        ArrayList<Double> result = new ArrayList<>();

        for (int i = 0; i < cues.get(currentCueNum).getFaderValues().size(); i++) {
            Double addVar = null;
            if(cues.get(currentCueNum).getFaderValues().get(i)!=null){
                addVar = cues.get(currentCueNum).getFaderValues().get(i);
            }else{
                for (int j = currentCueNum-1; j >=0 ; j--) {
                    if(cues.get(j).getFaderValues().get(i)!=null) {
                        addVar = cues.get(j).getFaderValues().get(i);
                        break;
                    }
                }
            }

            result.add(addVar);
        }

        return result;
    }

    public void jumpTo(int selectedIndex) {
        reset();

        Timeline delay = new Timeline(new KeyFrame(Duration.seconds(mainController.MIN_FADE_TIME + 0.05), event -> {
            setInitialCueVolume(mainController.cueListVolumeSlider.getValue());

            currentCueNum = selectedIndex;

            if(selectedIndex>0){
                List<Cue> prevCues = getCues().subList(0, getCurrentCueNum());
                List<Cue> volumeCues = new ArrayList<>();
                for(Cue cue : prevCues){
                    if(cue.getCueCommand().equals(MainController.COMMAND.VOLUME)){
                        volumeCues.add(cue);
                    }
                }
                if (!volumeCues.isEmpty()) {
                    mainController.cueListVolumeSlider.setValue(volumeCues.get(volumeCues.size()-1).getCueVol().get());
                } else{
                    mainController.cueListVolumeSlider.setValue(initialCueVolume);
                }
            }

            Cue nextCue = getCues().get(currentCueNum);

            faderManager.runFaders(getBacktrackFaderDb(currentCueNum));

            Timeline playDelay = new Timeline(new KeyFrame(Duration.seconds(mainController.MIN_FADE_TIME + 0.05), event2 -> {nextCue.run(); nextCue.setSelected(true);}));
            playDelay.play();
        }));
        delay.play();


    }



    public void reset() {
        currentCueNum = -1;

        for (Cue cue : cues) {
            cue.setSelected(false);
            cue.stop();
        }
        playlistManager.stop(mainController.MIN_FADE_TIME);

        ArrayList<Double> minDb = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            minDb.add((double) -41);
        }
        faderManager.runFaders(minDb);

        ExponentialFade volFade = mainController.getFades().remove("||CUELIST||");
        if(volFade!=null) volFade.remove();

        Timeline volChange = new Timeline(new KeyFrame(Duration.seconds(mainController.MIN_FADE_TIME + 0.05), event -> {
            if(initialCueVolume>=0){
                mainController.cueListVolumeSlider.setValue(initialCueVolume);
                initialCueVolume = -1;
            }
        }));
        volChange.play();
    }


    public void stop(double duration) {
        for (Cue cue : cues) {
            if(cue.getCueFile()!=null){
                cue.fade(duration);
            }
        }

        playlistManager.pause(duration);
    }
}
