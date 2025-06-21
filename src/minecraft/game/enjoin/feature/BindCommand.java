package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import minecraft.system.AG;
import minecraft.game.enjoin.api.CommandException;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.advantage.advisee.KeyStorage;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BindCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "clear" -> clearAllBindings(logger);
            case "list" -> listBoundKeys(logger);
            default ->
                    throw new CommandException(TextFormatting.DARK_RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, clear, list");
        }
    }

    @Override
    public String name() {
        return "bind";
    }

    @Override
    public String description() {
        return "Позволяет очищать бинды на функции";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(
                commandPrefix + "bind list - Получить список биндов",
                commandPrefix + "bind clear - Очистить список биндов",
                TextFormatting.UNDERLINE  + "Пример использования:" + TextFormatting.RED + " " +commandPrefix + "bind clear"
        );
    }


    private void clearAllBindings(Logger logger) {
        AG.getInst().getModuleManager().getModules().forEach(function -> function.setBind(0));
        logger.log(TextFormatting.GREEN + "Все клавиши были отвязаны от модулей");
    }

    private void listBoundKeys(Logger logger) {
        logger.log(TextFormatting.GRAY + "Список всех модулей с биндами:");

        AG.getInst().getModuleManager().getModules().stream()
                .filter(f -> f.getBind() != 0)
                .map(f -> {
                    String keyName = GLFW.glfwGetKeyName(f.getBind(), -1);
                    keyName = keyName != null ? keyName : "";
                    return String.format("%s [%s%s%s]", f.getName(), TextFormatting.RED, keyName, TextFormatting.GRAY);
                })
                .forEach(logger::log);
    }

}
