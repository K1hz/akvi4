package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.display.clientnotify.elements.WarningNotify;
import minecraft.game.display.clientnotify.most.NotifyManager;
import minecraft.game.display.clientnotify.most.NotifyType;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventKey;
import minecraft.system.AG;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.MathHelper;


@Defuse(name = "Elytra Target", description = "123", brand = Category.Combat)
public class Peregon extends Module {
    static final SliderSetting elytraRange = new SliderSetting("Дистанция элитры", 6.0f, 0.0f, 128.0f, 1f);
    public static CheckBoxSetting elytraPeregonValue = new CheckBoxSetting("Перегонять противника", true);
    static final SliderSetting elytraperegon = new SliderSetting("Значение перелёта", 5.0f, 1f, 7.0f, 0.1f);
    final BindSetting peregon = new BindSetting("Кнопка перегона", -1);

    public static boolean shouldPeregon = false;

    public Peregon() {
        this.addSettings(
                this.elytraRange,
                elytraPeregonValue,
                this.elytraperegon,
                this.peregon
        );
    }

    @Subscribe
    private void onEventKey(EventKey e) {
        if (e.getKey() == this.peregon.getValue()) {
            shouldPeregon = !shouldPeregon;
            NotifyManager notifies = new NotifyManager();
            AG.getInst().getNotifyManager().add(0, new WarningNotify(shouldPeregon ? "Элитра перегон включен" : "Элитра перегон выключен", 650));
        }
    }
}
