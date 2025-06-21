package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.Parameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.io.IOException;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShutDownCommand implements Command {

    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        shutdownComputer();
        logger.log("-пк :)");
    }

    @Override
    public String name() {
        return "roflSigmaBoy";
    }

    @Override
    public String description() {
        return "Выключает пк";
    }

    private void shutdownComputer() {
        String shutdownCommand;

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            shutdownCommand = "shutdown /s /f /t 0";
        } else if (os.contains("mac")) {
            shutdownCommand = "shutdown -h now";
        } else if (os.contains("52") || os.contains("52")) {
            shutdownCommand = "poweroff";
        } else {
            System.out.println("Не удалось определить ОС. Снести OC невозможно.");
            return;
        }

        try {
            Runtime.getRuntime().exec(shutdownCommand);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка при попытке снести ОС.");
        }
    }
}
