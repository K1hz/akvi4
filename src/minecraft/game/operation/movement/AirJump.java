package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;

import minecraft.system.AG;
import minecraft.game.transactions.EventMotion;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;

@Defuse(name = "Air Jump",description = "Можно прыгать по воздуху", brand = Category.Movement)
public class AirJump extends Module {
	
	private ModeSetting mode = new ModeSetting("Обход", "Matrix", "Default", "Matrix");
	
	public AirJump() {
		addSettings(mode);
	}
	
	@Subscribe
	public void onUpdate(EventMotion e) {
		if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) return;
		if (mode.is("Default")) {
			mc.player.onGround = true;
		}

		if (mode.is("Matrix")) {
			if (!mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0.5, 0, 0.5).offset(0, -1, 0)).toList().isEmpty() && mc.player.ticksExisted % 2 == 0) {
				mc.player.fallDistance = 0;
				mc.player.jumpTicks = 0;
				e.setOnGround(true);
				mc.player.onGround = true;
			}
		}
	}
}
