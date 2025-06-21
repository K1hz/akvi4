package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.transactions.EventRender2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class HotBar implements minecraft.game.display.clientrender.timeupdate.ElementRenderer {
    private final Minecraft mc = Minecraft.getInstance();
    final CompactAnimation animation = new CompactAnimation(Easing.EASE_OUT_QUAD, 100);

    double chat = animation.getValue();

    private final FontRenderer fontRenderer = this.mc.fontRenderer;
    @Subscribe
    public void render(EventRender2D e) {
        if (e.getType() != EventRender2D.Type.POST)
            return;
        int hotbarX = (this.mc.getMainWindow().getScaledWidth() - 190) / 2;
        int hotbarY = this.mc.getMainWindow().getScaledHeight() - 26;
        drawStyledRect((hotbarX - 1), (hotbarY - 2), 183.2F, 25.0F, 8, 1.0F, 2130706432);

        NonNullList<ItemStack> hotbarItems = this.mc.player.inventory.mainInventory;
        for (int i = 0; i < 9; i++) {
            int slotX = hotbarX + i * 21;
            int slotY = hotbarY;
            int slotColor = ColoringSystem.rgb(11,11,11);
            if (i == this.mc.player.inventory.currentItem)
                slotColor = 1439485132;
            GraphicsSystem.drawRoundedRect(slotX, slotY, 21.0F, 20.0F, 7, slotColor);
            ItemStack itemStack = (ItemStack)hotbarItems.get(i);
            if (!itemStack.isEmpty()) {
                this.mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, slotX + 2, slotY + 2);
                this.mc.getItemRenderer().renderItemOverlayIntoGUI(this.fontRenderer, itemStack, slotX + 2, slotY + 2, null);
            }
        }
        NonNullList<ItemStack> offhandItems = this.mc.player.inventory.offHandInventory;
        ItemStack offhandItem = (ItemStack)offhandItems.get(0);
        if (!offhandItem.isEmpty())

            for (int j = 0; j < 1; j++) {
                int slotX = hotbarX - 24 - j * 20;
                int slotY = hotbarY;
                int slotColor = 1426063360;

                drawStyledRect(slotX, slotY, 11, 21.0F, 4, 1.0F, 2130706432);
                ItemStack itemStack = (ItemStack)offhandItems.get(j);
                if (!itemStack.isEmpty()) {
                    this.mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, (int) (slotX + 2.5f), slotY + 2);
                    this.mc.getItemRenderer().renderItemOverlayIntoGUI(this.fontRenderer, itemStack, slotX + 2, slotY + 2, null);
                }
            }
    }

    private void drawStyledRect(float x, float y, float width, float height, float radius, float borderWidth, int color) {
        int alpha123 = ColoringSystem.rgb(20, 20, 20);
        GraphicsSystem.drawRoundedRect(x, y, width + 9, height, radius, ColoringSystem.rgb(20,20,20));
    }
}