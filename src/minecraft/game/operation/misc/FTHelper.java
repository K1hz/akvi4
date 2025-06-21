package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.transactions.EventKey;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.item.AirItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;


@Defuse(name = "FT Helper",description = "p", brand = Category.Player)
public class FTHelper extends Module {


    private final BindSetting disorientationKey = new BindSetting("Кнопка дезорентации", -1);
    private final BindSetting trapKey = new BindSetting("Кнопка трапки", -1);
    private final BindSetting flameKey = new BindSetting("Кнопка смерча", -1);
    private final BindSetting blatantKey = new BindSetting("Кнопка явной пыли", -1);
    private final BindSetting bowKey = new BindSetting("Кнопка арбалета", -1);
    private final BindSetting otrigaKey = new BindSetting("Кнопка отрыжки", -1);
    private final BindSetting serkaKey = new BindSetting("Кнопка серки", -1);
    private final BindSetting plastKey = new BindSetting("Кнопка пласта", -1);

    final StopWatch stopWatch = new StopWatch();
    InventoryOrigin.Hand handUtil = new InventoryOrigin.Hand();
    long delay;
    boolean disorientationThrow, trapThrow, flameThrow, blatantThrow, serkaThrow, otrigaThrow, bowThrow, plastThrow, shulkerThrow;

    public FTHelper() {
        addSettings(disorientationKey, trapKey, flameKey, blatantKey, serkaKey, bowKey, otrigaKey, plastKey);
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == disorientationKey.getValue()) {
            disorientationThrow = true;
        }
        if (e.getKey() == trapKey.getValue()) {
            trapThrow = true;
        }
        if (e.getKey() == flameKey.getValue()) {
            flameThrow = true;
        }
        if (e.getKey() == blatantKey.getValue()) {
            blatantThrow = true;
        }
        if (e.getKey() == otrigaKey.getValue()) {
            otrigaThrow = true;
        }
        if (e.getKey() == serkaKey.getValue()) {
            serkaThrow = true;
        }
        if (e.getKey() == bowKey.getValue()) {
            bowThrow = true;
        }
        if (e.getKey() == plastKey.getValue()) {
            plastThrow = true;
        }
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (disorientationThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("дезориентация", true);
            int invSlot = getItemForName("дезориентация", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Дезориентация не найдена!");
                disorientationThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                print("Заюзал дезориентацию!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            disorientationThrow = false;
        }

        if (trapThrow) {
            int hbSlot = getItemForName("трапка", true);
            int invSlot = getItemForName("трапка", false);


            if (invSlot == -1 && hbSlot == -1) {
                print("Трапка не найдена");
                trapThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP)) {
                print("Заюзал трапку!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            trapThrow = false;
        }
        if (flameThrow) {
            int hbSlot = getItemForName("огненный", true);
            int invSlot = getItemForName("огненный", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Огненный смерч не найден");
                flameThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.FIRE_CHARGE)) {
                print("Заюзал огненный смерч!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            flameThrow = false;
        }
        if (bowThrow) {
            int hbSlot = getItem(Items.CROSSBOW, true);
            int invSlot = getItem(Items.CROSSBOW, false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Арбалет не найден");
                bowThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.CROSSBOW)) {
                print("Заюзал арбалет!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            bowThrow = false;
        }
        if (serkaThrow) {
            int hbSlot = getItemForName("серная", true);
            int invSlot = getItemForName("серная", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Серка не найдена");
                serkaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                print("Заюзал серку!");
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
        if (otrigaThrow) {
            int hbSlot = getItemForName("отрыжки", true);
            int invSlot = getItemForName("отрыжки", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Отрыга не найдена");
                otrigaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                print("Заюзал отрыгу!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            otrigaThrow = false;
        }
        if (plastThrow) {
            int hbSlot = getItemForName("пласт", true);
            int invSlot = getItemForName("пласт", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Пласт не найден");
                plastThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP)) {
                print("Заюзал пласт!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            plastThrow = false;
        }
        if (blatantThrow) {
            int hbSlot = getItemForName("явная", true);
            int invSlot = getItemForName("явная", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Явная пыль не найдена");
                blatantThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.TNT)) {
                print("Заюзал явную пыль!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryOrigin.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            blatantThrow = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
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
        disorientationThrow = false;
        trapThrow = false;
        flameThrow = false;
        blatantThrow = false;
        plastThrow = false;
        otrigaThrow = false;
        serkaThrow = false;
        bowThrow = false;
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
    private int getItem(Item input, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }
            if (itemStack.getItem() == input) {
                return i;
            }
        }
        return -1;
    }
}