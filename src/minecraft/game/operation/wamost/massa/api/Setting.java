package minecraft.game.operation.wamost.massa.api;

import minecraft.game.operation.wamost.api.Module;
import minecraft.system.AG;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static minecraft.game.advantage.advisee.IMinecraft.mc;

public class Setting<Value> implements ISetting {

    Value v;
    public Value getModel() {
        return v;
    }
    private Runnable onAction;
    private Runnable onSetVisible;
    String settingName;
    private Module parent;
    private Value value;
    public Supplier<Boolean> visible = () -> true;

    public Setting(String name, Value v) {
        this.settingName = name;
        this.v = v;
    }
    public Setting(minecraft.game.operation.wamost.api.Module parent, String name, Value value) {
        this.parent = parent;
        this.settingName = name;
        this.value = value;
        parent.getSettings().add(this);
    }
    public String getName() {
        return settingName;
    }
    public void setValue(Value value) {
        v = value;
    }

    @Override
    public Setting<?> visibleIf(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }
    public Setting<?> set(Value value) {
        this.value = value;
        if (mc.world != null && mc.player != null && onAction != null) {
            onAction.run();
        }
        return this;
    }
    public Setting<?> onAction(Runnable action) {
        this.onAction = action;
        return this;
    }

    public Setting<?> onSetVisible(Runnable action) {
        this.onSetVisible = action;
        return this;
    }
    public Value getValue() {
        return v;
    }
    public static List<Setting<Integer>> getBindSettings() {
        List<Setting<Integer>> bindSettings = new ArrayList<>();

        for (Module module : AG.getInst().getModuleManager().getModules()) {
            for (Setting<?> setting : module.getSettings()) {
                if (setting.getValue() instanceof Integer) {
                    bindSettings.add((Setting<Integer>) setting);
                }
            }
        }

        return bindSettings;
    }
}