package minecraft.system.styles;


import java.awt.*;

public interface StyleFactory {
    Style createStyle(String name, Color firstColor, Color secondColor);
}
