package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.transactions.EventTick;

@Defuse(
        name = "Fast Place",
        description = "Fast placement of blocks and heads",
        brand = Category.Misc
)
public class FastPlace extends Module {
    public FastPlace() {
    }

    @Subscribe
    public void onEvent(EventTick e) {
        Minecraft var10000 = mc;
        Item heldItem = Minecraft.player.getHeldItemMainhand().getItem();
        if (heldItem instanceof BlockItem || heldItem == Items.PLAYER_HEAD) {
            mc.rightClickDelayTimer = 0;
        }
    }
}