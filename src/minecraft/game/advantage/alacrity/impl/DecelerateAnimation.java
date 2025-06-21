package minecraft.game.advantage.alacrity.impl;


import minecraft.game.advantage.alacrity.AnimationManager;
import minecraft.game.advantage.alacrity.Direction;

public class DecelerateAnimation extends AnimationManager {

    public DecelerateAnimation(int ms, double endPoint) {
        super(ms, endPoint);
    }

    public DecelerateAnimation(int ms, double endPoint, Direction direction) {
        super(ms, endPoint, direction);
    }

    protected double getEquation(double x) {
        double x1 = x / duration;
        return 1 - ((x1 - 1) * (x1 - 1));
    }
}
