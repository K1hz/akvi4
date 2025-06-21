package net.minecraft.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.inventory.container.ClickType;
import minecraft.game.operation.wamost.api.ModuleManager;
import minecraft.system.AG;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.advantage.advisee.IMinecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ChestScreen extends ContainerScreen<ChestContainer> implements IHasContainer<ChestContainer>, IMinecraft {
    /**
     * The ResourceLocation containing the chest GUI texture.
     */
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    /**
     * Window height is calculated with these values; the more rows, the higher
     */
    private final int inventoryRows;
    private final ITextComponent title;

    public ChestScreen(ChestContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.title = title;
        this.passEvents = false;
        int i = 222;
        int j = 114;
        this.inventoryRows = container.getNumRows();
        this.ySize = 114 + this.inventoryRows * 18;
        this.playerInventoryTitleY = this.ySize - 94;
    }

    Button button;
    ModuleManager moduleManager = AG.getInst().getModuleManager();
    SelfDestruct selfDestruct = moduleManager.getSelfDestruct();
    @Override
    protected void init() {
        super.init();
        if (!selfDestruct.unhooked) {
            this.addButton(new Button(width / 2 + 90, height / 2 - 84, 90, 20,
                    new StringTextComponent("Сложить всё"), (button) -> {
                if (mc.player != null && mc.playerController != null) {
                    dropItems();
                }
            }));
        }
        if (!selfDestruct.unhooked) {
            this.addButton(new Button(width / 2 + 90, height / 2 - 62, 90, 20,
                    new StringTextComponent("Забрать всё"), (button) -> { // Изменили текст кнопки
                if (mc.player != null && mc.playerController != null) {
                    takeItems(); // Вызываем метод для забора предметов
                }
            }));
        }
    }

    public void dropItems() {
        for (int i = 0; i < this.container.getInventory().size() && this.mc.currentScreen == this; ++i) {
            // Забираем предмет из сундука в инвентарь игрока
            mc.playerController.windowClick(this.container.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
        }
    }
    public void takeItems() {
        // Количество слотов в сундуке (число строк сундука умножаем на 9)
        int chestSlots = this.inventoryRows * 9;

        // Перебираем все слоты сундука
        for (int i = 0; i < chestSlots && this.mc.currentScreen == this; ++i) {
            // Перемещаем предметы из сундука в инвентарь игрока
            mc.playerController.windowClick(this.container.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
        }
    }


    @Override
    public void closeScreen() {
        super.closeScreen();
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
        this.minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.blit(matrixStack, i, j + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }
}
