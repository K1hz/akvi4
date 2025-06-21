package minecraft.game.advantage.luvvy;

import minecraft.game.advantage.advisee.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

@UtilityClass
public class EntitySystem implements IMinecraft {

    public Vector3d getPrevPositionVec(
            final Entity entity
    ) {
        return new Vector3d(
                entity.prevPosX,
                entity.prevPosY,
                entity.prevPosZ
        );
    }

    public Vector3d getInterpolatedPositionVec(
            final Entity entity
    ) {
        final Vector3d prev = getPrevPositionVec(entity);

        return prev.add(entity.getPositionVec()
                .subtract(prev)
                .scale(mc.getRenderPartialTicks())
        );
    }
}
