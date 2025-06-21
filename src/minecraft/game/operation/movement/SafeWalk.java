package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.advantage.luvvy.MovementSystem;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

@Defuse(name = "Safe Walk",description = "ХУЙ", brand = Category.Movement)
public class SafeWalk extends Module {
    @Subscribe
    private void onUpdate(EventUpdate e) {
    	assert mc.player != null;
        BlockPos pos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 1.0, mc.player.getPosZ());
        assert mc.world != null;
        mc.gameSettings.keyBindSneak.setPressed((MovementSystem.isBlockUnder(0.005f) || mc.world.getBlockState(pos).getBlock() == Blocks.AIR) && !(mc.player.isInWater() || mc.player.isInLava()));
        
    }
}
