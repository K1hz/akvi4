package minecraft.game.operation.misc;

import com.google.common.eventbus.Subscribe;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.transactions.EventTick;

@Defuse(
        name = "No Delay",
        description = "Уберает задержку на что-либо",
        brand = Category.Movement
)
public class NoDelay extends Module {
    final ModeListSetting target = new ModeListSetting("Убрать задержку на",
            new CheckBoxSetting("Установка блоков", false),
            new CheckBoxSetting("Ломание", false),
            new CheckBoxSetting("Прыжок", true)
    );

    public NoDelay() {
        addSettings(target);
    }

    @Subscribe
    public void onEvent(EventTick e) {
        if (mc == null || mc.player == null) {
            return;
        }
        Minecraft var10000 = mc;
        Item heldItem = Minecraft.player.getHeldItemMainhand().getItem();
        if (target.is("Установка блоков").getValue()) {
            if (heldItem instanceof BlockItem || heldItem == Items.PLAYER_HEAD) {
                mc.rightClickDelayTimer = 0;
            }
        }
        else {

        }
        if (target.is("Ломание").getValue()) {
            mc.playerController.blockHitDelay = 0;
        }
        if (target.is("Прыжок").getValue()) {
            mc.player.jumpTicks = 0;
        }
    }
}