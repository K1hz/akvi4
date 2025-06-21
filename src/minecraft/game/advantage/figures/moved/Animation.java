package minecraft.game.advantage.figures.moved;

import minecraft.game.advantage.alacrity.Direction;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.figures.moved.bezier.Bezier;
import minecraft.game.advantage.figures.moved.bezier.list.CubicBezier;
import minecraft.game.advantage.figures.moved.util.Easing;
import minecraft.game.advantage.figures.moved.util.Easings;
import lombok.Getter;
import org.lwjgl.system.CallbackI;

@Getter
public class Animation {
    private long start;
    private double duration;
    private double fromValue;
    private double toValue;
    private double value;
    private Easing easing = Easings.LINEAR;
    private Bezier bezier = new CubicBezier();
    private AnimationType type = AnimationType.EASING;
    protected Direction direction;
    protected double endPoint;
    private boolean debug = false;

    public Animation run(double valueTo, double duration) {
        return this.run(valueTo, duration, Easings.LINEAR, false);
    }

    public Animation run(double valueTo, double duration, Easing easing) {
        return this.run(valueTo, duration, easing, false);
    }

    public Animation run(double valueTo, double duration, Bezier bezier) {
        return this.run(valueTo, duration, bezier, false);
    }

    public Animation run(double valueTo, double duration, boolean safe) {
        return this.run(valueTo, duration, Easings.LINEAR, safe);
    }
    public Animation setDirection(Direction direction) {
        if (direction != direction) {
            direction = direction;
            TimeCounterSetting timeCounterSetting = new TimeCounterSetting();
            timeCounterSetting.setTime((long) (System.currentTimeMillis() - (duration - Math.min(duration, timeCounterSetting.getTime()))));
        }
        return this;
    }
    public Animation setDirection(boolean forwards) {
        Direction direction = forwards ? Direction.FORWARDS : Direction.BACKWARDS;
        if (direction != direction) {
            direction = direction;
            TimeCounterSetting timeCounterSetting = new TimeCounterSetting();
            timeCounterSetting.setTime((long) (System.currentTimeMillis() - (duration - Math.min(duration, timeCounterSetting.getTime()))));
        }
        return this;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public Animation run(double valueTo, Easing easing, boolean safe) {
        if (this.check(safe, valueTo)) {
            if (this.isDebug()) {
                System.out.println("Animate cancelled due to target val equals from val");
            }
        } else {
            this.setType(AnimationType.EASING)
                    .setEasing(easing)
                    .setStart(System.currentTimeMillis())
                    .setFromValue(this.getValue())
                    .setToValue(valueTo);

            if (this.isDebug()) {
                System.out.println("#animate {\n    to value: " + this.getToValue() + "\n    from value: " + this.getValue() + "\n}");
            }
        }
        return this;
    }

    public Animation run(double valueTo, double duration, Easing easing, boolean safe) {
        if (this.check(safe, valueTo)) {
            if (this.isDebug()) {
                System.out.println("Animate cancelled due to target val equals from val");
            }
        } else {
            this.setType(AnimationType.EASING).setEasing(easing).setDuration(duration * 1000.0D).setStart(System.currentTimeMillis()).setFromValue(this.getValue()).setToValue(valueTo);
            if (this.isDebug()) {
                System.out.println("#animate {\n    to value: " + this.getToValue() + "\n    from value: " + this.getValue() + "\n    duration: " + this.getDuration() + "\n}");
            }
        }
        return this;
    }

    public Animation run(double valueTo, double duration, Bezier bezier, boolean safe) {
        if (this.check(safe, valueTo)) {
            if (this.isDebug()) {
                System.out.println("Animate cancelled due to target val equals from val");
            }
        } else {
            this.setType(AnimationType.BEZIER).setBezier(bezier).setDuration(duration * 1000.0D).setStart(System.currentTimeMillis()).setFromValue(this.getValue()).setToValue(valueTo);
            if (this.isDebug()) {
                System.out.println("#animate {\n    to value: " + this.getToValue() + "\n    from value: " + this.getValue() + "\n    duration: " + this.getDuration() + "\n    type: " + this.getType().name() + "\n}");
            }

        }
        return this;
    }

    public boolean update() {
        boolean alive = this.isAlive();
        if (alive) {
            if (this.getType().equals(AnimationType.BEZIER)) {
                this.setValue(this.interpolate(this.getFromValue(), this.getToValue(), this.getBezier().getValue(this.calculatePart())));
            } else {
                this.setValue(this.interpolate(this.getFromValue(), this.getToValue(), this.getEasing().ease(this.calculatePart())));
            }
        } else {
            this.setStart(0L);
            this.setValue(this.getToValue());
        }
        return alive;
    }

    public boolean isAlive() {
        return !this.isFinished();
    }

    public boolean isFinished() {
        return this.calculatePart() >= 1.0D;
    }

    public double calculatePart() {
        return (double) (System.currentTimeMillis() - this.getStart()) / this.getDuration();
    }

    public boolean check(boolean safe, double valueTo) {
        return safe && this.isAlive() && (valueTo == this.getFromValue() || valueTo == this.getToValue() || valueTo == this.getValue());
    }

    public double interpolate(double start, double end, double pct) {
        return start + (end - start) * pct;
    }

    public Animation setStart(long start) {
        this.start = start;
        return this;
    }

    public Animation setDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public Animation setFromValue(double fromValue) {
        this.fromValue = fromValue;
        return this;
    }

    public Animation setToValue(double toValue) {
        this.toValue = toValue;
        return this;
    }

    public Animation setValue(double value) {
        this.value = value;
        return this;
    }

    public Animation setEasing(Easing easing) {
        this.easing = easing;
        return this;
    }

    public Animation setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public Animation setBezier(Bezier bezier) {
        this.bezier = bezier;
        return this;
    }

    public Animation setType(AnimationType type) {
        this.type = type;
        return this;
    }
    public void setEndPoint(double endPoint) {
        this.endPoint = endPoint;
    }
}