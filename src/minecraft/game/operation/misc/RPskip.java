package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.transactions.EventPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;

@Defuse(name = "SRP Spoof",description = "PENIS", brand = Category.Misc)
public class RPskip extends Module {

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof CResourcePackStatusPacket wrapper) {
            wrapper.action = CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED;
        }
    }
}
