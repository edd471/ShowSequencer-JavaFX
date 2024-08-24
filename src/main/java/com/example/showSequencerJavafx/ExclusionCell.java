package com.example.showSequencerJavafx;


import javafx.scene.control.TableCell;

/**Custom table cell for showing when playlistFile is excluded.
 */
public class ExclusionCell extends TableCell<PlaylistFile, Boolean> {


    /**Default Constructor
     */
    public ExclusionCell() {
    }

    @Override
    protected void updateItem(Boolean excluded, boolean empty) {
        super.updateItem(excluded, empty);
        if (empty || getTableRow() == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText("");
            if(excluded) {
                setStyle("-fx-background-color: red;");
            }
            else{
                setStyle("");
            }
        }
    }


}
