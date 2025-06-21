package minecraft.game.enjoin.interfaces;

public interface Command {
    void execute(Parameters parameters);

    String name();

    String description();
}
