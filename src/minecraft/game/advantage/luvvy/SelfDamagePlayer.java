package minecraft.game.advantage.luvvy;

import minecraft.game.transactions.EventDamageReceive;
import minecraft.game.transactions.EventPacket;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.figures.TimeCounterSetting;
import lombok.Getter;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effects;

public class SelfDamagePlayer implements IMinecraft {
    private final TimeCounterSetting timeTracker = new TimeCounterSetting();
    @Getter
    private boolean normalDamage;
    private boolean fallDamage;
    private boolean explosionDamage;
    private boolean arrowDamage;
    private boolean pearlDamage;

    public void onPacketEvent(EventPacket eventPacket) {
        boolean isDamage = this.fallDamage || this.arrowDamage || this.explosionDamage || this.pearlDamage;

        if (this.isBadEffects()) {
            return;
        }
        if (eventPacket.getPacket() instanceof SExplosionPacket) {
            this.explosionDamage = true;
        }
        if (!isDamage) {
            IPacket<?> packet = eventPacket.getPacket();
            if (packet instanceof SEntityStatusPacket statusPacket) {
                if (statusPacket.getOpCode() == 2 && statusPacket.getEntity(mc.world) == IMinecraft.mc.player) {
                    this.normalDamage = true;
                }
            }
        } else if (mc.player.hurtTime > 0) {
            this.normalDamage = false;
            this.reset();
        }
    }

    public boolean time(long time) {
        if (this.normalDamage) {
            if (this.timeTracker.isReached(time)) {
                this.normalDamage = false;
                this.timeTracker.reset();
                return true;
            }
        } else {
            this.timeTracker.reset();
        }
        return false;
    }

    public void processDamage(EventDamageReceive damageEvent) {
        switch (damageEvent.getDamageType()) {
            case FALL -> this.fallDamage = true;
            case ARROW -> this.arrowDamage = true;
            case ENDER_PEARL -> this.pearlDamage = true;
        }
        this.normalDamage = false;
    }

    public void resetDamages() {
        this.normalDamage = false;
        this.reset();
        this.timeTracker.reset();
    }

    private void reset() {
        this.fallDamage = false;
        this.explosionDamage = false;
        this.arrowDamage = false;
        this.pearlDamage = false;
    }

    private boolean isBadEffects() {
        if (mc.player == null) {
            return false;
        }
        return mc.player.isPotionActive(Effects.POISON) || mc.player.isPotionActive(Effects.WITHER)
                || mc.player.isPotionActive(Effects.INSTANT_DAMAGE);
    }

}