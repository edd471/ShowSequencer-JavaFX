package com.example.showSequencerJavafx;

import javafx.beans.property.SimpleDoubleProperty;

/**
 * Interface for compatibility between Cue, PlaylistFile and StatusRow classes
 */
public interface States {

    enum STATE{PLAYING, PAUSED, STOPPED}

    /**Getter for progress value
     * @return Percentage value for progress of a fade in this object
     */
    SimpleDoubleProperty getProgress();

    /**Getter for isSelected
     * @return True if selected, false otherwise
     */
    boolean isSelected();
}
