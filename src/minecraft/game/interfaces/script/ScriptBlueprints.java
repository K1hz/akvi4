package minecraft.game.interfaces.script;

import minecraft.game.interfaces.entity.Script;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ScriptBlueprints {
    private final List<Script> scripts = new CopyOnWriteArrayList<>();

    public Script createScript(String name) {
        Script script = new Script(name);
        scripts.add(script);
        return script;
    }

    public Optional<Script> getScript(String name) {
        return scripts.stream()
                .filter(script -> script.getName().equals(name))
                .findFirst();
    }

    public void removeScript(String name) {
        scripts.removeIf(script -> script.getName().equals(name));
    }

    public void clearAll() {
        scripts.clear();
    }

    public void updateAll() {
        scripts.forEach(script -> script.getScriptConstructor().update());
    }
}
