package minecraft.game.enjoin.api;

import minecraft.game.enjoin.interfaces.Logger;

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("message = " + message);
    }
}
