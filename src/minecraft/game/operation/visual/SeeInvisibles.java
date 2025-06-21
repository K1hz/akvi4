package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;

import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import net.minecraft.entity.player.PlayerEntity;

@Defuse(name = "See Invisible",description = "Убирает эффект невидимости у сущностей", brand = Category.Visual)
public class SeeInvisibles extends Module {
    public CheckBoxSetting visible = new CheckBoxSetting("Обновление", true);

    public SeeInvisibles() {
        this.addSettings(this.visible);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isInvisible()) {
                player.setInvisible(false);
            }
        }
    }
}
