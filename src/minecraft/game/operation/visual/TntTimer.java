package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.figures.MathSystem;

import minecraft.game.advantage.make.engine2d.Projection;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Map;

@Defuse(name = "TnT Timer",description = "Показывае сколько секунд осталось до взрыва", brand = Category.Visual)
public class TntTimer extends Module {

    private Map<String, CompactAnimation> animations = new HashMap<>();

    @Subscribe
    public void onDisplay(EventRender2D e) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof TNTEntity tnt) {
                final String name = MathSystem.round(tnt.getFuse() / 20.0F, 0.1) + "";
                Vector3d pos = Projection.interpolate(tnt, e.getPartialTicks());
                Vector2f vec = Projection.project(pos.x, pos.y + tnt.getHeight() + 0.5, pos.z);
                if (vec == null) return;

                float iconSize = 10;
                float width = ClientFonts.interBold[16].getWidth(name) + 4 + iconSize;
                float height = ClientFonts.interBold[16].getFontHeight();

                CompactAnimation easing = animations.getOrDefault(tnt.getDisplayName().getString(), null);
                if (easing == null) {
                    easing = new CompactAnimation(Easing.EASE_IN_OUT_CUBIC, 250);
                    animations.put(tnt.getDisplayName().getString(), easing);
                }

                boolean tntActive = tnt.getFuse() > 10;
                easing.run(tntActive ? 1 : 0);

                float x = (float) vec.x;
                float y = (float) vec.y;

                int black = ColoringSystem.setAlpha(ColoringSystem.rgb(10, 10, 10), (int) (140 * easing.getValue()));
                GraphicsSystem.drawImage(new ResourceLocation("render/images/modules/timers/tnt.png"), (x - width / 2), y, iconSize , iconSize, ColoringSystem.setAlpha(-1, (int) (255 * easing.getValue())));
                GraphicsSystem.drawRoundedRect((x - width / 2 - 2), (float) y - 2, (float) (width) + 4, (float) (height) + 4, 2, black);
                ClientFonts.interBold[16].drawCenteredString(e.getMatrixStack(), name, (x - width / 2 - 8 + iconSize * 2 + 8), y + 2.5f, ColoringSystem.setAlpha(-1, (int) (255 * easing.getValue())));
            }
        }
    }

}
