package minecraft.game.display.clientpanel2;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.RectanglesSystem;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.words.font.ClientFonts;
import org.lwjgl.glfw.GLFW;

@Setter
@Getter
public class SearchField {
    private int x, y, width, height;
    private String text;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;

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
        float animation = (float) (1 * ClickGuiScreen.getGradientAnimation().getValue());
        float posAnimation = 20 - 20 * animation;
        String textToDraw = text.isEmpty() && !typing ? placeholder : text;
        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "";
        int color = ColoringSystem.rgba(20, 20, 20, (int) (255 * animation));
        RectanglesSystem.getInstance().drawRoundedRectShadowed(matrixStack, x - posAnimation, y - 150, x + width - posAnimation, y + height - 150, 2, 3, color, color, color, color, false, false, true, true);
        RectanglesSystem.getInstance().drawRoundedRectShadowed(matrixStack, x - posAnimation, y - 150, x + width - posAnimation, y + height - 150, 2, 3, color, color, color, color, false, false, false, true);

        Scissor.push();
        Scissor.setFromComponentCoordinates(x - posAnimation, y - 150, width + posAnimation, height);
        ClientFonts.tenacity[15].drawString(matrixStack, textToDraw + cursor, x + 5 - posAnimation, y + (height - 8f) / 2 + 1.5f - 150, ColoringSystem.reAlphaInt(ColoringSystem.rgb(200, 200, 200), (int) (255 * animation)));
        Scissor.unset();
        Scissor.pop();
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (typing) {
            text += codePoint;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientReceive.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            text = "";
        }
        if (typing && keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            typing = false;
        }
        if (ClientReceive.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
            text += ClientReceive.pasteString();
        }
        if (ClientReceive.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_F) {
            typing = true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!MathSystem.isHovered((float) mouseX, (float) mouseY, x, y-300, width, height)){
            isFocused = false;
        }
        isFocused = MathSystem.isHovered((float) mouseX, (float) mouseY, x, y - 150, width, height);
        typing = isFocused;
        return isFocused;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }
    public void setFocused(boolean focused) { isFocused = focused; }
}
