package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.MultiNamedCommand;
import minecraft.game.enjoin.interfaces.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListCommand implements Command, MultiNamedCommand {

    final List<Command> commands;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        logger.log(TextFormatting.RED + "why" + TextFormatting.WHITE + " | " + TextFormatting.GRAY + "Помогает узнать как использовать команду");
        for (Command command : commands) {
            if (command == this) {
                continue;
            }

            String alias = ""; // потом доделаю

            logger.log(TextFormatting.RED + command.name() + TextFormatting.WHITE + " | " + TextFormatting.GRAY + command.description());
        }
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "Выдает список всех команд";
    }

    @Override
    public List<String> aliases() {
        return List.of("", "help");
    }
}
