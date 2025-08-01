package minecraft.game.display.clientgui.component.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientgui.api.component.Component;
import minecraft.game.operation.wamost.massa.elements.StringSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.SharedConstants;
import org.lwjgl.glfw.GLFW;

public class StringComponent extends Component {

    public StringSetting option;
    public boolean isTyping;
    public boolean isTextSelected;
    private String selectedText = "";

    public StringComponent(StringSetting option) {
        this.option = option;
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height = 28;
        y += 1;
        String text = option.getValue();

        float optionRectWidth = width / 1.1f;
        float optionRectHeight = 14;
        int rectX = (int) (x + 6);
        int rectY = (int) (y + 8);

        GraphicsSystem.drawRoundedRect(rectX, rectY, optionRectWidth, optionRectHeight, 3, ColoringSystem.rgba(20,20,20, 255));
        ClientFonts.clickGui[14].drawString(matrixStack, option.getName(), x + 8, y, -1);


        if (isTyping) {
            drawPulsingText(matrixStack, text, (int) (x + 10), (int) (y + 13));
        } else {
            if (text == null || text.isEmpty()) {
                String description = option.getDescription();
                ClientFonts.clickGui[14].drawString(matrixStack, description, x + 10, y + 13, -1);
            } else {
                ClientFonts.clickGui[14].drawString(matrixStack, text, (int) (x + 10), (int) (y + 13), -1);
            }
        }
    }

    private void drawPulsingText(MatrixStack matrixStack, String text, int x, int y) {
        long currentTime = System.currentTimeMillis();
        int blinkRate = 500;
        boolean showCursor = (currentTime / blinkRate) % 2 == 0;
        ClientFonts.clickGui[14].drawString(matrixStack, text + (showCursor ? "_" : ""), x, y, -1);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouse) {
        if (MathSystem.isHovered(mouseX, mouseY, x + 6, y + 8, width / 1.1f, 14)) {
            isTyping = !isTyping;
            if (isTyping) {
                selectedText = option.getValue();
            }
        } else {
            isTyping = false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }


    @Override
    public void keyTyped(int key, int scanCode, int modifiers) {
        if (isTyping) {
            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                deleteLastCharacter();
            } else if (key == GLFW.GLFW_KEY_ENTER) {
                isTyping = false;
            } else {
                String typedChar = GLFW.glfwGetKeyName(key, scanCode);
                if (typedChar != null && !typedChar.isEmpty()) {
                    option.setValue(option.getValue() + typedChar);

                }
            }
        }
    }

    private void pasteFromClipboard() {
        try {
            selectedText += GLFW.glfwGetClipboardString(Minecraft.getInstance().getMainWindow().getHandle());
            option.setValue(selectedText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void deleteLastCharacter() {
        if (!selectedText.isEmpty()) {
            selectedText = selectedText.substring(0, selectedText.length() - 1);
            option.setValue(selectedText);
        }
    }
    @Override
    public void charTyped(char codePoint, int modifiers) {
        if (isTyping) {
            if (isTextSelected) {
                option.setValue("");
                isTextSelected = false;
            }
            if (SharedConstants.isAllowedCharacter(codePoint)) {
                String currentValue = option.getValue();
                if (currentValue.length() < 12) {
                    option.setValue(currentValue + codePoint);
                }
            }
        }

    }
}
