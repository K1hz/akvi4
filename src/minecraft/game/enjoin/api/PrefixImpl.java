package minecraft.game.enjoin.api;

import lombok.Getter;
import minecraft.game.enjoin.feature.SetPrefixCommand;
import minecraft.game.enjoin.interfaces.Prefix;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrefixImpl implements Prefix {
    @Getter
    private String prefix = ".";

    public void set(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new IllegalArgumentException("Префикс не может быть пустым!");
        }
        this.prefix = prefix;
    }

    @Override
    public String get() {
        return prefix;
    }
}
