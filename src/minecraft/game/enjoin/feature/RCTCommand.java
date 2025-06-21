package minecraft.game.enjoin.feature;

import minecraft.game.enjoin.interfaces.Command;
import minecraft.game.enjoin.interfaces.Logger;
import minecraft.game.enjoin.interfaces.MultiNamedCommand;
import minecraft.game.enjoin.interfaces.Parameters;
import minecraft.game.advantage.advisee.ClientReceive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RCTCommand implements Command, MultiNamedCommand {

    final Logger logger;
    final Minecraft mc;

    @Override
    public void execute(Parameters parameters) {
        if (ClientReceive.isConnectedToServer("funtime") && !ClientReceive.isPvP()) {
            String anca = "";
            for (ScoreObjective team : mc.world.getScoreboard().getScoreObjectives()) {
                String an = team.getDisplayName().getString();
                if (an.contains("Анархия-")) {
                    anca = an.split("Анархия-")[1];
                    mc.player.sendChatMessage("/hub");
                    break;
                }
            }
            mc.player.sendChatMessage("/an" + anca);
            String finalAnca = anca;
            new Thread(() -> {
                try {
                    Thread.sleep(900);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mc.player.sendChatMessage("/an" + finalAnca);
            }).start();
        } else {
            logger.log(name() + " работает только на FunTime!");
        }
    }

    @Override
    public String name() {
        return "rct";
    }

    @Override
    public String description() {
        return "Перезаходит на анархию";
    }


    @Override
    public List<String> aliases() {
        return Collections.singletonList("reconnect");
    }
}
