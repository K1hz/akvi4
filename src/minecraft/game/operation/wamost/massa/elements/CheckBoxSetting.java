package minecraft.game.operation.wamost.massa.elements;


import java.util.function.Supplier;

import minecraft.game.operation.wamost.massa.api.Setting;

public class CheckBoxSetting extends Setting<Boolean> {
    public float anim;
    public CheckBoxSetting(String name, Boolean defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public CheckBoxSetting visibleIf(Supplier<Boolean> bool) {
        return (CheckBoxSetting) super.visibleIf(bool);
    }

}