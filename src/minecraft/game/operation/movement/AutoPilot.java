package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.transactions.EventInput;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.EnchantedGoldenAppleItem;
import net.minecraft.item.SkullItem;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;

@Defuse(name="Auto Item Pilot",description = "XUY", brand= Category.Movement)
public class AutoPilot extends Module {
    private ModeListSetting workwork = new ModeListSetting("Идти", new CheckBoxSetting("На шары", true), new CheckBoxSetting("На чарки", true), new CheckBoxSetting("На элитры", true), new CheckBoxSetting("На яйца призыва", false));
    final ModeSetting rejim = new ModeSetting("Режим ротации", "Обычная", "Обычная", "Плавная");
    public boolean skullItemNoNull = false;
    public boolean eggItemNoNull = false;
    public boolean elytraItemNoNull = false;
    public static Vector2f rotateVector = new Vector2f(0.0f, 0.0f);

    public AutoPilot() {
        this.addSettings(this.workwork, this.rejim);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        AutoPilot.rotateVector.x = AutoPilot.mc.player.rotationYaw;
        AutoPilot.rotateVector.y = AutoPilot.mc.player.rotationPitch;
        for (Entity entity : AutoPilot.mc.world.getAllEntities()) {
            if (!(entity instanceof ItemEntity)) continue;
            this.skullItemNoNull = ((ItemEntity)entity).getItem().getItem() instanceof SkullItem && (Boolean)this.workwork.is("На шары").getValue() != false;
            this.elytraItemNoNull = ((ItemEntity)entity).getItem().getItem() instanceof ElytraItem && (Boolean)this.workwork.is("На элитры").getValue() != false;
            this.elytraItemNoNull = ((ItemEntity)entity).getItem().getItem() instanceof EnchantedGoldenAppleItem && (Boolean)this.workwork.is("На чарки").getValue() != false;
            boolean bl = this.eggItemNoNull = ((ItemEntity)entity).getItem().getItem() instanceof SpawnEggItem && (Boolean)this.workwork.is("На яйца призыва").getValue() != false;
            if (((Boolean)this.workwork.is("На шары").getValue()).booleanValue() && ((ItemEntity)entity).getItem().getItem() instanceof SkullItem) {
                AutoPilot.rotateVector.x = AutoPilot.rotations(entity)[0];
                AutoPilot.rotateVector.y = AutoPilot.rotations(entity)[1];
            }
            if (((Boolean)this.workwork.is("На элитры").getValue()).booleanValue() && ((ItemEntity)entity).getItem().getItem() instanceof ElytraItem && !this.skullItemNoNull) {
                AutoPilot.rotateVector.x = AutoPilot.rotations(entity)[0];
                AutoPilot.rotateVector.y = AutoPilot.rotations(entity)[1];
            }
            if (((Boolean)this.workwork.is("На чарки").getValue()).booleanValue() && ((ItemEntity)entity).getItem().getItem() instanceof EnchantedGoldenAppleItem && !this.skullItemNoNull) {
                AutoPilot.rotateVector.x = AutoPilot.rotations(entity)[0];
                AutoPilot.rotateVector.y = AutoPilot.rotations(entity)[1];
            }
            if (!((Boolean)this.workwork.is("На яйца призыва").getValue()).booleanValue() || !(((ItemEntity)entity).getItem().getItem() instanceof SpawnEggItem) || this.elytraItemNoNull || this.skullItemNoNull) continue;
            AutoPilot.rotateVector.x = AutoPilot.rotations(entity)[0];
            AutoPilot.rotateVector.y = AutoPilot.rotations(entity)[1];
        }
    }

    @Subscribe
    private void onMotion(EventMotion e) {
        float yaw = AutoPilot.rotateVector.x;
        float pitch = AutoPilot.rotateVector.y;
        e.setYaw(yaw);
        e.setPitch(pitch);
        if (this.rejim.is("Обычная")) {
            AutoPilot.mc.player.rotationYaw = yaw;
            AutoPilot.mc.player.rotationPitch = pitch;
        }
        if (this.rejim.is("Плавная")) {
            AutoPilot.mc.player.rotationYawHead = yaw;
            AutoPilot.mc.player.renderYawOffset = yaw;
            AutoPilot.mc.player.rotationPitchHead = pitch;
        }
    }

    @Subscribe
    public void onInput(EventInput eventInput) {
        if (this.rejim.is("Обычная")) {
        }
        if (this.rejim.is("Плавная")) {
            AutoPilot.mc.player.rotationYawOffset = this.rotateVector.x;
        }
    }

    public static float[] rotations(Entity entity) {
        double x = entity.getPosX() - AutoPilot.mc.player.getPosX();
        double y = entity.getPosY() - AutoPilot.mc.player.getPosY() - 1.5;
        double z = entity.getPosZ() - AutoPilot.mc.player.getPosZ();
        double u = MathHelper.sqrt(x * x + z * z);
        float u2 = (float)(MathHelper.atan2(z, x) * 57.29577951308232 - 90.0);
        float u3 = (float)(-MathHelper.atan2(y, u) * 57.29577951308232);
        return new float[]{u2, u3};
    }
}