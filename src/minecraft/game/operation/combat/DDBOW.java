package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventPacket;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.item.BowItem;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.MathHelper;

@Defuse(name = "Super Bow",description = "Увеличивает силу лука", brand = Category.Misc)
public class DDBOW extends Module {

    final SliderSetting power = new SliderSetting("Сила", 40, 1, 100, 1);

    public DDBOW() {
        addSettings(power);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof CPlayerDiggingPacket p && e.isSend() && isBowInHand()) {
            if (p.getAction() == CPlayerDiggingPacket.Action.RELEASE_USE_ITEM) {
                mc.player.connection.preSendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                for (int i = 0; i < power.getValue(); i++) {
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() - 0.000000001, mc.player.getPosZ(), true));
                    mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(mc.player.getPosX(), mc.player.getPosY() + 0.000000001, mc.player.getPosZ(), false));
                }
                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, (float) MathHelper.clamp(mc.player.rotationPitch * 1.0001, -89.9, 89.9), false));
            }
        }
    }

    private boolean isBowInHand() {
        if (mc.player.getHeldItemMainhand().getItem() instanceof BowItem) {
            return true;
        }
        if (mc.player.getHeldItemOffhand().getItem() instanceof BowItem) {
            return true;
        }
        return false;
    }
}
