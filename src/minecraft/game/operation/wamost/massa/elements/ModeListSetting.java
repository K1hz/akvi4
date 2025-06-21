package minecraft.game.operation.wamost.massa.elements;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import minecraft.game.operation.wamost.massa.api.Setting;

public class ModeListSetting extends Setting<List<CheckBoxSetting>> {
    public ModeListSetting(String name, CheckBoxSetting... strings) {
        super(name, Arrays.asList(strings));
    }

    public CheckBoxSetting is(String settingName) {
        return getValue().stream().filter(booleanSetting -> booleanSetting.getName().equalsIgnoreCase(settingName)).findFirst().orElse(null);
    }

    public CheckBoxSetting get(int index) {
        return getValue().get(index);
    }

    public String getNames() {
        List<String> includedOptions = new ArrayList<>();
        for (CheckBoxSetting option : getValue()) {
            if (option.getValue()) {
                includedOptions.add(option.getName());
            }
        }
        return String.join(", ", includedOptions);
    }

    @Override
    public ModeListSetting visibleIf(Supplier<Boolean> bool) {
        return (ModeListSetting) super.visibleIf(bool);
    }
}