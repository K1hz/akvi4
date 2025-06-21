package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import minecraft.system.AG;
import minecraft.game.transactions.EventChangeWorld;
import minecraft.game.transactions.EventPreRender3D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.BlockSystemSettings;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.RectanglesSystem;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.joml.Math;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;
import minecraft.system.managers.Theme;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Defuse(name = "World Particle",description = "Добавляет партиклы в мир", brand = Category.Visual)
public class FireFly extends Module {
    private final List<FireFlyEntity> particles = new ArrayList<>();
    private final ModeSetting fallModeSetting = new ModeSetting("Режим", "Простой", "Простой", "Взлет");
    private final ModeSetting textureMode = new ModeSetting("Текстура", "Орбизы", "Орбизы", "Звезды", "Доллары", "Сердечки", "Молнии", "Снежинки", "Квадрат", "Куб", "Треугольник", "Крест");
    private final ModeSetting snowTextureMode = new ModeSetting("Текстура снежка", "Режим 1", "Режим 1", "Режим 2", "Режим 3", "Режим 4", "Режим 5").visibleIf(() -> textureMode.is("Снежинки"));
    private final SliderSetting count = new SliderSetting("Количество", 100, 10, 1000, 10);
    final SliderSetting size = new SliderSetting("Размер", 0.23f, 0.01f, 1, 0.01f);
    public final CheckBoxSetting randomColor = new CheckBoxSetting("Рандомный цвет", false);
    public final CheckBoxSetting whiteCetner = new CheckBoxSetting("Белый центер", false);
    private final ResourceLocation orbis = new ResourceLocation("render/images/firefly.png");
    private final ResourceLocation star = new ResourceLocation("render/images/modules/particle/star1.png");
    private final ResourceLocation cube = new ResourceLocation("render/images/modules/particle/cube1.png");
    private final ResourceLocation cubeblast = new ResourceLocation("render/images/modules/particle/cubeblast1.png");
    private final ResourceLocation ygol = new ResourceLocation("render/images/modules/particle/ygol1.png");
    private final ResourceLocation crest = new ResourceLocation("render/images/modules/particle/crest1.png");
    private final ResourceLocation light = new ResourceLocation("render/images/modules/particle/light1.png");
    private final ResourceLocation heart = new ResourceLocation("render/images/modules/particle/heart1.png");
    private final ResourceLocation show = new ResourceLocation("render/images/modules/particle/show1.png");
    private final ResourceLocation bucks = new ResourceLocation("render/images/modules/particle/bucks1.png");

    public FireFly() {
        addSettings(textureMode, snowTextureMode, fallModeSetting, count, size, randomColor, whiteCetner);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        particles.clear();
        spawnParticle(mc.player);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        particles.clear();
    }

    private void spawnParticle(LivingEntity entity) {
        double distance = MathSystem.random(5, 50), yawRad = Math.toRadians(MathSystem.random(0, 360)), xOffset = -Math.sin(yawRad) * distance, zOffset = Math.cos(yawRad) * distance;

        particles.add(new FireFlyEntity(new Vector3d(entity.getPosX() + xOffset, entity.getPosY() + (fallModeSetting.is("Взлет") ? MathSystem.random(-10, 0) : MathSystem.random(3, 75)), entity.getPosZ() + zOffset),
                new Vector3d(), particles.size(), ColoringSystem.random().hashCode()));
    }

    @Subscribe
    public void OnRender(EventChangeWorld e) {
        particles.clear();
    }

    private void texture() {
        if (textureMode.is("Звезды")) {
            RectanglesSystem.bindTexture(star);
        } else if (textureMode.is("Доллары")) {
            RectanglesSystem.bindTexture(bucks);
        } else if (textureMode.is("Сердечки")) {
            RectanglesSystem.bindTexture(heart);
        } else if (textureMode.is("Орбизы")) {
            RectanglesSystem.bindTexture(orbis);
        } else if (textureMode.is("Молнии")) {
            RectanglesSystem.bindTexture(light);
        } else if (textureMode.is("Куб")) {
            RectanglesSystem.bindTexture(cube);
        } else if (textureMode.is("Квадрат")) {
            RectanglesSystem.bindTexture(cubeblast);
        } else if (textureMode.is("Крест")) {
            RectanglesSystem.bindTexture(crest);
        } else if (textureMode.is("Треугольник")) {
            RectanglesSystem.bindTexture(ygol);
        } else if (textureMode.is("Снежинки")) {
            RectanglesSystem.bindTexture(show);
        }
    }

    @Subscribe
    public void onPreRender(EventPreRender3D event) {
        ClientPlayerEntity player = mc.player;

        // Убираем старые или слишком удаленные частицы
        particles.removeIf(particle ->
                particle.time.isReached(5000) ||
                        particle.position.distance(player.getPosX(), player.getPosY(), player.getPosZ()) >= 60
        );

        // Спавним новые частицы, если нужно
        if (particles.size() <= count.getValue().intValue()) {
            spawnParticle(player);
        }

        MatrixStack matrix = event.getMatrix();
        boolean lightEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);

        RenderSystem.pushMatrix();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        if (lightEnabled) {
            RenderSystem.disableLighting();
        }

        GL11.glShadeModel(GL11.GL_SMOOTH);
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        // Привязываем текстуру в зависимости от выбора
        texture();

