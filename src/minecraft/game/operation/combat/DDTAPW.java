package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventMouseButtonPress;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import net.minecraft.util.math.RayTraceResult;

@Defuse(name = "WTap",description = "Увеличивает дистанцию отбрасывания противника", brand = Category.Combat)
public class DDTAPW extends Module {

    @Subscribe
    public void onMouseClicK(EventMouseButtonPress e) {
        if (e.getButton() == 0 && mc.player != null && mc.player.isHandActive() && mc.currentScreen == null && (mc.objectMouseOver == null || mc.objectMouseOver.getType() != RayTraceResult.Type.BLOCK)) {
            mc.clickMouse();
        }
    }

}
