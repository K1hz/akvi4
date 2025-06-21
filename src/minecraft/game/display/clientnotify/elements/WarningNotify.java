package minecraft.game.display.clientnotify.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.display.clientnotify.most.Notify;
import minecraft.game.advantage.advisee.SoundPlayer;
import minecraft.game.advantage.figures.Vector4i;

import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.operation.visual.ClickGui;
import minecraft.system.AG;
import minecraft.system.styles.StyleManager;
import net.minecraft.client.gui.screen.ChatScreen;
import minecraft.game.operation.visual.Hud;
import minecraft.system.managers.Theme;

public class WarningNotify extends Notify {

    public WarningNotify(String content, long delay) {
        super(content, delay);
        animationY.setValue(window.getScaledHeight());
        alphaAnimation.setValue(0);
        SoundPlayer.playSound("warning.wav");
    }

    @Override
    public void render(MatrixStack matrixStack, int multiplierY) {
        int fontSize = 14;
        int iconSizeF = 26;
        this.end = ((this.getInit() + this.getDelay()) - System.currentTimeMillis()) <= (this.getDelay() - 500) - this.getDelay();
        if (mc.currentScreen instanceof ChatScreen) {
            chatOffset.run(ClientFonts.msMedium[fontSize].getFontHeight() + 32);
        } else {
            chatOffset.run(ClientFonts.msMedium[fontSize].getFontHeight() + 2);
        }

        float contentWidth = ClientFonts.msMedium[fontSize].getWidth(getContent());

        float x, y, width, height;
        float iconSize = ClientFonts.icons_wex[iconSizeF].getWidth("J");

        width = margin + contentWidth + margin;
        height = (margin / 2F) + ClientFonts.msMedium[fontSize].getFontHeight() + (margin / 2F);

        x = (float) (window.getScaledWidth() - width - margin) + 2;
        y = (float) ((window.getScaledHeight() - height) - 1 - (height * multiplierY) - (multiplierY * 4) - chatOffset.getValue());
        alphaAnimation.run(this.end ? 0 : 1);
        animationY.run(this.end ? y - 1 : y);

        float posX = (float) (window.getScaledWidth() / 2.0) - (width / 2.0f) + 5;
        float posY = (float) ((float) animationY.getValue() - (window.getScaledHeight() / 2.0) - (height / 2.0f) + 125);


        int color12347 = ColoringSystem.rgb(20, 20, 20); // По умолчанию черный
        int ckr = ColoringSystem.rgb(20, 20, 20); // По умолчанию черный

        double alphaValue = alphaAnimation.getValue();
        int i = ColoringSystem.setAlpha(ckr, (int) (255 * alphaValue));
        int o = ColoringSystem.setAlpha(ckr, (int) (255 * alphaValue));
        float animPos = (float) (width - width * alphaValue);
        GraphicsSystem.drawRoundedRect(posX - iconSize + animPos, posY, width - 2 + iconSize, height, 2, new Vector4i(i, i, o, o));
        ClientFonts.icons_wex[iconSizeF].drawString(matrixStack, "J", posX - (iconSize - 3) + animPos, posY - height / 2 + ClientFonts.icons_wex[iconSizeF].getFontHeight() - margin / 2 + 0.5f, ColoringSystem.reAlphaInt(-1, (int) (255 * alphaValue)));
        ClientFonts.msMedium[fontSize].drawString(matrixStack, getContent(), posX + margin + animPos, posY + margin / 2F + 2.5f, ColoringSystem.reAlphaInt(-1, (int) (255 * alphaAnimation.getValue())));
    }

    @Override
    public boolean hasExpired() {
        return this.animationY.isFinished() && this.end;
    }
}
