package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.advisee.KeyStorage;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.transactions.EventRender2D;
import minecraft.system.AG;
import minecraft.system.managers.drag.Dragging;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class KeyBinds implements ElementRenderer {

    final Dragging dragging;

    public CompactAnimation keyBindsWidth = new CompactAnimation(Easing.EASE_OUT_EXPO, 350);
    public CompactAnimation keyBindsHeight = new CompactAnimation(Easing.EASE_OUT_EXPO, 350);
    public CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_CUBIC, 400);

    float width;
    float height;

    @Subscribe
    public void render(EventRender2D eventRender2D) {
        boolean shouldRender = mc.ingameGUI.getChatGUI().getChatOpen();

        for (minecraft.game.operation.wamost.api.Module module : AG.getInst().getModuleManager().getModules()) {
            if (module.getBind() != 0 && module.isEnabled()) {
                shouldRender = true;
                break;
            }
        }

        animation.run(shouldRender ? 1 : 0);
        animation.update();

        float animationValue = (float) animation.getValue();
        if (animationValue <= 0) return;

        MatrixStack matrixStack = eventRender2D.getMatrixStack();

        float minWidth = 60;
        float initialHeight = ClientFonts.clickGui[16].getFontHeight() + 7;
        float ClientFontsize = 6f;
        float padding = 5;

        float posX = dragging.getX();
        float posY = dragging.getY();

        String name = "Key Binds";

        float maxWidth = ClientFonts.clickGui[18].getWidth(name) + padding * 3 + ClientFonts.icons_hud[24].getWidth("J");
        for (minecraft.game.operation.wamost.api.Module module : AG.getInst().getModuleManager().getModules()) {
            if (module.getAnimation().getValue() <= 0 || module.getBind() == 0) continue;

            String nameText = module.getName();
            float nameWidth = ClientFonts.clickGui[16].getWidth(nameText);

            String bindText = KeyStorage.getKey(module.getBind());
            float bindWidth = ClientFonts.clickGui[16].getWidth(bindText);

            float localWidth = padding + nameWidth + 15 + bindWidth + padding;
            maxWidth = Math.max(maxWidth, localWidth);
        }

        keyBindsWidth.run(Math.max(maxWidth, minWidth));
        width = keyBindsWidth.getNumberValue().floatValue();

        height = initialHeight;

        int alpha = (int) (255 * animationValue);

        GraphicsSystem.drawRoundedRect(posX, posY, width, height, 3, ColoringSystem.setAlpha(ColoringSystem.rgb(20,20,20), alpha));

        float iconHeight = ClientFonts.icons_hud[24].getFontHeight();
        float textHeight = ClientFonts.clickGui[18].getFontHeight();
        float centerY = posY + padding + (Math.max(iconHeight, textHeight) - iconHeight) / 2 + 1;

        ClientFonts.clickGui[18].drawString(matrixStack, name, posX + padding + ClientFonts.icons_hud[24].getWidth("J") + 4, centerY, ColoringSystem.setAlpha(-1, alpha));
        ClientFonts.icons_hud[24].drawString(matrixStack, "J", posX + padding, centerY, ColoringSystem.setAlpha(-1, alpha));

        float contentStartY = posY + initialHeight;

        for (Module module : AG.getInst().getModuleManager().getModules()) {
            module.getAnimation().update();

            if (!(module.getAnimation().getValue() > 0) || module.getBind() == 0) continue;

            String nameText = module.getName();

            String bindText = KeyStorage.getKey(module.getBind());
            float bindWidth = ClientFonts.clickGui[16].getWidth(bindText);

            float moduleHeight = (ClientFontsize + padding + 3) * (float) module.getAnimation().getValue();
            height += moduleHeight;

            int settingAlpha = (-1 >> 24) & 0xFF;
            int animationAlpha = (int) (255 * module.getAnimation().getValue());
            int combinedAlpha = (settingAlpha * animationAlpha * alpha) / (255 * 255);

            int textColor = ColoringSystem.setAlpha(-1, combinedAlpha);
            int bindColor = ColoringSystem.setAlpha(-1, combinedAlpha);

            GraphicsSystem.drawRoundedRect(posX, contentStartY, width, moduleHeight, 3, ColoringSystem.setAlpha(ColoringSystem.rgb(20,20,20), combinedAlpha));
            ClientFonts.clickGui[16].drawString(matrixStack, nameText, posX + padding, contentStartY + 5, textColor);

            float dividerX = posX + width - bindWidth - padding - 4;
            float bindX = posX + width - padding - bindWidth;

            GraphicsSystem.drawRoundedRect(dividerX, contentStartY + 3, 1.0f, moduleHeight - 5, 1, ColoringSystem.rgb(30, 30, 30));
            ClientFonts.clickGui[16].drawString(matrixStack, bindText, bindX, contentStartY + 5, bindColor);

            contentStartY += moduleHeight;
        }

        keyBindsHeight.run(height);
        height = keyBindsHeight.getNumberValue().floatValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }
}
