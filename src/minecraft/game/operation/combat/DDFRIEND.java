package minecraft.game.operation.combat;

import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventPacket;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CUseEntityPacket;
import com.google.common.eventbus.Subscribe;

@Defuse(name = "No Friend Damage",description = "Не дает бить друзей", brand = Category.Misc)
public class DDFRIEND extends Module {
	
	// дайте мне по роже пж
	
	@Subscribe
    public void onEvent(EventPacket event) {
        if (event.getPacket() instanceof CUseEntityPacket) {
            CUseEntityPacket cUseEntityPacket = (CUseEntityPacket) event.getPacket();
            Entity entity = cUseEntityPacket.getEntityFromWorld(mc.world);
            if (entity instanceof RemoteClientPlayerEntity &&
                    FriendManager.isFriend(entity.getName().getString()) &&
                    cUseEntityPacket.getAction() == CUseEntityPacket.Action.ATTACK) {
                event.cancel();
            }
        }
    }
	
}
