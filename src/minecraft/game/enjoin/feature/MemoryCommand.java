package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.MultiNamedCommand;
import minecraft.game.enjoin.interfaces.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MemoryCommand implements Command, MultiNamedCommand {

    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        System.gc();
        logger.log("Очистил память.");
    }

    @Override
    public String name() {
        return "memory";
    }

    @Override
    public String description() {
        return "Очищает память";
    }

    @Override
    public List<String> aliases() {
        return List.of("gc");
    }
}
