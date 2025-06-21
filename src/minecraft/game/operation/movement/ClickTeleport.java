package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventMouseButtonPress;
import minecraft.game.transactions.EventRender3D;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import minecraft.game.advantage.make.color.ColoringSystem;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.optifine.render.RenderUtils;

@Defuse(name = "Click Teleport",description = "Телепорт на блок", brand = Category.Movement)
public class ClickTeleport extends Module {

    private final ModeSetting mode = new ModeSetting("Обход", "Vanilla", "Vanilla", "Matrix", "Spartan");
    private final ModeSetting packetVanillaType = new ModeSetting("Тип пакетов", "NoPacket", "Packet", "None").visibleIf(() -> mode.is("Vanilla"));

    public ClickTeleport() {
        addSettings(mode, packetVanillaType);
    }

    private BlockRayTraceResult result;
    BlockPos posBlock = null;

    @Subscribe
    public void onUpdate(EventUpdate eventUpdate) {
        if (result != null) {
            posBlock = result.getPos();
            BlockPos e = posBlock;
            for (int i = 256; i > 0; --i) {
                Material material = mc.world.getBlockState(e).getMaterial();
                boolean isReplacelable = material.isReplaceable();
                if (!isReplacelable) continue;
                e = e.down();
            }
            posBlock = e;
        }
    }

    @Subscribe
    public void onMouseTick(EventMouseButtonPress e) {
        if (e.getButton() == 1 && result != null) {
            if (posBlock == null || mc.currentScreen != null) {
                return;
            }

            teleport(posBlock.getX() + 0.5f, posBlock.getY() + 1, posBlock.getZ() + 0.5f);
        }
    }

    @Subscribe
    public void onRender(EventRender3D e) {
        result = (BlockRayTraceResult) mc.player.pick(100, 1, false);
        if (result.getType() == RayTraceResult.Type.MISS) result = null;
        if (posBlock != null) RenderUtils.drawBlockBox(posBlock, ColoringSystem.rgba(200,200,200,255));
    }

    public static void matrixTp(double x, double y, double z, boolean canElytra) {
        boolean elytraEquiped;
        float f = mc.player.rotationYaw * ((float)Math.PI / 180);
        double h = mc.player.getDistanceSq(mc.player.getPosX() + x, mc.player.getPosY(), mc.player.getPosZ() + z);
        int de = (int)MathHelper.clamp(mc.player.getDistanceSq(x, y += h / 100.0, z) / 11.0, 1.0, 17.0);
        int de2 = (int)(Math.abs(y / 11.0) + Math.abs(h / 2.5));
        boolean bl = elytraEquiped = mc.player.inventory.armorInventory.get(2).getItem() == Items.ELYTRA;
        if (canElytra) {
            for (int i = 0; i < MathHelper.clamp(de2, 1, 17); ++i) {
                mc.player.connection.sendPacket(new CPlayerPacket(false));
            }
            if (elytraEquiped) {
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), false));
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z, false));
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
            } else {
                int elytra = InventoryOrigin.getInstance().getSlotInInventory(Items.ELYTRA);
                if (elytra != -1) {
                    mc.playerController.windowClick(0, elytra < 9 ? elytra + 36 : elytra, 1, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
                }
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), false));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), false));
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z, false));
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                if (elytra != -1) {
                    mc.playerController.windowClick(0, 6, 1, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(0, elytra < 9 ? elytra + 36 : elytra, 1, ClickType.PICKUP, mc.player);
                }
            }
            mc.player.setPositionAndUpdate(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z);
        } else {
            for (int i = 0; i < MathHelper.clamp(de2 + 1, 0, 19); ++i) {
                mc.player.connection.sendPacket(new CPlayerPacket(false));
            }
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z, false));
            mc.player.setPositionAndUpdate(mc.player.getPosX() + x, mc.player.getPosY() + y, mc.player.getPosZ() + z);
        }
        mc.player.swingArm(Hand.MAIN_HAND);
    }

    void teleport(double x, double y, double z) {
        if (mode.is("Vanilla")) {
            if (packetVanillaType.is("Packet")) {
                for (int i = 0; i < 5; i++) {
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                }
                for (int i = 0; i < 10; i++) {
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, true));
                }
            }

            mc.player.setPositionAndUpdate(x, y, z);
        } else if (mode.is("Spartan")) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SNEAKING));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y - 1.0, z, false));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, 1.0, z, false));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.42, z, true));
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SNEAKING));
        }
        if (mode.is("Matrix")) {
            y += 2.0;
            boolean elytra = InventoryOrigin.getItemSlot(Items.ELYTRA) != -1;
            matrixTp(x - mc.player.getPosX(), y - mc.player.getPosY(), z - mc.player.getPosZ(), elytra);
        }
    }
}
