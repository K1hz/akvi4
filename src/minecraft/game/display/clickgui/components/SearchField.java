package minecraft.game.display.clickgui.components;

import com.mojang.blaze3d.matrix.MatrixStack;


import lombok.Setter;
import lombok.Getter;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.operation.visual.ClickGui;
import minecraft.system.AG;
import org.lwjgl.glfw.GLFW;


@ Setter
@ Getter
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
        ClickGui clickGui = AG.getInst().getModuleManager().getClickGui();


        GraphicsSystem.drawRoundedRect(x, y, width, height, 6, ColoringSystem.rgba(15,15,15, 200));
        String textToDraw = text.isEmpty() && !typing ? placeholder : text;
        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "";
        Fonts.montserrat.drawText(matrixStack, textToDraw + cursor, x + 5, y + (height - 8) / 2 + 1, ColoringSystem.rgb(255, 255, 255), 6);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (isFocused) {
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
        if(keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE){
            typing = false;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!MathSystem.isHovered((float) mouseX, (float) mouseY, x, y, width, height)){
            isFocused = false;
        }
        isFocused = MathSystem.isHovered((float) mouseX, (float) mouseY, x, y, width, height);
        typing = isFocused;
        return isFocused;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }
    public void setFocused(boolean focused) { isFocused = focused; }
}
