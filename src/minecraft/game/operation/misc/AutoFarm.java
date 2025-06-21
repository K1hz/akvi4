package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.luvvy.InventoryOrigin;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventTick;
import minecraft.game.transactions.EventUpdate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

@Defuse(name = "Auto Farm", description = "Автоматически выполняет работу фермера (FT)", brand = Category.Misc)
public class AutoFarm extends Module {

    public final SliderSetting cooldownClick = new SliderSetting("Задержка кликов (в тиках)", 3f, 1f, 20f, 0.5f);
    public final CheckBoxSetting boneMeal = new CheckBoxSetting("Использовать муку", true);

    private Vector3d currentBlockPosition = null;

    private final InventoryOrigin.Hand handUtil = new InventoryOrigin.Hand();

    private int ticks = 0;

    public AutoFarm() {
        addSettings(cooldownClick, boneMeal);
    }

    @Subscribe
    public void onEvent(EventUpdate event){
        for (BlockPos pos : BlockPos.getAllInBoxMutable(mc.player.getPosition().add(-3, -3, -3),
                mc.player.getPosition().add(3, 3, 3))) {
            BlockState state = mc.world.getBlockState(pos);

            if(state.getBlockStateId() != Blocks.SWEET_BERRY_BUSH.getDefaultState().getBlockStateId()) continue;

            if(this.currentBlockPosition == null){
                this.currentBlockPosition = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
                mc.ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("set distance"));
            }
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
        if(this.currentBlockPosition == null) return;
        if(this.currentBlockPosition.distanceTo(mc.player.getPositionVec()) > 3){
            mc.ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("reset distance"));
            this.currentBlockPosition = null;
            return;
        }

        this.ticks++;

        if(this.ticks >= this.cooldownClick.getValue()){
            this.ticks = 0;

            Vector2f rotation = MathSystem.rotationToVec(this.currentBlockPosition);
            float yaw = rotation.x;
            float pitch = rotation.y;

            mc.player.rotationYaw = yaw;
            mc.player.rotationPitch = pitch;
            mc.player.rotationYawHead = yaw;
            mc.player.renderYawOffset = yaw;

            BlockPos pos = new BlockPos(this.currentBlockPosition.x, this.currentBlockPosition.y, this.currentBlockPosition.z);
            Vector3d center = new Vector3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);

            ActionResultType actionResult = mc.playerController.processRightClickBlock(
                    mc.player, mc.world, Hand.MAIN_HAND,
                    new BlockRayTraceResult(center, Direction.UP, pos, false));
            if (actionResult == ActionResultType.SUCCESS) {
                mc.player.swingArm(Hand.MAIN_HAND);
            }

            int slot = selectBoneMeal(getItemForName("Костная мука", true), getItemForName("Костная мука", false));
            if (slot > 8) {
                mc.playerController.pickItem(slot);
            }
            this.handUtil.handleItemChange(false);
            this.handUtil.setOriginalSlot(slot);
        }
    }

    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }

            String displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString());
            if (displayName != null && displayName.toLowerCase().contains(name)) {
                return i;
            }
        }
        return -1;
    }

    public int selectBoneMeal(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            return invSlot;
        }
        return -1;
    }

}
