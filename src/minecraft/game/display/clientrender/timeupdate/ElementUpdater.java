package minecraft.game.display.clientrender.timeupdate;

import minecraft.game.transactions.EventUpdate;
import minecraft.game.advantage.advisee.IMinecraft;

public interface ElementUpdater extends IMinecraft {

    void update(EventUpdate e);
}
