package minecraft.game.display.clientgui.component.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientgui.ClickGui;
import minecraft.game.display.clientgui.api.component.Component;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.system.AG;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.util.Easings;

public class BooleanComponent extends Component {

    public CheckBoxSetting option;
    private float animationProgress;
    private boolean isHovered;

    public BooleanComponent(CheckBoxSetting option) {
        this.option = option;
        this.setting = option;
    }

    @Override
    public void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY) {
        height = 15;
        float off = 2;

        animationProgress += (option.getValue() ? 1 : -1) * 0.1f;
        animationProgress = MathHelper.clamp(animationProgress, 0, 1);

        float switchWidth = 22;
        float switchHeight = 9;
        float switchX = x + width - switchWidth - 5;
        float switchY = y + off;

        float circleSize = 7;
        float circleX = Math.max(switchX + 6, Math.min(switchX  + (switchWidth - circleSize), switchX + (animationProgress * (switchWidth - circleSize))) + 2);
        float circleY = switchY + (switchHeight - circleSize) / 2 + 3.5f;

        int color = ColoringSystem.getColor(1);
        if (option.getValue()) {
            GraphicsSystem.drawRoundedRect(switchX, switchY, switchWidth, switchHeight, 3, new Vector4i(
                    ColoringSystem.setAlpha(AG.getInst().getStyleManager().getCurrentStyle().getSecondColor().getRGB(), 215),
                    ColoringSystem.setAlpha(AG.getInst().getStyleManager().getCurrentStyle().getSecondColor().getRGB(), 215),
                    ColoringSystem.setAlpha(AG.getInst().getStyleManager().getCurrentStyle().getFirstColor().getRGB(), 215),
                    ColoringSystem.setAlpha(AG.getInst().getStyleManager().getCurrentStyle().getFirstColor().getRGB(), 215)));
        } else {
            GraphicsSystem.drawRoundedRect(switchX, switchY, switchWidth, switchHeight, 3, ColoringSystem.rgba(24,24,24, 255));
        }

        GraphicsSystem.drawCircle(circleX, circleY, circleSize, ColoringSystem.rgb(255, 255, 255));

        ClientFonts.clickGui[14].drawString(matrixStack, option.getName(), x + 8f, y + 3 + off, -1);

        isHovered = MathSystem.isHovered(mouseX, mouseY, switchX, switchY, switchWidth, switchHeight);
        if (isHovered) {
            GLFW.glfwSetCursor(mc.getMainWindow().getHandle(), Cursors.HAND);
        } else {
            GLFW.glfwSetCursor(mc.getMainWindow().getHandle(), Cursors.ARROW);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && MathSystem.isHovered(mouseX, mouseY, x + width - width , y + height  - 12 / 2f - height / 2f, width,
                height)) {
            option.setValue(!option.getValue());

        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {}

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {}

    @Override
    public void charTyped(char codePoint, int modifiers) {}
}
