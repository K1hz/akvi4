package minecraft.game.enjoin.feature;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.MultiNamedCommand;
import minecraft.game.enjoin.interfaces.Parameters;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.ModuleManager;
import minecraft.system.AG;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.text.Normalizer;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ToggleCommand implements Command, MultiNamedCommand, SuggestableCommand {
    Logger logger;

    // Нормализация строки (удаление различий в символах)
    private String normalizeString(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
    }

    @Override
    public void execute(Parameters parameters) {
        if (parameters.size() == 0) {
            logger.log(TextFormatting.RED + "Укажите имя функции. Используйте " + TextFormatting.GRAY + ".toggle <название>");
            return;
        }

        String input = parameters.collectMessage(0).toLowerCase();
        ModuleManager moduleManager = AG.getInst().getModuleManager();

        // Приводим строку ввода к нормализованному виду
        String normalizedInput = normalizeString(input);

        Module matched = moduleManager.getModules().stream()
                .filter(m -> normalizeString(m.getName()).equals(normalizedInput))
                .findFirst()
                .orElse(null);

        if (matched == null) {
            logger.log(TextFormatting.RED + "Функция \"" + input + "\" не найдена.");
            return;
        }

        matched.toggle();

        String status = matched.isEnabled() ? TextFormatting.GREEN + "включена" : TextFormatting.RED + "выключена";
        logger.log(TextFormatting.GRAY + "Функция " + TextFormatting.YELLOW + matched.getName() + TextFormatting.GRAY + " теперь " + status + TextFormatting.GRAY + ".");
    }

    @Override
    public String name() {
        return "toggle";
    }

    @Override
    public String description() {
        return "Включает/выключает функцию по имени.";
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("t");
    }

    @Override
    public List<String> suggestions() {
        return AG.getInst().getModuleManager().getModules().stream()
                .map(Module::getName)
                .collect(Collectors.toList());
    }
}
