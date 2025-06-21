package minecraft.game.display.clientpanel.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.advantage.words.font.ClientFonts;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

/**
 * @author Ieo117
 * @created 16.06.2024
 */

public class SearchField {
    private int x, y, width, height;
    public String text;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;
    private static final int MAX_LENGTH = 15;

    public SearchField(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
        this.text = "";
        this.isFocused = false;
        this.typing = false;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        KawaseBlur.blur.render(() -> {
            GraphicsSystem.drawRoundedRect(x, y, width, height, new Vector4f(5, 5, 5, 5), new Vector4i(
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 65),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 65),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 65),
                    ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 65)
            ));
        });
        GraphicsSystem.drawRoundedRect(x, y, width, height, new Vector4f(5, 5, 5, 5), new Vector4i(
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(11, 11, 11), 145),
                ColoringSystem.setAlpha(ColoringSystem.rgb(34, 34, 34), 145)
        ));
        String textToDraw = text.isEmpty() && !typing ? placeholder : text;
        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "";
        ClientFonts.clickGui[14].drawString(matrixStack, textToDraw + cursor, x + 5, y + (height - 8) / 2 + 2, ColoringSystem.rgb(255, 255, 255));
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (isFocused && text.length() < MAX_LENGTH) {
            text += codePoint;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            typing = false;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!MathSystem.isHovered((float) mouseX, (float) mouseY, x, y, width, height)) {
            isFocused = false;
        }
        isFocused = MathSystem.isHovered((float) mouseX, (float) mouseY, x, y, width, height);
        typing = isFocused;
        return isFocused;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
    }
}
