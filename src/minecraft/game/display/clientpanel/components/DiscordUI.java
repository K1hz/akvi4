package minecraft.game.display.clientpanel.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscordUI {
    private int x, y, width, height, radius;
    public DiscordUI(int x, int y, int wight, int height, int radius){
        this.x = x;
        this.y = y;
        this.width= wight;
        this.height = height;
        this.radius = radius;
    }


}
