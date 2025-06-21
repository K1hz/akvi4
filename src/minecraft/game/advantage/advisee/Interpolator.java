package minecraft.game.advantage.advisee;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Interpolator {

    public double lerp(double input, double target, double step) {
        return input + step * (target - input);
    }

    public float lerp(float input, float target, double step) {
        return (float) (input + step * (target - input));
    }

    public int lerp(int input, int target, double step) {
        return (int) (input + step * (target - input));
    }
}