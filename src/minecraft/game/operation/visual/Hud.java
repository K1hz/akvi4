package minecraft.game.operation.visual;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import minecraft.game.advantage.advisee.AnimationMath;
import minecraft.game.advantage.advisee.AnimationNumbers;
import minecraft.game.advantage.figures.BlomSystem;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.luvvy.PlayerSettingsModule;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.advantage.words.MoreColorsSystem;
import minecraft.game.advantage.words.font.ClientFonts;
import minecraft.game.display.clientrender.elements.*;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.game.transactions.AttackEvent;
import minecraft.game.transactions.EventMotion;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;
import minecraft.system.managers.drag.Dragging;
import minecraft.system.styles.Style;
import minecraft.system.styles.StyleManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Defuse(name = "Hud", description = "123", brand = Category.Visual)
public class Hud extends minecraft.game.operation.wamost.api.Module {

    public final ModeListSetting elements = new ModeListSetting("Элементы",
            new CheckBoxSetting("Watermark", true),
            new CheckBoxSetting("Player Info", true),
            new CheckBoxSetting("KeyBinds", true),
            new CheckBoxSetting("Potion List", true),
            new CheckBoxSetting("Staff List",true),
            new CheckBoxSetting("Array List", true),
            new CheckBoxSetting("Target Hud", true),
            new CheckBoxSetting("Notification", true),
            new CheckBoxSetting("HotBar", true),
            new CheckBoxSetting("Motion Graphics", true),
            new CheckBoxSetting("Armor Hud", true)
    );

    public final ModeListSetting elementsWatermark = new ModeListSetting("Элементы watermark",
            new CheckBoxSetting("Login", true),
            new CheckBoxSetting("Fps", true),
            new CheckBoxSetting("Ping", true),
            new CheckBoxSetting("Time", false),
            new CheckBoxSetting("Server", false)
    ).visibleIf(() -> elements.is("Watermark").getValue());

//    public final ModeSetting arrayListSetting = new ModeSetting("Настройки array list","Стандартный текст","Стандартный текст", "Только большие буквы", "Только маленькие буквы").visibleIf(() -> this.elements.is("Array List").getValue());

    public final ModeListSetting elementsPlayerInfo = new ModeListSetting("Элементы player info",
            new CheckBoxSetting("Coordination", true),
            new CheckBoxSetting("Bps", true),
            new CheckBoxSetting("Tps", true)
    ).visibleIf(() -> elements.is("Player Info").getValue());


//    public static final ModeSetting themeMode = new ModeSetting("Режим темы","Основной","Основной","Галактический", "Огненный", "Зелёный", "Белый", "Ночной", "Морской", "Весенний", "Персиковый", "Лазурный", "Лимонный", "Гламурный", "Бирюзовый", "Коралловый", "Сиреневый", "Небесный", "Мятный", "Зелёно-Синий", "Розово-Красный", "Фиолетово-Белый", "Фиолетово-Синий", "Жёлто-Зеленый", "Жёлто-Розовый");
    public static final ModeSetting vibor = new ModeSetting("Палитра темы", "Основная", "Основная",
        "Фиолетовая", "Красная",
        "Белая", "Голубая",
        "Астольфо", "Кастомная",
        "Радужная");
    public static final ColorSetting color1 = new ColorSetting("1 цвет",ColoringSystem.rgb(255, 255, 255)).visibleIf(() -> vibor.is("Кастомная"));
    public static final ColorSetting color2 = new ColorSetting("2 цвет",ColoringSystem.rgb(255, 255, 255)).visibleIf(() -> vibor.is("Кастомная"));
    public final ModeSetting targetHudmode = new ModeSetting("Тип таргетхуда","Новый","Новый","Кругляшок").visibleIf(() -> elements.is("Target Hud").getValue());
    private float fps, health = 0, dotSpacing = 8f, blinkAlpha = 0, motionBps = 0;;

