package minecraft.game.enjoin.api;

import minecraft.game.enjoin.interfaces.AdviceCommandFactory;
import minecraft.game.enjoin.interfaces.CommandProvider;
import minecraft.game.enjoin.interfaces.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommandFactoryImpl implements AdviceCommandFactory {

    final Logger logger;
    @Override
    public AdviceCommand adviceCommand(CommandProvider commandProvider) {
        return new AdviceCommand(commandProvider, logger);
    }
}
