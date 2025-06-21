package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.util.Optional;

import static minecraft.game.advantage.advisee.IMinecraft.mc;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DDBOXCommand implements Command {

    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        // Получаем аргумент как строку
        Optional<String> argOptional = parameters.asString(0);

        // Проверяем, есть ли аргумент
        if (argOptional.isEmpty()) {
            logger.log("Использование: box <on|off>");
            return;
        }

        String arg = argOptional.get().toLowerCase(); // Преобразуем в нижний регистр для удобства

        switch (arg) {
            case "on":
                mc.getRenderManager().setDebugBoundingBox(true);
                logger.log("Боксы включены.");
                break;
            case "off":
                mc.getRenderManager().setDebugBoundingBox(false);
                logger.log("Боксы выключены.");
                break;
            default:
                logger.log("Некорректная команда. " + TextFormatting.UNDERLINE+ "Используйте:" + TextFormatting.GRAY + " box <on | off>");
        }
    }

    @Override
    public String name() {
        return "box";
    }

    @Override
    public String description() {
        return "Включает или выключает хитбоксы у энтити";
    }
}
