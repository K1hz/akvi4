package minecraft.game.advantage;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

public interface Base {
    Tessellator tessellator = Tessellator.getInstance();
    Minecraft mc = Minecraft.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    MainWindow sr = mc.getMainWindow();
}
