package minecraft.game.operation.misc;

import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.StringSetting;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;

@Defuse(name = "Client Brand", description = "Меняет бренд клиента", brand = Category.Misc)
public class CustomClientBrand extends Module {

    // Имя зафиксировано
    public static final ModeSetting loginMode123 = new ModeSetting("Бренд", "vanilla", "vanilla", "labymod", "forge", "fabric", "feather", "dsc.gg/aegisclient <-- ЛУЧШИЙ ЧИТ", "Custom");
    public static final StringSetting customText = new StringSetting("Кастомный ник", "AEGIS CLIENT", "Укажите текст").visibleIf(() -> loginMode123.is("Custom"));

    public CustomClientBrand() {
        addSettings(loginMode123, customText);
    }

    public String get() {
        // Return the custom text if the mode is "Custom", otherwise return the selected mode
        if (loginMode123.is("Custom")) {
            return customText.getValue();
        }
        return loginMode123.getValue();
    }
}
