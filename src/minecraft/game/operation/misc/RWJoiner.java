package minecraft.game.operation.misc;

import minecraft.game.advantage.luvvy.InventoryOrigin;
import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.figures.TimeCounterSetting;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@Defuse(name = "RW Joiner",description = "Заходит на определенный гриф рв", brand = Category.Player)
public class RWJoiner extends Module {

    private final SliderSetting griefSelection = new SliderSetting("Номер грифа", 1, 1, 42, 1);
    private final TimeCounterSetting timerUtil = new TimeCounterSetting();
    public RWJoiner() {
        this.addSettings(griefSelection);
    }

    public int grief;

    public void onEnable() {
        int slot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.COMPASS, true);
        if (slot != -1) {
            mc.player.inventory.currentItem = slot;
            mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
        }
        mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
        super.onEnable();
    }
    @Subscribe
    private void onUpdate(EventUpdate event) {
        grief = griefSelection.getValue().intValue();
        handleEventUpdate();
    }

    @Subscribe
    public void onPacket(EventPacket eventPacket) {
        String govno;
        if (eventPacket.getPacket() instanceof SJoinGamePacket) {
            try {
                govno = "Вы успешно зашли на " + grief + " гриф!";
                print(govno);
                this.toggle();
            } catch (Exception var5) {
            }
        }

        IPacket packetPacket = eventPacket.getPacket();
        if (packetPacket instanceof SChatPacket packet) {
            govno = TextFormatting.getTextWithoutFormattingCodes(packet.getChatComponent().getString());
            if (govno.contains("К сожалению сервер переполнен") || govno.contains("Подождите 20 секунд!") || govno.contains("большой поток игроков") || govno.contains("Сервер перезагружается")) {
                int slot = InventoryOrigin.getInstance().getSlotInInventoryOrHotbar(Items.COMPASS, true);
                if (slot != -1) {
                    mc.player.inventory.currentItem = slot;
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
                }
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            }
        }
    }
    private void handleEventUpdate() {
        if (mc.currentScreen == null) {
            if (mc.player.ticksExisted < 5) {
                if (timerUtil.isReached(100)) {
                    mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                    timerUtil.reset();
                }
            }
        } else if (mc.currentScreen instanceof ChestScreen) {
            try {
                int numberGrief = grief;
                ContainerScreen container = (ContainerScreen)mc.currentScreen;

                for(int i = 0; i < container.getContainer().inventorySlots.size(); ++i) {
                    String s = ((Slot)container.getContainer().inventorySlots.get(i)).getStack().getDisplayName().getString();
                    if (ClientReceive.isConnectedToServer("reallyworld") && s.contains("ГРИФЕРСКОЕ ВЫЖИВАНИЕ") && this.timerUtil.isReached(50L)) {
                        mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, mc.player);
                        this.timerUtil.reset();
                    }

                    if (s.contains("ГРИФ #" + numberGrief + " (1.16.5") && this.timerUtil.isReached(50L)) {
                        mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.PICKUP, mc.player);
                        this.timerUtil.reset();
                    }
                }
            } catch (Exception e) {
            }
        }

    }
}
