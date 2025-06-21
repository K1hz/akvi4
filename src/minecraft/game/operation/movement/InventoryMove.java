package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import minecraft.game.transactions.EventInventoryClose;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.advantage.luvvy.MovementSystem;

import java.util.ArrayList;
import java.util.List;

@Defuse(name = "Gui Move",description = "Добавляет возможность бегать с открытым инвентарём", brand = Category.Movement)
public class InventoryMove extends Module {
    private int count;
    private final List<IPacket<?>> packet = new ArrayList<>();
    private final StopWatch wait = new StopWatch();
    private final ModeSetting mode = new ModeSetting("Обход", "Обычный", "Обычный", "FunTime", "Grim");
    private final CheckBoxSetting polarnew = new CheckBoxSetting("PolarNew(Funtime)", false).visibleIf(() -> mode.is("FunTime"));

    public InventoryMove() {
        addSettings(mode, polarnew);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player != null) {

            final KeyBinding[] pressedKeys = {
                    mc.gameSettings.keyBindForward,
                    mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft,
                    mc.gameSettings.keyBindRight,
                    mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint
            };

            if (mode.getValue().equals("FunTime")) {
                if (!wait.isReached(polarnew.getValue() ? 940 : 430)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }
            if (mode.getValue().equals("Grim")) {
                if (!wait.isReached(250)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }

            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) {
                return;
            }

            updateKeyBindingState(pressedKeys);
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SConfirmTransactionPacket p10) {
        }
        if (mode.getValue().equals("FunTime")) {
            if (e.getPacket() instanceof CClickWindowPacket p && MovementSystem.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }

            }
            if (e.getPacket() instanceof CCloseWindowPacket p && MovementSystem.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {

                }

            }
        }
        if (mode.getValue().equals("Grim")) {
            if (e.getPacket() instanceof CClickWindowPacket p && MovementSystem.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }

            }
            if (e.getPacket() instanceof CCloseWindowPacket p && MovementSystem.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {

                }
            }
        }
    }

    @Subscribe
    public void onClose(EventInventoryClose e) {
        if (mode.getValue().equals("FunTime")) {
            if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MovementSystem.isMoving()) {
                new Thread(() -> {
                    wait.reset();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket<?> p : packet) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                if (!polarnew.getValue()) {
                    e.cancel();
                }
            }
        }
        if (mode.getValue().equals("Grim")) {
            if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MovementSystem.isMoving()) {
                new Thread(() -> {
                    wait.reset();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket<?> p : packet) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                if (!polarnew.getValue()) {
                    e.cancel();
                }
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
}
