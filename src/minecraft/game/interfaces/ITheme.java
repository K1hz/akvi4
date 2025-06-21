package minecraft.game.interfaces;


import minecraft.system.AG;
import minecraft.system.styles.Style;

public interface ITheme {
    default Style getTheme() {
        return AG.getInst().getStyleManager().getCurrentStyle();
    }

    default void setTheme(Style theme) {
        AG.getInst().getStyleManager().setCurrentStyle(theme);
    }
}
