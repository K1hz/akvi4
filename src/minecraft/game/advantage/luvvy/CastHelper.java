package minecraft.game.advantage.luvvy;

import java.util.ArrayList;
import java.util.List;

import minecraft.system.AG;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;

public class CastHelper {

    private final List<EntityType> casts = new ArrayList<>();

    public static EntityType isInstanceof(Entity entity, EntityType... types) {
        for (EntityType type : types) {
            if (entity == Minecraft.getInstance().player) {
                if (type == EntityType.SELF)
                    return type;
            }
            if (type == EntityType.VILLAGERS && entity instanceof VillagerEntity) {
                return type;
            }
            if (type == EntityType.FRIENDS && entity instanceof PlayerEntity) {
                if (AG.getInst().getFriendManager().isFriend(String.valueOf(entity.getName()))) {
                    return type;
                }
            }
            if (type == EntityType.PLAYERS && entity instanceof PlayerEntity
                    && entity != Minecraft.getInstance().player && !AG.getInst().getFriendManager().isFriend(String.valueOf(entity.getName()))) {
                return type;
            }
            if (type == EntityType.MOBS && entity instanceof MobEntity) {
                return type;
            }
            if (type == EntityType.ANIMALS && (entity instanceof AnimalEntity || entity instanceof GolemEntity)) {
                return type;
            }
        }
        return null;
    }

    public CastHelper apply(EntityType type) {
        this.casts.add(type);
        return this;
    }

    public EntityType[] build() {
        return this.casts.toArray(new EntityType[0]);
    }

    public enum EntityType {
        PLAYERS, MOBS, ANIMALS, VILLAGERS, FRIENDS, SELF;
    }
}
