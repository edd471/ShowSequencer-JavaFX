module com.example.showSequencerJavafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.dlsc.formsfx;
    requires java.desktop;
    requires java.prefs;
    requires javafx.base;
    requires java.sql;
    requires java.naming;
    requires jssc;

    opens com.example.showSequencerJavafx to javafx.fxml;
    exports com.example.showSequencerJavafx;
}