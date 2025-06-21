package minecraft.game.advantage.luvvy;

import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import minecraft.system.AG;
import minecraft.game.transactions.EventPacket;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.advantage.advisee.IMinecraft;
import org.apache.commons.lang3.StringUtils;

public class InventoryOrigin implements IMinecraft {

    @Getter
    private static InventoryOrigin instance = new InventoryOrigin();


    public static int findEmptySlot(boolean inHotBar) {
        int start = inHotBar ? 0 : 9;
        int end = inHotBar ? 9 : 45;

        for (int i = start; i < end; ++i) {
            if (!mc.player.inventory.getStackInSlot(i).isEmpty()) {
                continue;
            }

            return i;
        }
        return -1;
    }

    public static int findBlockInHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) continue;
            return i;
        }
        return -1;
    }

    public static Slot getInventorySlot(Item item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(item) && s.slotNumber >= mc.player.openContainer.inventorySlots.size() - 36).findFirst().orElse(null);
    }

    public static Slot getInventorySlot(List<Item> item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> item.contains(s.getStack().getItem()) && s.slotNumber >= mc.player.openContainer.inventorySlots.size() - 36).findFirst().orElse(null);
    }

    public Slot getFoodMaxSaturationSlot() {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().getFood() != null && !s.getStack().getItem().getFood().canEatWhenFull()).max(Comparator.comparingDouble(s -> s.getStack().getItem().getFood().getSaturation())).orElse(null);
    }

    public static int getInventoryCount(Item item) {
        return IntStream.range(0, 45).filter(i -> mc.player.inventory.getStackInSlot(i).getItem().equals(item)).map(i -> mc.player.inventory.getStackInSlot(i).getCount()).sum();
    }

    public void clickSlot(Slot slot, int button, ClickType clickType, boolean packet) {
        if (slot != null) clickSlotId(slot.slotNumber, button, clickType, packet);
    }

    public void clickSlotId(int slot, int button, ClickType clickType, boolean packet) {
        if (packet) {
            mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, slot, button, clickType, ItemStack.EMPTY, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
        } else {
            mc.playerController.windowClick(mc.player.openContainer.windowId, slot, button, clickType, mc.player);
        }
    }

    public int getPrice(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getTag();
        if (tag == null) return -1;
        String price = StringUtils.substringBetween(tag.toString(), "\"text\":\" $", "\"}]");
        if (price == null || price.isEmpty()) return -1;
        price = price.replaceAll(" ", "").replaceAll(",", "");
        return Integer.parseInt(price);
    }

    public static int getHotbarSlotOfItem() {
        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() == Items.ELYTRA) {
                return -2;
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == Items.ELYTRA) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static int getSlotIDFromItem(Item item) {
        for (ItemStack stack : mc.player.getArmorInventoryList()) {
            if (stack.getItem() == item) {
                return -2;
            }
        }
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == item) {
                slot = i;
                break;
            }
        }
        if (slot < 9 && slot != -1) {
            slot = slot + 36;
        }
        return slot;
    }

    public static void moveItem(int from, int to, boolean air) {

        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        if (air)
            pickupItem(from, 0);
    }


    public static void moveItemTime(int from, int to, boolean air, int time) {

        if (from == to)
            return;
        pickupItem(from, 0, time);
        pickupItem(to, 0, time);
        if (air)
            pickupItem(from, 0, time);
    }

    public static void moveItem(int from, int to) {
        if (from == to)
            return;
        pickupItem(from, 0);
        pickupItem(to, 0);
        pickupItem(from, 0);
    }

    public static void pickupItem(int slot, int button) {
        mc.playerController.windowClick(0, slot, button, ClickType.PICKUP, mc.player);
    }

    public static void pickupItem(int slot, int button, int time) {
        mc.playerController.windowClickFixed(0, slot, button, ClickType.PICKUP, mc.player, time);
    }

    public int getAxeInInventory(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;

        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    public int findBestSlotInHotBar() {
        int emptySlot = findEmptySlot();
        if (emptySlot != -1) {
            return emptySlot;
        } else {
            return findNonSwordSlot();
        }
    }

    private int findEmptySlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty() && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    private int findNonSwordSlot() {
        for (int i = 0; i < 9; i++) {
            if (!(mc.player.inventory.getStackInSlot(i).getItem() instanceof SwordItem) && !(mc.player.inventory.getStackInSlot(i).getItem() instanceof ElytraItem) && mc.player.inventory.currentItem != i) {
                return i;
            }
        }
        return -1;
    }

    public int getSlotInInventory(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public int getSlotInInventoryOrHotbar(Item item, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static int getSlotInInventoryOrHotbar() {
        int firstSlot = 0;
        int lastSlot = 9;
        int finalSlot = -1;
        for (int i = firstSlot; i < lastSlot; i++) {

            if (Block.getBlockFromItem(mc.player.inventory.getStackInSlot(i).getItem()) instanceof SlabBlock) {
                finalSlot = i;
            }
        }

        return finalSlot;
    }

    public static boolean doesHotbarHaveItem(Item item) {
        for(int i = 0; i < 9; ++i) {
            mc.player.inventory.getStackInSlot(i);
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                return true;
            }
        }

        return false;
    }

    public static void inventorySwapClick(Item item, boolean rotation) {
        if (InventoryHelper.getItemIndex(item) != -1) {
            int i;
            if (doesHotbarHaveItem(item)) {
                for(i = 0; i < 9; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                        if (i != mc.player.inventory.currentItem) {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(i));
                        }

                        if (rotation) {
                            DDATTACK DDATTACK = AG.getInst().getModuleManager().getDDATTACK();
                            if (DDATTACK.getTarget() != null) {
                                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                            }
                        }

                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                        if (i != mc.player.inventory.currentItem) {
                            mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                        }
                        break;
                    }
                }
            }

            if (!doesHotbarHaveItem(item)) {
                for(i = 0; i < 36; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                        mc.playerController.windowClick(0, i, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem % 8 + 1));
                        if (rotation) {
                            DDATTACK DDATTACK = AG.getInst().getModuleManager().getDDATTACK();
                            if (DDATTACK.getTarget() != null) {
                                mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.isOnGround()));
                            }
                        }

                        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(net.minecraft.util.Hand.MAIN_HAND));
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                        mc.playerController.windowClick(0, i, mc.player.inventory.currentItem % 8 + 1, ClickType.SWAP, mc.player);
                        break;
                    }
                }
            }

        }
    }

    public static class Hand {
        public static boolean isEnabled;
        private boolean isChangingItem;
        private int originalSlot = -1;

        public void onEventPacket(EventPacket eventPacket) {
            if (!eventPacket.isReceive()) {
                return;
            }
            if (eventPacket.getPacket() instanceof SHeldItemChangePacket) {
                this.isChangingItem = true;
            }
        }

        public void handleItemChange(boolean resetItem) {
            if (this.isChangingItem && this.originalSlot != -1) {
                isEnabled = true;
                mc.player.inventory.currentItem = this.originalSlot;
                if (resetItem) {
                    this.isChangingItem = false;
                    this.originalSlot = -1;
                    isEnabled = false;
                }
            }
        }

        public void setOriginalSlot(int slot) {
            this.originalSlot = slot;
        }
    }

    public static int getItemSlot(Item input) {
        Iterator var1 = mc.player.getArmorInventoryList().iterator();

        while(var1.hasNext()) {
            ItemStack stack = (ItemStack)var1.next();
            if (stack.getItem() == input) {
                return -2;
            }
        }

        int slot = -1;

        for(int i = 0; i < 36; ++i) {
            ItemStack s = mc.player.inventory.getStackInSlot(i);
            if (s.getItem() == input) {
                slot = i;
                break;
            }
        }

        if (slot < 9 && slot != -1) {
            slot += 36;
        }

        return slot;
    }

}
