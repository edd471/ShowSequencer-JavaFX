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


/**Class to contain and manage all cues*/
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


    /**Cue manager constructor. Sets Variables and gets references to other manager classes
     * @param mainController Controller class for main menu
     */
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


    /**Getter for Current cue number
     * @return Number of current cue
     */
    public int getCurrentCueNum(){
        return currentCueNum;
    }

    /**Setter for master cue volume. Sets all PLAY cues' players volumes according to
     * the input volume and the cues individual volume parameters.
     * @param volume Main cue volume
     */
    public void setCurrentCueVolume(double volume) {
        for(Cue cue : cues){
            if(cue.getCueFile()!=null && cue.getCueCommand().equals(MainController.COMMAND.PLAY) &&
               cue.getCueFile().getPlayer().getFadeProgress().get()==1){

                cue.getCueFile().getPlayer().setVolume((cue.getCueVol().get()/100)*(volume/100));
            }
        }
    }

    /**Getter for initial cue volume.
     * @return Master cue volume before any cues are run
     */
    public double getInitialCueVolume() {
        return initialCueVolume;
    }

    /**Setter for initial cue volume
     * @param i Master cue volume before any cues are run
     */
    public void setInitialCueVolume(double i) {
        initialCueVolume = i;
    }

    /**Getter for SFX files
     * @return List of available sound effect playlistFiles
     */
    public ObservableList<PlaylistFile> getSFXFiles() {
        return SFXFiles;
    }

    /**Getter for cue clipboard
     * @return List of cues in the clipboard
     */
    public ArrayList<Cue> getCueClipboard() {
        return cueClipboard;
    }

    /**Getter for cues list
     * @return List of all cues
     */
    public ObservableList<Cue> getCues() {
        return cues;
    }

    /**Setter for SFX directory
     * @param selectedDirectory Directory containing SFX files
     */
    public void setSFXDirectory(File selectedDirectory){
        sfxDirectory = selectedDirectory;

        List<File> files = List.of(Objects.requireNonNull(selectedDirectory.listFiles()));
        List<File> musicFiles = files.stream().filter(x -> x.getName().endsWith(".mp3") ||
                                                           x.getName().endsWith(".wav") ||
                                                           x.getName().endsWith(".mpeg")).toList();

        SFXFiles.clear();
        int i = 1;
        for (File musicFile : musicFiles) {
            PlaylistFile newFile = new PlaylistFile(musicFile.getName(), i, false,
                                                    Paths.get(musicFile.getAbsolutePath()).toUri(),
                                                    mainController.getPlaylistTable(), mainController,
                                                    mainController.cueListVolumeSlider);
            SFXFiles.add(newFile);
            i++;
        }
    }

    /**Getter for SFX directory
     * @return Directory containing SFX files
     */
    public File getSFXDirectory() {
        return sfxDirectory;
    }

    /**Pastes a single cue from the clipboard into the provided index
     * @param selectedIndex Index of cue to be replaced with clipboard
     */
    public void pasteCue(int selectedIndex) {
        getCues().set(selectedIndex, new Cue(cueClipboard.get(0)));
    }

    /**Adds all cues from the clipboard to the cues list starting at the provided index
     * @param selectionStart Index to add cues below
     */
    public void pasteCueAsNew(int selectionStart) {
        int currentSelection = selectionStart;
        for (Cue cue : cueClipboard){
            cues.add(currentSelection, new Cue(cue));
            currentSelection++;
        }
    }

    /**Clears clipboard and adds copies of provided cues to cue clipboard.
     * @param selectedItems Items to copy to clipboard
     */
    public void copyCues(ObservableList<Cue> selectedItems) {
        cueClipboard.clear();
        System.out.println(selectedItems.size());
        for(Cue cue : selectedItems){
            System.out.println("DONE");
            getCueClipboard().add(new Cue(cue));
        }
    }

    /**Add new cue with default parameters to cue list
     */
    public void addCue() {
        Cue newCue = new Cue("0.0", "", -1, MainController.COMMAND.NONE, null,
                      75, 0, mainController.getCueAudioTable(), mainController);
        System.out.println(mainController.getCueAudioTable().getSelectionModel().getSelectedIndex());
        if(mainController.getCueAudioTable().getSelectionModel().getSelectedIndex()>=0){
            cues.add(mainController.getCueAudioTable().getSelectionModel().getSelectedIndex()+1,newCue);
        } else {
            cues.add(newCue);
        }
    }

    /**Removes all provided cues from the cue list and stops them from playing
     * @param selectedCues List of cues to remove
     */
    public void removeCue(List<Cue> selectedCues) {
        for (Cue cue : selectedCues) {
            cue.stop();
            cues.remove(cue);
        }
    }

    /**Increments current cue number. If on first cue sets the initial cue volume. Runs the cue
     * and sends fader values to faderManger to run. Sets selected to true for the running cue and false
     * for the previous cue.
     */
    public void next() {
        if(currentCueNum<cues.size()-1){
            if(currentCueNum ==-1) setInitialCueVolume(mainController.cueListVolumeSlider.getValue());

            if(currentCueNum >=0){cues.get(currentCueNum).setSelected(false);}

            currentCueNum++;

            if(currentCueNum>=0) {
                cues.get(currentCueNum).setSelected(true);
                cues.get(currentCueNum).run(true);
                faderManager.runFaders(cues.get(currentCueNum).getFaderValues());
            }
        }
    }

    /**Decrements current cue number. If on first cue resets the cues.
     * Backtracks the previous cue and replays the new current cue.
     * Sends fader values found by looking up the last values for each fader in the cue list.
     * Sets selected for current cue and unsets selected for previous cue.
     */
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

    /**Finds most recent fader value from a list of previous cues for each fader.
     * @param currentCueNum Index to get previous cues from inclusively
     * @return List of fader values.
     */
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

    /**Resets cues. After small delay for reset to complete, sets correct master cue volume for
     * new cue index. After small delay for master cue volume to change, run cue and faders.
     * @param selectedIndex Index of cue to jump to.
     */
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

            Timeline playDelay = new Timeline(new KeyFrame(Duration.seconds(mainController.MIN_FADE_TIME + 0.05),
                                              event2 -> {nextCue.run(true); nextCue.setSelected(true);}));
            playDelay.play();
        }));
        delay.play();


    }


    /**Sets current cue number to -1. Stops all cues. Sets all faders to min.
     * Stops any volume fades and fades master cue volume to initial cue volume.
     */
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


    /**Fades all cues in cue list and pauses the playlist in a given time.
     * @param duration Time to perform fades
     */
    public void stop(double duration) {
        for (Cue cue : cues) {
            if(cue.getCueFile()!=null){
                cue.fade(duration);
            }
        }

        playlistManager.pause(duration);
    }
}
