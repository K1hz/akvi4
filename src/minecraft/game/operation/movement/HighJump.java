package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CEntityActionPacket.Action;
import net.minecraft.network.play.server.SEntityMetadataPacket;

@Defuse(
        name = "High Jump",
        description = "123",
        brand = Category.Movement
)
public class HighJump extends Module {
    private final TimeCounterSetting timerUtil = new TimeCounterSetting();
    float boost = 0.35F;
    private ModeSetting mode = new ModeSetting("Обход", "Grim Elytra", "Grim Elytra", "Grim Boat");
//    private final SliderSetting height = (new SliderSetting("Высота прыжка", 1.6F, 0.1F, 2.0F, 0.05F)).visibleIf(() -> this.mode.is("Grim Boat"));
    public HighJump() {
        addSettings(mode);
    }

    @Subscribe
    public void onEvent(EventUpdate event) {
        if (this.mode.is("Grim Boat")) {
            for(Entity entity : mc.world.getAllEntities()) {
                if (entity instanceof BoatEntity && (double)mc.player.getDistance(entity) < (double)2.0F && mc.player.getDistance(entity) < 2.0F) {
                    mc.gameSettings.keyBindJump.setPressed(false);
                    if (mc.player.isOnGround()) {
                            mc.player.setMotion((double)0.0F, 1.72, (double)0.0F);

                    }
                }
            }
        }
        if (this.mode.is("Grim Elytra")) {
            if (event instanceof EventUpdate) {
                if (mc.player.moveForward > 0.0F) {
                    return;
                }
                if (mc.player.rotationPitchHead < 18) {
                    mc.player.rotationPitch = 0;
                }
                if (mc.player.rotationPitchHead > -18) {
                    mc.player.rotationPitch  = 0;
                }
            }
        }

        if (this.mode.is("Grim Elytra")) {
            if (event instanceof EventUpdate) {
                if (mc.player.isOnGround()) {
                    this.boost = 0.35F;
                }

                if (!mc.player.isOnGround() && mc.player.fallDistance == 0.0F) {
                    for(int i = 0; i < 9; ++i) {
                        if (mc.player.inventory.getStackInSlot(i).getItem() == Items.ELYTRA) {
                            if ((double)this.boost < 1.3) {
                                this.boost += 0.012F;
                            }

                            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
                            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, Action.START_FALL_FLYING));
                            mc.playerController.windowClick(0, 6, i, ClickType.SWAP, mc.player);
                            mc.player.motion.y = (double)this.boost;
                        }
                    }
                }
            }
        }


    }

    @Subscribe
    public void onEvent12(EventPacket e) {
        if (this.mode.is("Grim Elytra")) {
            if (e.getPacket() instanceof SEntityMetadataPacket && ((SEntityMetadataPacket)e.getPacket()).getEntityId() == mc.player.getEntityId() && !mc.player.isElytraFlying()) {
                e.open();
            }
        }
    }

    public void onDisable() {
        super.onDisable();
        if (this.mode.is("Grim Elytra")) {
            this.boost = 0.36F;
        }
    }
}
