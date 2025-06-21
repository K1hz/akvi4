package minecraft.game.enjoin.api;

import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.advantage.advisee.IMinecraft;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MinecraftLogger implements Logger, IMinecraft {
    @Override
    public void log(String message) {
        print(message);
    }
}
