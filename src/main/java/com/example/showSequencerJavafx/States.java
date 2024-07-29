package com.example.showSequencerJavafx;

import javafx.beans.property.SimpleDoubleProperty;

public interface States {

    enum STATE{PLAYING, PAUSED, STOPPED}

    SimpleDoubleProperty getProgress();
    boolean isSelected();
}
