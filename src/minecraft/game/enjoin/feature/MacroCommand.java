package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import minecraft.system.AG;
import minecraft.system.managers.macro.MacroManager;
import minecraft.game.enjoin.api.CommandException;
import minecraft.game.advantage.advisee.KeyStorage;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MacroCommand implements Command, MultiNamedCommand, CommandWithAdvice {

    final MacroManager macroManager;
    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElseThrow();
        switch (commandType) {
            case "add" -> addMacro(parameters);
            case "remove" -> removeMacro(parameters);
            case "clear" -> clearMacros();
            case "list" -> printMacrosList();
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear, list");
        }
    }

    @Override
    public String name() {
        return "macro";
    }

    @Override
    public String description() {
        return "Позволяет управлять макросами";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "macro add <name> <key> <message> - Добавить новый макрос",
                commandPrefix + "macro remove <name> - Удалить макрос",
                commandPrefix + "macro list - Получить список макросов",
                commandPrefix + "macro clear - Очистить список макросов",
                TextFormatting.UNDERLINE  + "Пример использования:" + TextFormatting.RED + " " + commandPrefix + "macro add home H /home home"
        );
    }

    @Override
    public List<String> aliases() {
        return List.of("macros");
    }

    private void addMacro(Parameters parameters) {
        String macroName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.GRAY + "Укажите название макроса."));
        String macroKey = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.GRAY + "Укажите кнопку при нажатии которой сработает макрос."));

        String macroMessage = parameters.collectMessage(3);

        if (macroMessage.isEmpty()) {
            throw new CommandException(TextFormatting.RED + "Укажите сообщение, которое будет писать макрос.");
        }
        Integer key = KeyStorage.getKey(macroKey.toUpperCase());

        if (key == null) {
            logger.log("Клавиша " + macroKey + " не найдена!");
            return;
        }

        checkMacroExist(macroName);

        macroManager.addMacro(macroName, macroMessage, key);

        logger.log(TextFormatting.GREEN +
                "Добавлен макрос с названием " + TextFormatting.RED
                + macroName + TextFormatting.GREEN
                + " с кнопкой " + TextFormatting.RED
                + macroKey + TextFormatting.GREEN
                + " с командой " + TextFormatting.RED
                + macroMessage);
    }

    private void removeMacro(Parameters parameters) {
        String macroName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.GRAY + "Укажите название макроса."));

        AG.getInst().getMacroManager().deleteMacro(macroName);

        logger.log(TextFormatting.GREEN + "Макрос " + TextFormatting.RED + macroName + TextFormatting.GREEN + " был успешно удален!");
    }

    private void clearMacros() {
        AG.getInst().getMacroManager().clearList();
        logger.log(TextFormatting.GREEN + "Все макросы были удалены.");
    }

    private void printMacrosList() {
        if (AG.getInst().getMacroManager().isEmpty()) {
            logger.log(TextFormatting.RED + "Список пустой");
            return;
        }
        AG.getInst().getMacroManager().macroList
                .forEach(macro -> logger.log(TextFormatting.WHITE + "Название: " + TextFormatting.GRAY
                        + macro.getName() + TextFormatting.WHITE + ", Команда: " + TextFormatting.GRAY
                        + macro.getMessage() + TextFormatting.WHITE + ", Кнопка: " + TextFormatting.GRAY
                        + macro.getKey()));
    }

    private void checkMacroExist(String macroName) {
        if (macroManager.hasMacro(macroName)) {
            throw new CommandException(TextFormatting.RED + "Макрос с таким именем уже есть в списке!");
        }
    }
}
