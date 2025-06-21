package minecraft.game.operation.wamost.massa.elements;

import java.util.function.Supplier;

import minecraft.game.operation.wamost.massa.api.Setting;

public class BindSetting extends Setting<Integer> {
    public BindSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public BindSetting visibleIf(Supplier<Boolean> bool) {
        return (BindSetting) super.visibleIf(bool);
    }
}
