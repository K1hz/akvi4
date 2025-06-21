package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

@Defuse(name = "НitBох",description = "Увеличивает хитбокс игрока", brand = Category.Combat)
public class DDBOXED extends Module {
    public final SliderSetting size = new SliderSetting("Размер", 0.2f, 0.f, 3.f, 0.05f);
    public final CheckBoxSetting visible = new CheckBoxSetting("Видимые", false);
    public DDBOXED() {
        addSettings(size,visible);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (!visible.getValue() || mc.player == null) {
            return;
        }

        float sizeMultiplier = this.size.getValue() * 2.5F;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isNotValid(player)) {
                player.setBoundingBox(calculateBoundingBox(player, sizeMultiplier));
            }
        }
    }

    private boolean isNotValid(PlayerEntity player) {
        return player == mc.player || !player.isAlive();
    }

    private AxisAlignedBB calculateBoundingBox(Entity entity, float size) {
        double minX = entity.getPosX() - size;
        double minY = entity.getBoundingBox().minY;
        double minZ = entity.getPosZ() - size;
        double maxX = entity.getPosX() + size;
        double maxY = entity.getBoundingBox().maxY;
        double maxZ = entity.getPosZ() + size;

        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
