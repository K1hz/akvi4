package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.system.AG;
import minecraft.game.transactions.EventPreRender3D;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.combat.DDBOT;
import minecraft.game.operation.misc.NameProtect;
import minecraft.system.managers.Theme;
import minecraft.game.advantage.alacrity.AnimationManager;
import minecraft.game.advantage.alacrity.Direction;
import minecraft.game.advantage.alacrity.impl.DecelerateAnimation;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.Vector4i;
import minecraft.game.advantage.make.engine2d.Projection;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.RectanglesSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.make.font.Fonts;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

@Defuse(name = "Esp",description = "Добавляет информацию над сущностями", brand = Category.Visual)
public class ESP extends Module {
    public ModeListSetting remove = new ModeListSetting("Убрать", new CheckBoxSetting("Боксы", false), new CheckBoxSetting("Зачарования", false));
    public ModeListSetting targets = new ModeListSetting("Отображать",
            new CheckBoxSetting("Себя", true),
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Предметы", false),
            new CheckBoxSetting("Мобы", false)
            );
    final SliderSetting alphaЫШЯУ = new SliderSetting("Прозрачность", 255f, 1f, 255, 1f);
    final SliderSetting size = new SliderSetting("Толщина боксов", 0.45f, 0.1f, 3, 0.01f).visibleIf(() -> !this.remove.is("Боксы").getValue());
    final SliderSetting sizeNametags = new SliderSetting("Размер NameTags", 0.5f, 0.1f, 3, 0.01f).visibleIf(() -> this.targets.is("Игроки").getValue() || this.targets.is("Себя").getValue() || this.targets.is("Мобы").getValue());;
    final SliderSetting sizeItemTags = new SliderSetting("Размер ItemTags", 0.5f, 0.1f, 3, 0.01f).visibleIf(() -> this.targets.is("Предметы").getValue());;

    private final AnimationManager alpha = new DecelerateAnimation(600, 255);
    private LivingEntity currentTarget;
    private long lastTime = System.currentTimeMillis();

    float healthAnimation = 0.0f;

    public ESP() {
        addSettings(targets, remove,size,sizeNametags, sizeItemTags);
    }

    float length;

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    public ColorSetting color = new ColorSetting("Color", -1);

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (AG.getInst().getModuleManager().getDDATTACK().getTarget() != null) {
            currentTarget = AG.getInst().getModuleManager().getDDATTACK().getTarget();
        }

        this.alpha.setDirection(AG.getInst().getModuleManager().getDDATTACK().getTarget() != null ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Subscribe
    public void onRender(EventPreRender3D e) {
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }
    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        if (mc.world == null || e.getType() != EventRender2D.Type.PRE) {
            return;
        }

        positions.clear();


        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            if (!(entity instanceof PlayerEntity && entity != mc.player && targets.is("Игроки").getValue()
                    || entity instanceof ItemEntity && targets.is("Предметы").getValue()
                    || (entity instanceof AnimalEntity || entity instanceof MobEntity) && targets.is("Мобы").getValue()
                    || entity == mc.player && targets.is("Себя").getValue() && !(mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)
            )) continue;

            double x = MathSystem.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
            double y = MathSystem.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
            double z = MathSystem.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());

            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

            Vector4f position = null;
            for (int i = 0; i < 8; i++) {
                Vector2f vector = Projection.project(i % 2 == 0 ? aabb.minX : aabb.maxX, (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY, (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            positions.put(entity, position);
        }


        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);

        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Vector4f position = entry.getValue();
            if (entry.getKey() instanceof LivingEntity entity) {
                if (!remove.is("Боксы").getValue()) {
                    GraphicsSystem.drawBox((double) position.x, (double) position.y, (double) position.z, (double) position.w, this.size.getValue(), FriendManager.isFriend(entity.getName().getString()) ? ColoringSystem.green(1) : ColoringSystem.rgb(20,20,20));
                }
                float hpOffset = 3f;
                float out = 0.5f;
            }
        }
        Tessellator.getInstance().draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living) {
                String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();
                Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();

                if (mc.getCurrentServerData() != null) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }

                Vector4f position = entry.getValue();
                float width = position.z - position.x;

                GL11.glPushMatrix();

