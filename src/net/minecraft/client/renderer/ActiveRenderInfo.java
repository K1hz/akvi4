package net.minecraft.client.renderer;

import lombok.Getter;
import minecraft.game.operation.visual.NoRender;
import minecraft.game.transactions.EventCancelOverlay;

import minecraft.system.AG;
import minecraft.game.transactions.EventCamera;
import minecraft.game.operation.misc.DDBETTER;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.advantage.figures.MathSystem;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.optifine.reflect.Reflector;

public class ActiveRenderInfo {
    private boolean valid;
    private IBlockReader world;
    private Entity renderViewEntity;
    private Vector3d pos = Vector3d.ZERO;
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private final Vector3f look = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private float pitch;
    private float yaw;
    private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private boolean thirdPerson;
    private float height;
    private float previousHeight;
    private float interpolatedDistance;
    private boolean firsttime;
    @Getter
    private boolean thirdPersonReverse;

    private EventCamera event = new EventCamera(0, 0, 0);

    public void update(IBlockReader worldIn, Entity renderViewEntity, boolean thirdPersonIn, boolean thirdPersonReverseIn, float partialTicks) {
        this.valid = true;
        this.world = worldIn;
        this.renderViewEntity = renderViewEntity;
        this.thirdPerson = thirdPersonIn;

        this.thirdPersonReverse = thirdPersonReverseIn;
        this.setDirection(renderViewEntity.getYaw(partialTicks), renderViewEntity.getPitch(partialTicks));
        this.setPosition(
                MathHelper.lerp((double) partialTicks, renderViewEntity.prevPosX, renderViewEntity.getPosX()),
                MathHelper.lerp((double) partialTicks, renderViewEntity.prevPosY, renderViewEntity.getPosY()) +
                        (double) MathHelper.lerp(partialTicks, this.previousHeight, this.height),
                MathHelper.lerp((double) partialTicks, renderViewEntity.prevPosZ, renderViewEntity.getPosZ())
        );

        event.partialTicks = partialTicks;
        event.yaw = renderViewEntity.getYaw(partialTicks);
        event.pitch = renderViewEntity.getPitch(partialTicks);
        AG.getInst().getEventBus().post(event);

        if (thirdPersonIn) {
            if (thirdPersonReverseIn) {
                this.setDirection(this.yaw + 180.0F, -this.pitch);
            }
            DDBETTER DDBETTER = AG.getInst().getModuleManager().getDDBETTER();

            if (SelfDestruct.unhooked) {
                this.movePosition(-this.calcCameraDistance(4.0D), 0.0D, 0.0D);
            } else if (DDBETTER.isEnabled()) {
                if (DDBETTER.distancecamera1.getValue()) {
                    // Если distancecamera1 включён, используем пользовательские значения
                    this.movePosition(
                            -this.calcCameraDistance(DDBETTER.distanceCamera.getValue()),
                            DDBETTER.upcamera.getValue(),
                            DDBETTER.postionCamera.getValue()
                    );
                } else {
                    // Если distancecamera1 выключен, используем стандартные значения
                    this.movePosition(-this.calcCameraDistance(4.0D), 0.0D, 0.0D);
                }
            } else {
                // Действие по умолчанию, если модуль DDBETTER выключен
                this.movePosition(-this.calcCameraDistance(4.0D), 0.0D, 0.0D);
            }
        } else {
            // Логика для отсутствия третьего лица
            calcCameraDistance(1);
        }

        if (renderViewEntity instanceof LivingEntity && ((LivingEntity) renderViewEntity).isSleeping()) {
            Direction direction = ((LivingEntity) renderViewEntity).getBedDirection();
            this.setDirection(direction != null ? direction.getHorizontalAngle() - 180.0F : 0.0F, 0.0F);
            this.movePosition(0.0D, 0.3D, 0.0D);
        }
    }


    public void interpolateHeight() {
        if (this.renderViewEntity != null) {
            this.previousHeight = this.height;
            this.height += (this.renderViewEntity.getEyeHeight() - this.height) * 0.5F;
        }
    }

    /**
     * Checks for collision of the third person camera and returns the distance
     */

//    private double calcCameraDistance(double startingDistance) {
//        interpolatedDistance = MathUtil.fast((float) interpolatedDistance, (float) startingDistance, 10);
//        return interpolatedDistance;
//    }

