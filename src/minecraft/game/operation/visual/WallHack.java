package minecraft.game.operation.visual;

import minecraft.system.AG;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;

@Defuse(name = "Wall Hack",description = "Добавляет возможность видеть игроков через блоки", brand = Category.Visual)
public class WallHack extends Module {

    @Override
    public void onEnable() {
        super.onEnable();
    }

}
