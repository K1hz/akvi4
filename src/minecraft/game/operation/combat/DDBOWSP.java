package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.item.BowItem;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

@Defuse(name = "Bow Spammer",description = "Спамит стрелами из лука", brand = Category.Misc)
public class DDBOWSP extends Module {

    private final SliderSetting delay = new SliderSetting("Задержка", 1.5f, 1, 5, 0.1f);

    public DDBOWSP() {
        addSettings(delay);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof BowItem && mc.player.isHandActive() && mc.player.getItemInUseMaxCount() >= delay.getValue()) {
            mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, new BlockPos(0, 0, 0), mc.player.getHorizontalFacing()));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.stopActiveHand();
        }
    }

}
