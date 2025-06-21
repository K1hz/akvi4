package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventInput;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;
import net.minecraft.util.math.vector.Vector2f;


import java.util.Random;

@Defuse(name="Anti Aim", description="AA Крутилка!", brand= Category.Player)
public class AntiAim
        extends Module {
    private final CheckBoxSetting realBoolean = new CheckBoxSetting("Менять хитбокс", false);
    private final CheckBoxSetting randomReal = new CheckBoxSetting("БрутФорс", true).visibleIf(this::lambda$new$0);
    private final CheckBoxSetting fakeBoolean = new CheckBoxSetting("АнтиАимы", true);
    private final ModeSetting fakeModeYaw = new ModeSetting("Менять Yaw", "Jitter", "Jitter", "Static", "Random", "Defense").visibleIf(this::lambda$new$1);
    private final SliderSetting yawSlider = new SliderSetting("Угол Yaw", 60.0f, 1.0f, 70.0f, 1.0f).visibleIf(this::lambda$new$2);
    private final ModeSetting fakeModePitch = new ModeSetting("Менять Pitch", "Defense", "Defense", "Custom").visibleIf(this::lambda$new$3);
    private final SliderSetting pitchSlider = new SliderSetting("Угол Pitch", 65.0f, 0.0f, 90.0f, 1.0f).visibleIf(this::lambda$new$4);
    private final CheckBoxSetting zeroPitch = new CheckBoxSetting("Zero pitch on land", false).visibleIf(this::lambda$new$5);
    private final CheckBoxSetting chivoBlyat = new CheckBoxSetting("Отображать у всех", false).visibleIf(this::lambda$new$6);
    private final Random random = new Random();
    public static Vector2f rotateVector = new Vector2f(0, 0);
    float yaw = 0.0f;
    float pitch = 0.0f;
    long timeLanded = 0L;
    int delayTime = 500;
    boolean can = true;

    public AntiAim() {
        this.addSettings(this.realBoolean, this.randomReal, this.fakeBoolean, this.fakeModeYaw, this.fakeModePitch, this.zeroPitch, this.yawSlider, this.pitchSlider, this.chivoBlyat);
    }

    @Subscribe
    public void onMotion(EventMotion eventMotion) {
        block25: {
            block24: {
            if (!chivoBlyat.getValue()) {
                if (AG.getInst().getModuleManager().getDDATTACK().isEnabled()) break block24;
                if (AG.getInst().getModuleManager().getDDATTACK().isEnabled()) break block25;
            }
            else {
                if (!AG.getInst().getModuleManager().getDDATTACK().isEnabled()) break block24;
                if (!AG.getInst().getModuleManager().getDDATTACK().isEnabled()) break block25;
            }
                AG.getInst().getModuleManager().getDDATTACK();
                DDATTACK ddattack = AG.getInst().getModuleManager().getDDATTACK();
                if (ddattack.getTarget() != null) break block25;
            }
            if (((Boolean)this.fakeBoolean.getValue()).booleanValue()) {
                if (AntiAim.mc.gameSettings.keyBindUseItem.pressed || AG.getInst().getModuleManager().getAutopotion().isActive() || AntiAim.mc.gameSettings.keyBindAttack.pressed || AntiAim.mc.currentScreen != null) {
                    this.can = false;
                    return;
                }
                this.can = true;
                if (this.fakeModeYaw.is("Jitter")) {
                    if (AntiAim.mc.player.ticksExisted % 2 == 0) {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + ((Float)this.yawSlider.getValue()).floatValue() + 180.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    } else {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw - ((Float)this.yawSlider.getValue()).floatValue() + 180.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    }
                }
                if (this.fakeModeYaw.is("Static")) {
                    AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f;
                    this.yaw = AntiAim.mc.player.renderYawOffset;
                }
                if (this.fakeModeYaw.is("Defense")) {
                    AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f;
                    this.yaw = AntiAim.mc.player.renderYawOffset;
                    if (AntiAim.mc.player.ticksExisted % this.randomInt(2, 6) == 0) {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + (float)this.randomInt(12, 60) + 200.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    } else {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw - (float)this.randomInt(12, 60) + 200.0f;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    }
                }
                if (this.fakeModeYaw.is("Random")) {
                    int n = this.randomInt(1, 180);
                    if (this.random.nextBoolean()) {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f + (float)n;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    } else {
                        AntiAim.mc.player.rotationYawHead = AntiAim.mc.player.renderYawOffset = AntiAim.mc.player.rotationYaw + 180.0f - (float)n;
                        this.yaw = AntiAim.mc.player.renderYawOffset;
                    }
                }
                if (this.fakeModePitch.is("Custom")) {
                    this.pitch = AntiAim.mc.player.rotationPitchHead = ((Float)this.pitchSlider.getValue()).floatValue();
                }
                if (this.fakeModePitch.is("Defense")) {
                    this.pitch = AntiAim.mc.player.rotationPitchHead = ((Float)this.pitchSlider.getValue()).floatValue();
                    if (AntiAim.mc.player.ticksExisted % this.randomInt(4, 12) == 0) {
                        AntiAim.mc.player.rotationPitchHead = -65.0f;
                        this.pitch = -65.0f;
                    }
                }
                if (((Boolean)this.zeroPitch.getValue()).booleanValue()) {
                    if (AntiAim.mc.player.isOnGround()) {
                        if (this.timeLanded == 0L) {
                            this.timeLanded = System.currentTimeMillis();
                        }
                        if (System.currentTimeMillis() - this.timeLanded <= (long)this.delayTime) {
                            AntiAim.mc.player.rotationPitchHead = 0.0f;
                            this.pitch = 0.0f;
                        }
                    } else {
                        this.timeLanded = 0L;
                    }
                }
                if (((Boolean)this.chivoBlyat.getValue()).booleanValue() && this.can) {
                    eventMotion.setPitch(this.pitch);
                    eventMotion.setYaw(this.yaw);
                }
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate eventUpdate) {
        if (((Boolean)this.realBoolean.getValue()).booleanValue()) {
            if (AntiAim.mc.player.isInWater() || AntiAim.mc.gameSettings.keyBindUseItem.pressed || AG.getInst().getModuleManager().getAutopotion().isActive() || AntiAim.mc.gameSettings.keyBindAttack.pressed || AntiAim.mc.currentScreen != null) {
                AntiAim.mc.player.stopFallFlying();
                return;
            }
            int n = 4;
            if (((Boolean)this.randomReal.getValue()).booleanValue()) {
                n = this.randomInt(4, 8);
            }
            if (AntiAim.mc.player.ticksExisted % n == 0) {
                AntiAim.mc.player.startFallFlying();
            } else {
                AntiAim.mc.player.stopFallFlying();
            }
        }
    }
    @Subscribe
    public void onInput(EventInput eventInput) {
    }
    public void reset() {
        this.yaw = AntiAim.mc.player.rotationYaw;
        this.pitch = AntiAim.mc.player.rotationPitch;
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private int randomInt(int n, int n2) {
        return this.random.nextInt(n2 - n + 1) + n;
    }

    private Boolean lambda$new$6() {
        return (Boolean)this.fakeBoolean.getValue();
    }

    private Boolean lambda$new$5() {
        return (Boolean)this.fakeBoolean.getValue();
    }

    private Boolean lambda$new$4() {
        return (Boolean)this.fakeBoolean.getValue();
    }

    private Boolean lambda$new$3() {
        return (Boolean)this.fakeBoolean.getValue();
    }

    private Boolean lambda$new$2() {
        return (Boolean)this.fakeBoolean.getValue() != false && this.fakeModeYaw.is("Jitter");
    }

    private Boolean lambda$new$1() {
        return (Boolean)this.fakeBoolean.getValue();
    }

    private Boolean lambda$new$0() {
        return (Boolean)this.realBoolean.getValue();
    }
}

