package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.figures.TimeCounterSetting;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import org.lwjgl.glfw.GLFW;

@Defuse(name = "Autо Sort",description = "Сортирует инвентарь", brand = Category.Misc)
public class DDSORT extends Module {

    private final TimeCounterSetting second = new TimeCounterSetting();
    private CheckBoxSetting onPressAltKey = new CheckBoxSetting("Только при Alt", false);
    private SliderSetting droperDelay = new SliderSetting("Задержка выбрасывания", 100.0F, 0.0F, 1000.0F, 10);
    private CheckBoxSetting armor = new CheckBoxSetting("Выкидывать броню", false);

    public DDSORT() {
        addSettings(onPressAltKey, droperDelay, armor);
    }

    @Subscribe
    private void onUpdt(EventUpdate event) {
        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS;
        if (!ctrlDown && onPressAltKey.getValue()) {
            return;
        }
        if (mc.currentScreen == null || mc.currentScreen instanceof InventoryScreen) {
            for (int i = 9; i < 45; ++i) {
                ItemStack is = mc.player.inventory.getStackInSlot(i);
                if (is == mc.player.inventory.armorInventory.get(0)) continue;
                if (is == mc.player.inventory.armorInventory.get(1)) continue;
                if (is == mc.player.inventory.armorInventory.get(2)) continue;
                if (is == mc.player.inventory.armorInventory.get(3) || !this.shouldDrop(is, i)) continue;
                if (this.second.isReached(droperDelay.getValue().longValue()) && mc.player.getHeldItemMainhand().getItem() != Items.FIREWORK_ROCKET) {
                    mc.playerController.windowClick(0, i, 1, ClickType.THROW, mc.player);
                    this.second.reset();
                }
            }
        }
    }

