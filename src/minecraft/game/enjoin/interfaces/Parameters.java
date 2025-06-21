package minecraft.game.enjoin.interfaces;

import java.util.Optional;

public interface Parameters {
    int size();

    Optional<Integer> asInt(int index);

    Optional<Float> asFloat(int index);

    Optional<Double> asDouble(int index);


    Optional<String> asString(int index);

    String collectMessage(int startIndex);
}
