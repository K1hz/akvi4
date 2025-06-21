package net.minecraft.block;

import minecraft.game.operation.movement.MoveHelper;
import minecraft.system.AG;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import static minecraft.game.advantage.advisee.IMinecraft.mc;

public class WebBlock extends Block
{
	
    public WebBlock(Properties properties)
    {
        super(properties);
    }

    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
    {
        if (AG.getInst().getModuleManager().getMoveHelper().isEnabled() && AG.getInst().getModuleManager().getMoveHelper().noWeb.getValue() && AG.getInst().getModuleManager().getMoveHelper().noWebMode.is("Old Grim")) {
            mc.player.connection.sendPacketWithoutEvent(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
            System.out.print("123");
            return;
        }
        else {
            entityIn.setMotionMultiplier(state, new Vector3d(0.25D, (double)0.05F, 0.25D));
        }
    }
}
