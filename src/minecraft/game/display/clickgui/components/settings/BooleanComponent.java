package minecraft.game.display.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;

import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.display.clickgui.components.builder.Component;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

/**
 * BooleanComponent
 */
public class BooleanComponent extends Component {

    private final CheckBoxSetting setting;


    public BooleanComponent(CheckBoxSetting setting) {
        this.setting = setting;
        setHeight(16);
        animation = animation.animate(setting.getValue() ? 1 : 0, 0.5f, Easings.CIRC_OUT);
    }

    private Animation animation = new Animation();
    private float width, height;
    private boolean hovered = false;
    private final ResourceLocation booleansetting = new ResourceLocation("render/images/check.png");

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {

        super.render(stack, mouseX, mouseY);
        animation.update();
        Fonts.sfbold.drawText(stack, setting.getName(), getX() + 7, getY() + 6.5f / 2f + 1, ColoringSystem.rgb(153, 153, 153), 6.5f, 0.02f);

        width = 15;
        height = 7;
        GraphicsSystem.drawRoundedRect(getX() + getWidth() - width - 1, getY() + getHeight() / 2f - height / 2f-1.5f, width-5, height+3, 3f, ColoringSystem.rgba(55,55,55, 170)); //ColoringSystem.setAlpha(ColoringSystem.rgb(96,96,96), 100)
        int color = ColoringSystem.interpolate(ColoringSystem.rgb(153, 153, 153),ColoringSystem.rgb(153, 153, 153), 1 - (float) animation.getValue());
        GraphicsSystem.drawImage(booleansetting, getX() + getWidth() - width, getY() - 1.5f + getHeight() / 2f - height / 2f + 2f, 8.0f, 8.0f, ColoringSystem.setAlpha(color, (int) (125 * animation.getValue())));

        if (isHovered(mouseX, mouseY)) {
            if (MathSystem.isHovered(mouseX, mouseY, getX() + getWidth() - width, getY() + getHeight() / 2f - height / 2f , width,
                    height)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
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
        if (mouse == 0 && MathSystem.isHovered(mouseX, mouseY, getX() + getWidth() - width , getY() + getHeight() / 2f - height / 2f, width,
                height)) {
            setting.setValue(!setting.getValue());
            animation = animation.animate(setting.getValue() ? 1 : 0, 0.2f, Easings.CIRC_OUT);
            SoundPlayer.playSound(setting.getValue() ? "guienablecheckbox.wav" : "guidisablecheckbox.wav");
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }

}