package minecraft.game.advantage.make;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.advantage.make.other.Stencil;
import minecraft.game.advantage.make.shader.ShaderModules;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class GaussianBlur  {

    private static final ShaderModules gaussianBlur = new ShaderModules("blur");
    private static Framebuffer framebuffer = new Framebuffer(1, 1, false, false);

    private static void setupUniforms(float dir1, float dir2, float radius) {
        gaussianBlur.setUniform("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0F / (float) IMinecraft.mc.getMainWindow().getWidth(), 1.0F / (float) IMinecraft.mc.getMainWindow().getHeight());
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);

        final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        for (int i = 0; i <= radius; i++) {
            weightBuffer.put(calculateGaussianValue(i, radius / 2));
        }

        weightBuffer.rewind();
        RenderSystem.glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static void startBlur(){
        Stencil.initStencilToWrite();
    }
    public static void endBlur(float radius, float compression) {
        Stencil.readStencilBuffer(1);

        framebuffer = ShaderModules.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);

        GlStateManager.bindTexture(IMinecraft.mc.getFramebuffer().framebufferTexture);
        ShaderModules.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        IMinecraft.mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        gaussianBlur.setUniformf("direction", 0, compression);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderModules.drawQuads();
        gaussianBlur.detach();

        Stencil.uninitStencilBuffer();
        GlStateManager.color4f(-1,-1,1,-1);
        GlStateManager.bindTexture(0);
    }

    public static void blur(float radius, float compression) {
        framebuffer = ShaderModules.createFrameBuffer(framebuffer);

        framebuffer.framebufferClear(false);
        framebuffer.bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(compression, 0, radius);

        GlStateManager.bindTexture(IMinecraft.mc.getFramebuffer().framebufferTexture);
        ShaderModules.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.detach();

        IMinecraft.mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.attach();
        setupUniforms(0, compression, radius);

        GlStateManager.bindTexture(framebuffer.framebufferTexture);
        ShaderModules.drawQuads();
        gaussianBlur.detach();

        GlStateManager.color4f(-1,-1,1,-1);
        GlStateManager.bindTexture(0);
    }

    public static float calculateGaussianValue(float x, float sigma) {
        double output = 1.0 / Math.sqrt(2.0 * Math.PI * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }
}
