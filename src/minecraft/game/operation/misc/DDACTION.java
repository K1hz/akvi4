package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.system.managers.friend.Friend;
import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.Locale;

@Defuse(name = "Autо Асtiоns",description = "Автоматические действия", brand = Category.Misc)
public class DDACTION extends Module {

    public static ModeListSetting actions = new ModeListSetting("Действия",
        new CheckBoxSetting("AutoTPAccept", false),
        new CheckBoxSetting("AutoFish", false),
        new CheckBoxSetting("AutoTool", false),
        new CheckBoxSetting("AutoRespawn", true)
    );

    private final CheckBoxSetting onlyFriend = new CheckBoxSetting("Только друзья", true).visibleIf(() -> actions.is("AutoTPAccept").getValue());
    public final CheckBoxSetting silent = new CheckBoxSetting("Незаметный", true).visibleIf(() -> actions.is("AutoTool").getValue());
    private final String[] teleportMessages = new String[]{"has requested teleport", "просит телепортироваться", "хочет телепортироваться к вам", "просит к вам телепортироваться"};
    private final TimeCounterSetting delay = new TimeCounterSetting();
    private boolean isHooked = false;
    private boolean needToHook = false;
    public int itemIndex = -1, oldSlot = -1;
    boolean status;

    public DDACTION() {
        addSettings(actions, onlyFriend);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) {
            return;
        }
        if (actions.is("AutoTool").getValue() && !mc.player.isCreative()) {
            if (isMousePressed()) {
                itemIndex = findBestToolSlotInHotBar();
                if (itemIndex != -1) {
                    status = true;

                    if (oldSlot == -1) {
                        oldSlot = mc.player.inventory.currentItem;
                    }

                    if (silent.getValue()) {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(itemIndex));
                    } else {
                        mc.player.inventory.currentItem = itemIndex;
                    }
                }
            } else if (status && oldSlot != -1) {
                if (silent.getValue()) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
                } else {
                    mc.player.inventory.currentItem = oldSlot;
                }

                itemIndex = oldSlot;
                status = false;
                oldSlot = -1;
            }
        }

        if (actions.is("AutoRespawn").getValue()) {
            if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime > 5) {
                mc.player.respawnPlayer();
                mc.displayGuiScreen(null);
            }
        }

        if (actions.is("AutoFish").getValue()) {
            if (delay.isReached(600) && isHooked) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                isHooked = false;
                needToHook = true;
                delay.reset();
            }

            if (delay.isReached(300) && needToHook) {
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                needToHook = false;
                delay.reset();
            }
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) return;

        if (actions.is("AutoFish").getValue()) {
            if (e.getPacket() instanceof SPlaySoundEffectPacket p) {
                if (p.getSound().getName().getPath().equals("entity.fishing_bobber.splash")) {
                    isHooked = true;
                    delay.reset();
                }
            }
        }

        if (actions.is("AutoTPAccept").getValue()) {
            if (e.getPacket() instanceof SChatPacket p) {
                String raw = p.getChatComponent().getString().toLowerCase(Locale.ROOT);
                String message = TextFormatting.getTextWithoutFormattingCodes(p.getChatComponent().getString());
                if (isTeleportMessage(message)) {
                    if (onlyFriend.getValue()) {
                        boolean yes = false;

                        for (Friend friend : FriendManager.getFriends()) {
                            if (raw.contains(friend.getName().toLowerCase(Locale.ROOT))) {
                                yes = true;
                                break;
                            }
                        }

                        if (!yes) return;
                    }

                    mc.player.sendChatMessage("/tpaccept");
                }
            }
        }
    }

    private boolean isTeleportMessage(String message) {
        return Arrays.stream(this.teleportMessages).map(String::toLowerCase).anyMatch(message::contains);
    }

    private int findBestToolSlotInHotBar() {
        if (mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
            Block block = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int slot = 0; slot < 9; slot++) {
                float speed = mc.player.inventory.getStackInSlot(slot)
                        .getDestroySpeed(block.getDefaultState());

                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = slot;
                }
            }
            return bestSlot;
        }
        return -1;
    }


    private boolean isMousePressed() {
        return mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.isKeyDown();
    }

    @Override
    public void onDisable() {
        status = false;
        itemIndex = -1;
        oldSlot = -1;
        super.onDisable();
    }

}