    public boolean shouldDrop(ItemStack stack, int slot) {
        if (stack.getItem() instanceof BlockItem && !(stack.getItem() instanceof SkullItem) && stack.getItem() != BlockItem.getItemById(7) && stack.getItem() != BlockItem.getItemById(33) && stack.getItem() != BlockItem.getItemById(29) && stack.getItem() != BlockItem.getItemById(46) && stack.getItem() != BlockItem.getItemById(120) && stack.getItem() != BlockItem.getItemById(41) && stack.getItem() != BlockItem.getItemById(89) && stack.getItem() != BlockItem.getItemById(14) && stack.getItem() != BlockItem.getItemById(133) && stack.getItem() != BlockItem.getItemById(129) && stack.getItem() != BlockItem.getItemById(56) && stack.getItem() != BlockItem.getItemById(57) && stack.getItem() != BlockItem.getItemById(165) && stack.getItem() != BlockItem.getItemById(52) && stack.getItem() != BlockItem.getItemById(30) && stack.getItem() != BlockItem.getItemById(49) && stack.getItem() != BlockItem.getItemById(130) && stack.getItem() != BlockItem.getItemById(49) && stack.getItem() != BlockItem.getItemById(219) && stack.getItem() != BlockItem.getItemById(220) && stack.getItem() != BlockItem.getItemById(221) && stack.getItem() != BlockItem.getItemById(222) && stack.getItem() != BlockItem.getItemById(223) && stack.getItem() != BlockItem.getItemById(224) && stack.getItem() != BlockItem.getItemById(225) && stack.getItem() != BlockItem.getItemById(226) && stack.getItem() != BlockItem.getItemById(227) && stack.getItem() != BlockItem.getItemById(228) && stack.getItem() != BlockItem.getItemById(229) && stack.getItem() != BlockItem.getItemById(230) && stack.getItem() != BlockItem.getItemById(231) && stack.getItem() != BlockItem.getItemById(232) && stack.getItem() != BlockItem.getItemById(233) && stack.getItem() != BlockItem.getItemById(234)) {
            return true;
        }
        if ((stack.getItem() == Items.STICK || stack.getItem() == BlockItem.getItemById(324) || stack.getItem() == BlockItem.getItemById(330) || stack.getItem() == BlockItem.getItemById(427) || stack.getItem() == BlockItem.getItemById(428) || stack.getItem() == BlockItem.getItemById(429) || stack.getItem() == BlockItem.getItemById(430) || stack.getItem() == BlockItem.getItemById(431) || stack.getItem() == BlockItem.getItemById(328) || stack.getItem() == BlockItem.getItemById(407) || stack.getItem() == BlockItem.getItemById(342) || stack.getItem() == BlockItem.getItemById(333) || stack.getItem() == BlockItem.getItemById(444) || stack.getItem() == BlockItem.getItemById(445) || stack.getItem() == BlockItem.getItemById(446) || stack.getItem() == BlockItem.getItemById(447) || stack.getItem() == BlockItem.getItemById(448) || stack.getItem() == Items.BEETROOT_SEEDS || stack.getItem() == Items.WHEAT || stack.getItem() == Items.MELON_SEEDS || stack.getItem() == Items.PUMPKIN_SEEDS || stack.getItem() == Items.WHEAT_SEEDS || stack.getItem() instanceof DyeItem || stack.getItem() == Items.CLOCK || stack.getItem() == Items.COMPASS || stack.getItem() == Items.PAPER || stack.getItem() == Items.FISHING_ROD || stack.getItem() == Items.SLIME_BALL || stack.getItem() == Items.CLAY_BALL || stack.getItem() == Items.BONE || stack.getItem() == Items.BOWL || stack.getItem() == Items.CARROT_ON_A_STICK || stack.getItem() == Items.FEATHER || stack.getItem() == Items.GLASS_BOTTLE || stack.getItem() == Items.SADDLE || stack.getItem() instanceof SignItem || stack.getItem() == Items.MAP || stack.getItem() == Items.EGG || stack.getItem() == Items.HOPPER_MINECART || stack.getItem() == Items.FLOWER_POT || stack.getItem() == Items.LEATHER || stack.getItem() == Items.NAME_TAG || stack.getItem() == Items.SHEARS || stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.LEAD)) {
            return true;
        }
        if ((stack.getItem() == Items.ROTTEN_FLESH || stack.getItem() == Items.POTATO || stack.getItem() == Items.CHICKEN || stack.getItem() == Items.BEEF || stack.getItem() == Items.TROPICAL_FISH || stack.getItem() == Items.PUFFERFISH || stack.getItem() == Items.MUTTON || stack.getItem() == Items.PORKCHOP || stack.getItem() == Items.RABBIT || stack.getItem() == Items.COOKIE)) {
            return true;
        }
        if (((armor.getValue() && stack.getItem() instanceof ArmorItem
                || stack.getItem() instanceof BucketItem || stack.getItem() instanceof FlintAndSteelItem || stack.getItem() == Items.COAL || stack.getItem() == Items.GOLD_INGOT || stack.getItem() == Items.IRON_INGOT || stack.getItem() == Items.COBBLESTONE))) {
            return true;
        }
        return ((stack.getItem() == Items.WOODEN_SWORD || stack.getItem() == Items.WOODEN_PICKAXE || stack.getItem() == Items.WOODEN_AXE || stack.getItem() == Items.WOODEN_SHOVEL || stack.getItem() == Items.WOODEN_HOE) || (stack.getItem() == Items.STONE_SWORD || stack.getItem() == Items.STONE_PICKAXE || stack.getItem() == Items.STONE_AXE || stack.getItem() == Items.STONE_SHOVEL || stack.getItem() == Items.STONE_HOE) || (stack.getItem() == Items.IRON_SWORD || stack.getItem() == Items.IRON_PICKAXE || stack.getItem() == Items.IRON_AXE || stack.getItem() == Items.IRON_SHOVEL || stack.getItem() == Items.IRON_HOE) || (stack.getItem() == Items.GOLDEN_SWORD || stack.getItem() == Items.GOLDEN_PICKAXE || stack.getItem() == Items.GOLDEN_AXE || stack.getItem() == Items.GOLDEN_SHOVEL || stack.getItem() == Items.GOLDEN_HOE));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        print(getName() + " к сожалению не очищает хотбар");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
