package minecraft.game.enjoin.feature;

import minecraft.system.AG;
import minecraft.game.enjoin.interfaces.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.math.NumberUtils;

import minecraft.game.enjoin.api.CommandException;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HClipCommand implements Command, CommandWithAdvice, MultiNamedCommand {
    final Prefix prefix;
    final Logger logger;
    final Minecraft mc;

    @Override
    public void execute(Parameters parameters) throws CommandException {
        String distanceStr = parameters.asString(0)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Необходимо указать расстояние для перемещения."));

        if (!NumberUtils.isCreatable(distanceStr)) {
            logger.log(TextFormatting.RED + "Пожалуйста, введите числовое значение для расстояния.");
            return;
        }

        double distance = Double.parseDouble(distanceStr);
        Vector3d lookVector = mc.player.getLookVec().normalize();

        double totalOffsetX = lookVector.x * distance;
        double totalOffsetZ = lookVector.z * distance;

        int steps = (int) Math.ceil(Math.abs(distance) / 0.5);
        double stepOffsetX = totalOffsetX / steps;
        double stepOffsetZ = totalOffsetZ / steps;
        double currentX = mc.player.getPosX();
        double currentY = mc.player.getPosY();
        double currentZ = mc.player.getPosZ();

        for (int i = 0; i < steps; i++) {
            currentX += stepOffsetX;
            currentZ += stepOffsetZ;
            mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(currentX, currentY, currentZ, mc.player.isOnGround()));
            mc.player.setPosition(currentX, currentY, currentZ);
        }

        String blockUnit = Math.abs(distance) > 1 ? "блоков" : "блок";
        logger.log(TextFormatting.GRAY + String.format("Вы переместились на %.1f %s по горизонтали.", Math.abs(distance), blockUnit));
    }

    @Override
    public String name() {
        return "hclip";
    }

    @Override
    public String description() {
        return "Телепортирует вперёд/назад по горизонтали";
    }


    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(
                TextFormatting.GRAY + commandPrefix + "hclip <distance> - Телепортация на указанное расстояние",
                TextFormatting.UNDERLINE  + "Пример использования:" + TextFormatting.RED + " " + commandPrefix + "hclip 1"
        );
    }

    @Override
    public List<String> aliases() {
        return List.of("hc");
    }
}