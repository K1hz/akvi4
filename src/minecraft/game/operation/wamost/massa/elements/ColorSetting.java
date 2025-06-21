package minecraft.game.operation.wamost.massa.elements;


import java.util.function.Supplier;

import minecraft.game.operation.wamost.massa.api.Setting;

public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }
    @Override
    public ColorSetting visibleIf(Supplier<Boolean> bool) {
        return (ColorSetting) super.visibleIf(bool);
    }
}