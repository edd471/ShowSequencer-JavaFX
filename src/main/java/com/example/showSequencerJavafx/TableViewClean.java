package com.example.showSequencerJavafx;

import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;

/**
 * @author Craig Day <craigday@gmail.com>
 */
public class TableViewClean<T> extends TableView<T> {

    private boolean cleaned = false;
    private final CleanSide cleanSide;

    public enum CleanSide {
        VERTICAL,
        HORIZONTAL,
        BOTH
    }

    public TableViewClean() {
        this(CleanSide.BOTH);
    }

    public TableViewClean(CleanSide cleanSide) {
        this.cleanSide = cleanSide;
        switch (cleanSide) {
            case VERTICAL:
                getStyleClass().add("tableViewCleanVertical");
                break;
            case HORIZONTAL:
                getStyleClass().add("tableViewCleanHorizontal");
                break;
            case BOTH:
                getStyleClass().add("tableViewCleanBoth");
                break;
        }
    }

    @Override
    protected void layoutChildren() {
        if (! cleaned) {
            if (cleanSide == CleanSide.VERTICAL || cleanSide == CleanSide.BOTH) {
                for (Node n: lookupAll(".scroll-bar:vertical")) {
                    if (n instanceof ScrollBar) {
                        ScrollBar scrollBar = (ScrollBar) n;
                        scrollBar.setPrefWidth(0);
                        scrollBar.setMaxWidth(0);
                        scrollBar.setVisible(false);
                        scrollBar.setOpacity(1);
                    }
                }
            }
            if (cleanSide == CleanSide.HORIZONTAL || cleanSide == CleanSide.BOTH) {
                for (Node n: lookupAll(".scroll-bar:horizontal")) {
                    if (n instanceof ScrollBar) {
                        ScrollBar scrollBar = (ScrollBar) n;
                        scrollBar.setPrefHeight(0);
                        scrollBar.setMaxHeight(0);
                        scrollBar.setVisible(false);
                        scrollBar.setOpacity(1);
                    }
                }
            }
            cleaned = true;
        }
        super.layoutChildren();
    }
}
