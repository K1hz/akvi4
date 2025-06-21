package minecraft.game.advantage.make.other;

import minecraft.game.advantage.make.shader.ShaderModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;

public class KawaseBlur {

    public static KawaseBlur blur = new KawaseBlur();

    public final CustomFramebuffer BLURRED;
    public final CustomFramebuffer ADDITIONAL;
    CustomFramebuffer blurTarget = new CustomFramebuffer(false).setLinear();

    public KawaseBlur() {
        BLURRED = new CustomFramebuffer(false).setLinear();
        ADDITIONAL = new CustomFramebuffer(false).setLinear();
       
    }
    public void render(Runnable run) {

        Stencil.initStencilToWrite();
        run.run();
        Stencil.readStencilBuffer(1);
         BLURRED.draw();
        Stencil.uninitStencilBuffer();
    }
    public void updateBlur(float offset, int steps) {

        Minecraft mc = Minecraft.getInstance();
        Framebuffer mcFramebuffer = mc.getFramebuffer();
        ADDITIONAL.setup();
        mcFramebuffer.bindFramebufferTexture();
        ShaderModules.kawaseDown.attach();
        ShaderModules.kawaseDown.setUniform("offset", offset);
        ShaderModules.kawaseDown.setUniformf("resolution", 1f / mc.getMainWindow().getWidth(),
                1f / mc.getMainWindow().getHeight());
        CustomFramebuffer.drawTexture();
        CustomFramebuffer[] buffers = {this.ADDITIONAL, this.BLURRED };
        for (int i = 1; i < steps; ++i) {
            int step = i % 2;
            buffers[step].setup();
            buffers[(step + 1) % 2].draw();
        }
        ShaderModules.kawaseUp.attach();
        ShaderModules.kawaseUp.setUniform("offset", offset);
        ShaderModules.kawaseUp.setUniformf("resolution", 1f / mc.getMainWindow().getWidth(),
                1f / mc.getMainWindow().getHeight());
        for (int i = 0; i < steps; ++i) {
            int step = i % 2;
            buffers[(step + 1) % 2].setup();
            buffers[step].draw();
        }
        ShaderModules.kawaseUp.detach();
        mcFramebuffer.bindFramebuffer(false);
    }

}
