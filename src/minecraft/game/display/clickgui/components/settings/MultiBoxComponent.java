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
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.system.managers.Theme;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class MultiBoxComponent extends Component {

    final ModeListSetting setting;

    float width = 0;
    float heightPadding = 0;
    float spacing = 2;

    public MultiBoxComponent(ModeListSetting setting) {
        this.setting = setting;
        setHeight(22);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.sfbold.drawText(stack, setting.getName(), getX() + 7, getY() + 2, ColoringSystem.rgb(153, 153, 153), 5.5f, 0.05f);

        float offset = 0;
        float heightoff = 0;
        boolean plused = false;
        boolean anyHovered = false;

        for (CheckBoxSetting text : setting.getValue()) {
            float textWidth = Fonts.sfbold.getWidth(text.getName(), 5.5f, 0.05f) + 2;
            float textHeight = Fonts.sfbold.getHeight(5.5f) + 1;


            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing + 1;
                plused = true;
            }

            if (MathSystem.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10f + heightoff, textWidth, textHeight)) {
                anyHovered = true;
            }

            if (text.getValue()) {
                GraphicsSystem.drawRoundedRect(getX() + 6.5f + offset, getY() + 9.5f + heightoff, textWidth + 2, 9, 1, ColoringSystem.setAlpha(ColoringSystem.rgb(96,96,96), 100));
                Fonts.sfbold.drawText(stack, text.getName(), getX() + 8 + offset, getY() + 11.5f + heightoff, ColoringSystem.rgb(153, 153, 153), 5.5f, 0.07f);
            } else {
                GraphicsSystem.drawRoundedRect(getX() + 6.5f + offset, getY() + 9.5f + heightoff, textWidth + 2, 9, 1, ColoringSystem.setAlpha(ColoringSystem.rgba(31, 31, 31, 310), 100));
                Fonts.sfbold.drawText(stack, text.getName(), getX() + 8 + offset, getY() + 11.5f + heightoff, ColoringSystem.rgb(153, 153, 153), 5.5f, 0.05f);
            }

            offset += textWidth + spacing;
        }

        if (isHovered(mouseX, mouseY)) {
            if (anyHovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }

        width = plused ? getWidth() - 15 : offset;
        setHeight(16 + heightoff);
        heightPadding = heightoff;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float offset = 0;
        float heightoff = 0;

        for (CheckBoxSetting text : setting.getValue()) {
            float textWidth = Fonts.sfbold.getWidth(text.getName(), 5.5f, 0.05f) + 2;
            float textHeight = Fonts.sfbold.getHeight(5.5f) + 1;

            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing + 1;
            }

            if (mouse == 0 && MathSystem.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 10 + heightoff, textWidth, textHeight)) {
                text.setValue(!text.getValue());
                SoundPlayer.playSound("guichangemode.wav");
            }

            offset += textWidth + spacing;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
