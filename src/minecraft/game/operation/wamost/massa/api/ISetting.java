package minecraft.game.operation.wamost.massa.api;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> visibleIf(Supplier<Boolean> bool);
}