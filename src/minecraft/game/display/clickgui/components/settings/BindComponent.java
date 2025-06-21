package minecraft.game.display.clickgui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;

import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.make.Cursors;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.display.clickgui.components.builder.Component;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.system.managers.Theme;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class BindComponent extends Component {

    final BindSetting setting;

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        this.setHeight(16);
    }

    boolean activated;
    boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 6.5f / 2f + 1, ColoringSystem.rgb(153, 153, 153), 6.5f, 0.05f);
        String bind = KeyStorage.getKey(setting.getValue());

        if (bind == null || setting.getValue() == -1) {
            bind = "Нету";
        }
        boolean next = Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) >= 16;
        float x = next ? getX() + 5 : getX() + getWidth() - 7 - Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f);
        float y = getY() + 5.5f / 2f + (5.5f / 2f) + (next ? 8 : 0);
        GraphicsSystem.drawRoundedRect(x - 2 + 0.5F, y - 2, Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4, 5.5f + 4, 2, Theme.mainRectColor);
        Fonts.montserrat.drawText(stack, bind, x, y, activated ? Theme.textColor : ColoringSystem.rgb(153, 153, 153), 5.5f, activated ? 0.1f : 0.05f);

        if (isHovered(mouseX, mouseY)) {
            if (MathSystem.isHovered(mouseX, mouseY, x - 2 + 0.5F, y - 2, Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4, 5.5f + 4)) {
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
        setHeight(next ? 22 : 16);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (activated) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                setting.setValue(-1);
                SoundPlayer.playSound("guibindreset.wav");
                activated = false;
                return;
            }
            setting.setValue(key);
            SoundPlayer.playSound("guibinding.wav");
            activated = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY) && mouse == 0) {
            activated = !activated;
        }

        if (activated && mouse >= 1) {
            System.out.println(-100 + mouse);
            setting.setValue(-100 + mouse);
            activated = false;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}
