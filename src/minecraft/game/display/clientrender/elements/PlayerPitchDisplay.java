package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.advisee.AnimationNumbers;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.transactions.EventRender2D;
import minecraft.system.AG;
import minecraft.system.managers.drag.Dragging;
import net.minecraft.util.text.TextFormatting;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class PlayerPitchDisplay implements ElementRenderer {

    final Dragging dragging;

    public CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_CUBIC, 400);

    float width;
    float height = 40;

    @Subscribe
    public void render(EventRender2D eventRender2D) {

        float pitch = mc.player.rotationPitchHead;
        float speed = mc.player.getAIMoveSpeed();
        float yaw = mc.player.rotationYawHead;

        animation.run(1);
        animation.update();

        float animationValue = (float) animation.getValue();
        if (animationValue <= 0) return;

        MatrixStack matrixStack = eventRender2D.getMatrixStack();

        float posX = dragging.getX();
        float posY = dragging.getY();

        int alpha = (int) (255 * animationValue);

        String pitchText = "Pitch: " + TextFormatting.RED + String.format("%.2f", pitch);
        String speedText = "Speed: " + TextFormatting.RED + String.format("%.2f", Math.hypot(mc.player.prevPosX - mc.player.getPosX(), mc.player.prevPosZ - mc.player.getPosZ()) * 20);
        String yawText = "Yaw: " + TextFormatting.RED + String.format("%.2f", yaw);

        float pitchTextWidth = ClientFonts.clickGui[18].getWidth(pitchText);

        float yawTextWidth = ClientFonts.clickGui[18].getWidth(yawText);
        width = Math.max(pitchTextWidth, yawTextWidth) + 5;

        GraphicsSystem.drawRoundedRect(posX, posY, 80, 80, 4, ColoringSystem.setAlpha(ColoringSystem.rgb(20, 20, 20), alpha));

        float pitchTextX = posX + 3;
        float pitchTextY = posY + (height - ClientFonts.clickGui[18].getFontHeight()) / 2 - 10;
        ClientFonts.clickGui[18].drawString(matrixStack, pitchText, pitchTextX, pitchTextY, ColoringSystem.setAlpha(-1, alpha));

        float yawTextX = posX + 3;
        float yawTextY = posY + (height - ClientFonts.clickGui[18].getFontHeight()) / 2;
        ClientFonts.clickGui[18].drawString(matrixStack, yawText, yawTextX, yawTextY, ColoringSystem.setAlpha(-1, alpha));

        float speedTextX = posX + 3;
        float speedTextY = posY + (height - ClientFonts.clickGui[18].getFontHeight()) / 2 + 10;
        ClientFonts.clickGui[18].drawString(matrixStack, speedText, speedTextX, speedTextY, ColoringSystem.setAlpha(-1, alpha));

        dragging.setWidth(80);
        dragging.setHeight(80);
    }
}