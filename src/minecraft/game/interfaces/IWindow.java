package minecraft.game.interfaces;

import minecraft.game.advantage.advisee.IMinecraft;
import org.joml.Vector2d;

public interface IWindow extends IMinecraft {
    default Vector2d scaled() {
        return ScaleMath.getMouse(mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
    }

}