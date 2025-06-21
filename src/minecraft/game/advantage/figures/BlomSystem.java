package minecraft.game.advantage.figures;

import com.google.common.collect.Queues;
import com.mojang.blaze3d.systems.IRenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import minecraft.game.advantage.Base;
import minecraft.game.advantage.make.shader.ShaderModules;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.mojang.blaze3d.systems.RenderSystem.glUniform1;

public class BlomSystem implements Base {

    private static final ShaderModules bloom = new ShaderModules("bloom");
    private static final Framebuffer inFrameBuffer = new Framebuffer(1, 1, true, false);
    private static final Framebuffer outFrameBuffer = new Framebuffer(1, 1, true, false);
    private static final ConcurrentLinkedQueue<IRenderCall> renderQueue = Queues.newConcurrentLinkedQueue();

    public static void registerRenderCall(IRenderCall rc) {
        renderQueue.add(rc);
    }

    public static void draw(int radius, float exp, boolean fill) {
        if (renderQueue.isEmpty())
            return;

        setupBuffer(inFrameBuffer);
        setupBuffer(outFrameBuffer);
        inFrameBuffer.bindFramebuffer(true);

        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute();
        }

        inFrameBuffer.unbindFramebuffer();
        outFrameBuffer.bindFramebuffer(true);
        bloom.attach();
        bloom.setUniformf("radius", radius);
        bloom.setUniformf("exposure", exp);
        bloom.setUniform("textureIn", 0);
        bloom.setUniform("textureToCheck", 20);
        bloom.setUniform("avoidTexture", fill ? 1 : 0);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2f));
        }

        weightBuffer.rewind();
        glUniform1(bloom.getUniform("weights"), weightBuffer);
        bloom.setUniformf("texelSize", 1.0F / (float) mc.getMainWindow().getWidth(), 1.0F / (float) mc.getMainWindow().getHeight());
        bloom.setUniformf("direction", 1.0F, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL30.GL_ONE, GL30.GL_SRC_ALPHA);
        GL30.glAlphaFunc(GL30.GL_GREATER, 0.0001f);
        inFrameBuffer.bindFramebufferTexture();
        ShaderModules.drawQuads();
        mc.getFramebuffer().bindFramebuffer(false);
        RenderSystem.blendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
        bloom.setUniformf("direction", 0.0F, 1.0F);
        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        inFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        ShaderModules.drawQuads();
        bloom.detach();
        outFrameBuffer.unbindFramebuffer();
        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
        mc.getFramebuffer().bindFramebuffer(false);
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double PI = 3.141592653;
        double output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public static Framebuffer setupBuffer(Framebuffer frameBuffer) {
        if (frameBuffer.framebufferWidth != mc.getMainWindow().getWidth() || frameBuffer.framebufferHeight != mc.getMainWindow().getHeight()) {
            frameBuffer.resize(Math.max(1, mc.getMainWindow().getWidth()), Math.max(1, mc.getMainWindow().getHeight()), false);
        } else {
            frameBuffer.framebufferClear(false);
        }

        frameBuffer.setFramebufferColor(0.0f, 0.0f, 0.0f, 0.0f);
        return frameBuffer;
    }
}
