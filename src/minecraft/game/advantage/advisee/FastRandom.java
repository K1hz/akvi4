package minecraft.game.advantage.advisee;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FastRandom extends Random {
    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
    private Random random = null;
    private volatile boolean seedSet, seedUpdated;
    private volatile long seed;

    private void validateRandom() {
        if (this.random == null) {
            this.random = new Random(this.seed);
            this.seedUpdated = false;
        } else if (this.seedUpdated) {
            this.random.setSeed(this.seed);
            this.seedUpdated = false;
        }
    }

    public void setSeed(long seed) {
        this.seed = seed;
        this.seedSet = true;
        this.seedUpdated = true;
    }

    public void nextBytes(byte[] bytes) {
        if (this.seedSet) {
            this.validateRandom();
            this.random.nextBytes(bytes);
        } else {
            this.threadLocalRandom.nextBytes(bytes);
        }
    }

    public int nextInt() {
        if (this.seedSet) {
            this.validateRandom();
            return this.random.nextInt();
        } else {
            return this.threadLocalRandom.nextInt();
        }
    }

    public int nextInt(int bound) {
        if (this.seedSet) {
            this.validateRandom();
            return this.random.nextInt(bound);
        } else {
            return this.threadLocalRandom.nextInt(bound);
        }
    }

    public long nextLong() {
        if (this.seedSet) {
            this.validateRandom();
            return this.random.nextLong();
        } else {
            return this.threadLocalRandom.nextLong();
        }
    }

    public boolean nextBoolean() {
        if (this.seedSet) {
            this.validateRandom();
            return this.random.nextBoolean();
        } else {
            return this.threadLocalRandom.nextBoolean();
        }
    }

    public float nextFloat() {
        if (this.seedSet) {
            this.validateRandom();
            return this.random.nextFloat();
        } else {
            return this.threadLocalRandom.nextFloat();
        }
    }

    public double nextDouble() {
        if (this.seedSet) {
            this.validateRandom();
            return this.random.nextDouble();
        } else {
            return this.threadLocalRandom.nextDouble();
        }
    }

    public double nextGaussian() {
        if (this.seedSet) {
            this.validateRandom();
            return this.random.nextGaussian();
        } else {
            return this.threadLocalRandom.nextGaussian();
        }
    }
}