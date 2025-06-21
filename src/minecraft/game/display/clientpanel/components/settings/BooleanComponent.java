package minecraft.game.display.clientpanel.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector2i;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientpanel.components.builder.Component;
import minecraft.game.operation.visual.Hud;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.system.AG;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import static minecraft.game.advantage.advisee.IMinecraft.mc;

public class BooleanComponent extends Component {

    public CheckBoxSetting option;
    private float animationProgress;
    private boolean isHovered;
    private Animation animation = new Animation();

    public BooleanComponent(CheckBoxSetting option) {
        this.option = option;
        this.setting = option;
    }

    @Override
    public void render(MatrixStack matrixStack, float mouseX, float mouseY) {
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
            GraphicsSystem.drawRoundedRect(switchX, switchY, switchWidth, switchHeight, radius, new Vector4i(
                    ColoringSystem.setAlpha(Hud.getColor(1), 215),
                    ColoringSystem.setAlpha(Hud.getColor(90), 215),
                    ColoringSystem.setAlpha(Hud.getColor(180), 215),
                    ColoringSystem.setAlpha(Hud.getColor(360), 215)));
        } else {
            GraphicsSystem.drawRoundedRect(switchX, switchY, switchWidth, switchHeight, radius, ColoringSystem.rgba(16,16,16, 45));
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
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (mouse == 0 && MathSystem.isHovered(mouseX, mouseY, getX() + getWidth() - width , getY() + getHeight() / 2f - height / 2f, width,
                height)) {
            option.setValue(!option.getValue());
            animation = animation.animate(option.getValue() ? 1 : 0, 0.2f, Easings.CIRC_OUT);
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }
    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
