package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.transactions.EventUpdate;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;

@Defuse(name = "RWFixHp",description = "Ненужно", brand = Category.Visual)
public class ReallyWorldFIXHP extends Module {
    public CheckBoxSetting grief = new CheckBoxSetting("Новый гриф", true);
    public ReallyWorldFIXHP() {
        this.addSettings(this.grief);
    }

    @Getter
    private float RealHp;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        // Обрабатываем каждого игрока на сервере
        for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
            // Получаем объект scoreboard
            Scoreboard scoreboard = player.getWorldScoreboard();
            ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreObjective != null) {
                // Получаем очки для игрока
                Score score = scoreboard.getOrCreateScore(player.getScoreboardName(), scoreObjective);
                String scoreText = Integer.toString(score.getScorePoints()) + " " + scoreObjective.getDisplayName();
                String scoreNumber = scoreText.replaceAll("[^0-9]", ""); // Извлекаем цифры из строки

                try {
                    // Преобразуем строку в число
                    int hps = Integer.parseInt(scoreNumber);

                    // Проверяем, что значение HP не превышает максимальное здоровье игрока
                    if (hps <= player.getMaxHealth()) {
                        RealHp = (float) hps; // Обновляем значение здоровья
                    }
                } catch (NumberFormatException exception) {
                    // Игнорируем ошибку, если не удалось преобразовать строку в число
                }
            }
        }
    }
}