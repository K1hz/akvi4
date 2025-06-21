package minecraft.game.operation.misc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;

@Defuse(name = "No Interact",description = "Не дает открывать и использовать блоки", brand = Category.Player)
public class NoInteract extends Module {

    public CheckBoxSetting allBlocks = new CheckBoxSetting("Все блоки", false);
    public ModeListSetting ignoreInteract = new ModeListSetting("Обьекты",
            new CheckBoxSetting("Стойки", true),
            new CheckBoxSetting("Сундуки", true),
            new CheckBoxSetting("Двери", true),
            new CheckBoxSetting("Кнопки", true),
            new CheckBoxSetting("Воронки", true),
            new CheckBoxSetting("Раздатчики", true),
            new CheckBoxSetting("Нотные блоки", true),
            new CheckBoxSetting("Верстаки", true),
            new CheckBoxSetting("Люки", true),
            new CheckBoxSetting("Печки", true),
            new CheckBoxSetting("Калитки", true),
            new CheckBoxSetting("Наковальни", true),
            new CheckBoxSetting("Рычаги", true)).visibleIf(() -> !allBlocks.getValue());

    public NoInteract() {
        addSettings(ignoreInteract, allBlocks);
    }
    public Set<Integer> getBlocks() {
        Set<Integer> blocks = new HashSet<>();
        addBlocksForInteractionType(blocks, 1, 147, 329, 270);
        addBlocksForInteractionType(blocks, 2, 173, 161, 485, 486, 487, 488, 489, 720, 721);
        addBlocksForInteractionType(blocks, 3, 183, 308, 309, 310, 311, 312, 313, 718, 719, 758);
        addBlocksForInteractionType(blocks, 4, 336);
        addBlocksForInteractionType(blocks, 5, 70, 342, 508);
        addBlocksForInteractionType(blocks, 6, 74);
        addBlocksForInteractionType(blocks, 7, 151);
        addBlocksForInteractionType(blocks, 8, 222, 223, 224, 225, 226, 227, 712, 713, 379);
        addBlocksForInteractionType(blocks, 9, 154, 670);
        addBlocksForInteractionType(blocks, 10, 250, 475, 476, 477, 478, 479, 714, 715);
        addBlocksForInteractionType(blocks, 11, 328, 327, 326);
        addBlocksForInteractionType(blocks, 12, 171);
        return blocks;
    }

    private void addBlocksForInteractionType(Set<Integer> blocks, int interactionType, Integer... blockIds) {
        if (ignoreInteract.get(interactionType).getValue()) {
            blocks.addAll(Arrays.asList(blockIds));
        }
    }
}
