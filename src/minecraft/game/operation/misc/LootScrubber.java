package minecraft.game.operation.misc;


import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.TimerTransaction;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Defuse(name = "Loot Scrubber", description = "Выкидывает хлам из инвентаря.", brand = Category.Misc)
public class LootScrubber extends Module {
    public ModeSetting modes = new ModeSetting("Мод", "Легитный", "Легитный", "Обычный");
    private final ModeListSetting filter = new ModeListSetting("Выбрасывать:",
                    new CheckBoxSetting("Алмазную броню", true),
                    new CheckBoxSetting("Алмазные инструменты", false),
                    new CheckBoxSetting("Алмазное оружие", true),
                    new CheckBoxSetting("Паутину", false),
                    new CheckBoxSetting("Гнилую плоть", false),
                    new CheckBoxSetting("Перья", false),
                    new CheckBoxSetting("Кольчужную броню", true),
                    new CheckBoxSetting("Золотую броню", false),
                    new CheckBoxSetting("Железную броню", true),
                    new CheckBoxSetting("Кожаную броню", false),
                    new CheckBoxSetting("Порох", false),
                    new CheckBoxSetting("Нити", true),
                    new CheckBoxSetting("Палки", false),
                    new CheckBoxSetting("Стрелы", true),
                    new CheckBoxSetting("Стрелы с эффектами", false),
                    new CheckBoxSetting("Деревянные инструменты", false),
                    new CheckBoxSetting("Золотые инструменты", true),
                    new CheckBoxSetting("Каменные инструменты", false),
                    new CheckBoxSetting("Железные инструменты", true),
                    new CheckBoxSetting("Яйца мобов", false),
                    new CheckBoxSetting("Семена", false),
                    new CheckBoxSetting("Уголь", false),
                    new CheckBoxSetting("Цветы", false),
                    new CheckBoxSetting("Растения", false));

    private final TimerTransaction stopWatch = TimerTransaction.create();
    private int currentSlot = -1;
    private final Set<Item> diamondArmor = new HashSet();
    private final Set<Item> diamondTools = new HashSet();
    private final Set<Item> diamondWeapons = new HashSet();

