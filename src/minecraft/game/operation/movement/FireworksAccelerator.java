package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.transactions.EventUpdate;

@Defuse(name = "Fireworks Accelerator",description = "Баг элитры", brand = Category.Movement)
public class FireworksAccelerator extends Module {
    public FireworksAccelerator() {
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
    }
}