    private java.util.ArrayList<Float> motiongraph = new java.util.ArrayList<>();
    final WaterMark watermark;

    final PlayerInfo playerInfo;
    final KeyBinds keyBind;
    final Potions potions;
   final StaffList staffList;
    final TargetHud targetHud;
    final ArmorHud armorHud;
    final HotBar hotBar;
    final ArrayList arrayList;


    @Getter
    private final CopyOnWriteArrayList<TargetHud.HeadParticle> particle = new CopyOnWriteArrayList();
    @Subscribe
    private void onAttack(AttackEvent e) {
        if (elements.is("Target Hud").getValue()) {
            targetHud.onAttack(e);
        }
    }
    @Subscribe
    public void onUpdate(EventMotion eventMotion) {
        float bps = (float) (Math.hypot(mc.player.getPosX() - mc.player.prevPosX, mc.player.getPosZ() - mc.player.prevPosZ) * (double) mc.timer.timerSpeed * 20.0D);
        this.motionBps = (float) AnimationMath.lerp(this.motionBps, bps,90);
        motiongraph.add(this.motionBps);
        if (motiongraph.size() >= 90) motiongraph.remove(0);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.gameSettings.showDebugInfo) {
            return;
        }

        if (elements.is("Array List").getValue()) arrayList.update(e);

