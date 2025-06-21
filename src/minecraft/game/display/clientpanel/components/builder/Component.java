package minecraft.game.display.clientpanel.components.builder;

import lombok.Getter;
import lombok.Setter;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.display.clientpanel.DropDown;
import minecraft.game.display.clientpanel.Panel;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.api.Setting;

@Getter
@Setter
public class Component implements IBuilder {

    public float x, y, width, height;
    private Panel panel;
    public int background = ColoringSystem.rgb(44,44,44);
    public float radius = 4;
    public float radiusOther = 5;
    public Setting<?> setting;
    public Module module;
    public DropDown parent;
    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public void setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public boolean isHovered(float mouseX, float mouseY, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isVisible() {
        return true;
    }

}