        if (!particles.isEmpty()) {
            particles.forEach(fireFlyEntity -> fireFlyEntity.update(true));

            float pos = size.getValue();
            for (FireFlyEntity particle : particles) {
                updateParticleAlpha(particle);

                int alpha = (int) Math.clamp(0, (int) (particle.getAlpha().getValue()), particle.getAngle());
                int colorGlow = randomColor.getValue() ?
                        ColoringSystem.reAlphaInt(particle.getColor(), alpha) :
                        ColoringSystem.reAlphaInt(Hud.getColor(particle.index * 99), alpha);

                renderParticle(matrix, particle, pos, alpha, colorGlow);
            }
        }

        cleanupRenderState(lightEnabled, matrix);
    }

    private void updateParticleAlpha(FireFlyEntity particle) {
        if ((int) particle.getAlpha().getValue() != 255 && !particle.time.isReached(particle.alpha.getDuration())) {
            particle.getAlpha().run(255);
        }
        if ((int) particle.getAlpha().getValue() != 0 && particle.time.isReached(5000 - particle.alpha.getDuration())) {
            particle.getAlpha().run(0);
        }
    }


    private void renderParticle(MatrixStack matrix, FireFlyEntity particle, float pos, int alpha, int colorGlow) {
        final Vector3d vec = particle.getPosition();
        matrix.push();
        RectanglesSystem.setupOrientationMatrix(matrix, (float) vec.x, (float) vec.y, (float) vec.z);
        matrix.rotate(mc.getRenderManager().getCameraOrientation());
        matrix.translate(0, pos / 2F, 0);

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RectanglesSystem.drawRect(matrix, -pos, -pos, pos, pos, colorGlow, colorGlow, colorGlow, colorGlow, true, true);

        // Белый центр
        float size = pos / 1F + 0.2F;
        int color = ColoringSystem.reAlphaInt(Theme.rectColor, alpha);
        if (this.whiteCetner.getValue()) {
            RectanglesSystem.drawRect(matrix, -size, -size, size, size, color, color, color, color, true, true);
        }
        if (!this.whiteCetner.getValue()) {

        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        matrix.pop();
    }

    private void cleanupRenderState(boolean lightEnabled, MatrixStack matrix) {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );

        GlStateManager.clearCurrentColor();
        GL11.glShadeModel(GL11.GL_FLAT);

        if (lightEnabled) {
            RenderSystem.enableLighting();
        }

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
        matrix.pop();
        RenderSystem.popMatrix();
    }


    @Getter
    private static class FireFlyEntity {
        private final int index;
        private final TimeCounterSetting time = new TimeCounterSetting();
        private final CompactAnimation alpha = new CompactAnimation(Easing.LINEAR, 150);
        private final int color;

        public final Vector3d position;
        private final Vector3d delta;

        public FireFlyEntity(final Vector3d position, final Vector3d DDNVLC, final int index, int color) {
            this.position = position;
            this.delta = new Vector3d(DDNVLC.x, DDNVLC.y, DDNVLC.z);
            this.index = index;
            this.color = color;
            this.time.reset();
        }

        public void update(boolean physics) {
            if (physics) {
                final Block block1 = BlockSystemSettings.getBlock(this.position.x, this.position.y, this.position.z + this.delta.z);
                if (isValidBlock(block1))
                    this.delta.z *= -0.8;

                final Block block2 = BlockSystemSettings.getBlock(this.position.x, this.position.y + this.delta.y, this.position.z);
                if (isValidBlock(block2)) {
                    this.delta.x *= 0.999F;
                    this.delta.z *= 0.999F;
                    this.delta.y *= -0.7;
                }

                final Block block3 = BlockSystemSettings.getBlock(this.position.x + this.delta.x, this.position.y, this.position.z);
                if (isValidBlock(block3))
                    this.delta.x *= -0.8;
            }

            this.updateMotion();
        }

        private boolean isValidBlock(Block block) {
            return !(block instanceof AirBlock)
                    && !(block instanceof BushBlock)
                    && !(block instanceof AbstractButtonBlock)
                    && !(block instanceof TorchBlock)
                    && !(block instanceof LeverBlock)
                    && !(block instanceof AbstractPressurePlateBlock)
                    && !(block instanceof CarpetBlock)
                    && !(block instanceof FlowingFluidBlock);
        }

        public int getAngle() {
            return (int) Math.clamp(0, 255, ((Math.sin(time.getTime() / 250D) + 1F) / 2F) * 255);
        }

        public void updateMotion() {
            double motion = 0.00005;
            this.delta.x += (Math.random() - 0.5) * motion;
            this.delta.y += (Math.random() - 0.5) * motion;

            if (!AG.getInst().getModuleManager().getFireFly().fallModeSetting.is("Простой")) {
                this.delta.y = 1.3;
            }

            this.delta.z += (Math.random() - 0.5) * motion;

            double maxSpeed = 0.03;
            this.delta.x = MathHelper.clamp(this.delta.x, -maxSpeed, maxSpeed);
            this.delta.y = MathHelper.clamp(this.delta.y, -maxSpeed, maxSpeed);
            this.delta.z = MathHelper.clamp(this.delta.z, -maxSpeed, maxSpeed);

            this.position.x += this.delta.x;
            this.position.y += this.delta.y;
            this.position.z += this.delta.z;

        }
    }
}
