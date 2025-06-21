/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package minecraft.game.advantage.advisee;

import minecraft.game.transactions.CancelEvent;

public class SlowSneakEvent
        extends CancelEvent {
    public double slowFactor;

    public SlowSneakEvent(double d) {
        this.slowFactor = d;
    }

    public void setSlowFactor(double d) {
        this.slowFactor = d;
    }

    public double getSlowFactor() {
        return this.slowFactor;
    }
}

