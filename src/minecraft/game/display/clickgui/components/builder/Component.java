package minecraft.game.display.clickgui.components.builder;

import lombok.Getter;
import lombok.Setter;
import minecraft.game.display.clickgui.Panel;

@Getter
@Setter
public class Component implements IBuilder {

    private float x, y, width, height;
    private Panel panel;

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isHovered(float mouseX, float mouseY, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isVisible() {
        return true;
    }

}
