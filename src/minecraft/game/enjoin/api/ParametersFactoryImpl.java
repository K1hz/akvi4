package minecraft.game.enjoin.api;

import minecraft.game.enjoin.interfaces.Parameters;
import minecraft.game.enjoin.interfaces.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}
