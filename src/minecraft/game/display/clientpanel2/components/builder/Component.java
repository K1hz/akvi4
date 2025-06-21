package minecraft.game.display.clientpanel2.components.builder;


import lombok.Getter;
import lombok.Setter;
import minecraft.game.display.clientpanel2.Panel;
import minecraft.game.operation.wamost.massa.api.Setting;

@Getter
@Setter
public class Component implements IBuilder {

//    private float x, y, width, height;
    public float x, y, width, height;
    private Panel panel;
    public float radiusDEV = 4;
    public float radiusOtherDEV = 5;
    public Setting<?> settingDEV;

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isHovered(float mouseX, float mouseY, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void setPosition(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isVisible() {
        return true;
    }

}
