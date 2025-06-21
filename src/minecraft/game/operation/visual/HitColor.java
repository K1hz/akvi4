package minecraft.game.operation.visual;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import minecraft.game.operation.combat.DDPOTION;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.ColorSetting;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Hit Color",description = "123", brand = Category.Visual)
public class HitColor extends Module {

    private final ColorSetting color = new ColorSetting("Цвет", 1);
    public HitColor() {
        this.addSettings(

                this.color
        );
    }
}