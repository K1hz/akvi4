package net.minecraft.item;

import minecraft.system.AG;
import minecraft.game.operation.wamost.api.ModuleManager;
import minecraft.game.operation.combat.DDCOLDWN;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class EnderPearlItem extends Item {
    public EnderPearlItem(Properties builder) {
        super(builder);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        worldIn.playSound((PlayerEntity) null, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), SoundEvents.ENTITY_ENDER_PEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        playerIn.getCooldownTracker().setCooldown(this, 20);

        ModuleManager moduleManager = AG.getInst().getModuleManager();
        DDCOLDWN DDCOLDWN = moduleManager.getDDCOLDWN();

        DDCOLDWN.ItemEnum itemEnum = minecraft.game.operation.combat.DDCOLDWN.ItemEnum.getItemEnum(this);

        if (DDCOLDWN.isEnabled() && itemEnum != null && DDCOLDWN.isCurrentItem(itemEnum)) {
            DDCOLDWN.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
        } else {
            playerIn.getCooldownTracker().setCooldown(this, 20);
        }
        if (!worldIn.isRemote) {
            EnderPearlEntity enderpearlentity = new EnderPearlEntity(worldIn, playerIn);
            enderpearlentity.setItem(itemstack);
            enderpearlentity.func_234612_a_(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
//            enderpearlentity.func_234612_a_(playerIn, (targetPearl.isState() ? targetPearl.rotateVector.y : playerIn.rotationPitch), (targetPearl.isState() ? targetPearl.rotateVector.x : playerIn.rotationYaw), 0.0F, 1.5F, 1.0F);
            worldIn.addEntity(enderpearlentity);
        }

        playerIn.addStat(Stats.ITEM_USED.get(this));

        if (!playerIn.abilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        return ActionResult.func_233538_a_(itemstack, worldIn.isRemote());
    }
}
