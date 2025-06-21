package minecraft.game.advantage.make.engine2d;

import java.util.List;
import java.util.ArrayList;

import lombok.RequiredArgsConstructor;
import minecraft.system.AG;
import minecraft.system.managers.Theme;
import minecraft.game.operation.misc.DDBETTER;
import minecraft.game.advantage.make.color.ColoringSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import minecraft.game.advantage.advisee.IMinecraft;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Vector2f;

import static minecraft.game.advantage.words.font.Wrapper.TESSELLATOR;
import static minecraft.game.interfaces.IRenderAccess.BUFFER;
import static org.lwjgl.opengl.GL11.*;

public class RectanglesSystem implements IMinecraft {

    @Getter
	public static RectanglesSystem instance = new RectanglesSystem();
	
	public static final List<Vec2fColored> VERTEXES_COLORED = new ArrayList<>();
	public final List<Vector2f> VERTEXES = new ArrayList<>();
    int[] LEFT_UP = new int[]{-90, 0};
    int[] RIGHT_UP = new int[]{0, 90};
    int[] RIGHT_DOWN = new int[]{90, 180};
    int[] LEFT_DOWN = new int[]{180, 270};
    @RequiredArgsConstructor
    public enum HeadSide {
        FRONT(8, 8, 16, 16),
        BACK(24, 8, 32, 16),
        RIGHT(0, 8, 8, 16),
        LEFT(16, 8, 24, 16),
        TOP(8, 0, 16, 8),
        BOTTOM(16, 0, 24, 8),
        ////////////////////////////////
        O_FRONT(40, 8, 48, 16),
        O_BACK(56, 8, 64, 16),
        O_RIGHT(32, 8, 40, 16),
        O_LEFT(48, 8, 56, 16),
        O_TOP(40, 0, 48, 8),
        O_BOTTOM(48, 0, 56, 8),
        ;

        private final int x1, y1, x2, y2;
        private final int size = 64;
    }
    public static void bindTexture(ResourceLocation location) {
        mc.getTextureManager().bindTexture(location);
    }

    public Vec2fColored getOfVec3f(Vector2f vec2f, int color) {
        return new Vec2fColored(vec2f.x, vec2f.y, color);
    }

    public void drawRect(MatrixStack matrix, float x, float y, float x2, float y2, int color, boolean bloom) {
        drawRect(matrix, x, y, x2, y2, color, color, color, color, bloom, false);
    }
    private static float[] calculateUV(int x1, int y1, int x2, int y2, int texWidth, int texHeight) {
        float u1 = (float) x1 / texWidth;
        float v1 = (float) y1 / texHeight;
        float u2 = (float) x2 / texWidth;
        float v2 = (float) y2 / texHeight;

        return new float[]{u1, v1, u2, v2};
    }
    public static void drawHeadSide(MatrixStack matrixStack, ResourceLocation texture, double x, double y, double z, double width, double height, HeadSide head, boolean depth) {
        matrixStack.push();
        mc.getTextureManager().bindTexture(texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.shadeModel(7425);
        RenderSystem.enableAlphaTest();
        if (depth)
            RenderSystem.depthMask(true);
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);

        float[] uv = calculateUV(head.x1, head.y1, head.x2, head.y2, head.size, head.size);

        Matrix4f matrix = matrixStack.getLast().getMatrix();

        BUFFER.begin(GlStateManager.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        BUFFER.pos(matrix, (float) x, (float) (y + height), (float) z).tex(uv[0], uv[1]).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        BUFFER.pos(matrix, (float) (x + width), (float) (y + height), (float) z).tex(uv[2], uv[1]).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        BUFFER.pos(matrix, (float) (x + width), (float) y, (float) z).tex(uv[2], uv[3]).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        BUFFER.pos(matrix, (float) x, (float) y, (float) z).tex(uv[0], uv[3]).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        TESSELLATOR.draw();

        if (depth)
            RenderSystem.depthMask(false);
        RenderSystem.shadeModel(7424);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.clearCurrentColor();

        matrixStack.pop();
    }
    public static void drawSmoothRect(MatrixStack matrixStack, double left, double top, double right, double bottom, int color) {
        drawRect(left, top, right, bottom, color, matrixStack);

        glScalef(0.5f, 0.5f, 0.5f);
        drawRect(left * 2.0f - 1.0f, top * 2.0f, left * 2.0f, bottom * 2.0f - 1.0f, color, matrixStack);
        drawRect(left * 2.0f, top * 2.0f - 1.0f, right * 2.0f, top * 2.0f, color, matrixStack);
        drawRect(right * 2.0f, top * 2.0f, right * 2.0f + 1.0f, bottom * 2.0f - 1.0f, color, matrixStack);
        glScalef(2.0f, 2.0f, 2.0f);
    }

    public static void drawRect(double left, double top, double right, double bottom, int color, MatrixStack matrixStack) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }

