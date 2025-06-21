package minecraft.game.interfaces;


import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.system.AG;

public interface IAccess extends IMinecraft, IWindow, IMouse, ITheme {
    AG ag = AG.getInst();
}