package minecraft.game.interfaces;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public interface IRenderAccess extends IAccess {
    Tessellator TESSELLATOR = Tessellator.getInstance();
    BufferBuilder BUFFER = TESSELLATOR.getBuffer();
    Executor THREAD_POOL = Executors.newFixedThreadPool(1);

    Profiler render2dProfiler = new Profiler();
    Profiler dragProfiler = new Profiler();
}
