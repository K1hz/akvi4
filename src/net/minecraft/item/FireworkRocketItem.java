package net.minecraft.item;

import com.google.common.collect.Lists;

import minecraft.game.advantage.figures.TimeCounterSetting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class FireworkRocketItem extends Item {
    public FireworkRocketItem(Properties builder) {
        super(builder);
    }


    /**
     * Called when this item is used when targetting a Block
     */
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();

        if (!world.isRemote) {
            ItemStack itemstack = context.getItem();
            Vector3d vector3d = context.getHitVec();
            Direction direction = context.getFace();
            FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(world, context.getPlayer(), vector3d.x + (double) direction.getXOffset() * 0.15D, vector3d.y + (double) direction.getYOffset() * 0.15D, vector3d.z + (double) direction.getZOffset() * 0.15D, itemstack);
            world.addEntity(fireworkrocketentity);
            itemstack.shrink(1);
        }

        return ActionResultType.func_233537_a_(world.isRemote);
    }

    public static long delay;
    public TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();

    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (playerIn.isElytraFlying()) {
            ItemStack itemstack = playerIn.getHeldItem(handIn);

            if (!worldIn.isRemote) {
                worldIn.addEntity(new FireworkRocketEntity(worldIn, itemstack, playerIn));
                if (!playerIn.isCreative()) {
                    itemstack.shrink(1);
                }
            }
            return ActionResult.func_233538_a_(playerIn.getHeldItem(handIn), worldIn.isRemote());

        } else {
            return ActionResult.resultPass(playerIn.getHeldItem(handIn));
        }
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        CompoundNBT compoundnbt = stack.getChildTag("Fireworks");

        if (compoundnbt != null) {
            if (compoundnbt.contains("Flight", 99)) {
                tooltip.add((new TranslationTextComponent("item.minecraft.firework_rocket.flight")).appendString(" ").appendString(String.valueOf((int) compoundnbt.getByte("Flight"))).mergeStyle(TextFormatting.GRAY));
            }

            ListNBT listnbt = compoundnbt.getList("Explosions", 10);

            if (!listnbt.isEmpty()) {
                for (int i = 0; i < listnbt.size(); ++i) {
                    CompoundNBT compoundnbt1 = listnbt.getCompound(i);
                    List<ITextComponent> list = Lists.newArrayList();
                    FireworkStarItem.func_195967_a(compoundnbt1, list);

                    if (!list.isEmpty()) {
                        for (int j = 1; j < list.size(); ++j) {
                            list.set(j, (new StringTextComponent("  ")).append(list.get(j)).mergeStyle(TextFormatting.GRAY));
                        }

                        tooltip.addAll(list);
                    }
                }
            }
        }
    }

    public static enum Shape {
        SMALL_BALL(0, "small_ball"),
        LARGE_BALL(1, "large_ball"),
        STAR(2, "star"),
        CREEPER(3, "creeper"),
        BURST(4, "burst");

        private static final Shape[] VALUES = Arrays.stream(values()).sorted(Comparator.comparingInt((p_199796_0_) -> {
            return p_199796_0_.index;
        })).toArray((p_199797_0_) -> {
            return new Shape[p_199797_0_];
        });
        private final int index;
        private final String shapeName;

        private Shape(int indexIn, String nameIn) {
            this.index = indexIn;
            this.shapeName = nameIn;
        }

        public int getIndex() {
            return this.index;
        }

        public String getShapeName() {
            return this.shapeName;
        }

        public static Shape get(int indexIn) {
            return indexIn >= 0 && indexIn < VALUES.length ? VALUES[indexIn] : SMALL_BALL;
        }
    }
}
