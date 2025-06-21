package minecraft.game.display.clientgui.api.setting;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> setShown(Supplier<Boolean> bool);
}