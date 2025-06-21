package minecraft.game.enjoin.interfaces;

import minecraft.game.enjoin.api.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}
