package minecraft.game.enjoin.feature;

import minecraft.system.AG;
import minecraft.game.enjoin.interfaces.*;
import minecraft.system.managers.friend.Friend;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

import minecraft.system.managers.friend.FriendManager;
import minecraft.game.enjoin.api.CommandException;
import minecraft.game.advantage.luvvy.PlayerSettingsModule;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;
    final Minecraft mc;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElseThrow();
        switch (commandType) {
            case "add" -> addFriend(parameters, logger);
            case "remove" -> removeFriend(parameters, logger);
            case "clear" -> clearFriendList(logger);
            case "list" -> getFriendList(logger);
            default -> throw new CommandException(TextFormatting.RED
                    + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear, list");
        }
    }

    @Override
    public String name() {
        return "friend";
    }

    @Override
    public String description() {
        return "Позволяет управлять списком друзей";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "friend add <name> - Добавить друга по имени",
                commandPrefix + "friend remove <name> - Удалить друга по имени",
                commandPrefix + "friend list - Получить список друзей",
                commandPrefix + "friend clear - Очистить список друзей",
                TextFormatting.UNDERLINE  + "Пример использования:" + TextFormatting.RED  + " "+ commandPrefix + "friend add dedinside"
        );
    }

    private void addFriend(final Parameters parameters, Logger logger) {
        String friendName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя друга для добавления/удаления."));

        if (!PlayerSettingsModule.isNameValid(friendName)) {
            logger.log(TextFormatting.RED + "Недопустимое имя.");
            return;
        }

        if (friendName.equalsIgnoreCase(mc.player.getName().getString())) {
            logger.log(TextFormatting.RED + "Вы не можете добавить себя в друзья, как бы вам не хотелось");
            return;

        }

        if (FriendManager.isFriend(friendName)) {
            logger.log(TextFormatting.RED + "Этот игрок уже находится в вашем списке друзей.");
            return;
        }
        FriendManager.add(friendName);
        logger.log(TextFormatting.GRAY + "Вы успешно добавили " + TextFormatting.GRAY + friendName + TextFormatting.GRAY + " в друзья!");
    }

    private void removeFriend(final Parameters parameters, Logger logger) {
        String friendName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя друга для добавления/удаления."));
        if (FriendManager.isFriend(friendName)) {
            FriendManager.remove(friendName);
            logger.log(TextFormatting.GRAY + "Вы успешно удалили " + TextFormatting.GRAY + friendName
                    + TextFormatting.GRAY + " из друзей!");
            return;
        }
        logger.log(TextFormatting.RED + friendName + " не найден в списке друзей");
    }

    private void getFriendList(Logger logger) {
        if (FriendManager.getFriends().isEmpty()) {
            logger.log(TextFormatting.RED + "Список друзей пустой.");
            return;
        }

        logger.log(TextFormatting.GRAY + "Список друзей:");
        for (Friend friend : FriendManager.getFriends()) {
            logger.log(TextFormatting.GRAY + friend.getName());
        }
    }

    private void clearFriendList(Logger logger) {
        if (FriendManager.getFriends().isEmpty()) {
            logger.log(TextFormatting.RED + "Список друзей пустой.");
            return;
        }
        FriendManager.clear();
        logger.log(TextFormatting.GRAY + "Список друзей очищен.");
    }
}