    public LootScrubber() {
        setEnabled(false, true);
        this.addSettings(this.modes,this.filter);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player != null) {
            if (this.modes.is("Легитный")) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    if (this.stopWatch.hasReached(45L)) {
                        for(int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
                            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
                            if (!itemStack.isEmpty() && this.shouldDropItem(itemStack) && i != this.currentSlot) {
                                this.currentSlot = i;
                                this.dropItemAtSlot(i);
                                this.stopWatch.reset();
//                           print("Выброшен предмет: " + itemStack.getTranslationKey());
                                return;
                            }
                        }
                    }

                }
            }
            if (this.modes.is("Обычный")) {
                    if (this.stopWatch.hasReached(45L)) {
                        for(int i = 0; i < mc.player.inventory.mainInventory.size(); ++i) {
                            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
                            if (!itemStack.isEmpty() && this.shouldDropItem(itemStack) && i != this.currentSlot) {
                                this.currentSlot = i;
                                this.dropItemAtSlot(i);
                                this.stopWatch.reset();
//                           print("Выброшен предмет: " + itemStack.getTranslationKey());
                                return;
                            }
                        }
                    }
            }
        }
    };

    private void dropItemAtSlot(int slot) {
        if (mc.player != null && mc.playerController != null) {
            ItemStack stack = mc.player.inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                mc.playerController.windowClick(mc.player.container.windowId, slot, 0, ClickType.THROW, mc.player);
                mc.playerController.windowClick(mc.player.container.windowId, slot, 1, ClickType.THROW, mc.player);
            }
        }

    }

    private boolean shouldDropItem(ItemStack itemStack) {
        this.diamondArmor.add(Items.DIAMOND_HELMET);
        this.diamondArmor.add(Items.DIAMOND_CHESTPLATE);
        this.diamondArmor.add(Items.DIAMOND_LEGGINGS);
        this.diamondArmor.add(Items.DIAMOND_BOOTS);
        this.diamondTools.add(Items.DIAMOND_SHOVEL);
        this.diamondTools.add(Items.DIAMOND_PICKAXE);
        this.diamondTools.add(Items.DIAMOND_AXE);
        this.diamondTools.add(Items.DIAMOND_HOE);
        this.diamondWeapons.add(Items.DIAMOND_SWORD);
        Item item = itemStack.getItem();
        if (filter.is("Алмазную броню").getValue() && this.diamondArmor.contains(item)) {
            return true;
        } else if (filter.is("Алмазные инструменты").getValue() && this.diamondTools.contains(item)) {
            return true;
        } else if (filter.is("Алмазное оружие").getValue() && this.diamondWeapons.contains(item)) {
            return true;
        } else if (filter.is("Паутину").getValue() && item == Items.COBWEB) {
            return true;
        } else if (filter.is("Гнилую плоть").getValue() && item == Items.ROTTEN_FLESH) {
            return true;
        } else if (filter.is("Перья").getValue() && item == Items.FEATHER) {
            return true;
        } else if (!filter.is("Кольчужную броню").getValue() || item != Items.CHAINMAIL_HELMET && item != Items.CHAINMAIL_CHESTPLATE && item != Items.CHAINMAIL_LEGGINGS && item != Items.CHAINMAIL_BOOTS) {
            if (filter.is("Золотую броню").getValue() && (item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS)) {
                return true;
            } else if (!filter.is("Железную броню").getValue() || item != Items.IRON_HELMET && item != Items.IRON_CHESTPLATE && item != Items.IRON_LEGGINGS && item != Items.IRON_BOOTS) {
                if (!filter.is("Кожаную броню").getValue() || item != Items.LEATHER_HELMET && item != Items.LEATHER_CHESTPLATE && item != Items.LEATHER_LEGGINGS && item != Items.LEATHER_BOOTS) {
                    if (filter.is("Порох").getValue() && item == Items.GUNPOWDER) {
                        return true;
                    } else if (filter.is("Нити").getValue() && item == Items.STRING) {
                        return true;
                    } else if (filter.is("Палки").getValue() && item == Items.STICK) {
                        return true;
                    } else if (filter.is("Стрелы").getValue() && item == Items.ARROW) {
                        return true;
                    } else if (filter.is("Стрелы с эффектами").getValue() && item == Items.TIPPED_ARROW) {
                        return true;
                    } else if (filter.is("Деревянные инструменты").getValue() && (item == Items.WOODEN_SHOVEL || item == Items.WOODEN_PICKAXE || item == Items.WOODEN_AXE || item == Items.WOODEN_HOE)) {
                        return true;
                    } else if (filter.is("Золотые инструменты").getValue() && (item == Items.GOLDEN_SHOVEL || item == Items.GOLDEN_PICKAXE || item == Items.GOLDEN_AXE || item == Items.GOLDEN_HOE)) {
                        return true;
                    } else if (filter.is("Каменные инструменты").getValue() && (item == Items.STONE_SHOVEL || item == Items.STONE_PICKAXE || item == Items.STONE_AXE || item == Items.STONE_HOE)) {
                        return true;
                    } else if (filter.is("Железные инструменты").getValue() && (item == Items.IRON_SHOVEL || item == Items.IRON_PICKAXE || item == Items.IRON_AXE || item == Items.IRON_HOE)) {
                        return true;
                    } else if (filter.is("Яйца мобов").getValue() && item instanceof SpawnEggItem) {
                        return true;
                    } else if (!filter.is("Семена") .getValue()|| item != Items.WHEAT_SEEDS && item != Items.PUMPKIN_SEEDS && item != Items.MELON_SEEDS && item != Items.BEETROOT_SEEDS) {
                        if (filter.is("Уголь") .getValue()&& (item == Items.COAL || item == Items.CHARCOAL)) {
                            return true;
                        } else if (filter.is("Цветы").getValue() && (item == Items.DANDELION || item == Items.POPPY || item == Items.BLUE_ORCHID || item == Items.ALLIUM || item == Items.AZURE_BLUET || item == Items.RED_TULIP || item == Items.ORANGE_TULIP || item == Items.WHITE_TULIP || item == Items.PINK_TULIP || item == Items.OXEYE_DAISY || item == Items.CORNFLOWER || item == Items.LILY_OF_THE_VALLEY || item == Items.WITHER_ROSE)) {
                            return true;
                        } else {
                            return filter.is("Растения").getValue() && (item == Items.GRASS || item == Items.FERN || item == Items.DEAD_BUSH || item == Items.SEAGRASS || item == Items.SEA_PICKLE || item == Items.SUNFLOWER || item == Items.LILAC || item == Items.ROSE_BUSH || item == Items.PEONY || item == Items.LARGE_FERN);
                        }
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
}
