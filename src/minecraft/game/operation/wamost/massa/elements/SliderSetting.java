package minecraft.game.operation.wamost.massa.elements;


import java.util.function.Supplier;

import minecraft.game.operation.wamost.massa.api.Setting;

public class SliderSetting extends Setting<Float> {

    public float min;
    public float max;
    public float increment;

    public SliderSetting(String name, float defaultVal, float min, float max, float increment) {
        super(name, defaultVal);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    @Override
    public SliderSetting visibleIf(Supplier<Boolean> bool) {
        return (SliderSetting) super.visibleIf(bool);
    }
}