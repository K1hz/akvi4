package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventPacket;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import lombok.Getter;
import net.minecraft.network.play.server.SExplosionPacket;

@Getter
@Defuse(name = "Nо Рush",description = "Убирает отталкивание", brand = Category.Movement)
public class DDPUSH extends Module {

    public static ModeListSetting modes = new ModeListSetting("Тип",
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Вода", false),
            new CheckBoxSetting("Взрывы", false),
            new CheckBoxSetting("Блоки", true));

    public DDPUSH() {
        addSettings(modes);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isReceive()) {
            if (modes.is("Взрывы").getValue()) {
                if (e.getPacket() instanceof SExplosionPacket) {
                    e.cancel();
                }
            }
        }
    }
}
