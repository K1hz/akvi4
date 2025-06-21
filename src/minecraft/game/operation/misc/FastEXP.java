package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Items;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.transactions.EventTick;

@Defuse(
        name = "Fast Experience",
        description = "porno",
        brand = Category.Misc
)
public class FastEXP extends Module {
    public FastEXP() {
    }

    @Subscribe
    public void onEvent(EventTick e) {
        if (mc == null || mc.player == null) {
            return;
        }
        Minecraft var10000 = mc;
        if (Minecraft.player.getHeldItemMainhand().getItem() == Items.EXPERIENCE_BOTTLE) {
            mc.rightClickDelayTimer = 1;
        }
    }
}