        double finalLeft = left;
        double finalTop = top;
        double finalRight = right;
        double finalBottom = bottom;
        start2Draw(() -> {
            setColor(color);
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION);
            bufferBuilder.pos(matrixStack, finalLeft, finalBottom).endVertex();
            bufferBuilder.pos(matrixStack, finalRight, finalBottom).endVertex();
            bufferBuilder.pos(matrixStack, finalRight, finalTop).endVertex();
            bufferBuilder.pos(matrixStack, finalLeft, finalTop).endVertex();
            bufferBuilder.finishDrawing();
            WorldVertexBufferUploader.draw(bufferBuilder);
        });
    }

    public static void start2Draw(Runnable runnable) {
        boolean isEnabled = glIsEnabled(GL_BLEND);
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_ALPHA_TEST);

        runnable.run();

        if (!isEnabled)
            glDisable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_ALPHA_TEST);
    }

    public static void setColor(int color) {
        glColor4ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF), (byte) (color >> 24 & 0xFF));
    }

    public void drawRect(MatrixStack matrix, float x, float y, float x2, float y2, int color) {
        drawRect(matrix, x, y, x2, y2, color, false);
    }

    @Getter
    @AllArgsConstructor
    public static class Vec2fColored {
        float x, y;
        int color;
    }

    @Getter
    @AllArgsConstructor
    public class Vec2fMatrix {
        MatrixStack matrix;
        float x, y;
        int color;
    }

    @Getter
    @AllArgsConstructor
    public class Vec3fColored {
        MatrixStack matrix;
        float x, y, z;
        int color;
    }

    public static void setupOrientationMatrix(MatrixStack matrix, float x, float y, float z) {
        setupOrientationMatrix(matrix, (double) x, y, z);
    }

    public static void setupOrientationMatrix(MatrixStack matrix, double x, double y, double z) {
        float partialTicks = mc.getRenderPartialTicks();
        EntityRendererManager rendererManager = mc.getRenderManager();
        final Vector3d renderPos = rendererManager.info.getProjectedView();
        boolean flag = mc.gameSettings.getPointOfView().func_243192_a() || mc.gameSettings.getPointOfView().func_243194_c().func_243193_b();
        matrix.translate(x - renderPos.x, y - renderPos.y, z - renderPos.z);
    }

    public static void drawRect(MatrixStack matrix, float x, float y, float x2, float y2, int color1, int color2, int color3, int color4, boolean bloom, boolean texture) {
        VERTEXES_COLORED.clear();

        // Добавление вершин с цветами
        VERTEXES_COLORED.add(new Vec2fColored(x, y, color1));
        VERTEXES_COLORED.add(new Vec2fColored(x2, y, color2));
        VERTEXES_COLORED.add(new Vec2fColored(x2, y2, color3));
        VERTEXES_COLORED.add(new Vec2fColored(x, y2, color4));

        // Включаем режим для свечения (если bloom == true)
        if (bloom) {
            // Могут быть настройки для свечения, например, более яркие цвета
            // Для свечения можно использовать специальный эффект или усилить яркость (например, сделать цвет более ярким)
            color1 = ColoringSystem.setAlpha(color1, 255); // Сделать более ярким
            color2 = ColoringSystem.setAlpha(color2, 255);
            color3 = ColoringSystem.setAlpha(color3, 255);
            color4 = ColoringSystem.setAlpha(color4, 255);
        }


        // Отправляем вершины на рендеринг
        drawVertexesList(matrix, VERTEXES_COLORED, GL11.GL_POLYGON, texture, bloom);
    }

    
    public void drawRoundedRectShadowed(MatrixStack matrix, float x, float y, float x2, float y2, float round, float shadowSize, int color1, int color2, int color3, int color4, boolean bloom, boolean sageColor, boolean rect, boolean shadow) {
        float roundMax = Math.max(x2 - x, y2 - y);
        round = Math.max(Math.min(round, roundMax), 0);
        shadowSize = Math.max(shadowSize, 0);

        x += round;
        y += round;
        x2 -= round;
        y2 -= round;
        if (rect) {
            drawRect(matrix, x, y, x2, y2, color1, color2, color3, color4, bloom, false);
            if (round != 0) {
                drawLimitersSegments(matrix, x, y, x2, y2, round, 0, color1, color2, color3, color4, false, false, bloom);
                drawRoundSegments(matrix, x, y, x2, y2, round, color1, color2, color3, color4, bloom);
            }
        }
        DDBETTER DDBETTER = AG.getInst().getModuleManager().getDDBETTER();
        if (shadow && shadowSize > 0 && !DDBETTER.fpsBoot.getValue()) {
            drawLimitersSegments(matrix, x - round, y - round, x2 + round, y2 + round, shadowSize, round, color1, color2, color3, color4, sageColor, true, bloom);
            drawShadowSegmentsExtract(matrix, x, y, x2, y2, round, shadowSize, color1, color2, color3, color4, sageColor, bloom);
        }

    }
    public void drawRoundedRectShadowed1(MatrixStack matrix, float x, float y, float x2, float y2, float round, float shadowSize, int color1, int color2, int color3, int color4, boolean bloom, boolean sageColor, boolean rect, boolean shadow) {
        float roundMax = Math.max(x2 - x, y2 - y);
        round = Math.max(Math.min(round, roundMax), 0);
        shadowSize = Math.max(shadowSize, 0);

        x += round;
        y += round;
        x2 -= round;
        y2 -= round;
        if (rect) {
            drawRect(matrix, x, y, x2, y2, color1, color2, color3, color4, bloom, false);
            if (round != 0) {
                drawLimitersSegments(matrix, x, y, x2, y2, round, 0, color1, color2, color3, color4, false, false, bloom);
                drawRoundSegments(matrix, x, y, x2, y2, round, color1, color2, color3, color4, bloom);
            }
        }
        DDBETTER DDBETTER = AG.getInst().getModuleManager().getDDBETTER();
        if (shadow && shadowSize > 0 && !DDBETTER.gavno.getValue()) {
            drawLimitersSegments(matrix, x - round, y - round, x2 + round, y2 + round, shadowSize + 1, round, ColoringSystem.rgba(30,30,30,155), ColoringSystem.rgba(30,30,30,155), ColoringSystem.rgba(30,30,30,155), ColoringSystem.rgba(30,30,30,155), sageColor, true, bloom);
            drawShadowSegmentsExtract(matrix, x, y, x2, y2, round, shadowSize + 1, ColoringSystem.rgba(30,30,30,155), ColoringSystem.rgba(30,30,30,155), ColoringSystem.rgba(30,30,30,155), ColoringSystem.rgba(30,30,30,155), sageColor, bloom);
        }

    }
    
    public void drawShadowSegment(MatrixStack matrix, float x, float y, double radius, int color, boolean sageColor, int[] side, boolean bloom) {
        int color2 = sageColor ? 0 : ColoringSystem.reAlphaInt(color, 0);
        drawDuadsSegment(matrix, x, y, 0, radius, color, color2, bloom, side);
    }

    public void drawShadowSegment(MatrixStack matrix, float x, float y, double radiusRound, double radiusShadow, int color, boolean sageColor, int[] side, boolean bloom) {
        int color2 = sageColor ? 0 : ColoringSystem.reAlphaInt(color, 0);
        drawDuadsSegment(matrix, x, y, radiusRound, radiusShadow, color, color2, bloom, side);
    }
    
    public void drawShadowSegment(MatrixStack matrix, float x, float y, double radius, int color, boolean sageColor, int[] side) {
        drawShadowSegment(matrix, x, y, radius, color, sageColor, side, false);
    }

    public void drawShadowSegments(MatrixStack matrix, float x, float y, float x2, float y2, double radius, int color1, int color2, int color3, int color4, boolean sageColor, boolean bloom) {
        drawShadowSegment(matrix, x, y, radius, color1, sageColor, LEFT_UP, bloom);
        drawShadowSegment(matrix, x2, y, radius, color2, sageColor, RIGHT_UP, bloom);
        drawShadowSegment(matrix, x2, y2, radius, color3, sageColor, RIGHT_DOWN, bloom);
        drawShadowSegment(matrix, x, y2, radius, color4, sageColor, LEFT_DOWN, bloom);
    }


    public void drawShadowSegmentsExtract(MatrixStack matrix, float x, float y, float x2, float y2, double radiusStart, double radiusEnd, int color1, int color2, int color3, int color4, boolean sageColor, boolean bloom) {
        drawShadowSegment(matrix, x, y, radiusStart, radiusEnd, color1, sageColor, LEFT_UP, bloom);
        drawShadowSegment(matrix, x2, y, radiusStart, radiusEnd, color2, sageColor, RIGHT_UP, bloom);
        drawShadowSegment(matrix, x2, y2, radiusStart, radiusEnd, color3, sageColor, RIGHT_DOWN, bloom);
        drawShadowSegment(matrix, x, y2, radiusStart, radiusEnd, color4, sageColor, LEFT_DOWN, bloom);
    }

    public void drawShadowSegments(MatrixStack matrix, float x, float y, float x2, float y2, double radius, int color1, int color2, int color3, int color4, boolean sageColor) {
        drawShadowSegments(matrix, x, y, x2, y2, radius, color1, color2, color3, color4, sageColor, false);
    }

    public void drawShadowSegmentsExtract(MatrixStack matrix, float x, float y, float x2, float y2, double radiusStart, double radiusEnd, int color1, int color2, int color3, int color4, boolean sageColor) {
        drawShadowSegmentsExtract(matrix, x, y, x2, y2, radiusStart, radiusEnd, color1, color2, color3, color4, sageColor, false);
    }
    
    public void drawLimitersSegments(MatrixStack matrix, float x, float y, float x2, float y2, float radius, float appendOffsets, int color1, int color2, int color3, int color4, boolean sageColor, boolean retainZero, boolean bloom) {
        int c5 = retainZero ? sageColor ? 0 : ColoringSystem.reAlphaInt(color1, 0) : color1;
        int c6 = retainZero ? sageColor ? 0 : ColoringSystem.reAlphaInt(color2, 0) : color2;
        int c7 = retainZero ? sageColor ? 0 : ColoringSystem.reAlphaInt(color3, 0) : color3;
        int c8 = retainZero ? sageColor ? 0 : ColoringSystem.reAlphaInt(color4, 0) : color4;
        //up
        drawRect(matrix, x + appendOffsets, y - radius, x2 - appendOffsets, y, c5, c6, color2, color1, bloom, false);
        //down
        drawRect(matrix, x + appendOffsets, y2, x2 - appendOffsets, y2 + radius, color4, color3, c7, c8, bloom, false);
        //left
        drawRect(matrix, x - radius, y + appendOffsets, x, y2 - appendOffsets, c5, color1, color4, c8, bloom, false);
        //right
        drawRect(matrix, x2, y + appendOffsets, x2 + radius, y2 - appendOffsets, color2, c6, c7, color3, bloom, false);
    }
    
    public void drawRoundSegments(MatrixStack matrix, float x, float y, float x2, float y2, double radius, int color1, int color2, int color3, int color4, boolean bloom) {
        drawRoundSegment(matrix, x, y, radius, color1, LEFT_UP, bloom);
        drawRoundSegment(matrix, x2, y, radius, color2, RIGHT_UP, bloom);
        drawRoundSegment(matrix, x2, y2, radius, color3, RIGHT_DOWN, bloom);
        drawRoundSegment(matrix, x, y2, radius, color4, LEFT_DOWN, bloom);
    }
    
    public void drawRoundSegment(MatrixStack matrix, float x, float y, double radius, int color, int[] side, boolean bloom) {
        drawDuadsSegment(matrix, x, y, 0, radius, color, color, bloom, side);
    }

    public void drawRoundSegment(MatrixStack matrix, float x, float y, double radius, int color, int[] side) {
        drawDuadsSegment(matrix, x, y, 0, radius, color, color, false, side);
    }
    
    public void drawDuadsSegment(MatrixStack matrix, float x, float y, double radius, double expand, int color, int color2, boolean bloom, int[] side) {
        VERTEXES_COLORED.clear();
        int index = 0;
        for (Vector2f vec2f : generateRadiusCircledVertexes(matrix, x, y, radius, radius + expand, side[0], side[1], 9, true)) {
            VERTEXES_COLORED.add(getOfVec3f(vec2f, index % 2 == 1 ? color2 : color));
            ++index;
        }
        drawVertexesList(matrix, VERTEXES_COLORED, GL12.GL_TRIANGLE_STRIP, false, bloom);
    }
    
    public List<Vector2f> generateRadiusCircledVertexes(MatrixStack matrix, float x, float y, double radius1, double radius2, double startRadius, double endRadius, double step, boolean doublepart) {
        VERTEXES.clear();
        double radius = startRadius;
        while (radius <= endRadius) {
            if (radius > endRadius) radius = endRadius;
            float x1 = (float) (Math.sin(Math.toRadians(radius)) * radius1);
            float y1 = (float) (-Math.cos(Math.toRadians(radius)) * radius1);
            VERTEXES.add(new Vector2f(x + x1, y + y1));
            if (doublepart) {
                x1 = (float) (Math.sin(Math.toRadians(radius)) * radius2);
                y1 = (float) (-Math.cos(Math.toRadians(radius)) * radius2);
                VERTEXES.add(new Vector2f(x + x1, y + y1));
            }
            radius += step;
        }
        return VERTEXES;
    }
    
    public static void drawVertexesList(MatrixStack matrix, List<Vec2fColored> vec2c, int begin, boolean texture, boolean bloom) {
        setupRenderRect(texture, bloom);
        buffer.begin(begin, texture ? DefaultVertexFormats.POSITION_TEX_COLOR : DefaultVertexFormats.POSITION_COLOR);
        int counter = 0;
        for (final Vec2fColored vec : vec2c) {
            float[] rgba = ColoringSystem.rgba(vec.getColor());
            buffer.pos(matrix.getLast().getMatrix(), vec.getX(), vec.getY(), 0);
            if (texture) buffer.tex(counter == 0 || counter == 3 ? 0 : 1, counter == 0 || counter == 1 ? 0 : 1);
            buffer.color(rgba[0], rgba[1], rgba[2], rgba[3]);
            buffer.endVertex();
            counter++;
        }
        tessellator.draw();
        endRenderRect(bloom);
    }

    public static void setupRenderRect(boolean texture, boolean bloom) {
        if (texture) RenderSystem.enableTexture();
        else RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.shadeModel(7425);
//        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, bloom ? GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA : GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.disableAlphaTest();
        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
    }

    public static void endRenderRect(boolean bloom) {
        RenderSystem.enableAlphaTest();
        if (bloom)
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        RenderSystem.depthMask(true);
        RenderSystem.shadeModel(7424);
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
//        RenderSystem.enableDepthTest();
        RenderSystem.clearCurrentColor();
    }

}
