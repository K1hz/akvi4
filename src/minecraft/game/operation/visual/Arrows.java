package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import minecraft.game.advantage.make.font.Fonts;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.system.AG;
import minecraft.system.managers.friend.FriendManager;
import minecraft.game.transactions.EventChangeWorld;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.combat.DDBOT;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ColorSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.system.managers.Theme;
import minecraft.game.advantage.figures.moved.Animation;
import minecraft.game.advantage.figures.moved.util.Easings;
import minecraft.game.advantage.luvvy.MovementSystem;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

@Defuse(name = "Triangles",description = "Добавляет стрелки к сущностям", brand = Category.Visual)
public class Arrows extends Module {
    public ModeListSetting targets = new ModeListSetting("Отображать",
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Предметы", false),
            new CheckBoxSetting("Мобы", false)
    );
    final ModeSetting mode = new ModeSetting("Режим указателя", "Обычный", "Обычный", "Старый", "Новый", "Приятный");
    private final CheckBoxSetting distance = new CheckBoxSetting("Отображать дистанцию", true);
    final ModeSetting playerColorMode = new ModeSetting("Выбор цвета игроков", "Client", "Client", "Custom").visibleIf(() -> targets.is("Игроки").getValue());
    final ModeSetting mobColorMode = new ModeSetting("Выбор цвета мобов", "Custom", "Client", "Custom").visibleIf(() -> targets.is("Мобы").getValue());
    final ModeSetting friendColorMode = new ModeSetting("Выбор цвета друзей", "Custom", "Client", "Custom").visibleIf(() -> targets.is("Игроки").getValue());
    final ModeSetting itemColorMode = new ModeSetting("Выбор цветов предметов", "Custom", "Client", "Custom").visibleIf(() -> targets.is("Предметы").getValue());
    final ColorSetting playerColor = new ColorSetting("Цвет игроков", -1).visibleIf(() -> playerColorMode.is("Custom") && targets.is("Игроки").getValue());
    final ColorSetting mobColor = new ColorSetting("Цвет мобов", -1).visibleIf(() -> mobColorMode.is("Custom") && targets.is("Мобы").getValue());
    final ColorSetting friendColor = new ColorSetting("Цвет друзей", ColoringSystem.rgb(94, 255, 69)).visibleIf(() -> friendColorMode.is("Custom") && targets.is("Игроки").getValue());
    final ColorSetting itemColor = new ColorSetting("Цвет предметов", ColoringSystem.rgb(255, 72, 69)).visibleIf(() -> itemColorMode.is("Custom") && targets.is("Предметы").getValue());

    public Arrows() {
        addSettings(mode,targets,distance, playerColorMode, mobColorMode, friendColorMode, itemColorMode, playerColor, mobColor, friendColor, itemColor);
    }

    @Setter
    @Getter
    private boolean render = false;
    private final Animation yawAnimation = new Animation();
    private final Animation moveAnimation = new Animation();
    private final Animation openAnimation = new Animation();
    private float addX;
    private float addY;

    public int getPlayerColor() {
        return playerColorMode.is("Custom") ? playerColor.getValue() : Hud.getColor(1);
    }

    public int getMobColor() {
        return mobColorMode.is("Custom") ? mobColor.getValue() : Hud.getColor(1);
    }

    public int getFriendColor() {
        return friendColorMode.is("Custom") ? friendColor.getValue() : Hud.getColor(1);
    }

    public int getItemColor() {
        return itemColorMode.is("Custom") ? itemColor.getValue() : Hud.getColor(1);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        setRender(false);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        setRender(true);
    }

    @Subscribe
    public void onWorldChange(EventChangeWorld e) {
        setRender(isRender());
    }

