package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import minecraft.system.AG;
import minecraft.system.managers.friend.FriendManager;
import net.minecraft.client.Minecraft;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import net.minecraft.util.text.StringTextComponent;

@Defuse(name = "Fake Stats", description = "Скрывает настоящий ник", brand = Category.Visual)
public class FakeStats extends Module {

    public static String fakeName = "Князь"; // Имя зафиксировано

    public CheckBoxSetting hideFriends = new CheckBoxSetting(
            "Скрыть Друзей",
            false // По умолчанию не скрывать друзей
    );

    public FakeStats() {
        addSettings(hideFriends); // Добавление настройки для скрытия друзей
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        // Логика для обновлений (оставьте пустым или добавьте дополнительную логику, если нужно)
    }

    public static String getReplaced(String input) {
        // Проверяем, активирован ли модуль NameProtect
        if (AG.getInst() != null && AG.getInst().getModuleManager().getNameProtect().isEnabled()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), fakeName);
        }
        return input;
    }

    public String getDisplayName(String originalName) {
        // Если включен модуль скрытия друзей, возвращаем оригинальное имя
        if (hideFriends.getValue() && isFriend(originalName)) {
            return ""; // Возвращаем пустое имя для друзей, если это требуется
        }

        // В любом другом случае, заменяем имя только на табе или в нужном контексте
        return originalName.equals(Minecraft.getInstance().player.getName()) ? fakeName : originalName;
    }

    private boolean isFriend(String playerName) {
        // Здесь можно добавить логику для определения, является ли игрок другом
        return FriendManager.isFriend(playerName);
    }
}