        elementsWatermark.visibleIf(() -> elements.is("Watermark").getValue());
       if (elements.is("Staff List").getValue()) staffList.update(e);
    }

    @Subscribe
    public void onRender2D(EventRender2D event) {
        if (mc.gameSettings.showDebugInfo || event.getType() != EventRender2D.Type.POST) {
            return;
        }

        if (elements.is("Watermark").getValue()) watermark.render(event);
        if (elements.is("HotBar").getValue()) hotBar.render(event);
        if (elements.is("Motion Graphics").getValue()) drawMotionGraphics();
        if (elements.is("Player info").getValue()) playerInfo.render(event);
        if (elements.is("Array List").getValue()) arrayList.render(event);
        if (elements.is("KeyBinds").getValue()) keyBind.render(event);
        if (elements.is("Potion List").getValue()) potions.render(event);
       if (elements.is("Staff List").getValue()) staffList.render(event);
        if (elements.is("Target Hud").getValue()) targetHud.render(event);
        if (elements.is("Armor Hud").getValue()) armorHud.render(event);
    }
    
    public static int viborTEM(int index) {
        int speed = 5;
        StyleManager styleManager = AG.getInst().getStyleManager();
        if (vibor.is("Основная")){
            return ColoringSystem.gradient(ColoringSystem.rgb(0, 123, 255), ColoringSystem.rgb(0, 60, 255), index, speed);
        }else if (vibor.is("Фиолетовая")){
            return ColoringSystem.gradient(ColoringSystem.rgb(138, 79, 255),ColoringSystem.rgb(242, 64, 255),index,speed);
        }else if (vibor.is("Красная")) {
            return ColoringSystem.gradient(ColoringSystem.rgb(255, 10, 10), ColoringSystem.rgb(255, 82, 82), index, speed);
        }else if (vibor.is("Голубая")) {
            return ColoringSystem.gradient(ColoringSystem.rgb(0, 204, 255), ColoringSystem.rgb(0, 255, 170), index, speed);
        }else if (vibor.is("Белая")) {
            return ColoringSystem.gradient(ColoringSystem.rgb(255, 255, 255), ColoringSystem.rgb(175, 176, 177), index, speed);
        }else if (vibor.is("Радужная")){
            return MathSystem.astolfo(5, (int) (index / 1.4f), 0.75F, 1, 1F);
        }else if (vibor.is("Астольфо")) {
            return ColoringSystem.gradient(ColoringSystem.rgb(243, 160, 232), ColoringSystem.rgb(171, 250, 243), index, speed);
        }else if (vibor.is("Кастомная")) {
            return ColoringSystem.gradient(color1.getValue(), color2.getValue(), index, speed);
        }

        return index;
    }
    public static int getColor(int index) {
        return viborTEM(index + 16);
    }

    public static int getColor(int index, float mult) {
        return viborTEM((int)((float)index * mult + 16.0F) + 16);
    }
    Dragging motionGraphics = AG.getInst().createDrag(this, "Motion Graphics", 8, 300);
    public Hud() {
        Dragging keyBinds = AG.getInst().createDrag(this, "KeyBinds", 185, 5);
        Dragging potions = AG.getInst().createDrag(this, "Potions", 278, 5);
       Dragging staffStatisticsz = AG.getInst().createDrag(this, "Staff List", 96, 5);
        Dragging targetHUD = AG.getInst().createDrag(this, "Target Hud", 74, 128);

        watermark = new WaterMark();
        arrayList = new ArrayList();
        hotBar = new HotBar();
        playerInfo = new PlayerInfo();
        keyBind = new KeyBinds(keyBinds);
        this.potions = new Potions(potions);
       staffList = new StaffList(staffStatisticsz);
        targetHud = new TargetHud(targetHUD);
        armorHud = new ArmorHud();

            this.addSettings(
                    this.elements, this.elementsWatermark, this. elementsPlayerInfo, this.targetHudmode, vibor, color1, color2);
    }

    public void drawMotionGraphics() {
        float posX = motionGraphics.getX();
        float posY = motionGraphics.getY();
        float width = motionGraphics.getWidth();
        float height = motionGraphics.getHeight();

//        GraphicsSystem.drawRoundedRect(posX - 0.5f, posY - 0.5f, width + 1, height + 1, 4, ColoringSystem.setAlpha(Hud.getColor(0), 255));
        GraphicsSystem.drawRoundedRect(posX, posY, width, height,3, ColoringSystem.rgba(20,20,20,255));

        AnimationNumbers animationNumbers = new AnimationNumbers();
        String bps = animationNumbers.BPSAnim(mc.player, true);
        ClientFonts.msSemiBold[14].drawString(new MatrixStack(), "BPS: " + bps, posX + 4, posY + 4, -1);

        GlStateManager.pushMatrix();

        BlomSystem.registerRenderCall(() -> {
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            GL11.glLineWidth(1);
            GL11.glBegin(GL11.GL_LINE_STRIP);

            for (int i = 0; i < motiongraph.size(); i++) {
                float alpha = (i * 4);

                int color = ColoringSystem.getColorBlack(i * 10);
                float red = ((color >> 16) & 0xFF) / 255F;
                float green = ((color >> 8) & 0xFF) / 255F;
                float blue = (color & 0xFF) / 255F;
                float alphaValue = alpha / 255F;

                GL11.glColor4f(red, green, blue, alphaValue);
                GL11.glVertex2d(posX + i + 0.5, posY + height - 4 - MathHelper.clamp(motiongraph.get(i), 0, motionGraphics.getHeight() - 15));
            }

            GL11.glEnd();

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
        });
        BlomSystem.draw(10, 2, false);

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(1);
        GL11.glBegin(GL11.GL_LINE_STRIP);

        for (int i = 0; i < motiongraph.size(); i++) {
            float alpha = (i * 4);

            int color = ColoringSystem.getColorBlack(i * 10);
            float red = ((color >> 16) & 0xFF) / 255F;
            float green = ((color >> 8) & 0xFF) / 255F;
            float blue = (color & 0xFF) / 255F;
            float alphaValue = alpha / 255F;

            GL11.glColor4f(red, green, blue, alphaValue);
            GL11.glVertex2d(posX + i + 0.5, posY + height - 4 - MathHelper.clamp(motiongraph.get(i), 0, motionGraphics.getHeight() - 15));
        }

        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GlStateManager.popMatrix();

        motionGraphics.setWidth(90);
        motionGraphics.setHeight(30);
        mc.gameRenderer.setupOverlayRendering();
    }
}