    private double calcCameraDistance(double startingDistance) {
        // Создание события для отмены перекрытия камеры

        // Получение экземпляра модуля DDBETTER
        DDBETTER DDBETTER = AG.getInst().getModuleManager().getDDBETTER();

        // Проверка, включен ли модуль DDBETTER и активирована ли плавная камера
        boolean isBetter = DDBETTER.isEnabled() && DDBETTER.smoothCamera.getValue();

        // Получаем значение дистанции из настроек DDBETTER
        float cameraDistance = 4.0F; // Стандартное значение дистанции
        if (DDBETTER.isEnabled() && DDBETTER.distancecamera1.getValue()) {
            cameraDistance = (float) DDBETTER.distanceCamera.getValue();
        }

        // Если SelfDestruct.unhooked включен, устанавливаем дистанцию в 4.0F
        if (SelfDestruct.unhooked) {
            cameraDistance = 4.0F;
        }

        // Если DDBETTER выключен или его опция дистанции камеры отключена, устанавливаем стандартное значение
        startingDistance = cameraDistance;

        // Если активирован модуль DDBETTER с плавной камерой
        if (SelfDestruct.unhooked) { //ваш initilization в котором хранится мод при анхуке
            for (int i = 0; i < 8; ++i) {
                float f = (float) ((i & 1) * 2 - 1);
                float f1 = (float) ((i >> 1 & 1) * 2 - 1);
                float f2 = (float) ((i >> 2 & 1) * 2 - 1);
                f = f * 0.1F;
                f1 = f1 * 0.1F;
                f2 = f2 * 0.1F;
                Vector3d vector3d = this.pos.add((double) f, (double) f1, (double) f2);
                Vector3d vector3d1 = new Vector3d(this.pos.x - (double) this.look.getX() * startingDistance + (double) f + (double) f2, this.pos.y - (double) this.look.getY() * startingDistance + (double) f1, this.pos.z - (double) this.look.getZ() * startingDistance + (double) f2);
                RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, this.renderViewEntity));

                if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                    double d0 = raytraceresult.getHitVec().distanceTo(this.pos);

                    if (d0 < startingDistance) {
                        startingDistance = d0;
                    }
                }
            }

            // Если модуль SelfDestruct не активен и DDBETTER включен с плавной камерой
            if (!SelfDestruct.unhooked && isBetter) {
                interpolatedDistance = MathSystem.fast((float) interpolatedDistance, (float) startingDistance, 10);
                startingDistance = interpolatedDistance;
            }
        }

        return startingDistance;
    }






    /**
     * Moves the render position relative to the view direction, for third person
     * camera
     */
    public void movePosition(double distanceOffset, double verticalOffset, double horizontalOffset) {
        double d0 = (double) this.look.getX() * distanceOffset + (double) this.up.getX() * verticalOffset
                + (double) this.left.getX() * horizontalOffset;
        double d1 = (double) this.look.getY() * distanceOffset + (double) this.up.getY() * verticalOffset
                + (double) this.left.getY() * horizontalOffset;
        double d2 = (double) this.look.getZ() * distanceOffset + (double) this.up.getZ() * verticalOffset
                + (double) this.left.getZ() * horizontalOffset;
        this.setPosition(new Vector3d(this.pos.x + d0, this.pos.y + d1, this.pos.z + d2));
    }

    public void moveForward(double distanceOffset) {
        double d0 = (double) this.look.getX() * distanceOffset;
        double d2 = (double) this.look.getZ() * distanceOffset;
        this.setPosition(new Vector3d(this.pos.x + d0, this.pos.y, this.pos.z + d2));
    }

    public void setDirection(float pitchIn, float yawIn) {
        this.pitch = yawIn;
        this.yaw = pitchIn;
        this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation.multiply(Vector3f.YP.rotationDegrees(-pitchIn));
        this.rotation.multiply(Vector3f.XP.rotationDegrees(yawIn));
        this.look.set(0.0F, 0.0F, 1.0F);
        this.look.transform(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F);
        this.up.transform(this.rotation);
        this.left.set(1.0F, 0.0F, 0.0F);
        this.left.transform(this.rotation);
    }

    /**
     * Sets the position and blockpos of the active render
     */
    public void setPosition(double x, double y, double z) {
        this.setPosition(new Vector3d(x, y, z));
    }

    public void setPosition(Vector3d posIn) {
        this.pos = posIn;
        this.blockPos.setPos(posIn.x, posIn.y, posIn.z);
    }

    public Vector3d getProjectedView() {
        return this.pos;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    public Entity getRenderViewEntity() {
        return this.renderViewEntity;
    }

    public boolean isValid() {
        return this.valid;
    }

    public boolean isThirdPerson() {
        return this.thirdPerson;
    }

    public FluidState getFluidState() {
        if (!this.valid) {
            return Fluids.EMPTY.getDefaultState();
        } else {
            FluidState fluidstate = this.world.getFluidState(this.blockPos);
            return !fluidstate.isEmpty() && this.pos.y >= (double) ((float) this.blockPos.getY()
                    + fluidstate.getActualHeight(this.world, this.blockPos)) ? Fluids.EMPTY.getDefaultState()
                            : fluidstate;
        }
    }

    public BlockState getBlockState() {
        return !this.valid ? Blocks.AIR.getDefaultState() : this.world.getBlockState(this.blockPos);
    }

    public void setAnglesInternal(float p_setAnglesInternal_1_, float p_setAnglesInternal_2_) {
        this.yaw = p_setAnglesInternal_1_;
        this.pitch = p_setAnglesInternal_2_;
    }

    public BlockState getBlockAtCamera() {
        if (!this.valid) {
            return Blocks.AIR.getDefaultState();
        } else {
            BlockState blockstate = this.world.getBlockState(this.blockPos);

            if (Reflector.IForgeBlockState_getStateAtViewpoint.exists()) {
                blockstate = (BlockState) Reflector.call(blockstate, Reflector.IForgeBlockState_getStateAtViewpoint,
                        this.world, this.blockPos, this.pos);
            }

            return blockstate;
        }
    }

    public final Vector3f getViewVector() {
        return this.look;
    }

    public final Vector3f getUpVector() {
        return this.up;
    }

    public void clear() {
        this.world = null;
        this.renderViewEntity = null;
        this.valid = false;
    }
}
