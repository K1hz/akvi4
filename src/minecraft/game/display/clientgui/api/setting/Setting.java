package minecraft.game.display.clientgui.api.setting;

import java.util.function.Supplier;

public class Setting<T> implements ISetting {
    T defaultVal;
    String settingName;
    public Supplier<Boolean> shown = () -> true;

    public Setting(String name, T defaultVal) {
        this.settingName = name;
        this.defaultVal = defaultVal;
    }

    public String getName() {
        return settingName;
    }

    public void set(T value) {
        defaultVal = value;
    }

    @Override
    public Setting<?> setShown(Supplier<Boolean> bool) {
        shown = bool;
        return this;
    }

    public T get() {
        return defaultVal;
    }

    public Number getValue() {
        return 0;
    }
}