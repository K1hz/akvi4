package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.transactions.EventRender2D;
import minecraft.system.managers.drag.Dragging;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;

@RequiredArgsConstructor
public class Potions implements ElementRenderer {

    final Dragging dragging;

    public CompactAnimation potionWidth = new CompactAnimation(Easing.EASE_OUT_CUBIC, 350);
    public CompactAnimation potionHeight = new CompactAnimation(Easing.EASE_OUT_CUBIC, 350);
    public CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_CUBIC, 400);

    float width;
    float height;

    @Subscribe
    public void render(EventRender2D eventRender2D) {
        boolean shouldRender = mc.ingameGUI.getChatGUI().getChatOpen() || !mc.player.getActivePotionEffects().isEmpty();

        animation.run(shouldRender ? 1 : 0);
        animation.update();

        float animationValue = (float) animation.getValue();
        if (animationValue <= 0) return;

        MatrixStack ms = eventRender2D.getMatrixStack();

        float minWidth = 66;
        float initialHeight = ClientFonts.clickGui[16].getFontHeight() + 7;
        float ClientFontsize = 6f;
        float padding = 5;

        float posX = dragging.getX();
        float posY = dragging.getY();

        String name = "Potions";

        float maxWidth = ClientFonts.clickGui[18].getWidth(name) + padding * 3 + ClientFonts.icons_hud[22].getWidth("K");
        for (EffectInstance ef : mc.player.getActivePotionEffects()) {
            String ampStr = (ef.getAmplifier() + 1) + "";
            String nameText = I18n.format(ef.getEffectName());
            float nameWidth = ClientFonts.clickGui[16].getWidth(nameText + ampStr);
            String durationText = EffectUtils.getPotionDurationString(ef, 1);
            float durationWidth = ClientFonts.clickGui[16].getWidth(durationText);

            float localWidth = padding + 15 + nameWidth + 15 + durationWidth + padding;
            maxWidth = Math.max(maxWidth, localWidth);
        }

        potionWidth.run(Math.max(maxWidth, minWidth));
        width = potionWidth.getNumberValue().floatValue();
        height = initialHeight;

        GraphicsSystem.drawRoundedRect(posX, posY, width, height, 3, ColoringSystem.setAlpha(ColoringSystem.rgb(20,20,20), (int) (255 * animationValue)));

        float iconHeight = ClientFonts.icons_hud[22].getFontHeight();
        float textHeight = ClientFonts.clickGui[18].getFontHeight();
        float centerY = posY + padding + (Math.max(iconHeight, textHeight) - iconHeight) / 2 + 1;

        ClientFonts.clickGui[18].drawString(ms, name, posX + padding + ClientFonts.icons_hud[22].getWidth("K") + 2, centerY, ColoringSystem.setAlpha(-1, (int) (255 * animationValue)));
        ClientFonts.icons_hud[22].drawString(ms, "K", posX + padding, centerY, ColoringSystem.setAlpha(-1, (int) (255 * animationValue)));

        float contentStartY = posY + initialHeight;

        for (EffectInstance ef : mc.player.getActivePotionEffects()) {
            String ampStr = ef.getAmplifier() == 0 ? "" : " " + (ef.getAmplifier() + 1);
            String nameText = I18n.format(ef.getEffectName());
            String durationText = EffectUtils.getPotionDurationString(ef, 1);
            float durationWidth = ClientFonts.clickGui[16].getWidth(durationText);

            float rowHeight = ClientFontsize + padding + 3;
            height += rowHeight;

            GraphicsSystem.drawRoundedRect(posX, contentStartY, width, rowHeight, 3, ColoringSystem.setAlpha(ColoringSystem.rgb(20,20,20), (int) (255 * animationValue)));

            Effect effect = ef.getPotion();
            PotionSpriteUploader potionSpriteUploader = mc.getPotionSpriteUploader();
            TextureAtlasSprite textureAtlasSprite = potionSpriteUploader.getSprite(effect);
            mc.getTextureManager().bindTexture(textureAtlasSprite.getAtlasTexture().getTextureLocation());
            RenderSystem.enableBlend();
            DisplayEffectsScreen.blit(ms, (int) (posX + padding), (int) (contentStartY + 3.5f), 0, 9, 9, textureAtlasSprite);
            RenderSystem.disableBlend();

            int color = isBadEffect(ef) ? ColoringSystem.rgb(255, 71, 71) : -1;

            float textX = posX + padding + 12;
            ClientFonts.clickGui[16].drawString(ms, nameText + ampStr, textX, contentStartY + 5, ColoringSystem.setAlpha(color, (int) (255 * animationValue)));

            float separatorX = posX + width - padding - durationWidth - 4;
            float durationX = posX + width - padding - durationWidth;

            GraphicsSystem.drawRoundedRect(separatorX, contentStartY + 3, 1.0f, rowHeight - 5, 1, ColoringSystem.setAlpha(ColoringSystem.rgb(30, 30, 30), (int) (255 * animationValue)));
            ClientFonts.clickGui[16].drawString(ms, durationText, durationX, contentStartY + 5, ColoringSystem.setAlpha(color, (int) (255 * animationValue)));

            contentStartY += rowHeight;
        }

        potionHeight.run(height);
        height = potionHeight.getNumberValue().floatValue();

        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    private boolean isBadEffect(EffectInstance effect) {
        return effect.getPotion() == Effects.SLOWNESS
                || effect.getPotion() == Effects.BLINDNESS
                || effect.getPotion() == Effects.WEAKNESS
                || effect.getPotion() == Effects.WITHER
                || effect.getPotion() == Effects.POISON
                || effect.getPotion() == Effects.MINING_FATIGUE
                || effect.getPotion() == Effects.NAUSEA
                || effect.getPotion() == Effects.UNLUCK
                || effect.getPotion() == Effects.HUNGER;
    }
}
