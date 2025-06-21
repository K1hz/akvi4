package minecraft.game.interfaces.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import minecraft.game.interfaces.script.ScriptConstructor;

@Getter
@RequiredArgsConstructor
public class Script {
    private final String name;
    private final ScriptConstructor scriptConstructor = ScriptConstructor.create();
}
