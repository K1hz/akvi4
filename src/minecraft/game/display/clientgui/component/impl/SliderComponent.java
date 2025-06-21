package minecraft.game.display.clientgui.component.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientgui.api.component.Component;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.system.AG;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.glfw.GLFW;

public class SliderComponent extends Component {

    public SliderSetting option;

    public SliderComponent(SliderSetting option) {
        this.option = option;
        this.setting = option;
    }

    private float newValue, lastValue;
    boolean isHovered;
    boolean drag;
    float anim;

    @Override
    public void drawComponent(MatrixStack stack, int mouseX, int mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.sfbold.drawText(stack, setting.getName(), x + 5, y+ 4.5f / 2f + 1, ColoringSystem.rgb(153, 153, 153), 5.5f, 0.05f);
        Fonts.sfbold.drawText(stack, String.valueOf(setting.getValue()), x + width - 6 - Fonts.sfbold.getWidth(String.valueOf(setting.getValue()), 5.5f), y+ 4.5f / 2f + 1, ColoringSystem.rgb(153, 153, 153), 5.5f, 0.05f);

        GraphicsSystem.drawRoundedRect(x + 5, y+ 12, width - 10, 5, 0.6f, ColoringSystem.rgba(55,55,55,100));
        anim = MathSystem.fast(anim, (width - 10) * (option.getValue() - option.min) / (option.max - option.min), 20);
        float sliderWidth = anim;
        GraphicsSystem.drawRoundedRect(x + 5, y+ 12, sliderWidth, 5, 1,
        new Vector4i(
                AG.getInst().getStyleManager().getCurrentStyle().getSecondColor().getRGB(),
                AG.getInst().getStyleManager().getCurrentStyle().getSecondColor().getRGB(),
                AG.getInst().getStyleManager().getCurrentStyle().getFirstColor().getRGB(),
                AG.getInst().getStyleManager().getCurrentStyle().getFirstColor().getRGB()));

                GraphicsSystem.drawCircle(x + 5 + sliderWidth, y+ 14.5f, 6.5f, ColoringSystem.rgba(255, 255, 255,255));
        //GraphicsSystem.drawShadowCircle(x + 5 + sliderWidth, y+ 13, 10, Theme.rectColor);
        if (drag) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(),
                    GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
            float newValue = (float) MathHelper.clamp(
                    MathSystem.round((mouseX - x - 5) / (width - 10) * (option.max - option.min) + option.min,
                            option.increment), option.min, option.max);
            if (newValue != lastValue) {
                option.setValue(newValue);
                lastValue = newValue;
                SoundPlayer.playSound("guislidermove.wav");
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (MathSystem.isHovered(mouseX, mouseY, x + 5, y+ 10, width - 10, 6)) {
                if (!isHovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.RESIZEH);
                    isHovered = true;
                }
            } else {
                if (isHovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    isHovered = false;
                }
            }
        }
    }



    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovered) { // Проверяем левую кнопку и наведение на слайдер
            drag = true;
        }
    }



    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        drag = false;
    }

    @Override
    public void keyTyped(int keyCode, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
    }

    public void close() {
        drag = false;
        isHovered = false;
    }
}
