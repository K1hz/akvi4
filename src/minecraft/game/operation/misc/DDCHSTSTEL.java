package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Chеst Stеаlеr", description = "Забирает все предметы с сундука", brand = Category.Player)
public class DDCHSTSTEL extends Module {

    final SliderSetting delay = new SliderSetting("Задержка", 100.0f, 0.0f, 1000.0f, 1.0f);

    public DDCHSTSTEL() {
        addSettings(delay);
    }

    final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player.openContainer instanceof ChestContainer container) {
            IInventory lowerChestInventory = container.getLowerChestInventory();
            for (int index = 0; index < lowerChestInventory.getSizeInventory(); ++index) {
                ItemStack stack = lowerChestInventory.getStackInSlot(index);
                if (shouldMoveItem(stack)) {
                    if (delay.getValue() == 0.0f) {
                        moveItem(container, index, lowerChestInventory.getSizeInventory());
                    } else {
                        if (TimeCounterSetting.isReached(delay.getValue().longValue())) {
                            mc.playerController.windowClick(container.windowId, index, 0, ClickType.QUICK_MOVE, mc.player);
                            TimeCounterSetting.reset();
                        }
                    }
                }
            }
        }
    }

    private boolean shouldMoveItem(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.getItem() != Item.getItemById(0);
    }

    private void moveItem(ChestContainer container, int index, int multi) {
        for (int i = 0; i < multi; i++) {
            mc.playerController.windowClick(container.windowId, index + i, 0, ClickType.QUICK_MOVE, mc.player);
        }
    }

}
