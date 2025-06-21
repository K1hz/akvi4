package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventInput;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.transactions.EventRender3D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.system.managers.Theme;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import minecraft.game.advantage.luvvy.MovementSystem;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.optifine.render.RenderUtils;

import java.util.ArrayList;
import java.util.List;

@Defuse(name = "Auto Crystall",description = "Авто-взрыв кристала", brand = Category.Combat)
public class DDCRST extends Module {

    public final ModeListSetting options = new ModeListSetting("Опции",
            new CheckBoxSetting("Не взрывать себя", true),
            new CheckBoxSetting("Коррекция движения", false),
            new CheckBoxSetting("Ставить кристаллы", true),
            new CheckBoxSetting("Ставить обсидиан", true),
            new CheckBoxSetting("Ротация", true),
            new CheckBoxSetting("Визуализация", true)
    );

    private final ModeSetting distanceMode = new ModeSetting("Тип радиуса", "Vanilla", "Vanilla", "Custom");
    private final SliderSetting customDistance = new SliderSetting("Радиус", 5, 2.5f, 6, 0.05f).visibleIf(() -> distanceMode.is("Custom"));
    private final SliderSetting customUp = new SliderSetting("Вверх", 2, 1, 6, 0.05f);
    private final SliderSetting customDown = new SliderSetting("Вниз", 2, 1, 6, 0.05f);
    private final SliderSetting breakDelay = new SliderSetting("Задержка (мс)", 100, 0, 500, 1);

    public DDCRST() {
        addSettings(options, distanceMode, customDistance, customUp, customDown, breakDelay);
    }

    private Entity crystalTarget = null;
    public Vector2f rotate = new Vector2f(0, 0);
    private Vector3d obsidianVec = new Vector3d(0,0,0);
    private BlockPos closestObsidian = null;
    private Entity closestCrystal;
    private List<BlockPos> obsidianPositions = new ArrayList<>();
    private TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    private boolean crystalAttack = false;

    double distance() {
        return distanceMode.is("Vanilla") ? mc.playerController.getBlockReachDistance() : customDistance.getValue();
    }

    public boolean check() {
        return (crystalTarget != null || closestObsidian != null) && rotate != null && options.is("Коррекция движения").getValue() && (options.is("Ротация").getValue());
    }

    @Override
    public void onDisable() {
        reset();

        super.onDisable();
    }

    @Subscribe
    public void onRender3D(EventRender3D e) {
        if (options.is("Визуализация").getValue()) {
            if (obsidianVec != null) {
                RenderUtils.drawBlockBox(new BlockPos(obsidianVec.getX(), obsidianVec.getY(), obsidianVec.getZ()), Theme.mainRectColor);
            }
        }
    }

    @Subscribe
    public void onMoveInput(EventInput e) {
        if (check()) {
            MovementSystem.fixMovement(e, rotate.x);
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        boolean entityFound = false;

        int obsidianSlot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.OBSIDIAN, true);
        int crystalSlot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.OBSIDIAN, true);

        if (crystalSlot != -1 || obsidianSlot != -1) {
            for (Entity entity : mc.world.getAllEntities()) {
                if (entity == mc.player || FriendManager.isFriend(entity.getName().getString()) ||
                        entity instanceof EnderCrystalEntity || entity instanceof ProjectileItemEntity ||
                        entity instanceof ItemEntity || entity instanceof ThrowableEntity ||
                        entity instanceof FallingBlockEntity || entity instanceof ExperienceOrbEntity) {
                    continue;
                }

                if (mc.player.getDistance(entity) <= 8) {
                    entityFound = true;
                    break;
                }
            }
        }

        if (!entityFound) {
            obsidianPositions.clear();
            crystalAttack = false;
            closestCrystal = null;
            closestObsidian = null;
            rotate = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
            return;
        }

