package minecraft.game.display.clientpanel2.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.display.clientpanel2.components.builder.Component;
import minecraft.game.operation.visual.Hud;
import minecraft.system.AG;
import minecraft.system.styles.StyleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.system.managers.Theme;

/**
 * SliderComponent
 */
public class SliderComponent extends Component {

    private final SliderSetting setting;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        this.setHeight(18);
    }
    private float newValue, lastValue;
    private float anim;
    private boolean drag;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.sfui.drawText(stack, setting.getName(), getX() + 5, getY() + 4.5f / 2f + 1, ColoringSystem.rgba(255,255,255,255), 6f, 0.05f);
        Fonts.sfui.drawText(stack, String.valueOf(setting.getValue()), getX() + getWidth() - 5 - Fonts.sfui.getWidth(String.valueOf(setting.getValue()), 5.5f), getY() + 4.5f / 2f + 1, ColoringSystem.rgba(255,255,255,255), 5.5f, 0.05f);
        StyleManager styleManager = AG.getInst().getStyleManager();
        GraphicsSystem.drawRoundedRect(getX() + 5, getY() + 11, getWidth() - 10, 6, 1, ColoringSystem.rgba(24,24,24, 255));

        anim = MathSystem.fast(anim, (getWidth() - 10) * (setting.getValue() - setting.min) / (setting.max - setting.min), 20);
        float sliderWidth = anim;

        GraphicsSystem.drawRoundedRect(getX() + 4.5f, getY() + 11, sliderWidth, 6, 1, new Vector4i(
                ColoringSystem.setAlpha(Hud.getColor(1), 215),
                ColoringSystem.setAlpha(Hud.getColor(90), 215),
                ColoringSystem.setAlpha(Hud.getColor(180), 215),
                ColoringSystem.setAlpha(Hud.getColor(360), 215)));

        if (drag) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(),
                    GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
            float newValue = (float) MathHelper.clamp(
                    MathSystem.round((mouseX - getX() - 5) / (getWidth() - 10) * (setting.max - setting.min) + setting.min,
                            setting.increment), setting.min, setting.max);
            if (newValue != lastValue) {
                setting.setValue(newValue);
                lastValue = newValue;
                SoundPlayer.playSound("guislidermove.wav");
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (MathSystem.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 5)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.RESIZEH);
                    hovered = true;
                }
            } else {
                if (hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    hovered = false;
                }
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        if (MathSystem.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 5)) {
            drag = true;
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        // TODO Auto-generated method stub
        drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}