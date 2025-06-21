
package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import minecraft.system.AG;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.managers.friend.FriendManager;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;

@Defuse(name = "Name Protect",description = "Скрывает настоящий ник", brand = Category.Visual)
public class NameProtect extends Module {

    public static String fakeName = "Protected"; // Имя зафиксировано

    public CheckBoxSetting hideFriends = new CheckBoxSetting(
            "Скрыть Друзей",
            false// По умолчанию не скрывать друзей
    );

    public NameProtect() {
        addSettings(hideFriends); // Добавление настройки для скрытия друзей
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        // Удалено обновление fakeName - теперь оно фиксированное
    }

    public static String getReplaced(String input) {
        if (AG.getInst() != null && AG.getInst().getModuleManager().getNameProtect().isEnabled()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), fakeName);
        }
        return input;
    }

    public String getDisplayName(String originalName) {
        if (hideFriends.getValue() && FriendManager.isFriend(originalName)) {
            return fakeName; // Возвращаем "protected", если друг скрыт
        }
        return originalName; // В противном случае возвращаем оригинальное имя
    }
}
