package com.example.showSequencerJavafx;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;


/**Exponential fade class to generate a timeline to fade a value from
 * one value to another in a given time exponentially.
 */
public class ExponentialFade {

    String fadeID;
    Timeline timeline = new Timeline();
    SimpleDoubleProperty newVol = new SimpleDoubleProperty();
    SimpleDoubleProperty progress = new SimpleDoubleProperty();
    double startVol;
    double targetVol;
    double duration;
    MainController mainController;

    /**Set the onFinished runnable for the fade including the runnable passed
     * in to the fade.
     * @param onFinished Runnable to be run on fade finished
     */
    public void setOnFinished(Runnable onFinished) {
        timeline.setOnFinished(event-> {
            onFinished.run();
            if(targetVol<=1){
                newVol.set(startVol);
            }
            mainController.getFades().remove(fadeID, this);
            progress.set(0);
        });
    }

    /**Constructor for exponential fade. Sets variables, finds possible previous fade values,
     * creates timeline with appropriate values to fade exponentially with listener to the fades
     * progress and a listener to newVol variable.
     * @param duration Duration of fade.
     * @param startVol Intended start volume of fade
     * @param targetVol Intended target volume of fade
     * @param fadeID Unique ID for each faded object to control interactions
     * @param mainController Main controller for GUI
     */
    public ExponentialFade(double duration, double startVol, double targetVol, String fadeID, MainController mainController) {

        this.fadeID = fadeID;
        this.startVol = startVol;
        this.targetVol = targetVol;
        this.mainController = mainController;
        this.newVol.set(startVol);
        this.progress.set(0.01);
        this.duration = duration;

        handlePreviousFade();

        if(this.startVol==this.targetVol) return;

        setOnFinished(()->{});

        boolean grow = this.startVol<this.targetVol;

        if(startVol<=1) targetVol = this.targetVol;

        startVol = this.newVol.get();

        startVol = Math.max(0.9, startVol);
        targetVol = Math.max(0.9, targetVol);

        if(duration>0){
            double finalStartVol = startVol;
            double finalTargetVol = targetVol;

            Interpolator interpolator = getInterpolator(grow);

            KeyFrame exp = new KeyFrame(Duration.seconds(duration), new KeyValue(progress, 1, interpolator));
            timeline.getKeyFrames().add(exp);

            ChangeListener<Number> listener = (obs, oldValue, newValue) -> {
                if((double)newValue>0){
                    if(grow){
                        this.newVol.set(finalStartVol + ((Double) newValue * (finalTargetVol-finalStartVol)));
                    }else{
                        this.newVol.set(finalTargetVol + ((Double)newValue) * (finalStartVol-finalTargetVol));
                    }
                }
            };

            progress.addListener(listener);

            double finalTargetVol1 = targetVol;
            KeyFrame removeListener = new KeyFrame(Duration.seconds(duration), event -> {progress.removeListener(listener); newVol.set(finalTargetVol1);});
            timeline.getKeyFrames().add(removeListener);

        }else{
            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(1),
                    event -> this.newVol.set(this.targetVol)
            );
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.setCycleCount(1);
    }


    private static Interpolator getInterpolator(boolean grow) {
        Interpolator interpolator;

        if (grow) interpolator = new Interpolator() {
            @Override
            protected double curve(double v) {
                return Math.expm1(v)/Math.expm1(1);
            }
        };
        else interpolator = new Interpolator() {
            @Override
            protected double curve(double v) {
                return 1-(Math.log1p(v)/Math.log1p(1));
            }
        };
        return interpolator;
    }


    /**Stops the timeline and sets the progress to 0.
     */
    public void remove(){
        progress.set(0);
        timeline.stop();
    }

    /**Finds start and end vol of any fade of the same ID that may be still
     * running. Sets the start and end vol of this fade depending on if it is
     * a grow or fade. Then removes the previous fade.
     */
    private void handlePreviousFade(){
        ExponentialFade prevFade = mainController.getFades().put(fadeID, this);
        if(prevFade==null) return;

        if((startVol<=targetVol)==(prevFade.startVol<=prevFade.targetVol) && startVol!=targetVol){
            this.startVol = prevFade.startVol;
            this.targetVol = prevFade.targetVol;
        }else{
            this.startVol = prevFade.targetVol;
            this.targetVol = prevFade.startVol;
        }

        this.newVol.set(prevFade.newVol.get());

        prevFade.remove();

    }
}
