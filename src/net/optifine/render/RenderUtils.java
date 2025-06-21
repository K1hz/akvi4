package net.optifine.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import com.mojang.blaze3d.systems.RenderSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;

public class RenderUtils
{
    private static boolean flushRenderBuffers = true;
    private static Minecraft mc = Minecraft.getInstance();

    public static boolean setFlushRenderBuffers(boolean flushRenderBuffers)
    {
        boolean flag = RenderUtils.flushRenderBuffers;
        RenderUtils.flushRenderBuffers = flushRenderBuffers;
        return flag;
    }
    public Vector3d cameraPos() {
        return mc.gameRenderer.getActiveRenderInfo().getProjectedView();
    }
    public void setupWorldRenderer() {
        RenderSystem.disableLighting();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableTexture();
        setupBlend();
    }

    public void cleanupWorldRenderer() {
        cleanupBlend();
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();
    }

    private void enableBlendAndSmoothLines(float lineWidth) {
        setupBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(lineWidth);
    }

    private void disableBlendAndSmoothLines() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1F);
        cleanupBlend();
    }

    private static void setupBlend() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void cleanupBlend() {
        RenderSystem.disableBlend();
    }
    public void drawLine(MatrixStack matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color, float lineWidth, boolean depth, boolean cull) {
        float dx1 = (float) (x1 - cameraPos().x);
        float dy1 = (float) (y1 - cameraPos().y);
        float dz1 = (float) (z1 - cameraPos().z);

        float dx2 = (float) (x2 - cameraPos().x);
        float dy2 = (float) (y2 - cameraPos().y);
        float dz2 = (float) (z2 - cameraPos().z);

        matrix.push();
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        setupWorldRenderer();
        enableBlendAndSmoothLines(lineWidth);
        if (!depth) RenderSystem.disableDepthTest();
        if (!cull) RenderSystem.disableCull();
        BufferBuilder bufferBuilder = new BufferBuilder(1);
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(matrix4f, dx1, dy1, dz1).color(color).endVertex();
        bufferBuilder.pos(matrix4f, dx2, dy2, dz2).color(color).endVertex();
        Tessellator tessellator = new Tessellator();
        tessellator.draw();
        if (!cull) RenderSystem.enableCull();
        if (!depth) RenderSystem.enableDepthTest();
        disableBlendAndSmoothLines();
        cleanupWorldRenderer();
        matrix.pop();
    }
    public void drawLineQuad(MatrixStack matrix, float x1, float y1, float z1, float x2, float y2, float z2, int color, float lineWidth, boolean depth, boolean cull) {
        drawLine(matrix, x1, y1, z1, x2, y2, z1, color, lineWidth, depth, cull);
        drawLine(matrix, x2, y1, z1, x2, y2, z2, color, lineWidth, depth, cull);
        drawLine(matrix, x2, y1, z2, x1, y2, z2, color, lineWidth, depth, cull);
        drawLine(matrix, x1, y1, z2, x1, y2, z1, color, lineWidth, depth, cull);
    }
    public static boolean isFlushRenderBuffers()
    {
        return flushRenderBuffers;
    }
    public void drawBoundingBox(MatrixStack matrix, AxisAlignedBB bb, int color, float lineWidth, boolean depth, boolean cull) {
        if (!depth) RenderSystem.disableDepthTest();
        if (!cull) RenderSystem.disableCull();
        drawLineQuad(matrix, (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ, color, lineWidth, true, true);
        drawLineQuad(matrix, (float) bb.minX, (float) bb.maxY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ, color, lineWidth, true, true);

        drawLine(matrix, (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.minX, (float) bb.maxY, (float) bb.minZ, color, lineWidth, true, true);
        drawLine(matrix, (float) bb.maxX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.minZ, color, lineWidth, true, true);
        drawLine(matrix, (float) bb.maxX, (float) bb.minY, (float) bb.maxZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ, color, lineWidth, true, true);
        drawLine(matrix, (float) bb.minX, (float) bb.minY, (float) bb.maxZ, (float) bb.minX, (float) bb.maxY, (float) bb.maxZ, color, lineWidth, true, true);
        if (!cull) RenderSystem.enableCull();
        if (!depth) RenderSystem.enableDepthTest();
    }
    public static void flushRenderBuffers()
    {
        if (flushRenderBuffers)
        {
            RenderTypeBuffers rendertypebuffers = mc.getRenderTypeBuffers();
            rendertypebuffers.getBufferSource().flushRenderBuffers();
            rendertypebuffers.getCrumblingBufferSource().flushRenderBuffers();
        }
    }

    public static void drawBlockBox(BlockPos blockPos, int color) {
        drawBox(new AxisAlignedBB(blockPos).offset(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z), color);
    }

    public static void drawBox(AxisAlignedBB bb, int color) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL_DEPTH_TEST);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glLineWidth(1);
        float[] rgb = ColoringSystem.rgba(color);
        GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
        vertexbuffer.pos(bb.minX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.minZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.maxX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.minY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        vertexbuffer.pos(bb.minX, bb.maxY, bb.maxZ).color(rgb[0], rgb[1], rgb[2], rgb[3]).endVertex();
        tessellator.draw();
        GlStateManager.color4f(rgb[0], rgb[1], rgb[2], rgb[3]);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL_DEPTH_TEST);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glPopMatrix();

    }
}
