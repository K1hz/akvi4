package minecraft.game.display.clientpanel2.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;

import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel2.components.builder.Component;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;

import static minecraft.game.advantage.advisee.IMinecraft.mc;

public class BindComponent extends Component {

    public BindSetting option;
    private boolean bind;
    private boolean isHovered;
    public static BindComponent binding = null;

    public BindComponent(BindSetting option) {
        this.option = option;
    }
    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY) {
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
            GraphicsSystem.drawRoundedRect(endX, y + 3, bindWidth, 11, 1, ColoringSystem.rgba(24,24,24, 255));
            GraphicsSystem.drawRoundedOutline(endX, y + 3, bindWidth, 11, 0.1f, Vector4f.copy(1), Vector4i.copy(0xFF282932));
            ClientFonts.clickGui[14].drawCenteredString(matrixStack, bindText, endX + (bindWidth / 2), y + 7, -1);
        } else {
            GraphicsSystem.drawRoundedRect(x + 8, bindY, bindWidth, 11, 1, ColoringSystem.rgba(24,24,24, 255));
            GraphicsSystem.drawRoundedOutline(x + 8, bindY, bindWidth, 11, 0.1f, Vector4f.copy(1), Vector4i.copy(0xFF282932));
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
    public void mouseClick(float mouseX, float mouseY, int mouseButton) {
        float bindWidth = ClientFonts.clickGui[14].getWidth((option.getValue() == 0 || option.getValue() == -1) ? "Не найдено" : KeyStorage.getKey(option.getValue())) + 10;
        float endX = x + width - bindWidth - 6;
        float bindY = (ClientFonts.clickGui[14].getWidth(option.getName()) <= width - bindWidth - 14) ? y + 3 : y + 15;

        boolean isBindRectHovered = MathSystem.isHovered(mouseX, mouseY, endX, bindY, bindWidth, 11);


        if (!bind && binding == null && isBindRectHovered) {
            binding = this;
            bind = true;
            return;
        }

        if (bind && this.equals(binding)) {
            if (mouseButton == 2) {
                option.setValue(-98);
            } else if (mouseButton > 2) {
                option.setValue(-100 + mouseButton);
            } else {
                option.setValue(mouseButton);
            }
            binding = null;
            bind = false;
        }
    }
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (bind && this.equals(binding)) {
            if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_KP_DECIMAL) {
                option.setValue(0);
            } else {
                option.setValue(keyCode);
            }
            binding = null;
            bind = false;
        }
    }


    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }
    @Override
    public boolean isVisible() {
        return option.visible.get();
    }
}
