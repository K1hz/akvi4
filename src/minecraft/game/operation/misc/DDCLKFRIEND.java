package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;

import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventKey;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.BindSetting;
import minecraft.game.advantage.luvvy.PlayerSettingsModule;
import net.minecraft.entity.player.PlayerEntity;

@Defuse(name = "Click Friend",description = "Добавляет в друзья по кнопке", brand = Category.Player)
public class DDCLKFRIEND extends Module {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    public DDCLKFRIEND() {
        addSettings(throwKey);
    }
    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == throwKey.getValue() && mc.pointedEntity instanceof PlayerEntity) {

            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();

            if (!PlayerSettingsModule.isNameValid(playerName)) {
                print("Невозможно добавить бота в друзья, увы, как бы вам не хотелось это сделать");
                return;
            }

            if (FriendManager.isFriend(playerName)) {
                FriendManager.remove(playerName);
                printStatus(playerName, true);
            } else {
                FriendManager.add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) print(name + " удалён из друзей");
        else print(name + " добавлен в друзья");
    }
}
