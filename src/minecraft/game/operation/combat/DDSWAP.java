package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;

import minecraft.game.transactions.EventCooldown;
import minecraft.game.transactions.EventKey;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.*;
import net.minecraft.potion.Effects;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Item Swap",description = "Перемещает предметы", brand = Category.Combat)
public class DDSWAP extends Module {
    final ModeSetting swapMode = new ModeSetting("Тип", "Умный", "Умный", "По бинду");
    final ModeSetting itemType = new ModeSetting("Предмет", "Щит", "Щит", "Геплы", "Тотем", "Шар");
    final ModeSetting swapType = new ModeSetting("Свапать на", "Геплы", "Щит", "Геплы", "Тотем", "Шар");

    final BindSetting keyToSwap = new BindSetting("Кнопка", -1).visibleIf(() -> swapMode.is("По бинду"));
    final SliderSetting health = new SliderSetting("Здоровье", 11.0F, 5.0F, 19.0F, 0.5F).visibleIf(() -> swapMode.is("Умный"));
    final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    boolean shieldIsCooldown;
    int oldItem = -1;

    final TimeCounterSetting delay = new TimeCounterSetting();
    final DDTOTEM DDTOTEM;

    public DDSWAP(DDTOTEM DDTOTEM) {
        this.DDTOTEM = DDTOTEM;
        addSettings(swapMode, itemType, swapType, keyToSwap, health);
    }

    @Subscribe
    public void onEventKey(EventKey e) {
        if (!swapMode.is("По бинду")) {
            return;
        }

        ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
        boolean isOffhandNotEmpty = !(offhandItemStack.getItem() instanceof AirItem);

        if (e.isKeyDown(keyToSwap.getValue()) && TimeCounterSetting.isReached(200)) {
            Item currentItem = offhandItemStack.getItem();
            boolean isHoldingSwapItem = currentItem == getSwapItem();
            boolean isHoldingSelectedItem = currentItem == getSelectedItem();
            int selectedItemSlot = getSlot(getSelectedItem());
            int swapItemSlot = getSlot(getSwapItem());

            if (selectedItemSlot >= 0) {
                if (!isHoldingSelectedItem) {
                    InventoryOrigin.moveItem(selectedItemSlot, 45, isOffhandNotEmpty);
                    TimeCounterSetting.reset();
                    return;
                }
            }
            if (swapItemSlot >= 0) {
                if (!isHoldingSwapItem) {
                    InventoryOrigin.moveItem(swapItemSlot, 45, isOffhandNotEmpty);
                    TimeCounterSetting.reset();
                }
            }
        }
    }

    @Subscribe
    private void onCooldown(EventCooldown e) {
        shieldIsCooldown = isCooldown(e);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!swapMode.is("Умный")) {
            return;
        }

        Item currentItem = mc.player.getHeldItemOffhand().getItem();

        if (TimeCounterSetting.isReached(400L)) {
            swapIfShieldIsBroken(currentItem);
            swapIfHealthToLow(currentItem);
            TimeCounterSetting.reset();
        }
        boolean isRightClickWithGoldenAppleActive = false;

        if (currentItem == Items.GOLDEN_APPLE && !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE)) {
            isRightClickWithGoldenAppleActive = mc.gameSettings.keyBindUseItem.isKeyDown();
        }


        if (isRightClickWithGoldenAppleActive) {
            TimeCounterSetting.reset();
        }
    }

    @Override
    public void onDisable() {
        shieldIsCooldown = false;
        oldItem = -1;
        super.onDisable();
    }

    private void swapIfHealthToLow(Item currentItem) {
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);

        int goldenAppleSlot = getSlot(getSwapItem());

        if (shieldIsCooldown || !gappleIsNotCooldown) {
            return;
        }

        if (isLowHealth() && !isHoldingGoldenApple && isHoldingSelectedItem) {
            InventoryOrigin.moveItem(goldenAppleSlot, 45, isOffhandNotEmpty);
            if (isOffhandNotEmpty && oldItem == -1) {
                oldItem = goldenAppleSlot;
            }
        } else if (!isLowHealth() && isHoldingGoldenApple && oldItem >= 0) {
            InventoryOrigin.moveItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private void swapIfShieldIsBroken(Item currentItem) {
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);
        int goldenAppleSlot = getSlot(getSwapItem());

        if (shieldIsCooldown && !isHoldingGoldenApple && isHoldingSelectedItem && gappleIsNotCooldown) {
            InventoryOrigin.moveItem(goldenAppleSlot, 45, isOffhandNotEmpty);
            if (isOffhandNotEmpty && oldItem == -1) {
                oldItem = goldenAppleSlot;
            }
            print(shieldIsCooldown + "");
        } else if (!shieldIsCooldown && isHoldingGoldenApple && oldItem >= 0) {
            InventoryOrigin.moveItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private boolean isLowHealth() {
        float currentHealth = mc.player.getHealth() + (mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f);
        return currentHealth <= health.getValue();
    }

    private boolean isCooldown(EventCooldown cooldown) {
        Item item = cooldown.getItem();


        if (!itemType.is("Shield")) {
            return false;
        } else {
            return cooldown.isAdded() && item instanceof ShieldItem;
        }
    }

    private Item getSwapItem() {
        return getItemByType(swapType.getValue());
    }

    private Item getSelectedItem() {
        return getItemByType(itemType.getValue());
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Шар" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }

    private int getSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                if (mc.player.inventory.getStackInSlot(i).isEnchanted()) {
                    finalSlot = i;
                    break;
                } else {
                    finalSlot = i;
                }
            }
        }
        if (finalSlot < 9 && finalSlot != -1) {
            finalSlot = finalSlot + 36;
        }
        return finalSlot;
    }
}