        findAndAttackCrystal();
        findAndClickObsidian();
        findAndPlaceObsidian();
    }


    private void findAndAttackCrystal() {
        closestCrystal = null;
        double closestDistanceToTarget = Double.MAX_VALUE;
        double maxDistanceToCrystal = distance();

        if (!options.is("Ставить кристаллы").getValue()) {
            crystalAttack = true;
        }

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderCrystalEntity enderCrystal) {
                double distanceToCrystal = mc.player.getDistance(enderCrystal);
                if (distanceToCrystal > maxDistanceToCrystal) {
                    continue;
                }

                if (mc.player.getPosY() >= enderCrystal.getPosY() && options.is("Не взрывать себя").getValue()) {
                    continue;
                }

                if (distanceToCrystal < closestDistanceToTarget) {
                    closestDistanceToTarget = distanceToCrystal;
                    closestCrystal = enderCrystal;
                }
            }
        }

        if (closestCrystal != null && crystalAttack) {
            crystalTarget = closestCrystal;
            if (TimeCounterSetting.isReached(breakDelay.getValue().longValue())) {
                mc.playerController.attackEntity(mc.player, closestCrystal);
                mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
                crystalTarget = null;
                obsidianPositions.clear();
                TimeCounterSetting.reset();
            }
        } else {
            reset();
        }
    }


    private void findAndClickObsidian() {
        int crystal = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.END_CRYSTAL, true);
        if (crystal == -1 || !options.is("Ставить кристаллы").getValue()) return;

        double closestDistanceToTarget = Double.MAX_VALUE;
        double maxDistanceToObsidian = distance() * 2;
        closestObsidian = null;
        obsidianPositions.clear();
        crystalAttack = false;

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity == mc.player || FriendManager.isFriend(entity.getName().getString()) || entity instanceof EnderCrystalEntity || entity instanceof ProjectileItemEntity || entity instanceof ItemEntity || entity instanceof ThrowableEntity || entity instanceof FallingBlockEntity || entity instanceof ExperienceOrbEntity) {
                continue;
            }

            for (int x = (int) -distance(); x <= distance(); x++) {
                for (int z = (int) -distance(); z <= distance(); z++) {
                    for (int y = -customDown.getValue().intValue(); y <= customUp.getValue().intValue(); y++) {
                        BlockPos pos = new BlockPos(entity.getPosX() + x, entity.getPosY() - 0.5f + y, entity.getPosZ() + z);
                        if (mc.world.getBlockState(pos).getBlock() == Blocks.OBSIDIAN) {
                            Block blockAbove = mc.world.getBlockState(pos.up()).getBlock();
                            if (!(blockAbove instanceof AirBlock)) {
                                continue;
                            }

                            if (entity.getPosition().equals(pos.up())
                                    || mc.player.getDistanceSq(Vector3d.copyCentered(pos)) > maxDistanceToObsidian
                                    || mc.player.getPosition().equals(pos.up()) || entity.getPosY() - 0.5f < pos.getY()
                                    || pos.getY() < mc.player.getPosY() && options.is("Не взрывать себя").getValue()
                                    || !entity.isAlive() || DDBOT.isBot(entity)
                                    || entity.getPosition().equals(pos.up())
                                    || isEntityOverlappingBlock(entity, pos)
                            ) {
                                continue;
                            }

                            double distanceToTarget = entity.getDistanceSq(Vector3d.copyCentered(pos));
                            if (distanceToTarget < closestDistanceToTarget) {
                                closestDistanceToTarget = distanceToTarget;
                                closestObsidian = pos;
                                obsidianPositions.clear();
                                obsidianPositions.add(closestObsidian);

                                if (entity.getPosY() + 0.5f > pos.getY() && AG.getInst().getModuleManager().getDDATTACK().isEnabled() && AG.getInst().getModuleManager().getDDATTACK().getTarget() != null && AG.getInst().getModuleManager().getDDATTACK().crystalAuraRule) {
                                    AG.getInst().getModuleManager().getDDATTACK().crystalAuraRule = true;
                                } else {
                                    AG.getInst().getModuleManager().getDDATTACK().crystalAuraRule = false;
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!obsidianPositions.isEmpty()) {
            int last = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = crystal;
            obsidianVec = new Vector3d(closestObsidian.getX() + 0.5, closestObsidian.getY() + 0.5, closestObsidian.getZ() + 0.5);
            BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(obsidianVec, Direction.UP, closestObsidian, false);
            mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, rayTraceResult);
            mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            mc.player.inventory.currentItem = last;
            obsidianPositions.clear();
            crystalAttack = true;
        }
    }

    private void findAndPlaceObsidian() {
        int obsidianSlot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.OBSIDIAN, true);
        int crystalSlot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.OBSIDIAN, true);
        if (obsidianSlot == -1 && crystalSlot == -1 || !options.is("Ставить обсидиан").getValue() || !options.is("Ставить кристаллы").getValue() || closestCrystal != null || crystalAttack) return;

        double closestDistanceToTarget = Double.MAX_VALUE;
        double maxDistanceToObsidian = distance() * 2;
        closestObsidian = null;
        obsidianPositions.clear();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity == mc.player || FriendManager.isFriend(entity.getName().getString()) ||
                    entity instanceof EnderCrystalEntity || entity instanceof ProjectileItemEntity ||
                    entity instanceof ItemEntity || entity instanceof ThrowableEntity ||
                    entity instanceof FallingBlockEntity || entity instanceof ExperienceOrbEntity) {
                continue;
            }

            if (mc.player.getPosY() >= entity.getPosY()) {
                continue;
            }

            boolean hasObsidianNearby = false;
            for (int x = (int) -distance(); x <= distance(); x++) {
                for (int z = (int) -distance(); z <= distance(); z++) {
                    BlockPos nearbyPos = new BlockPos(entity.getPosX() + x, entity.getPosY() - 1, entity.getPosZ() + z);
                    if (mc.world.getBlockState(nearbyPos).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(nearbyPos).getBlock() == Blocks.BEDROCK) {
                        hasObsidianNearby = true;
                        break;
                    }
                }
                if (hasObsidianNearby) break;
            }

            if (hasObsidianNearby) {
                continue;
            }

            for (int x = (int) -distance() / 2; x <= distance() / 2; x++) {
                for (int z = (int) -distance() / 2; z <= distance() / 2; z++) {
                    BlockPos pos = new BlockPos(entity.getPosX() + x, entity.getPosY() - 1, entity.getPosZ() + z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.AIR && mc.world.getBlockState(pos.down()).getBlock() != Blocks.AIR) {
                        double distanceToPlayer = mc.player.getDistanceSq(Vector3d.copyCentered(pos));
                        if (distanceToPlayer > maxDistanceToObsidian) {
                            continue;
                        }

                        if (pos.equals(entity.getPosition()) || pos.getY() < mc.player.getPosY()) {
                            continue;
                        }

                        double distanceToTarget = entity.getDistanceSq(Vector3d.copyCentered(pos));
                        if (distanceToTarget < closestDistanceToTarget) {
                            closestDistanceToTarget = distanceToTarget;
                            closestObsidian = pos;
                            obsidianPositions.clear();
                            obsidianPositions.add(closestObsidian);
                        }
                    }
                }
            }
        }

        if (!obsidianPositions.isEmpty()) {
            int last = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = obsidianSlot;
            Vector3d obsidianVec = new Vector3d(closestObsidian.getX() + 0.5, closestObsidian.getY() - 0.5, closestObsidian.getZ() + 0.5);
            BlockRayTraceResult rayTraceResult = new BlockRayTraceResult(obsidianVec, Direction.UP, closestObsidian, false);
            mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, rayTraceResult);
            mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            mc.player.inventory.currentItem = last;
            obsidianPositions.clear();
        }
    }

    private boolean isEntityOverlappingBlock(Entity entity, BlockPos pos) {
        AxisAlignedBB entityBoundingBox = entity.getBoundingBox();
        AxisAlignedBB blockBoundingBox = new AxisAlignedBB(pos).grow(0.5);

        return entityBoundingBox.intersects(blockBoundingBox);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (options.is("Ротация").getValue()) {
            if (!obsidianPositions.isEmpty()) {
                rotate = MathSystem.rotationToVec(obsidianVec);
                applyRotation(e, rotate);
            } else {
                rotate = MathSystem.rotationToEntity(closestCrystal);
                applyRotation(e, rotate);
            }
        }
    }

    private void applyRotation(EventMotion e, Vector2f rotate) {
        if (rotate != null) {
            e.setYaw(rotate.x);
            e.setPitch(rotate.y);
            mc.player.renderYawOffset = rotate.x;
            mc.player.rotationYawHead = rotate.x;
            mc.player.rotationPitchHead = rotate.y;
        }

        if (options.is("Коррекция движения").getValue()) {
            mc.player.rotationYawOffset = rotate == null ? mc.player.rotationYaw : rotate.x;
        }
    }

    public void reset() {
        closestObsidian = null;
        closestCrystal = null;
        crystalTarget = null;
        obsidianVec = null;
        obsidianPositions.clear();
        crystalAttack = false;
        AG.getInst().getModuleManager().getDDATTACK().crystalAuraRule = true;
        if (options.is("Коррекция движения").getValue()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
    }
}
