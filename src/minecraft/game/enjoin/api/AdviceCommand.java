package minecraft.game.enjoin.api;

import minecraft.game.enjoin.interfaces.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommand implements Command {

    final CommandProvider commandProvider;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandName = parameters.asString(0).orElseThrow(() -> new CommandException("Вы не указали имя команды"));
        Command command = commandProvider.command(commandName);

        if (!(command instanceof CommandWithAdvice commandWithAdvice)) {
            throw new CommandException(TextFormatting.RED + "К данной команде нет советов!");
        }

        logger.log(TextFormatting.WHITE + "Пример использования команды:");
        for (String advice : commandWithAdvice.adviceMessage()) {
            logger.log(TextFormatting.GRAY + advice);
        }
    }

    @Override
    public String name() {
        return "why";
    }

    @Override
    public String description() {
        return "null";
    }
}
