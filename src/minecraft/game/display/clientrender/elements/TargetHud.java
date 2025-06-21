package minecraft.game.display.clientrender.elements;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.alacrity.Direction;
import minecraft.game.advantage.alacrity.easing.CompactAnimation;
import minecraft.game.advantage.alacrity.easing.Easing;
import minecraft.game.advantage.alacrity.impl.EaseBackIn;
import minecraft.game.advantage.alacrity.impl.EaseInOutQuad;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.other.KawaseBlur;
import minecraft.game.advantage.make.other.Scissor;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.timeupdate.ElementRenderer;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.operation.misc.NameProtect;
import minecraft.game.operation.visual.Hud;
import minecraft.game.transactions.AttackEvent;
import minecraft.game.transactions.EventRender2D;
import minecraft.system.AG;
import minecraft.system.managers.drag.Dragging;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TargetHud implements ElementRenderer {
    final TimeCounterSetting timerUtil = new TimeCounterSetting();
    final Dragging drag;
    LivingEntity entity = null;
    boolean allow;
    final EaseBackIn animation = new EaseBackIn(400, 1, 1);
    float healthAnimation = 0.0f;
    public CompactAnimation animation123 = new CompactAnimation(Easing.EASE_OUT_CUBIC, 400);
    float absorptionAnimation = 0.0f;

    float spacing = 5;
    float headSize = 30;
    float width = 160 / 1.5f;
    float height = 49 / 1.5f;

    public void onAttack(AttackEvent e) {
        if (e.entity == mc.player) {
            return;
        }
        if (entity == null) {
            return;
        }
    }

    @Subscribe
    public void render(EventRender2D eventRender2D) {
        float size;
        entity = findTarget(entity);

        boolean out = !allow || timerUtil.isReached(1000);
        DDATTACK ddattack = AG.getInst().getModuleManager().getDDATTACK();
        boolean shouldRender = mc.ingameGUI.getChatGUI().getChatOpen() || ddattack.getTarget() !=null;
        animation.setDuration(out ? 155 : 155);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);


        if (entity != null) {

            float posX = drag.getX();
            float posY = drag.getY();

            drag.setWidth(width);
            drag.setHeight(height);

            float hp = entity.getHealth() ;
            float maxHp = entity.getMaxHealth() ;

            healthAnimation = MathSystem.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);
            absorptionAnimation = MathSystem.fast(absorptionAnimation, MathHelper.clamp(entity.getAbsorptionAmount() / maxHp, 0, 1), 10);

            float animationValue = (float) animation.getOutput();
            float halfAnimationValueRest = (1 - animationValue) / 2f;

            GlStateManager.pushMatrix();
            sizeAnimation(posX + (width / 2), posY + (height / 2), animation.getOutput());
            if (AG.getInst().getModuleManager().getHud().targetHudmode.is("Новый")) {
                float nameWidth = ClientFonts.clickGui[16].getWidth(getDisplayName(entity));
                width = Math.max(150 / 1.5f, nameWidth + headSize + spacing * 2);
                height = 56 / 1.5f;
                headSize = 20;
                GraphicsSystem.drawRoundedRect(posX, posY, width, height, 3, ColoringSystem.setAlpha(ColoringSystem.rgb(20,20,20), 255));

                float headY = posY + spacing - 1;

                drawTargetHead(entity, posX + spacing, headY, headSize, headSize);
                GraphicsSystem.drawRadiusHead(posX + spacing, headY, headSize, headSize, 4, ColoringSystem.rgb(20,20,20));

                float lineX = posX + headSize + spacing * 2;

                Scissor.push();
                Scissor.setFromComponentCoordinates(posX, posY, width, height);
                ClientFonts.clickGui[16].drawString(eventRender2D.getMatrixStack(), getDisplayName(entity), lineX, posY + spacing + 2, -1);
                Scissor.unset();
                Scissor.pop();

                this.drawItemStack(posX + headSize + spacing + spacing - 2f , posY + spacing + 3.0F + spacing + spacing - 8.5F, 5.1F, this.entity);

                float barWidth = width - (headSize + spacing * 1.5f);
                float barY = posY + height - spacing * 2 - 1;
                Vector4i healthColor;
                float color124 = Hud.getColor(1);
                float color1245 = Hud.getColor(90);  // Assign the value within the block
                if (hp / maxHp > 0.9)
                    healthColor = new Vector4i((int) color124, (int) color124, (int) color1245,(int) color1245);
                else if (hp / maxHp > 0.8 ) {
                    healthColor = new Vector4i((int) color124, (int) color124, (int) color1245,(int) color1245);
                } else if (hp / maxHp > 0.7) {
                    healthColor = new Vector4i((int) color124, (int) color124, (int) color1245,(int) color1245);
                } else if (hp / maxHp > 0.6) {
                    healthColor = new Vector4i((int) color124, (int) color124, (int) color1245,(int) color1245);
                } else if (hp / maxHp > 0.5) {
                    healthColor = new Vector4i((int) color124, (int) color124, (int) color1245,(int) color1245);
                } else if (hp / maxHp > 0.4) {
                    healthColor = new Vector4i((int) color124, (int) color124, (int)color1245, (int)color1245);
                } else if (hp / maxHp > 0.3) {
                    healthColor = new Vector4i((int) color124, (int) color124, (int)color1245,(int) color1245);
                } else if (hp / maxHp > 0.2) {
                    healthColor = new Vector4i((int) color124, (int) color124,(int) color1245,(int) color1245);
                } else if (hp / maxHp > 0.1) {
                    healthColor = new Vector4i((int) color124, (int) color124,(int) color1245,(int) color1245);
                } else {
                    healthColor = new Vector4i((int) color124, (int) color124,(int) color1245,(int) color1245);
                }
                GraphicsSystem.drawRoundedRect(posX  + spacing, posY + height + 1 - 4 - spacing * 2 + 2, (width - 29) * healthAnimation, 7, new Vector4f(2, 2, 2, 2), healthColor);

                String hpText = String.format("%.0f", hp) + "hp";
                float textX = posX + width - spacing - ClientFonts.clickGui[14].getWidth(hpText) + 1;
                ClientFonts.clickGui[14].drawString(eventRender2D.getMatrixStack(), hpText, textX, barY + 1, -1);
                GlStateManager.popMatrix();
            }

            if (AG.getInst().getModuleManager().getHud().targetHudmode.is("Кругляшок")) {
                headSize = 27;
                height = 55 / 1.5f;

                float baseWidth = 185 / 1.5f;
                float circleWidth = 30;
                width = Math.max(baseWidth, headSize + spacing * 3 + circleWidth);

                GraphicsSystem.drawRoundedRect(posX, posY, width, height, 3, ColoringSystem.rgb(20,20,20));

                drawTargetHead(entity, posX + spacing, posY + (height / 2) - (headSize / 2), headSize, headSize);
                GraphicsSystem.drawRadiusHead(posX + spacing, posY + (height / 2) - (headSize / 2), headSize, headSize, 5, ColoringSystem.rgb(20,20,20));

                float lineX = posX + headSize + spacing + spacing - 5;

                Scissor.push();
                Scissor.setFromComponentCoordinates(lineX + spacing - 1, posY + spacing + 0, 57, height);
                ClientFonts.clickGui[16].drawString(eventRender2D.getMatrixStack(), getDisplayName(entity), lineX + spacing - 1, posY + spacing + 3, -1);
                Scissor.unset();
                Scissor.pop();


                this.drawItemStack(posX + headSize + spacing + spacing - 3.5f, posY + spacing + 6 + spacing + spacing - 8.5F, 4.5F, this.entity);

                float hpPercent = (hp / maxHp) * 100.0f;
                float circleX = posX + width - 18;
                float circleY = posY + height / 2.0f;
                float circleRadius = 11.0f;

                GraphicsSystem.drawCircleFilled(circleX, circleY, 0, 360, circleRadius, 2.0f, false, ColoringSystem.setAlpha(ColoringSystem.rgb(1,1,1), 150));
                GraphicsSystem.drawCircleFilled(circleX, circleY, 0, 360 * (hpPercent / 100.0f), circleRadius, 2.0f, false, Hud.getColor(1));

                String hpText = String.format("%.0f%%", hpPercent);
                float textX = circleX - ClientFonts.clickGui[14].getWidth(hpText) / 2.0f + 0.5f;
                float textY = circleY - ClientFonts.clickGui[14].getFontHeight() / 2.0f + 3;
                ClientFonts.clickGui[14].drawString(eventRender2D.getMatrixStack(), hpText, textX, textY, -1);

                GlStateManager.popMatrix();
            }
        }
    }

    private String getDisplayName(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            if (entity == mc.player && AG.getInst().getModuleManager().getNameProtect().isEnabled()) {
                return "Protected";
            }
            if (AG.getInst().getFriendManager().isFriend(player.getName().getString()) && AG.getInst().getModuleManager().getNameProtect().isEnabled()) {
                return "Protected";
            }
        }
        return entity.getName().getString();
    }



    private void drawItemStack(float x, float y, float offset, LivingEntity entity) {
        java.util.ArrayList<ItemStack> stackList = new java.util.ArrayList(Arrays.asList(findTarget(entity).getHeldItemMainhand(), findTarget(entity).getHeldItemOffhand()));
        java.util.ArrayList<ItemStack> stackList1 = new ArrayList((Collection)findTarget(entity).getArmorInventoryList());
        AtomicReference<Float> posX = new AtomicReference(x);
        AtomicReference<Float> posXArmor = new AtomicReference(x);
        stackList.stream().filter((stack) -> {
            return !stack.isEmpty();
        }).forEach((stack) -> {
            GraphicsSystem.drawItemStack(stack, (Float)posX.getAndAccumulate(offset + 5.0F, Float::sum), y + 4.0F, true, true, 0.66F);
        });
        stackList1.stream().filter((stack) -> {
            return !stack.isEmpty();
        }).forEach((stack) -> {
            GraphicsSystem.drawItemStack(stack, (Float)posXArmor.getAndAccumulate(offset + 5.0F, Float::sum) + 21.0F, y + 4.0F, true, true, 0.72F);
        });
    }

    private LivingEntity findTarget(LivingEntity currentTarget) {
        LivingEntity auraTarget = AG.getInst().getModuleManager().getDDATTACK().getTarget();
        if (auraTarget != null) {
            timerUtil.reset();
            allow = true;
            return auraTarget;
        }

        LivingEntity legitauraTarget = AG.getInst().getModuleManager().getLegitATTACK().getTarget();
        if (legitauraTarget != null) {
            timerUtil.reset();
            allow = true;
            return legitauraTarget;
        }

        if (mc.currentScreen instanceof ChatScreen) {
            timerUtil.reset();
            allow = true;
            return mc.player;
        }

        allow = false;
        return currentTarget;
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity == null) return;

        if (entity instanceof PlayerEntity) {
            drawHead(entity, x, y, Math.min(width, height));
        } else {
            ResourceLocation texture = new ResourceLocation("render/images/target.png");

            float centeredX = x + (width / 2) - (width / 2) + 2;
            float centeredY = y + (height / 2) - (height / 2) - 1;

            GraphicsSystem.drawImage(texture, centeredX, centeredY, width - 1, height - 1, -1);
        }
    }

    public void drawHead(LivingEntity entity, float x, float y, float size) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        ResourceLocation texture = mc.getRenderManager().getRenderer(entity).getEntityTexture(entity);
        mc.getTextureManager().bindTexture(texture);
        float hurtEffect = (entity.hurtTime - (entity.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
        GL11.glColor4f(1, 1 - hurtEffect, 1 - hurtEffect, 1);
        AbstractGui.drawScaledCustomSizeModalRect(x, y, 8, 8, 8, 8, size, size, 64, 64);

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public static class HeadParticle {
        private Vector3d pos;
        private final Vector3d end;
        private final long time;
        private float alpha;

        public HeadParticle(Vector3d pos) {
            this.pos = pos;

            double radius = ThreadLocalRandom.current().nextDouble(40.0, 80.0);
            double angle = ThreadLocalRandom.current().nextDouble(0, 2 * Math.PI);

            double heightOffset = ThreadLocalRandom.current().nextDouble(-30.0, 30.0);
            double offsetX = radius * Math.cos(angle);
            double offsetY = radius * Math.sin(angle);

            this.end = pos.add(offsetX, offsetY, heightOffset);
            this.time = System.currentTimeMillis();
        }

        public void update() {
            this.alpha = MathSystem.lerp(this.alpha, 1.0F, 10.0F);
            this.pos = MathSystem.fast(this.pos, this.end, 0.5F);
        }
    }
}