    @Subscribe
    public void onDisplay(EventRender2D e) {
        openAnimation.update();
        moveAnimation.update();
        yawAnimation.update();

        if (!render && openAnimation.getValue() == 0 && openAnimation.isFinished()) return;

        final float moveAnim = calculateMoveAnimation();

        openAnimation.run(render ? 1 : 0, 0.1, Easings.BACK_OUT, true);
        moveAnimation.run(render ? moveAnim : 0, 0.3, Easings.BACK_OUT, true);
        yawAnimation.run(mc.gameRenderer.getActiveRenderInfo().getYaw(), 0.1, Easings.BACK_OUT, true);

        final double cos = Math.cos(Math.toRadians(yawAnimation.getValue()));
        final double sin = Math.sin(Math.toRadians(yawAnimation.getValue()));
        double radius = moveAnimation.getValue() - 20;
        final double xOffset = (scaled().x / 2F) - radius;
        final double yOffset = (scaled().y / 2F) - radius;

        for (Entity entity : mc.world.getAllEntities()) {
            if (DDBOT.isBot(entity)) continue;
            if (!(entity instanceof PlayerEntity && targets.is("Игроки").getValue()
                    || entity instanceof ItemEntity && targets.is("Предметы").getValue()
                    || (entity instanceof AnimalEntity || entity instanceof MobEntity || entity instanceof WaterMobEntity) && targets.is("Мобы").getValue()
            )) continue;
            if (entity == mc.player) continue;

            Vector3d vector3d = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
            final double xWay = (((entity.getPosX() + (entity.getPosX() - entity.lastTickPosX) * mc.getRenderPartialTicks()) - vector3d.x) * 0.01D);
            final double zWay = (((entity.getPosZ() + (entity.getPosZ() - entity.lastTickPosZ) * mc.getRenderPartialTicks()) - vector3d.z) * 0.01D);
            final double rotationY = -(zWay * cos - xWay * sin);
            final double rotationX = -(xWay * cos + zWay * sin);
            final double angle = Math.toDegrees(Math.atan2(rotationY, rotationX));
            double x = ((radius * Math.cos(Math.toRadians(angle))) + xOffset + radius);
            double y = ((radius * Math.sin(Math.toRadians(angle))) + yOffset + radius);
            Crosshair crosshair = AG.getInst().getModuleManager().getCrosshair();
            if (crosshair.isEnabled() && crosshair.mode.is("Орбиз") && !crosshair.staticCrosshair.getValue() && mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) {
                addX = crosshair.getAnimatedYaw();
                addY = crosshair.getAnimatedPitch();
            } else {
                addX = addY = 0;
            }

            x += addX;
            y += addY;

            if (isValidRotation(rotationX, rotationY, radius)) {
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0D);
                GL11.glRotated(angle, 0D, 0D, 1D);
                GL11.glRotatef(90F, 0F, 0F, 1F);

                int color = 0;
                if (entity instanceof MobEntity || entity instanceof AnimalEntity || entity instanceof WaterMobEntity) {
                    color = getMobColor();
                } else if (FriendManager.isFriend(TextFormatting.getTextWithoutFormattingCodes(entity.getName().getString()))) {
                    color = getFriendColor();
                } else if (entity instanceof PlayerEntity) {
                    color = getPlayerColor();
                } else if (entity instanceof ItemEntity) {
                    color = getItemColor();
                }

                if (this.mode.is("Старый")) {
                    GraphicsSystem.drawImage(new ResourceLocation("render/images/arrow.png"), -8.0F, -9.0F, 16.0F, 16.0F, color);
                }
                if (this.mode.is("Новый")) {
                    GraphicsSystem.drawImage(new ResourceLocation("render/images/arrows.png"), -8.0F, -9.0F, 18, 18, color);
                }
                if (this.mode.is("Приятный")) {
                    GraphicsSystem.drawImage(new ResourceLocation("render/images/arrownews.png"), -8.0F, -9.0F, 18, 18, color);
                }
                if (this.mode.is("Обычный")) {
                    Fonts.icons.drawText(e.getMatrixStack(), "D", -6.0F, -7.0F, color, 10);
                }
                if (this.distance.getValue()) {
                    String text = String.valueOf((int) mc.player.getDistance(entity)) + "b";
                    float textWidth = ClientFonts.msSemiBold[12].getWidth(text);

                    if (this.mode.is("Обычный")) {
                        GraphicsSystem.drawRoundedRect(6f, -6.5f, 2 +textWidth, 7, 1, ColoringSystem.rgb(0, 0, 0));
                        ClientFonts.msSemiBold[12].drawString(e.getMatrixStack(), text, 7, -4, -1);
                    }
                    if (this.mode.is("Приятный")) {
                        GraphicsSystem.drawRoundedRect(6f, -6.5f, 2 +textWidth, 7, 1, ColoringSystem.rgb(0, 0, 0));
                        ClientFonts.msSemiBold[12].drawString(e.getMatrixStack(), text, 7, -4, -1);
                    }
                    if (this.mode.is("Новый")) {
                        GraphicsSystem.drawRoundedRect(6f, -6.5f, 2 +textWidth, 7, 1, ColoringSystem.rgb(0, 0, 0));
                        ClientFonts.msSemiBold[12].drawString(e.getMatrixStack(), text, 7, -4, -1);
                    }
                    if (this.mode.is("Старый")) {
                        GraphicsSystem.drawRoundedRect(4f, -6.5f, 2 +textWidth, 7, 1, ColoringSystem.rgb(0, 0, 0));
                        ClientFonts.msSemiBold[12].drawString(e.getMatrixStack(), text, 5, -4, -1);
                    }
                }

                GL11.glPopMatrix();
            }
        }
    }

    private float calculateMoveAnimation() {
        float set = 56;
        if (mc.currentScreen instanceof ContainerScreen<?> container) {
            set = Math.max(container.ySize, container.xSize) / 2F + 50;
        }
        float moveAnim = set;
        if (MovementSystem.isMoving()) {
            moveAnim += mc.player.isSneaking() ? 5 : 10;
        } else if (mc.player.isSneaking()) {
            moveAnim -= 2;
        }
        return moveAnim;
    }

    private boolean isValidRotation(double rotationX, double rotationY, double radius) {
        final double mrotY = -rotationY;
        final double mrotX = -rotationX;
        return MathHelper.sqrt(mrotX * mrotX + mrotY * mrotY) < radius;
    }

}
