package com.example.showSequencerJavafx;
import javafx.geometry.Insets;
import javafx.scene.control.TableRow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**Custom TableRow to show the status of playlistFile or Cue objects in the table.
 * @param <T> Cue or PlaylistFile object.
 */
public class StatusRow<T extends States> extends TableRow<T> {

    /**Run when table row is updated.
     * @param item Object in the table row
     * @param empty Empty row if true, false otherwise.
     */
    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setStyle("");
        } else {

            double progress = item.getProgress().get();

            if(progress>0 && getTableView().getId().equals("cueListTableAudio")){

                double progressStart = 0;
                double progressEnd = 0;

                // Calculate the width of columns before the "Audio Playback" column
                for (int i = 0; i < 3; i++) {
                    progressStart += getTableView().getVisibleLeafColumns().get(i).getWidth();
                }

                // Calculate the width of columns after the "Audio Playback" column
                for (int i = 7; i < getTableView().getVisibleLeafColumns().size(); i++) {
                    progressEnd += getTableView().getVisibleLeafColumns().get(i).getWidth();
                }

                // Create the gradient fill
                LinearGradient gradient = new LinearGradient(
                        0, 0, progress, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.GREEN),
                        new Stop(progress, Color.GREEN),
                        new Stop(progress, Color.TRANSPARENT)
                );

                // Apply the background with calculated insets
                setBackground(new Background(new BackgroundFill(
                        gradient, CornerRadii.EMPTY, new Insets(0, progressEnd, 0, progressStart)
                )));

            }

            if (item.isSelected()) {
                setStyle("-fx-border-width: 4; -fx-border-color: green; -fx-padding: -4");
            }else {
                setStyle("");
            }
        }
    }
}
