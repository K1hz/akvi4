package minecraft.game.advantage.figures;

import lombok.Setter;

@Setter
public class TimerTransaction {

    private long lastMS;

    private TimerTransaction() {
        reset();
    }

    public static TimerTransaction create() {
        return new TimerTransaction();
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public long elapsedTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public boolean hasReached(long time) {
        return elapsedTime() >= time;
    }

    public boolean hasReached(long time, boolean reset) {
        boolean hasElapsed = elapsedTime() >= time;
        if (hasElapsed && reset) {
            reset();
        }
        return hasElapsed;
    }

    public boolean hasReached(double ms) {
        return elapsedTime() >= ms;
    }

    public boolean delay(long ms) {
        boolean hasDelayElapsed = elapsedTime() - ms >= 0;
        if (hasDelayElapsed) {
            reset();
        }
        return hasDelayElapsed;
    }
}