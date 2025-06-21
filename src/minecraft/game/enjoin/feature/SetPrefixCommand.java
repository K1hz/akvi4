package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.api.PrefixImpl;
import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SetPrefixCommand implements Command {

    final Logger logger;
    final PrefixImpl prefixImpl;

    @Override
    public void execute(Parameters parameters) {
        Optional<String> argOptional = parameters.asString(0);

        if (argOptional.isEmpty()) {
            logger.log("Пример использования: prefix set <префикс>");
            return;
        }

        String newPrefix = argOptional.get();

        if (newPrefix.length() > 3) {
            logger.log("Ошибка: префикс не может быть длиннее 3 символов!");
            return;
        }

        prefixImpl.set(newPrefix); // Устанавливаем новый префикс
        logger.log("Префикс изменен на: " + TextFormatting.RED + newPrefix);
    }

    @Override
    public String name() {
        return "prefix";
    }

    @Override
    public String description() {
        return "Устанавливает новый префикс для команд";
    }
}
