package minecraft.game.enjoin.interfaces;

import minecraft.game.enjoin.api.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
