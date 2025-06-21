package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.transactions.EventRender2D;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;

public class ArmorHud implements ElementRenderer {

    @Subscribe
    public void render(EventRender2D eventRender2D) {
        if (mc == null || mc.player == null) return;

        ItemRenderer itemRenderer = mc.getItemRenderer();
        FontRenderer fontRenderer = mc.fontRenderer;
        int x = mc.getMainWindow().getScaledWidth() / 2 + 100;
        int y = mc.getMainWindow().getScaledHeight() - (16 + 2);
        int itemSpacing = 25;

        MatrixStack matrixStack = eventRender2D.getMatrixStack();

        ItemStack mainHand = mc.player.getHeldItemMainhand();
        if (!mainHand.isEmpty()) {
            renderDurability(matrixStack, mainHand, x, y - 10);
            itemRenderer.renderItemAndEffectIntoGUI(mainHand, x, y);
            itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mainHand, x, y, null);
            x += itemSpacing;
        }

        for (ItemStack itemStack : mc.player.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;

            renderDurability(matrixStack, itemStack, x, y - 10);
            itemRenderer.renderItemAndEffectIntoGUI(itemStack, x, y);
            itemRenderer.renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, null);
            x += itemSpacing;
        }
    }

    private void renderDurability(MatrixStack matrixStack, ItemStack itemStack, int x, int y) {
        int durability = itemStack.getMaxDamage() - itemStack.getDamage();
        int maxDurability = itemStack.getMaxDamage();
        int durabilityPercentage = (int) ((durability / (float) maxDurability) * 100);
        String durabilityText = durabilityPercentage + "%";

        float textWidth = ClientFonts.clickGui[16].getWidth(durabilityText);
        float centeredX = x + (18 - textWidth) / 2;

        ClientFonts.clickGui[16].drawString(matrixStack, durabilityText, centeredX, y, -1);
    }
}
