package minecraft.game.display.clientgui.api.component;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.display.clientgui.ClickGui;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.api.Setting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;

public abstract class Component implements IComponent, IMinecraft {

    public float x, y, width, height;
    public Module module;
    public ClickGui parent;
    public Setting<?> setting;
    public SliderSetting settingslider;

    public int background = ColoringSystem.rgb(20,20,20);
    public float radius = 4;
    public float radiusOther = 5;

    public boolean isHovered(int mouseX, int mouseY, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY, float x,float y, float width, float height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public boolean isHovered(int mouseX, int mouseY, float height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }

    public void setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public abstract void drawComponent(MatrixStack matrixStack, int mouseX, int mouseY);

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public abstract void mouseReleased(int mouseX, int mouseY, int mouseButton);

    @Override
    public abstract void keyTyped(int keyCode, int scanCode, int modifiers);

    @Override
    public abstract void charTyped(char codePoint, int modifiers);

    public void onConfigUpdate() {
    }
}