                String friendPrefix = FriendManager.isFriend(entity.getName().getString()) ? TextFormatting.GREEN + "[F] " : "";
                String creativePrefix = "";
                if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative()) {
                    creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + "Creative" + TextFormatting.GRAY + "]";
                } else {
                    if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                        creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + (int) hp + TextFormatting.GRAY + "]";
                    } else {
                        creativePrefix = TextFormatting.GRAY + " [" + TextFormatting.RED + ((int) hp + (int) living.getAbsorptionAmount()) + TextFormatting.GRAY + "]";
                    }
                }

                healthAnimation = MathSystem.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);

                TextComponent name = (TextComponent) ITextComponent.getTextComponentOrEmpty(friendPrefix);
                int colorRect = FriendManager.isFriend(entity.getName().getString()) ? ColoringSystem.rgba(66, 163, 60, 120) : ColoringSystem.setAlpha(ColoringSystem.rgb(20,20,20), 255);

                NameProtect nameProtect = AG.getInst().getModuleManager().getNameProtect();
                boolean hideFriendsEnabled = nameProtect.isEnabled() && nameProtect.hideFriends.getValue();

                name.append(hideFriendsEnabled && FriendManager.isFriend(entity.getName().getString())
                        ? ITextComponent.getTextComponentOrEmpty(TextFormatting.RED + "protected")
                        : entity.getDisplayName());

                name.appendString(creativePrefix);
                glCenteredScale(position.x + width / 2f - length / 2f - 4, position.y - 9, length + 8, 13, this.sizeNametags.getValue());//ник игрока

                length = mc.fontRenderer.getStringPropertyWidth(name);
                float x1 = position.x + width / 2f - length / 2f - 4;
                float y1 = position.y - 15.5f;
                RectanglesSystem.getInstance().drawRoundedRectShadowed(e.getMatrixStack(), x1, y1, x1 + length + 8, y1 + 13, 0, 2, colorRect, colorRect, colorRect, colorRect, false, false, true, false);
                mc.fontRenderer.func_243246_a(e.getMatrixStack(), name, position.x + width / 2f - length / 2f, position.y - 12.5f, -1);

                GL11.glPopMatrix();
                drawItems(e.getMatrixStack(), living, (int) (position.x + width / 2f), (int) (position.y - 14.5f));
            } else if (entity instanceof ItemEntity item) {
                Vector4f position = entry.getValue();
                float width = position.z - position.x;
                ITextComponent displayName = new StringTextComponent((item.getItem().getDisplayName().getString() + (item.getItem().getCount() < 1 ? "" : " x" + item.getItem().getCount())));

                float scale = this.sizeItemTags.getValue();
                float length = mc.fontRenderer.getStringPropertyWidth(displayName) * scale; // Учитываем масштаб
                GL11.glPushMatrix();




                glCenteredScale(position.x + width / 2f - length / 2f, position.y - 7, length, 10, this.sizeItemTags.getValue()); //предметы
                mc.fontRenderer.func_243248_b(e.getMatrixStack(), displayName, position.x + width / 2f - length / 2f, position.y - 7, -1);
                GL11.glPopMatrix();
            }
        }

        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }
    }

    public boolean isInView(Entity ent) {

        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }
    int index = 0;
    private void drawPotions(MatrixStack matrixStack, LivingEntity entity, float posX, float posY) {
        for (Iterator var8 = entity.getActivePotionEffects().iterator(); var8.hasNext(); ++index) {
            EffectInstance effectInstance = (EffectInstance)var8.next();

            int amp = effectInstance.getAmplifier() + 1;
            String ampStr = "";

            if (amp >= 1 && amp <= 9) {
                ampStr = " " + amp;
            }


            String text = EffectUtils.getPotionDurationString(effectInstance, 1) + " - " + I18n.format(effectInstance.getEffectName(), new Object[0]) + ampStr;
            PotionSpriteUploader potionspriteuploader = mc.getPotionSpriteUploader();
            Effect effect = effectInstance.getPotion();
            float iconSize = (float) (8);
            TextureAtlasSprite textureatlassprite = potionspriteuploader.getSprite(effect);
            mc.getTextureManager().bindTexture(textureatlassprite.getAtlasTexture().getTextureLocation());
            DisplayEffectsScreen.blit(matrixStack, (float) (posX),  (int)posY - 0.5f, 10, 8, 8, textureatlassprite);

            Fonts.montserrat.drawTextWithOutline(matrixStack, text, posX + iconSize, posY, ColoringSystem.setAlpha(-1, (int) (255)), 6, 0.02f);
            posY += Fonts.montserrat.getHeight(6) + 1;
        }
    }

    private void drawItems(MatrixStack matrixStack, LivingEntity entity, int posX, int posY) {
        int size = 8;
        int padding = 6;

        float fontHeight = Fonts.consolas.getHeight(6);

        List<ItemStack> items = new ArrayList<>();

        ItemStack mainStack = entity.getHeldItemMainhand();

        if (!mainStack.isEmpty()) {
            items.add(mainStack);
        }

        for (ItemStack itemStack : entity.getArmorInventoryList()) {
            if (itemStack.isEmpty()) continue;
            items.add(itemStack);
        }

        ItemStack offStack = entity.getHeldItemOffhand();

        if (!offStack.isEmpty()) {
            items.add(offStack);
        }

        posX -= (items.size() * (size + padding)) / 2f;

        for (ItemStack itemStack : items) {
            if (itemStack.isEmpty()) continue;

            GL11.glPushMatrix();

            glCenteredScale(posX, posY - 5, size / 2f, size / 2f, 0.5f);

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, posX, posY - 5);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, posX, posY - 5, null);

            GL11.glPopMatrix();

            if (itemStack.isEnchanted() && !remove.is("Зачарования").getValue()) {
                int ePosY = (int) (posY - fontHeight);

                Map<Enchantment, Integer> enchantmentsMap = EnchantmentHelper.getEnchantments(itemStack);

                for (Enchantment enchantment : enchantmentsMap.keySet()) {
                    int level = enchantmentsMap.get(enchantment);

                    if (level < 1 || !enchantment.canApply(itemStack)) continue;

                    IFormattableTextComponent iformattabletextcomponent = new TranslationTextComponent(enchantment.getName());

                    String enchText = iformattabletextcomponent.getString().substring(0, 3) + level;

                    Fonts.consolas.drawText(matrixStack, enchText, posX, ePosY - 5, -1, 5, 0.03f);

                    ePosY -= (int) fontHeight;
                }
            }

            posX += size + padding;
        }
    }

    public boolean isValid(Entity e) {
        if (DDBOT.isBot(e)) return false;

        return isInView(e);
    }

    public static void drawMcRect(
            double left,
            double top,
            double right,
            double bottom,
            int color) {
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

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        bufferbuilder.pos(left, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, bottom, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(right, top, 1.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos(left, top, 1.0F).color(f, f1, f2, f3).endVertex();

    }

    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        glTranslatef(x + w / 2, y + h / 2, 0);
        glScalef(f, f, 1);
        glTranslatef(-x - w / 2, -y - h / 2, 0);
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return Math.max(10f, 1000 / distance) * (size / 30f) / (fov == 70 ? 1 : fov / 70.0f);
    }
}
