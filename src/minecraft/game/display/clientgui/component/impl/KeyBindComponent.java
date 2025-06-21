package minecraft.game.display.clientgui.component.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientgui.api.component.Component;
import minecraft.game.display.clientgui.component.ModuleComponent;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

public class KeyBindComponent extends Component {

    public BindSetting option;
    private boolean bind;
    private boolean isHovered;
    public static KeyBindComponent binding = null;

    public KeyBindComponent(BindSetting option) {
        this.option = option;
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height = 18;
        String bindText;

        if (this.equals(binding)) {
            bindText = "...";
        } else {
            bindText = (option.getValue() == 0 || option.getValue() == -1) ? "N/A" : KeyStorage.getKey(option.getValue());
        }

        if (bindText == null || bindText.isEmpty()) {
            bindText = "N/A";
        }

        float bindWidth = ClientFonts.clickGui[14].getWidth(bindText) + 10;
        float maxWidthForOption = width - bindWidth - 14;
        float endX = x + width - bindWidth - 6;

        float optionWidth = ClientFonts.clickGui[14].getWidth(option.getName());
        boolean fitsInSingleLine = optionWidth <= maxWidthForOption;

        float bindY = fitsInSingleLine ? y + 3 : y + 15;
        if (!fitsInSingleLine) {
            height += 12;
        }

        ClientFonts.clickGui[14].drawString(matrixStack, option.getName(), x + 8, y + 6, -1);

        if (fitsInSingleLine) {
            GraphicsSystem.drawRoundedRect(endX, y + 3, bindWidth, 11, radius, background);
            GraphicsSystem.drawRoundedOutline(endX, y + 3, bindWidth, 11, 0.1f, Vector4f.copy(radius), Vector4i.copy(0xFF282932));
            ClientFonts.clickGui[14].drawCenteredString(matrixStack, bindText, endX + (bindWidth / 2), y + 7, -1);
        } else {
            GraphicsSystem.drawRoundedRect(x + 8, bindY, bindWidth, 11, radius, background);
            GraphicsSystem.drawRoundedOutline(x + 8, bindY, bindWidth, 11, 0.1f, Vector4f.copy(radius), Vector4i.copy(0xFF282932));
            ClientFonts.clickGui[14].drawCenteredString(matrixStack, bindText, x + 8 + (bindWidth / 2), bindY + 4, -1);
        }

        isHovered = MathSystem.isHovered(mouseX, mouseY, fitsInSingleLine ? endX : x + 8, bindY, bindWidth, 11);

        if (isHovered) {
            GLFW.glfwSetCursor(mc.getMainWindow().getHandle(), Cursors.HAND);
        } else {
            GLFW.glfwSetCursor(mc.getMainWindow().getHandle(), Cursors.ARROW);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float bindWidth = ClientFonts.clickGui[14].getWidth((option.getValue() == 0 || option.getValue() == -1) ? "N/A" : KeyStorage.getKey(option.getValue())) + 10;
        float endX = x + width - bindWidth - 6;
        float bindY = (ClientFonts.clickGui[14].getWidth(option.getName()) <= width - bindWidth - 14) ? y + 3 : y + 15;

        boolean isBindRectHovered = MathSystem.isHovered(mouseX, mouseY, endX, bindY, bindWidth, 11);

        if (ModuleComponent.binding != null) {
            return;
        }

        if (!bind && binding == null && isBindRectHovered) {
            binding = this;
            bind = true;
            return;
        }

        if (bind && this.equals(binding)) {
            if (mouseButton == 2) {
                option.set(-98);
            } else if (mouseButton > 2) {
                option.set(-100 + mouseButton);
            } else {
                option.set(mouseButton);
            }
            binding = null;
            bind = false;
        }
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
        if (bind && this.equals(binding)) {
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_KP_DECIMAL) {
                option.set(0);
            } else {
                option.set(keyCode);
            }
            binding = null;
            bind = false;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
}
