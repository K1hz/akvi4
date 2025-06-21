package minecraft.game.operation.misc;

import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;

@Defuse(name = "Anti Crash",  description = "AntiCrash", brand = Category.Misc)
public class AntiCrash extends Module {
    public CheckBoxSetting a = new CheckBoxSetting("Alice", true);
    public CheckBoxSetting b = new CheckBoxSetting("HolyWorld", true);
    public AntiCrash() {
        addSettings(a,b);
    }
}