package minecraft.game.advantage.alacrity.easing;

import lombok.Data;
import net.minecraft.util.math.vector.Vector2f;

@Data
public class CompactAnimation {

    private Easing easing;
    private long duration;
    private long millis;
    private long startTime;

    private double startValue;
    private double destinationValue;
    private double value;
    private boolean finished;

    public CompactAnimation(final Easing easing, final long duration) {
        this.easing = easing;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    /**
     * Updates the animation by using the easing function and time
     *
     * @param destinationValue the value that the animation is going to reach
     */
    public void run(final double destinationValue) {
        this.millis = System.currentTimeMillis();
        if (this.destinationValue != destinationValue) {
            this.destinationValue = destinationValue;
            this.reset();
        } else {
            this.finished = this.millis - this.duration > this.startTime;
            if (this.finished) {
                this.value = destinationValue;
                return;
            }
        }

        final double result = this.easing.getFunction().apply(this.getProgress());
        if (this.value > destinationValue) {
            this.value = this.startValue - (this.startValue - destinationValue) * result;
        } else {
            this.value = this.startValue + (destinationValue - this.startValue) * result;
        }
    }
    public Number getNumberValue() {
        return getValue();
    }
    public boolean update() {
        boolean alive = !isFinished();
        if (alive) {
            double progress = Math.min(1.0, getProgress());
            double newValue = (this.value > destinationValue)
                    ? startValue - (startValue - destinationValue) * easing.getFunction().apply(progress)
                    : startValue + (destinationValue - startValue) * easing.getFunction().apply(progress);

            setValue(newValue);
        } else {
            setValue(destinationValue);
        }
        return alive;
    }
    /**
     * Returns the progress of the animation
     *
     * @return value between 0 and 1
     */
    public double getProgress() {
        return (double) (System.currentTimeMillis() - this.startTime) / (double) this.duration;
    }

    /**
     * Resets the animation to the start value
     */
    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.startValue = value;
        this.finished = false;
    }
}
