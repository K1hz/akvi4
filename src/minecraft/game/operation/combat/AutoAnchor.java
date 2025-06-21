package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.transactions.EventBlockPlase;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

@Defuse(name = "Auto Anchor", description = "123", brand = Category.Combat)
public class AutoAnchor extends Module {

    BlockPos pos;
    int ticksAction, hotSlot;

    @Subscribe
    public void onPlaceBlock(EventBlockPlase event) {
        if (event.getStack().getItem() != Item.getItemFromBlock(Blocks.RESPAWN_ANCHOR)) return;
        if (pos != null) return;
        if (mc.player.getHeldItemOffhand().getItem() == Item.getItemFromBlock(Blocks.GLOWSTONE)) return;
        pos = event.getPosition();
        hotSlot = getSlotForGlowstone();
        ticksAction = 0;
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (pos == null) return;
        switch (ticksAction) {
            case 0 -> {
                if (mc.player.isSneaking())
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SNEAKING));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(hotSlot));
                rClickBlockPos(pos);
            }
            case 1 -> {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
            }
            case 2 -> {
                rClickBlockPos(pos);
                if (mc.player.isSneaking())
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SNEAKING));
            }
        }
        if (ticksAction >= 2) pos = null;
        ++ticksAction;
    }

    private void rClickBlockPos(BlockPos pos) {
        mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(mc.objectMouseOver.getHitVec(), Direction.UP, pos, true)));
        mc.player.swingArm(Hand.MAIN_HAND);
    }

    private int getSlotForGlowstone() {
        for (int i = 0; i < 8; ++i)
            if (mc.player.inventory.getStackInSlot(i).getItem() == Item.getItemFromBlock(Blocks.GLOWSTONE)) return i;
        return -1;
    }
}
