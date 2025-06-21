package minecraft.game.display.clientgui.api.component;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IComponent {

    default void render(MatrixStack stack, float mouseX, float mouseY) {
    }

    void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY);

    void mouseClicked(int mouseX, int mouseY, int mouseButton);

    void mouseReleased(int mouseX, int mouseY, int mouseButton);

    void keyTyped(int keyCode, int scanCode, int modifiers);

    void charTyped(char codePoint, int modifiers);
}
