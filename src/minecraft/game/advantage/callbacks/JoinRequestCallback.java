package minecraft.game.advantage.callbacks;

import com.sun.jna.Callback;
import minecraft.game.advantage.rpc.DiscordUser;

public interface JoinRequestCallback extends Callback {
    void apply(DiscordUser var1);
}