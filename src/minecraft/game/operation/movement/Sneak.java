package minecraft.game.operation.movement;

import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.figures.StopWatch;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.ModuleManager;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.system.AG;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.MovementInput;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Sneak",description = "123", brand = Category.Movement)
public class Sneak extends Module {
    private final CheckBoxSetting onlySneakPress = new CheckBoxSetting( "Только на шифте", true);


    private final StopWatch time = new StopWatch();


    @Subscribe
    public void onDisable() {
        if (!mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SNEAKING));
        }
    }

    @Subscribe
    public void onMove(MovementSystem.MoveEvent e) {
        if (!onlySneakPress.getValue() || mc.gameSettings.keyBindSneak.isKeyDown()) {

                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SNEAKING));
            } else {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SNEAKING));
            }
            mc.player.movementInput.sneaking = true;
        }
    }

