package minecraft.game.display.mainscreen;

import lombok.Getter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public class SnowParticle {
    private float x, y;
    private final float radius;
    private float speedX, speedY;
    private float alpha;

    public SnowParticle(float screenWidth, float screenHeight) {
        this.radius = ThreadLocalRandom.current().nextFloat(2.0f, 6.0f);
        reset(screenWidth, screenHeight);
    }

    public void update(float screenWidth, float screenHeight) {
        x += speedX;
        y += speedY;

        if (y > screenHeight - 50) {
            alpha -= 0.01f;
            if (alpha <= 0) {
                reset(screenWidth, screenHeight);
            }
        }

        if (x < -50 || x > screenWidth + 50) {
            reset(screenWidth, screenHeight);
        }
    }

    private void reset(float screenWidth, float screenHeight) {
        this.x = ThreadLocalRandom.current().nextFloat(0, screenWidth);
        this.y = ThreadLocalRandom.current().nextFloat(-100, -10);
        this.speedX = ThreadLocalRandom.current().nextFloat(-1.5f, 1.5f);
        this.speedY = ThreadLocalRandom.current().nextFloat(1.5f, 3.5f);
        this.alpha = 1.0f;
    }
}