package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.luvvy.PlayerSettingsModule;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Autо Lеаvе",description = "Делает определённое действие при появлении игроков", brand = Category.Misc)
public class DDLEAVER extends Module {

    final ModeSetting action = new ModeSetting("Действие", "Kick", "Kick", "/hub", "/spawn", "/home");
    final SliderSetting distance = new SliderSetting("Дистанция", 50.0f, 1.0f, 100.0f, 1.0f);

    public DDLEAVER() {
        addSettings(action, distance);
    }
    @Subscribe
    private void onUpdate(EventUpdate event) {
        mc.world.getPlayers().stream()
                .filter(this::isValidPlayer)
                .findFirst()
                .ifPresent(this::performAction);
    }

    private boolean isValidPlayer(PlayerEntity player) {
        return player.isAlive()
                && player.getHealth() > 0.0f
                && player.getDistance(mc.player) <= distance.getValue()
                && player != mc.player
                && PlayerSettingsModule.isNameValid(player.getName().getString())
                && !FriendManager.isFriend(player.getName().getString());
    }

    private void performAction(PlayerEntity player) {
        if (!action.getValue().equalsIgnoreCase("Kick")) {
            mc.player.sendChatMessage(action.getValue());
            mc.ingameGUI.func_238452_a_(new StringTextComponent("[DDLEAVER] " + player.getGameProfile().getName()),
                    new StringTextComponent("test"), -1,
                    -1, -1);
        } else {
            mc.player.connection.getNetworkManager().closeChannel(new StringTextComponent("Вы вышли с сервера! \n" + player.getGameProfile().getName()));
        }
        toggle();
    }
}
