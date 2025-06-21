package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import minecraft.game.advantage.advisee.AnimationNumbers;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.transactions.EventRender3D;
import minecraft.system.AG;
import net.minecraft.client.gui.screen.ChatScreen;

@RequiredArgsConstructor
public class PlayerInfo implements ElementRenderer {
    private static final float OFFSET = 3;

    @Subscribe
    public void render(EventRender2D eventRender2D) {
        float basePosY = calculateInitialPosY();

        float coordinationY = basePosY;
        float tpsY = basePosY - getElementHeight();
        float bpsY = AG.getInst().getModuleManager().getHud().elementsPlayerInfo.is("Coordination").getValue()
                ? tpsY - getElementHeight()
                : basePosY - getElementHeight() - 2;

        if (AG.getInst().getModuleManager().getHud().elementsPlayerInfo.is("Coordination").getValue()) {
            renderPosition(eventRender2D, coordinationY);
        }

        if (AG.getInst().getModuleManager().getHud().elementsPlayerInfo.is("Tps").getValue()) {
            renderTPS(eventRender2D, tpsY);
        }

        if (AG.getInst().getModuleManager().getHud().elementsPlayerInfo.is("Bps").getValue()) {
            renderBPS(eventRender2D, bpsY);
        }
    }

    private float calculateInitialPosY() {
        float fontHeight = ClientFonts.clickGui[16].getFontHeight();
        return window.getScaledHeight() - OFFSET - fontHeight + 3 - (mc.currentScreen instanceof ChatScreen ? 13 : 0);
    }

    private float getElementHeight() {
        float iconHeight = ClientFonts.icons_hud[20].getFontHeight();
        float fontHeight = ClientFonts.clickGui[16].getFontHeight();
        return Math.max(iconHeight, fontHeight) + OFFSET + 5;
    }

    private void renderPosition(EventRender2D eventRender2D, float posY) {
        float posXLeft = OFFSET + 2;

        float iconWidth = ClientFonts.icons_hud[20].getWidth("H");
        float iconHeight = ClientFonts.icons_hud[20].getFontHeight();

        float separatorWidth = 1.0f;
        float separatorPadding = 3.0f;

        String coordsText = (int) mc.player.getPosX() + ", " + (int) mc.player.getPosY() + ", " + (int) mc.player.getPosZ();
        float coordsWidth = ClientFonts.clickGui[16].getWidth(coordsText);
        float rectWidth = iconWidth + separatorWidth + coordsWidth + (3 * OFFSET) + separatorPadding + 2;
        float rectHeight = Math.max(ClientFonts.clickGui[16].getFontHeight(), iconHeight) + 5;

        GraphicsSystem.drawRoundedRect(posXLeft - OFFSET, posY - rectHeight + 8, rectWidth, rectHeight, 3, ColoringSystem.rgb(20,20,20));
        ClientFonts.icons_hud[20].drawString(eventRender2D.getMatrixStack(), "H", posXLeft + 1, posY - iconHeight + 8, -1);

        float separatorX = posXLeft + iconWidth + OFFSET;
        float separatorHeight = rectHeight - 4;
        GraphicsSystem.drawRoundedRect(separatorX, posY - rectHeight + 10, separatorWidth, separatorHeight, 1, ColoringSystem.rgb(30, 30, 30));
        ClientFonts.clickGui[16].drawString(eventRender2D.getMatrixStack(), coordsText, separatorX + separatorWidth + OFFSET, posY - ClientFonts.clickGui[16].getFontHeight() + 8, -1);
    }

    private void renderTPS(EventRender2D eventRender2D, float posY) {
        float posXRight = window.getScaledWidth() - OFFSET + 2;

        float iconWidth = ClientFonts.icons_hud[20].getWidth("G");
        float iconHeight = ClientFonts.icons_hud[20].getFontHeight();

        float separatorWidth = 1.0f;
        float separatorPadding = 3.0f;

        String tpsText = String.format("%.1f Tps", AG.getInst().getTpsCalc().getTPS());
        float tpsTextWidth = ClientFonts.clickGui[16].getWidth(tpsText);
        float rectWidth = tpsTextWidth + iconWidth + separatorWidth + (2 * OFFSET) + separatorPadding + 3;
        float rectHeight = Math.max(ClientFonts.clickGui[16].getFontHeight(), iconHeight) + 5;

        GraphicsSystem.drawRoundedRect(posXRight - rectWidth, posY + 11, rectWidth, rectHeight, 3, ColoringSystem.rgb(20,20,20));

        float textPosX = posXRight - rectWidth + OFFSET;
        ClientFonts.clickGui[16].drawString(eventRender2D.getMatrixStack(), tpsText, textPosX + 1, posY + ClientFonts.clickGui[16].getFontHeight() + 7, -1);

        float separatorX = textPosX + tpsTextWidth + separatorPadding + 1;
        float separatorHeight = rectHeight - 4;
        GraphicsSystem.drawRoundedRect(separatorX, posY + (rectHeight - separatorHeight) / 2 + 11, separatorWidth, separatorHeight, 1, ColoringSystem.rgb(30, 30, 30));

        float iconPosX = separatorX + separatorWidth + OFFSET - 1;
        ClientFonts.icons_hud[20].drawString(eventRender2D.getMatrixStack(), "G", iconPosX, posY + iconHeight + 6, -1);
    }

    private void renderBPS(EventRender2D eventRender2D, float posY) {
        float posXLeft = OFFSET + 2;

        float iconWidth = ClientFonts.icons_hud[22].getWidth("P");
        float iconHeight = ClientFonts.icons_hud[20].getFontHeight();

        float separatorWidth = 1.0f;
        float separatorPadding = 3.0f;

        AnimationNumbers animationNumbers = new AnimationNumbers();
        String bpsText = animationNumbers.BPSAnim(mc.player, true) + " Bps";
        float bpsWidth = ClientFonts.clickGui[16].getWidth(bpsText);
        float rectWidth = iconWidth + separatorWidth + bpsWidth + (3 * OFFSET) + separatorPadding;
        float rectHeight = Math.max(ClientFonts.clickGui[16].getFontHeight(), iconHeight) + 5;

        GraphicsSystem.drawRoundedRect(posXLeft - OFFSET, posY + 13, rectWidth, rectHeight, 3, ColoringSystem.rgb(20,20,20));

        ClientFonts.icons_nur[22].drawString(eventRender2D.getMatrixStack(), "P", posXLeft, posY + iconHeight + 8, -1);

        float separatorX = posXLeft + iconWidth + OFFSET - 2;
        float separatorHeight = rectHeight - 4;
        GraphicsSystem.drawRoundedRect(separatorX, posY + rectHeight, separatorWidth, separatorHeight, 1, ColoringSystem.rgb(30, 30, 30));

        ClientFonts.clickGui[16].drawString(eventRender2D.getMatrixStack(), bpsText, separatorX + separatorWidth + OFFSET, posY + ClientFonts.clickGui[16].getFontHeight() + 9, -1);
    }
}
