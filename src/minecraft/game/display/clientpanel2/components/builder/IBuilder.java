package minecraft.game.display.clientpanel2.components.builder;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IBuilder {

    default void render(MatrixStack stack, float mouseX, float mouseY) {
    }

    default void mouseClick(float mouseX, float mouseY, int mouse) {
    }
    default void charTyped(char codePoint, int modifiers) {
    }
    default void mouseRelease(float mouseX, float mouseY, int mouse) {
    }
    default void keyPressed(int key, int scanCode, int modifiers) {
    }
}
