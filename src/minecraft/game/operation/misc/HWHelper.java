package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.transactions.EventKey;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Defuse(name = "HW Helper", brand = Category.Player, description = "Юзает по нажатию кнопки предмет")
public class HWHelper extends Module {

    private final ModeListSetting mode = new ModeListSetting("Тип",
            new CheckBoxSetting("Использование по бинду", true));

    private final BindSetting stanKey = new BindSetting("Кнопка стана", -1)
            .visibleIf(() -> mode.is("Использование по бинду").getValue());
    private final BindSetting opitKey = new BindSetting("Кнопка пузыря опыта", -1)
            .visibleIf(() -> mode.is("Использование по бинду").getValue());
    private final BindSetting vzrivtrapKey = new BindSetting("Кнопка взрывной трапки", -1)
            .visibleIf(() -> mode.is("Использование по бинду").getValue());
    private final BindSetting flamepalkaKey = new BindSetting("Кнопка взрывной палочки", -1)
            .visibleIf(() -> mode.is("Использование по бинду").getValue());
    private final BindSetting serkaKey = new BindSetting("Кнопка молочного зелья", -1)
            .visibleIf(() -> mode.is("Использование по бинду").getValue());

    final StopWatch stopWatch = new StopWatch();
    InventoryOrigin.Hand handUtil = new InventoryOrigin.Hand();
    long delay;
    boolean stanThrow, opitThrow, vzrivtrapkaThrow, flamepalkaThrow, serkaThrow;

    public HWHelper() {
        addSettings(mode, stanKey, opitKey, vzrivtrapKey, flamepalkaKey, serkaKey);
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == stanKey.getValue()) {
            stanThrow = true;
        }
        if (e.getKey() == opitKey.getValue()) {
            opitThrow = true;
        }
        if (e.getKey() == vzrivtrapKey.getValue()) {
            vzrivtrapkaThrow = true;
        }
        if (e.getKey() == flamepalkaKey.getValue()) {
            flamepalkaThrow = true;
        }
        if (e.getKey() == serkaKey.getValue()) {
            serkaThrow = true;
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (stanThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("оглушение", true);
            int invSlot = getItemForName("оглушение", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Стан не найден!");
                stanThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHER_STAR)) {
                print("Использовал стан!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            stanThrow = false;
        }
        if (opitThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("пузырь опыта", true);
            int invSlot = getItemForName("пузырь опыта", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Пузырь опыта не найден!");
                opitThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.EXPERIENCE_BOTTLE)) {
                print("Использовал пузырь с опытом!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            opitThrow = false;
        }
        if (vzrivtrapkaThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("взрывная трапка", true);
            int invSlot = getItemForName("взрывная трапка", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Взрывная трапка не найдена!");
                vzrivtrapkaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.PRISMARINE_SHARD)) {
                print("Заюзал взрывную трапку!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            vzrivtrapkaThrow = false;
        }
        if (flamepalkaThrow) {
            // САЛАТ СПАСАЙ
            int hbSlot = getItemForName("взрывная палочка", true); // мега поиск ящика
            int invSlot = getItemForName("взрывная палочка", false); // мега поиск ящика


            if (invSlot == -1 && hbSlot == -1) {
                print("Взрыв. палка не найдена!");
                flamepalkaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.BLAZE_ROD)) {
                print("Использовал взрывную палочку!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            flamepalkaThrow = false;
        }
        if (serkaThrow) {
            int hbSlot = getItemForName("молочно", true);
            int invSlot = getItemForName("молочно", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Молочное зелье не найдено");
                serkaThrow = false;
                return;
            }


            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                print("Использовал молочное зелье!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            serkaThrow = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }


    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        flamepalkaThrow = false;
        vzrivtrapkaThrow = false;
        opitThrow = false;
        stanThrow = false;
        serkaThrow = false;
        delay = 0;
        super.onDisable();
    }

    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }

            String displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString());
            if (displayName != null && displayName.toLowerCase().contains(name)) {
                return i;
            }
        }
        return -1;
    }
}