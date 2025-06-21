package minecraft.game.enjoin.api;

import lombok.Value;

@Value
public class CommandException extends RuntimeException {
    String message;
}
