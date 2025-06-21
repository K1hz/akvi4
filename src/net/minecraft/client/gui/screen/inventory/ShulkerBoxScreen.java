package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.operation.wamost.api.ModuleManager;
import minecraft.system.AG;

public class ShulkerBoxScreen extends ContainerScreen<ShulkerBoxContainer> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");

    public ShulkerBoxScreen(ShulkerBoxContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        ++this.ySize;
    }

    ModuleManager moduleManager = AG.getInst().getModuleManager();
    SelfDestruct selfDestruct = moduleManager.getSelfDestruct();

    @Override
    protected void init() {
        super.init();

        if (!selfDestruct.unhooked) {
            this.addButton(new Button(width / 2 + 89, height / 2 - 83, 90, 20,
                    new StringTextComponent("Сложить всё"), (button) -> {
                if (minecraft.player != null && minecraft.playerController != null) {
                    dropItems();
                }
            }));

            this.addButton(new Button(width / 2 + 89, height / 2 - 61, 90, 20,
                    new StringTextComponent("Забрать всё"), (button) -> {
                if (minecraft.player != null && minecraft.playerController != null) {
                    takeItems();
                }
            }));
        }
    }

    public void dropItems() {
        // Перебираем все слоты, чтобы выбросить предметы из инвентаря игрока в шулкер
        for (int i = 0; i < this.container.getInventory().size() && this.minecraft.currentScreen == this; ++i) {
            // Перемещаем предметы в шулкер
            minecraft.playerController.windowClick(this.container.windowId, i, 0, ClickType.QUICK_MOVE, minecraft.player);
        }
    }

    public void takeItems() {
        // Количество слотов в шалкере (обычно первые 27 слотов)
        int shulkerSlots = 27;

        // Перебираем слоты шалкера
        for (int i = 0; i < shulkerSlots && this.minecraft.currentScreen == this; ++i) {
            // Перемещаем предметы из шалкера в инвентарь игрока
            minecraft.playerController.windowClick(this.container.windowId, i, 0, ClickType.QUICK_MOVE, minecraft.player);
        }
    }



    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
    }
}
