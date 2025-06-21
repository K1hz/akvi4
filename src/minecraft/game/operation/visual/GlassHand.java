package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.other.CustomFramebuffer;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.advantage.make.shader.impl.Outline;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.transactions.EventDisplay;
import net.minecraft.client.settings.PointOfView;
import org.lwjgl.opengl.GL11;

@Defuse(name = "Glass Hand",description = "123", brand = Category.Visual)
public class GlassHand extends Module {

    public CustomFramebuffer hands = new CustomFramebuffer(false).setLinear();
    public CustomFramebuffer mask = new CustomFramebuffer(false).setLinear();

    @Subscribe
    public void onRender(EventDisplay e) {
        if (e.getType() != EventDisplay.Type.HIGH) {
            return;
        }

        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
            KawaseBlur.blur.updateBlur(3, 4);
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlphaTest();
            ColoringSystem.setColor(Hud.getColor(0));
            KawaseBlur.blur.render(() -> {
                hands.draw();
            });

            Outline.registerRenderCall(() -> {
                hands.draw();
            });


            GlStateManager.disableAlphaTest();
            GlStateManager.popMatrix();
        }
    }

    public static void setSaturation(float saturation) {
        float[] saturationMatrix = {0.3086f * (1.0f - saturation) + saturation, 0.6094f * (1.0f - saturation), 0.0820f * (1.0f - saturation), 0, 0, 0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation) + saturation, 0.0820f * (1.0f - saturation), 0, 0, 0.3086f * (1.0f - saturation), 0.6094f * (1.0f - saturation), 0.0820f * (1.0f - saturation) + saturation, 0, 0, 0, 0, 0, 1, 0};
        GL11.glLoadMatrixf(saturationMatrix);
    }
}
