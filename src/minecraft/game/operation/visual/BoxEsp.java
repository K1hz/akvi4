package minecraft.game.operation.visual;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import minecraft.game.advantage.advisee.IMinecraft;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.transactions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.Event;
import org.lwjgl.opengl.GL11;

@Defuse(
        name = "3D Box Esp",
        description = "pizdak",
        brand = Category.Visual
)
public class BoxEsp extends Module {

    private static final double WIDTH = 0.6;
    private static final double HEIGHT = 1.8;

    private static final float[] FILL_COLOR = {1.0f, 1.0f, 1.0f, 0.5f};


    private static final float[] LINE_COLOR = {1.0f, 1.0f, 1.0f, 1.0f};

    public void onEvent(EventRender3D event) {
        if (!(event instanceof EventRender3D)) return;

        Minecraft mc = IMinecraft.mc;
        if (mc.world == null || mc.player == null || mc.getRenderManager() == null) return;

        try {
            if (mc.gameSettings.fov == 0) return;

            renderHitboxes(mc,
                    ((EventRender3D) event).getPartialTicks());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void renderHitboxes(Minecraft mc, float partialTicks) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();

        Vector3d view = mc.getRenderManager().info.getProjectedView();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            renderPlayerHitbox(player, view, partialTicks);
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private void renderPlayerHitbox(PlayerEntity player, Vector3d view, float partialTicks) {
        double x = lerp(player.lastTickPosX, player.getPosX(), partialTicks) - view.x;
        double y = lerp(player.lastTickPosY, player.getPosY(), partialTicks) - view.y;
        double z = lerp(player.lastTickPosZ, player.getPosZ(), partialTicks) - view.z;

        AxisAlignedBB box = new AxisAlignedBB(
                x - WIDTH/2, y, z - WIDTH/2,
                x + WIDTH/2, y + HEIGHT, z + WIDTH/2
        );


        renderBoxFill(box);


        renderBoxOutline(box);
    }

    private void renderBoxFill(AxisAlignedBB box) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);




        addQuad(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);


        addQuad(buffer, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ);

        addQuad(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.minY, box.minZ);

        addQuad(buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);

        addQuad(buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);

        addQuad(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.minY, box.maxZ);

        Tessellator.getInstance().draw();
    }

    private void renderBoxOutline(AxisAlignedBB box) {
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        addLine(buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.minY, box.minZ);

        addLine(buffer, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
        addLine(buffer, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ);
        addLine(buffer, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ);

        addLine(buffer, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
        addLine(buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
        addLine(buffer, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ);

        Tessellator.getInstance().draw();
    }

    private void addQuad(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double x3, double y3, double z3,
                         double x4, double y4, double z4) {
        buffer.pos(x1, y1, z1).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
        buffer.pos(x2, y2, z2).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
        buffer.pos(x3, y3, z3).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
        buffer.pos(x4, y4, z4).color(FILL_COLOR[0], FILL_COLOR[1], FILL_COLOR[2], FILL_COLOR[3]).endVertex();
    }

    private void addLine(BufferBuilder buffer,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2) {
        buffer.pos(x1, y1, z1).color(LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], LINE_COLOR[3]).endVertex();
        buffer.pos(x2, y2, z2).color(LINE_COLOR[0], LINE_COLOR[1], LINE_COLOR[2], LINE_COLOR[3]).endVertex();
    }

    private double lerp(double start, double end, float progress) {
        return start + (end - start) * progress;
    }

    public void onEventModule(Event event) {}

    public void register(LiteralArgumentBuilder<CommandSource> then) {
    }

    public class RenderLevelStageEvent {
    }
}