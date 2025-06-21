package minecraft.game.display.clientnotify.most;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.advisee.IMinecraft;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Notify implements IMinecraft {
    public final String content;
    public final long init = System.currentTimeMillis(), delay;
    public final CompactAnimation alphaAnimation = new CompactAnimation(Easing.EASE_IN_OUT_QUAD, 120); // Change easing to EASE_OUT_CUBIC for smoother fade
    public final CompactAnimation animationY = new CompactAnimation(Easing.EASE_IN_OUT_CUBIC, 120);
    public final CompactAnimation animationX = new CompactAnimation(Easing.EASE_OUT_BACK, 10);
    public final CompactAnimation chatOffset = new CompactAnimation(Easing.EASE_OUT_QUAD, 80);
    public static boolean end;
    public float margin = 4;

    // Render method with alpha application for smooth fade out
    public abstract void render(MatrixStack matrixStack, int multiplierY);

    // This will check if the notification has expired, based on the fading out process
    public boolean hasExpired() {
        long elapsedTime = System.currentTimeMillis() - init;

        // Update the alpha animation based on time and delay
        if (elapsedTime >= delay) {
            alphaAnimation.run(elapsedTime - delay);
        }

        // Check if the alpha value has reached zero, meaning the notification should be removed
        return alphaAnimation.getValue() == 0;
    }

    // Call this in your rendering logic to apply the alpha value to the notification display
    protected float getAlpha() {
        return (float) alphaAnimation.getValue();
    }
}
