package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.transactions.EventPacket;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlayerListItemPacket;

import java.util.*;

@Defuse(name = "Anti Воt", description = "Не дает ауре бить по антиботу", brand = Category.Combat)
public class DDBOT extends Module {

    public CheckBoxSetting matrix = new CheckBoxSetting("Matrix обход", true);

    public DDBOT() {
        setEnabled(false, true);
        this.addSettings(this.matrix);
    }

    private final Set<UUID> susPlayers = new ConcurrentSet<>();
    private static final Map<UUID, Boolean> botsMap = new HashMap<>();

    @Subscribe
    private void onUpdate(EventUpdate e) {
        Iterator<UUID> iterator = susPlayers.iterator();
        while (iterator.hasNext()) {
            UUID susPlayer = iterator.next();
            PlayerEntity entity = mc.world.getPlayerByUuid(susPlayer);

            if (entity != null) {
                String playerName = entity.getGameProfile().getName();

                boolean isNameBot = playerName.startsWith("CIT-");
                boolean isNameBot2 = playerName.startsWith("[ZNPC]");

                int count = 0;
                for (ItemStack item : entity.getArmorInventoryList()) {
                    if (!item.isEmpty()) count++;
                }

                boolean isFullArmor = count == 4;
                boolean isFakeUUID = !entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(playerName));

                boolean isBot = isFullArmor || isNameBot || isFakeUUID || isNameBot2;

                botsMap.put(susPlayer, isBot);
            }

            iterator.remove();
        }

        if (mc.player.ticksExisted % 100 == 0) {
            botsMap.keySet().removeIf(uuid -> mc.world.getPlayerByUuid(uuid) == null);
        }
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SPlayerListItemPacket p) {
            if (p.getAction() == SPlayerListItemPacket.Action.ADD_PLAYER) {
                for (SPlayerListItemPacket.AddPlayerData entry : p.getEntries()) {
                    GameProfile profile = entry.getProfile();

                    if (botsMap.containsKey(profile.getId()) || susPlayers.contains(profile.getId())) {
                        continue;
                    }

                    boolean isInvalid = profile.getProperties().isEmpty() && entry.getPing() != 0;
                    boolean isNameBot = profile.getName().startsWith("CIT-");

                    if (isInvalid || isNameBot) {
                        susPlayers.add(profile.getId());
                    }
                }
            }
        }
    }

    public static boolean isBot(Entity entity) {
        if (entity instanceof PlayerEntity) {
            String playerName = entity.getName().getString();
            boolean isNameBot = playerName.startsWith("CIT-");
            boolean isMarkedBot = botsMap.getOrDefault(entity.getUniqueID(), false);
            return isNameBot || isMarkedBot;
        }
        return false;
    }

    public static boolean isBotU(Entity entity) {
        return !entity.getUniqueID().equals(PlayerEntity.getOfflineUUID(entity.getName().getString())) && entity.isInvisible();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        botsMap.clear();
    }
}
