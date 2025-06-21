package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import minecraft.game.advantage.advisee.AuraUtil;
import minecraft.game.advantage.advisee.Interpolator;

import minecraft.game.display.clickgui.DropDown;
import minecraft.game.operation.movement.DDFRCM;
import minecraft.game.operation.wamost.massa.elements.*;
import minecraft.game.transactions.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SilverfishEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.passive.fish.CodEntity;
import net.minecraft.entity.passive.fish.PufferfishEntity;
import net.minecraft.entity.passive.fish.SalmonEntity;
import net.minecraft.entity.passive.fish.TropicalFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import minecraft.game.advantage.figures.MathSystem;
import minecraft.game.advantage.figures.GCDSensSystem;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.*;

import minecraft.game.advantage.make.engine2d.GraphicsSystem;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Module;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.system.AG;
import minecraft.system.managers.friend.FriendManager;

import static java.lang.Math.hypot;
import static net.minecraft.client.Minecraft.world;
import static net.minecraft.util.math.MathHelper.*;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@Defuse(name="Attack Аura",description = "Автоматические удары по сущности", brand = Category.Combat)
public class DDATTACK
        extends Module {
    private int hitnomiss;
    public final ModeSetting type = new ModeSetting(
            "Режим",
            "Grim Rotation",
            "Grim Rotation",
            "Smooth Rotation",
            "SpookyTime Rotation",
            "HolyWorld Rotation",
            "Advanced Rotation",
            "FunTime Rotation",
            "AI Rotation",
            "Custom Rotation"
    );
    private long spookyTimeLastChange = 0;
    private float spookyTimeTarget = 1.0f;
    private float spookyTimeStart = 0.5f;

    public CheckBoxSetting fovParent = new CheckBoxSetting("Ограничить фов удара", true).visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Снапы"));


    public CheckBoxSetting smoothBack = new CheckBoxSetting("Возвращать плавно", true).visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Пакетная"));
    public CheckBoxSetting fightBot = new CheckBoxSetting("Авто-пвп бот [BETA]", true).visibleIf(() -> !this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation"));
    final ModeSetting fightBotRejim = new ModeSetting("Режим ударов для бота", "Комба", "Комба", "Криты", "Смешивать", "Бежать на таргета [криты]", "Бежать на таргета [комба]", "Бежать на таргета [смеш..]")
            .visibleIf(() -> !this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation")&& this.fightBot.getValue());
    public CheckBoxSetting circle123 = new CheckBoxSetting("Отображать круг", true)
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Снапы") && this.fovParent.getValue());
    final ModeSetting aiming = new ModeSetting("Место наводки", "На голову", "На голову", "На шею", "На грудь", "На торс", "На ноги", "Автоматически").visibleIf(() -> this.type.is("Custom Rotation"));



    public CheckBoxSetting cameraabs = new CheckBoxSetting("Дергать камеру", true)
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Пакетная"));
    final SliderSetting yawABS = new SliderSetting("Сила YAW [abs]", 6.0f, 1.0f, 15.0f, 0.5f)
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Пакетная") && this.cameraabs.getValue());
    final SliderSetting pitchABS = new SliderSetting("Сила PITCH [abs]", 6.0f, 1.0f, 15.0f, 1f)
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Пакетная") && this.cameraabs.getValue());



    public CheckBoxSetting dopol = new CheckBoxSetting("Мультипоинты", true).visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Пакетная"));

    final SliderSetting minCPS = new SliderSetting("Минимум. CPS", 7.0f, 1.0f, 10.0f, 1.0f)
            .visibleIf(() -> !this.clickType.is("1.9") && this.type.is("Custom Rotation"));

    final SliderSetting maxCPS = new SliderSetting("Максимум. CPS", 10.0f, 1.0f, 20.0f, 1.0f)
            .visibleIf(() -> !this.clickType.is("1.9") && this.type.is("Custom Rotation"));

    final ModeSetting clickType = new ModeSetting("Режим пвп", "1.9", "1.8", "1.9").visibleIf(() -> this.type.is("Custom Rotation"));;

    public final ModeSetting strenghtMultiPoint = new ModeSetting(
            "Режим мультипоинтов",
            "Слабый",
            "Слабый",
            "Средний",
            "Сильный",
            "Кастомный"
    ).visibleIf(() -> this.type.is("Custom Rotation") && this.dopol.getValue()  && this.rejim.is("Пакетная"));

    final SliderSetting oneStrenghtYaw = new SliderSetting("Сила первого значения YAW", 6.0f, 0, 24.0f, 0.5f).visibleIf(() -> this.type.is("Custom Rotation") && this.dopol.getValue() && this.strenghtMultiPoint.is("Кастомный")  && this.rejim.is("Пакетная"));
    final SliderSetting twoStrenghtYaw = new SliderSetting("Сила второго значения YAW", 12.0f, 0, 24.0f, 0.5f).visibleIf(() -> this.type.is("Custom Rotation") && this.dopol.getValue()&& this.strenghtMultiPoint.is("Кастомный")  && this.rejim.is("Пакетная"));
    final SliderSetting oneStrenghtPitch = new SliderSetting("Сила первого значения PITCH", 6.0f, 0, 24.0f, 0.5f).visibleIf(() -> this.type.is("Custom Rotation")&& this.dopol.getValue() && this.strenghtMultiPoint.is("Кастомный")  && this.rejim.is("Пакетная"));
    final SliderSetting twoStrenghtPitch = new SliderSetting("Сила второго значения PITCH", 12.0f, 0, 24.0f, 0.5f).visibleIf(() -> this.type.is("Custom Rotation")&& this.dopol.getValue() && this.strenghtMultiPoint.is("Кастомный")  && this.rejim.is("Пакетная"));

    final SliderSetting preRange = new SliderSetting("Дистанция наводки", 0.3f, 0.0f, 16.0f, 0.1f)
            .visibleIf(() -> !this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation"));

    final SliderSetting fovValue = new SliderSetting("Значение фова", 35, 5f, 140, 1f).visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Снапы") && this.fovParent.getValue());

    final SliderSetting attackRange = new SliderSetting("Дистанция атаки", 3.0f, 2.5f, 6.0f, 0.1f);

    final SliderSetting miny = new SliderSetting("Плавность Y", 6.0f, 1.0f, 130, 1f)
            .visibleIf(() -> this.type.is("Custom Rotation"));
    final SliderSetting tick = new SliderSetting("Тики удара", 2.0f, 1.0f, 10.0f, 1.0f)
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Снапы"));
    //    final SliderSetting tickGRIM = new SliderSetting("Тики удара", 5.0f, 1.0f, 10.0f, 1.0f)
//            .visibleIf(() -> this.type.is("Grim Rotation"));
    final SliderSetting minxz = new SliderSetting("Плавность XZ", 6.0f, 1.0f, 180, 1f)
            .visibleIf(() -> this.type.is("Custom Rotation"));

    final SliderSetting posY = new SliderSetting("Позиция по Y", 1f, -1f, 1f, 0.01f)
            .visibleIf(() -> this.type.is("Custom Rotation") && !this.rejim.is("Снапы"));

    final ModeSetting rejim = new ModeSetting("Режим ротации", "Снапы", "Снапы", "Пакетная")
            .visibleIf(() -> this.type.is("Custom Rotation"));
    final ModeSetting resetSpeed = new ModeSetting("Возвращать", "Плавно", "Плавно", "Резко", "Никак")
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Снапы"));
    final SliderSetting resetSmooth = new SliderSetting("Плавность", 0.295F, 0.01F, 0.853f, 0.01F)
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Снапы") && this.resetSpeed.is("Плавно"));
    final ModeSetting speedpizda = new ModeSetting("Действие после удара", "Отводить", "Отводить", "Уменьшать плавность","Увеличивать плавность", "Опускать голову", "Поднимать голову" , "Сбрасывать pitch", "Сбрасывать yaw","Ничего")
            .visibleIf(() -> this.type.is("Custom Rotation") && this.rejim.is("Пакетная"));
    final ModeListSetting consider = new ModeListSetting("Учитывать",
            new CheckBoxSetting("Хп", true),
            new CheckBoxSetting("Броню", true),
            new CheckBoxSetting("Дистанцию", true),
            new CheckBoxSetting("Баффы", true)
    ).visibleIf(() -> (Boolean) this.options.is("Сортировать целей").getValue());

    final ModeListSetting targetValues = new ModeListSetting("Таргеты",
            new CheckBoxSetting("Игроки", true),
            new CheckBoxSetting("Голые игроки", true),
            new CheckBoxSetting("Мобы", false),
            new CheckBoxSetting("Животные", false),
            new CheckBoxSetting("Друзья", false),
            new CheckBoxSetting("Голые невидимки", true),
            new CheckBoxSetting("Невидимки", true)
    );


    final ModeListSetting options = new ModeListSetting("Опции",
            new CheckBoxSetting("Только криты", true),
            new CheckBoxSetting("Ломать щит", true),
            new CheckBoxSetting("Сортировать целей", true),
            new CheckBoxSetting("Нажимать шифт при ударе", true),
            new CheckBoxSetting("Отжимать щит", false),
            new CheckBoxSetting("Проверка на хитбокс", true),
            new CheckBoxSetting("Сохранять цель", true),
            new CheckBoxSetting("Коррекция движения", true),
            new CheckBoxSetting("Бить через стены", true),
            new CheckBoxSetting("Не бить если в гуи", true),
            new CheckBoxSetting("Не бить если кушаешь", false)
    );

    public CheckBoxSetting smartCrits = new CheckBoxSetting("Умные криты", false)
            .visibleIf(() -> this.options.is("Только криты").getValue());


    final ModeSetting correctionType = new ModeSetting("Коррекция движения", "Незаметный", "Незаметный", "Сфокусированный", "Адаптивная")

            .visibleIf(() -> (Boolean) this.options.is("Коррекция движения").getValue());

    final SliderSetting fixMoveValue = new SliderSetting("Дистанция адаптации", 1.0f, 0.1f, 2.0f, 0.1f)
            .visibleIf(() -> (Boolean) this.options.is("Коррекция движения").getValue() && this.correctionType.is("Адаптивная"));


    private final TimeCounterSetting TimeCounterSetting = new TimeCounterSetting();
    public static Vector2f rotateVector = new Vector2f(0, 0);
    public LivingEntity targetValue;
    private Entity selected;
    int ticks = 0;

    boolean isRotated = false;
    boolean isAttacking = false;
    boolean elytraTargetValue = false;
    boolean crystalAuraRule = true;
    boolean elytraTargetValueRule;
    float lastYaw;
    private int count;
    private long lastAttackTime = 0;
    float lastPitch;
    boolean pereletZnacheniy;
    public LivingEntity target;
    final DDPOTION autoPotion;
    private   boolean messageSent = false;
    private   boolean messageSentFight = false;
    private   boolean messageSentSP = false;

    double moreAttackDistanceOnElytra = 0.0;

    float aimDistance() {
        return !this.type.is("Grim Rotation") && !this.type.is("FunTime 360") && !this.type.is("FunTime Rotation") && !this.type.is("FunTime Legit") ? ((Float) this.preRange.getValue()).floatValue() : 0.0f;
    }

    float maxRange() {
        if (AG.getInst().getModuleManager().getPeregon().isEnabled()) {
            return this.attackDistance() + (DDATTACK.mc.player.isElytraFlying() ? ((Float) Peregon.elytraRange.getValue()).floatValue() : 0.0f) + this.aimDistance();
        } else if (!AG.getInst().getModuleManager().getPeregon().isEnabled()) {
            return this.attackDistance() + this.aimDistance();
        }
        return 0;
    }

    public DDATTACK(DDPOTION autoPotion) {
        this.autoPotion = autoPotion;
        this.addSettings(

                this.type,
                this.fightBot,
                this.fightBotRejim,
                this.minxz,
                this.miny,
                this.rejim,

                this.fovParent,
                this.circle123,
                this.fovValue,

                this.resetSpeed,
                this.resetSmooth,
                this.tick,
                this.speedpizda,



                this.aiming,
                this.preRange,
                this.attackRange,

                this.clickType,
                this.minCPS,
                this.maxCPS,
                this.smartCrits,
                this.smoothBack,
                this.dopol,
                this.strenghtMultiPoint,
                this.oneStrenghtYaw,
                this.twoStrenghtYaw,
                this.oneStrenghtPitch,
                this.twoStrenghtPitch,
                this.cameraabs,
                this.yawABS,
                this.pitchABS,

                this.targetValues,
                this.options,
                this.consider,
                this.correctionType,
                fixMoveValue
        );
    }
    int p;
    @Subscribe
    public void onInput(EventInput eventInput) {
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation") && this.fightBot.getValue()) {

        }
        else {
            if (((Boolean)this.options.is("Коррекция движения").getValue()).booleanValue() && this.correctionType.is("Незаметный") && this.crystalAuraRule) {
                MovementSystem.fixMovement(eventInput, this.rotateVector.x);
            }
        }
        if (this.type.is("Custom Rotation")) {
            if (this.dopol.getValue()) {
                if (!messageSent) {
                    print("Будьте осторожны при использовании мультипоинотов античит GRIM может задетектить");
                    messageSent = true;
                }
            } else {
                messageSent = false;
            }
        }

        if (this.type.is("AI Rotation")) {
            if (this.fightBot.getValue()) {
                if (!messageSentFight) {
                    print("Fight Bot находится в бета-тестировании и будет дорабатываться каждое обновление");
                    messageSentFight = true;
                }
            } else {
                messageSentFight = false;
            }
        }
        if (this.type.is("SpookyTime Rotation")) {
            if (!messageSentSP) {
                print("За баны с ротацией SpookyTime чит не несёт ответственности");
                messageSentSP = true;
            }
        } else {
            messageSentSP = false;
        }



        if (((Boolean)this.options.is("Коррекция движения").getValue()).booleanValue() && this.correctionType.is("Адаптивная") && this.crystalAuraRule) {
            if (getTarget() != null) {
                MovementSystem.fixMovementNoCorrection(eventInput, this.rotateVector.x, getTarget());
            }
            else {

            }
        }
        if (p > 0) {
            eventInput.setForward(0);
            p--;
        }
    }
    @Subscribe
    public void onRender(EventRender2D r) {
        int centerX = Minecraft.getInstance().getMainWindow().getScaledWidth() / 2;
        int centerY = Minecraft.getInstance().getMainWindow().getScaledHeight() / 2;
        if (this.fovParent.getValue()) {
            if (this.type.is("Custom Rotation") && this.rejim.is("Снапы")) {
                if (this.circle123.getValue()) {
                    GraphicsSystem.drawCircleWithFill(centerX, centerY, 360, 0, fovValue.getValue() * 4.3f, 0.01f, false, -1);
                }
            }
        }

    }


    private void attack() {

        if (getTarget() !=null && this.options.is("Нажимать шифт при ударе").getValue() && getTarget().getDistance(mc.player) <= this.attackRange.getValue())  {
            mc.gameSettings.keyBindSneak.setPressed(true);
        }
        int maxCPSValue;
        int minCPSValue = ((Float)this.minCPS.getValue()).intValue();
        if (minCPSValue > (maxCPSValue = ((Float)this.maxCPS.getValue()).intValue())) {
            maxCPSValue = minCPSValue;
        }

        int minMS = 1000 / maxCPSValue;
        int maxMS = 1000 / minCPSValue;
        Random random = new Random();
        int randomMS = random.nextInt(maxMS - minMS + 1) + minMS;
        this.getTimeCounterSetting().setLastMS(500L);
        if (this.type.is("Custom Rotation")) {
            this.getTimeCounterSetting().setLastMS(this.clickType.is("1.9") ? 500L : (long)randomMS);
        }
        DDCRIT.cancelCrit = true;
        if (AG.getInst().getModuleManager().getDdcrit().isEnabled() && DDCRIT.canUseDDCRIT()) {
            AG.getInst().getModuleManager().getDdcrit().sendCrit();
        }

        DDATTACK.mc.playerController.attackEntity(DDATTACK.mc.player, this.targetValue);
        DDATTACK.mc.player.swingArm(Hand.MAIN_HAND);
        DDCRIT.cancelCrit = false;


    }



    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (getTarget() !=null && this.options.is("Нажимать шифт при ударе").getValue() && getTarget().getDistance(mc.player) <= this.attackRange.getValue())  {
            mc.gameSettings.keyBindSneak.setPressed(false);
        }


        if (!this.crystalAuraRule) {
            return;
        }
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation") && this.fightBot.getValue() && this.fightBotRejim.is("Комба")) {

            if (getTarget() !=null) {
                if (mc.player.getDistance(getTarget()) > this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindJump.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(false);
                    mc.gameSettings.keyBindJump.setPressed(false);
                }


                if (mc.player.getDistance(getTarget()) > 1.9f) {
                    mc.gameSettings.keyBindForward.setPressed(true);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
            }
        }
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation") && this.fightBot.getValue() && this.fightBotRejim.is("Бежать на таргета [комба]")) {

            if (getTarget() !=null) {
                if (mc.player.getDistance(getTarget()) > this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindJump.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(false);
                    mc.gameSettings.keyBindJump.setPressed(false);
                }

                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue()) {
                    mc.gameSettings.keyBindJump.setPressed(false);
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindForward.setPressed(true);
                }

                if (mc.player.getDistance(getTarget()) > 1.9f) {
                    mc.gameSettings.keyBindForward.setPressed(true);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
            }
        }
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation") && this.fightBot.getValue() && this.fightBotRejim.is("Криты")) {

            if (getTarget() !=null) {
                if (mc.player.getDistance(getTarget()) > this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindJump.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(false);
                    mc.gameSettings.keyBindJump.setPressed(false);
                }

                if (mc.player.getDistance(getTarget()) < 1.9f) {
                    mc.gameSettings.keyBindForward.setPressed(false);

                    if (Math.random() > 0.3F) {
                        mc.gameSettings.keyBindRight.setPressed(false);
                        mc.gameSettings.keyBindLeft.setPressed(true);

                    }
                    else {
                        mc.gameSettings.keyBindLeft.setPressed(false);
                        mc.gameSettings.keyBindRight.setPressed(true);
                    }

                }
                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue()) {
                    mc.gameSettings.keyBindJump.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) > 1.9f) {
                    mc.gameSettings.keyBindForward.setPressed(true);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
            }
        }
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation") && this.fightBot.getValue() && this.fightBotRejim.is("Бежать на таргета [криты]")) {

            if (getTarget() !=null) {

                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue()) {
                    mc.gameSettings.keyBindJump.setPressed(true);
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindForward.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) > 1.9f) {

                    mc.gameSettings.keyBindForward.setPressed(true);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
            }
        }
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation") && this.fightBot.getValue() && this.fightBotRejim.is("Бежать на таргета [смеш..]")) {

            if (getTarget() !=null) {
                if (mc.player.getDistance(getTarget()) > this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindJump.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(false);
                    mc.gameSettings.keyBindJump.setPressed(false);
                }
                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue()) {
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindForward.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) > 1.9f) {
                    mc.gameSettings.keyBindForward.setPressed(true);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
                if (Math.random() < 0.08F) {
                    mc.gameSettings.keyBindJump.setPressed(true);
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            mc.gameSettings.keyBindJump.setPressed(false);
                        } catch (InterruptedException ignored) {}
                    }).start();
                }
            }
        }
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation") && this.fightBot.getValue() && this.fightBotRejim.is("Смешивать")) {

            if (getTarget() !=null) {
                if (mc.player.getDistance(getTarget()) > this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(true);
                    mc.gameSettings.keyBindJump.setPressed(true);
                }
                if (mc.player.getDistance(getTarget()) < this.attackRange.getValue() + 0.3f) {
                    mc.gameSettings.keyBindSprint.setPressed(false);
                    mc.gameSettings.keyBindJump.setPressed(false);
                }

                if (mc.player.getDistance(getTarget()) < 1.9f) {
                    mc.gameSettings.keyBindForward.setPressed(false);

                    if (Math.random() > 0.3F) {
                        mc.gameSettings.keyBindRight.setPressed(false);
                        mc.gameSettings.keyBindLeft.setPressed(true);

                    }
                    else {
                        mc.gameSettings.keyBindLeft.setPressed(false);
                        mc.gameSettings.keyBindRight.setPressed(true);
                    }

                }
                if (mc.player.getDistance(getTarget()) > 1.9f) {
                    mc.gameSettings.keyBindForward.setPressed(true);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindLeft.setPressed(false);
                    mc.gameSettings.keyBindRight.setPressed(false);
                }
                if (Math.random() < 0.08F) {
                    mc.gameSettings.keyBindJump.setPressed(true);
                    new Thread(() -> {
                        try {
                            Thread.sleep(100);
                            mc.gameSettings.keyBindJump.setPressed(false);
                        } catch (InterruptedException ignored) {}
                    }).start();
                }
            }
        }




        if (((Boolean)this.options.is("Сохранять цель").getValue()).booleanValue() &&
                (this.targetValue == null || !this.isValid(this.targetValue)) ||
                !((Boolean)this.options.is("Сохранять цель").getValue()).booleanValue()) {
            this.updateTargetValue();
        }
        if (this.type.is("Custom Rotation") && this.rejim.is("Пакетная")) {
            if (this.smoothBack.getValue()) {
                if (this.targetValue == null) {
                    this.smoothReset();
                }
            }
            if (!this.smoothBack.getValue()) {
                if (this.targetValue == null) {
                    this.reset();
                }
            }
        }
        else {
            if (this.targetValue == null) {
                this.reset();
            }
        }
        if (!(this.targetValue == null || this.autoPotion.isEnabled() && this.autoPotion.isActive())) {
            this.isRotated = false;

            if (this.shouldPlayerFalling() && this.TimeCounterSetting.hasTimeElapsed()) {
                if (this.type.is("Grim Rotation")) {
                    this.ticks = 5;
                }

                else if (this.type.is("FunTime Rotation")){
                    this.ticks = 1;
                }
                else if (this.type.is("Custom Rotation") || this.rejim.is("Снапы")){
                    this.ticks = ((Float)this.tick.getValue()).intValue();
                }
                this.forceAttack();
            }


            if (this.type.is("Grim Rotation")) {
                if (ticks > 0 || mc.player.isElytraFlying()) {
                    setRotate();
                    ticks--;
                    --this.ticks;
                } else {
                    this.reset();
                }
            }
            else if (this.type.is("FunTime Rotation")) {
                if (ticks > 0 || mc.player.isElytraFlying()) {
                    setRotate();
                    ticks--;
                    --this.ticks;
                } else {
                    this.smoothReset();

                }
            }

            else if (this.type.is("Custom Rotation") && this.rejim.is("Снапы")) {
                if (!this.fovParent.getValue()) {

                    if (ticks > 0 || mc.player.isElytraFlying()) {
                        this.setRotate();
                        --this.ticks;
                    } else {
                        if (this.resetSpeed.is("Плавно")) {
                            this.smoothReset();
                        }
                        if (this.resetSpeed.is("Резко")) {
                            this.reset();
                        }
                        if (this.resetSpeed.is("Никак")) {
                        }
                    }
                }
                if (this.fovParent.getValue()) {

                    if (ticks > 0 && LookTarget(getTarget(), mc.player.isElytraFlying())) {
                        this.setRotate();
                        --this.ticks;
                    } else {
                        if (this.resetSpeed.is("Плавно")) {
                            this.smoothReset();
                        }
                        if (this.resetSpeed.is("Резко")) {
                            this.reset();
                        }
                        if (this.resetSpeed.is("Никак")) {
                        }
                    }
                }
            }

            else if (!this.isRotated) {
                this.setRotate();
            }
        } else {
            this.TimeCounterSetting.setLastMS(0L);
            smoothReset();
        }

    }


    @Subscribe
    private void onWalking(EventMotion e) {
        Entity target = getTarget();

        e.setYaw(this.rotateVector.x);
        float yaw = this.rotateVector.x;
        DDATTACK.mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = PlayerSettingsModule.calculateCorrectYawOffset(yaw);
        mc.player.rotationPitchHead = rotateVector.y;
        float finalPitch;
//        if (this.type.is("Custom Rotation")) {
//
//        } else {
//            if (target != null) {
//                if (this.type.is("FunTime Rotation")) {
//                    double dx = target.getPosX() - mc.player.getPosX();
//                    double dz = target.getPosZ() - mc.player.getPosZ();
//                    double distanceXZ = Math.sqrt(dx * dx + dz * dz);
//
//                    distanceXZ = Math.min(Math.max(distanceXZ, 0), 3);
//
//                    finalPitch = (float) interpolate(distanceXZ, 0, 3, 75, 15);
//                    e.setPitch(finalPitch);
//
//                } else {
//                    finalPitch = this.rotateVector.y;
//                    e.setPitch(finalPitch);
//                }
//
//
//                double dx = target.getPosX() - mc.player.getPosX();
//                double dy = (target.getPosY() + target.getEyeHeight()) - (mc.player.getPosY() + mc.player.getEyeHeight());
//                double dz = target.getPosZ() - mc.player.getPosZ();
//
//                float yawToTarget = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90F;
//                float pitchToTarget = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
//
//                mc.player.rotationYawHead = yawToTarget;
//                mc.player.renderYawOffset = PlayerSettingsModule.calculateCorrectYawOffset(yawToTarget);
//                mc.player.rotationPitchHead = pitchToTarget;
//            }
//        }
//    }
    }

    private double interpolate(double value, double minIn, double maxIn, double minOut, double maxOut) {
        double t = (value - minIn) / (maxIn - minIn);
        return minOut + t * (maxOut - minOut);
    }

    public void setRotate() {
        this.elytraTargetValueRule = DDATTACK.mc.player.isElytraFlying() && this.targetValue.isElytraFlying();
        if (this.type.is("Grim Rotation")) {
            this.updateRotation(2.14748365E9f, 2.14748365E9f);

        } else {

            this.updateRotation(9999.0f, 9999.0f);
        }
    }


    private boolean LookTarget(LivingEntity target, boolean elytraFlying) {
        Vector3d playerDirection = mc.player.getLook(-1.0F);
        Vector3d targetDirection = target.getPositionVec().subtract(mc.player.getPositionVec()).normalize();
        double angle = Math.toDegrees(Math.acos(playerDirection.dotProduct(targetDirection)));
        return angle <= this.fovValue.getValue();
    }

    public float attackDistance() {
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) {
        }
        return (float)((double)((Float)this.attackRange.getValue()).floatValue() + this.moreAttackDistanceOnElytra);
    }

    private void updateTargetValue() {
        ArrayList<LivingEntity> targetValues = new ArrayList<LivingEntity>();
        for (Entity entity2 : world.getAllEntities()) {
            LivingEntity living;
            if (!(entity2 instanceof LivingEntity) || !this.isValid(living = (LivingEntity)entity2)) continue;
            targetValues.add(living);
        }
        if (targetValues.isEmpty()) {
            this.targetValue = null;
            return;
        }
        if (targetValues.size() == 1) {
            this.targetValue = (LivingEntity)targetValues.get(0);
            return;
        }
        targetValues.sort(Comparator.comparingDouble(entity -> MathSystem.entity(entity, (Boolean)this.consider.is("Хп").getValue(), (Boolean)this.consider.is("Броню").getValue(), (Boolean)this.consider.is("Дистанцию").getValue(), this.maxRange(), (Boolean)this.consider.is("Баффы").getValue())));
        this.targetValue = (LivingEntity)targetValues.get(0);
    }

    private float lerp(float start, float end, float speed) {
        return start + (end - start) * speed;
    }
    private float getRandomYOffset() {
        float[] possibleValues = {-1f, -1.3f, 1.5f, 1.7f, 1.9f};
        return possibleValues[(int) (Math.random() * possibleValues.length)];
    }

    @Subscribe
    private void onEventKey(EventKey e) {

    }

    private float shakeYawOffset = 0f;
    private boolean shakingRight = true;
    private float maxShake = 35f;
    private float shakeSpeed = 21f;
    private void updateRotation(float rotationYawSpeed, float rotationPitchSpeed) {
        float f;
        float scaleFactor = 1.5f;

        if (this.elytraTargetValueRule) {
            float peregonValue = 0.0f;

            if (Peregon.shouldPeregon) {
                peregonValue = Peregon.elytraperegon.getValue();
            } else {

            }

            Vector3d forward = this.targetValue.getForward().normalize();
            Vector3d forwardOffset = forward.scale(peregonValue);
            Vector3d newTargetPosition = this.targetValue.getPositionVec()
                    .add(forwardOffset)
                    .add(0.0, clamp(this.targetValue.getPosY() - (double) this.targetValue.getHeight(), 0.0, (double) (this.targetValue.getHeight() / 2.0f)), 0.0);

            Vector3d vec = newTargetPosition.subtract(DDATTACK.mc.player.getEyePosition(1.0f));

            float yawToTargetValue = (float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
            float pitchToTargetValue = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));

            float yawDelta = wrapDegrees(yawToTargetValue - this.rotateVector.x);
            float pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);

            switch ((String) this.type.getValue()) {
                case "Smooth Rotation":
                case "Grim Rotation":
                case "FunTime Rotation":
                case "AI Rotation":
                case "Custom Rotation":
                case "Advanced Rotation": {
                    float yaw = this.rotateVector.x + yawDelta;
                    float pitch = clamp(this.rotateVector.y + pitchDelta, -90.0f, 89);
                    float gcd = GCDSensSystem.getGCDValue();
                    yaw -= (yaw - this.rotateVector.x) % gcd;
                    pitch -= (pitch - this.rotateVector.y) % gcd;
                    this.rotateVector = new Vector2f(yaw, pitch);
                    break;
                }
            }
        } else {
            Vector3d vec = this.targetValue.getPositionVec().add(0.0, clamp(this.targetValue.getPosY() - (double) this.targetValue.getHeight(), 0.0, (double) (this.targetValue.getHeight() / 2.0f)), 0.0).subtract(DDATTACK.mc.player.getEyePosition(1.0f));
            float yawToTargetValue = (float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
            float pitchToTargetValue = (float)(-Math.toDegrees(Math.atan2(vec.y , hypot(vec.x, vec.z))));
            float pitchToTargetValueNEW = (float)(-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));
            float yawDelta = wrapDegrees(yawToTargetValue - this.rotateVector.x);
            float pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
            float pitchDeltaNEW = wrapDegrees(pitchToTargetValueNEW - this.rotateVector.y);
            float randomyaw = MathSystem.random(0.3F, 2.5f);
            int roundedYaw = (int) yawDelta;
            switch ((String) this.type.getValue()) {
                case "FunTime Rotation": {
                    maxShake = 0;
                    pitchToTargetValue = (float)(-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));
                    float yaw = Math.min(Math.max(Math.abs(yawDelta), 0), rotationYawSpeed);
                    float pitch = Math.min(Math.max(Math.abs(pitchDelta), 0), 0.1f);
                    float yaw33 = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);
                    float gcd = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);
                    pitch = clamp(this.rotateVector.y + (pitchDelta > 0.0f ? pitch : -pitch), -90, 90);
                    gcd -= (yaw33 - this.rotateVector.x) % GCDSensSystem.getGCDValue();
                    pitch -= (pitch - this.rotateVector.y) % GCDSensSystem.getGCDValue();

                    this.rotateVector = new Vector2f(gcd, pitch);
                    this.lastYaw = yaw33;
                    this.lastPitch = pitch;
                    maxShake = 40;
                    break;

                }

                case "Smooth Rotation": {
                    Vector3d vec1 = AuraUtil.getBestVector(getTarget());

                    pitchToTargetValue = (float)(-Math.toDegrees(Math.atan2(vec1.y, hypot(vec1.x, vec1.z))));
                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
                    float yaw = Math.min(Math.max(Math.abs(yawDelta), 0), rotationYawSpeed);
                    float pitch = Math.max(Math.abs(pitchDelta), 0);
                    float yaw33 = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -(yaw));
                    float gcd = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);
                    pitch = clamp(this.rotateVector.y + (pitchDelta > 0.0f ? pitch : -pitch), -90, 90);
                    gcd -= (yaw33 - this.rotateVector.x) % GCDSensSystem.getGCDValue();
                    pitch -= (pitch - this.rotateVector.y) % GCDSensSystem.getGCDValue();
                    this.rotateVector = new Vector2f(gcd, pitch);
                    this.lastYaw = yaw33;
                    this.lastPitch = pitch;
                    break;
                }
                case "Grim Rotation": {
                    float yaw = this.rotateVector.x + (float) roundedYaw;
                    float pitch = clamp(this.rotateVector.y + pitchDelta, -90, 90);
                    float gcd = GCDSensSystem.getGCDValue();
                    yaw -= (yaw - this.rotateVector.x) % gcd;
                    pitch -= (pitch - this.rotateVector.y) % gcd;
                    this.rotateVector = new Vector2f(yaw, pitch);
                    break;
                }
                case "Custom Rotation": {
                    Random random = new Random();
                    long currentTime = System.currentTimeMillis();
                    long lastUpdateTime = 0;
                    float targetYOffset = 0.35f;
                    float currentYOffset = 0.35f;
                    float UPDATE_INTERVAL = 700;
                    float currentYawOffset = 0.35f;

                    if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                        targetYOffset = getRandomYOffset();
                        lastUpdateTime = currentTime;
                    }


                    float stepSize = 0.1f;
                    currentYOffset = lerp(currentYOffset, targetYOffset, stepSize);
                    currentYawOffset = lerp(currentYawOffset, targetYOffset, stepSize);


                    float aimingValue = 0;
                    if (this.aiming.is("На шею")) {
                        aimingValue = -0.39f;
                    } else if (this.aiming.is("На грудь")) {
                        aimingValue = -0.1f;
                    } else if (this.aiming.is("На ноги")) {
                        aimingValue = 0.58f;
                    } else if (this.aiming.is("На торс")) {
                        aimingValue = 0.36f;
                    } else if (this.aiming.is("На голову")) {
                        aimingValue = -0.74f;
                    }


                    if (this.aiming.is("Автоматически")) {
                        if (mc.player.getDistance(getTarget()) > 3) {
                            aimingValue = -0.3f;
                        }
                        if (mc.player.getDistance(getTarget()) < 2) {
                            aimingValue = 0f;
                        }
                    }

                    pitchToTargetValue = (float)(-Math.toDegrees(Math.atan2(vec.y +- aimingValue, hypot(vec.x, vec.z))));
                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
                    yawToTargetValue = (float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
                    yawDelta = wrapDegrees(yawToTargetValue - this.rotateVector.x);
                    if (getTarget() !=null) {
                        if (getTarget().getDistance(mc.player) < this.attackRange.getValue() - 0.5f) {
                            if (this.speedpizda.is("Опускать голову") && this.rejim.is("Пакетная")) {
                                if (mc.player.fallDistance > 0.35) {
                                    pitchToTargetValue = (float)(-Math.toDegrees(Math.atan2(vec.y - 1.634, hypot(vec.x, vec.z))));
                                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
                                    yawToTargetValue = (float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x) - 0.654f) - 90.0);
                                    yawDelta = wrapDegrees(yawToTargetValue - this.rotateVector.x);
                                }
                            }
                        }
                    }
                    if (getTarget() !=null) {
                        if (getTarget().getDistance(mc.player) < this.attackRange.getValue() - 0.5f) {
                            if (this.speedpizda.is("Поднимать голову") && this.rejim.is("Пакетная")) {
                                if (mc.player.fallDistance > 0.35) {
                                    pitchToTargetValue = (float)(-Math.toDegrees(Math.atan2(vec.y + 1.634, hypot(vec.x, vec.z))));
                                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
                                    yawToTargetValue = (float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x) + 0.654f) - 90.0);
                                    yawDelta = wrapDegrees(yawToTargetValue - this.rotateVector.x);
                                }
                            }
                        }
                    }


                    float cos = (float) Math.cos(currentTime / 100D);
                    float sin = (float) Math.sin(currentTime / 100D);
                    float yaw = (float) Math.toDegrees(Math.atan2(-vec.x, vec.z));
                    float pitch = (float) Math.toDegrees(Math.atan2(-vec.y, hypot(vec.x, vec.z)));
                    float pidofi = 0;
                    float pidofil = 0;

                    if (this.cameraabs.getValue()) {
                        pidofi = this.yawABS.getValue();
                        pidofil = this.pitchABS.getValue();
                    }

                    if (!this.dopol.getValue()) {

                        yaw = Math.min(Math.max(Math.abs(yawDelta), pidofi), this.minxz.getValue());
                        pitch = Math.min(Math.max(Math.abs(pitchDelta), pidofil), this.miny.getValue());
                    }
                    if (getTarget() !=null) {
                        if (getTarget().getDistance(mc.player) < this.attackRange.getValue() - 0.5f) {
                            if (this.speedpizda.is("Уменьшать плавность") && this.rejim.is("Пакетная") && !this.dopol.getValue()) {
                                if (mc.player.fallDistance > 0.05) {
                                    yaw = Math.min(Math.max(Math.abs(yawDelta), pidofi), 8);
                                    pitch = Math.min(Math.max(Math.abs(pitchDelta), pidofil), 8);
                                }
                            }
                        }
                    }
                    if (getTarget() !=null) {
                        if (getTarget().getDistance(mc.player) < this.attackRange.getValue() - 0.5f) {
                            if (this.speedpizda.is("Увеличивать плавность") && this.rejim.is("Пакетная") && !this.dopol.getValue()) {
                                if (mc.player.fallDistance > 0.05) {
                                    yaw = Math.min(Math.max(Math.abs(yawDelta), pidofi), this.minxz.getValue() + 25);
                                    pitch = Math.min(Math.max(Math.abs(pitchDelta), pidofil), this.miny.getValue() + 7);
                                }
                            }
                        }
                    }
                    if (getTarget() !=null) {
                        if (getTarget().getDistance(mc.player) < this.attackRange.getValue() - 0.5f) {
                            if (this.speedpizda.is("Сбрасывать yaw") && this.rejim.is("Пакетная") && !this.dopol.getValue()) {
                                if (mc.player.fallDistance > 0.05) {
                                    yaw = Math.min(Math.max(Math.abs(yawDelta), pidofi), 0);
                                }
                            }
                        }
                    }

                    float yaw33 = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -(yaw));
                    float gcd = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);

                    if (dopol.getValue()) {

                        if (this.strenghtMultiPoint.is("Слабый")) {
                            yaw = yaw + randomLerp(2, 4) * cos + (random.nextFloat() * 2 - 1) * 3;
                            pitch = pitch + randomLerp(2, 4) * sin + (random.nextFloat() * 2 - 1) * 2;
                        } else if (this.strenghtMultiPoint.is("Средний")) {
                            yaw = yaw + randomLerp(6, 12) * cos + (random.nextFloat() * 2 - 1) * 3;
                            pitch = pitch + randomLerp(6, 12) * sin + (random.nextFloat() * 2 - 1) * 2;
                        } else if (this.strenghtMultiPoint.is("Сильный")) {
                            yaw = yaw + randomLerp(10, 20) * cos + (random.nextFloat() * 2 - 1) * 3;
                            pitch = pitch + randomLerp(10, 20) * sin + (random.nextFloat() * 2 - 1) * 2;
                        } else if (this.strenghtMultiPoint.is("Кастомный")) {
                            yaw = yaw + randomLerp(this.oneStrenghtYaw.getValue(), this.twoStrenghtYaw.getValue()) * cos + (random.nextFloat() * 2 - 1) * 3;
                            pitch = pitch + randomLerp(this.oneStrenghtPitch.getValue(), this.twoStrenghtPitch.getValue()) * sin + (random.nextFloat() * 2 - 1) * 2;
                        } else {
                            yaw = yaw;
                            pitch = pitch;
                        }
                    }

                    if (getTarget() !=null) {
                        if (getTarget().getDistance(mc.player) < this.attackRange.getValue() - 0.5f) {
                            if (this.speedpizda.is("Отводить") && !this.rejim.is("Снапы")) {
                                if (mc.player.fallDistance > 0.05) {
                                    float[] possibleValues1 = {-45, -35, -30, -25, -15, 0, 15, 25f, 30, 35, 45};
                                    int index = (int) ((System.currentTimeMillis() / 100) % possibleValues1.length);
                                    gcd = this.rotateVector.x + possibleValues1[index];
                                }
                            }
                        }
                    }

                    if (getTarget() !=null) {
                        if (getTarget().getDistance(mc.player) < this.attackRange.getValue() - 0.5f) {
                            if (this.speedpizda.is("Сбрасывать pitch") && this.rejim.is("Пакетная") && !this.dopol.getValue()) {
                                if (mc.player.fallDistance >= 0 && !mc.player.isOnGround()) {
                                    pitch = Math.min(Math.max(Math.abs(pitchDelta), pidofil), 0);
                                }
                            }
                        }
                    }


                    if (dopol.getValue()) {
                        pitch = clamp(pitch, -90, 90); // <-- вот эта строка добавлена
                        this.rotateVector = new Vector2f(yaw, pitch);
                    }

                    if (!dopol.getValue()) {
                        pitch = clamp(this.rotateVector.y + (pitchDelta > 0.0f ? pitch : -pitch), -76, 76);
                        gcd -= (yaw33 - this.rotateVector.x) % GCDSensSystem.getGCDValue();
                        pitch -= (pitch - this.rotateVector.y) % GCDSensSystem.getGCDValue();
                    }



                    if (dopol.getValue()) {
                        this.rotateVector = new Vector2f(yaw, pitch);
                    }
                    if (!dopol.getValue()) {
                        this.rotateVector = new Vector2f(gcd, pitch);
                    }
                    this.lastYaw = yaw33;
                    this.lastPitch = pitch;
                    break;
                }
                case "AI Rotation": {
                    Vector3d vec1 = AuraUtil.getBestVector(getTarget());

                    pitchToTargetValue = (float) (-Math.toDegrees(Math.atan2(vec1.y, hypot(vec1.x, vec1.z))));
                    yawToTargetValue = (float) Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0F;

                    yawDelta = wrapDegrees(yawToTargetValue - this.rotateVector.x);

                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);

                    float yaw = Math.min(Math.max(Math.abs(yawDelta), 0), 43);
                    float pitch = Math.min(Math.max(Math.abs(pitchDelta), 0), 15);

                    float yawFinal = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);


                    float pitchFinal = clamp(this.rotateVector.y + (pitchDelta > 0.0f ? pitch : -pitch), -89.0f, 89);

                    yawFinal -= (yawFinal - this.rotateVector.x) % GCDSensSystem.getGCDValue();
                    pitchFinal -= (pitchFinal - this.rotateVector.y) % GCDSensSystem.getGCDValue();

                    this.rotateVector = new Vector2f(yawFinal, pitchFinal);
                    this.lastYaw = yawFinal;
                    this.lastPitch = pitchFinal;
                    break;
                }
                case "SpookyTime Rotation": {
                    long currentTime = System.currentTimeMillis();
                    if (spookyTimeLastChange == 0) spookyTimeLastChange = currentTime;
                    float smoothMultiPitchY = lerp(spookyTimeStart, spookyTimeTarget,
                            Math.min(1.0f, (currentTime - spookyTimeLastChange) / 500.0f));
                    if (currentTime - spookyTimeLastChange > 500) {
                        spookyTimeLastChange = currentTime;
                        spookyTimeTarget = MathSystem.random(-1.0f, 1f);
                        spookyTimeStart = smoothMultiPitchY;
                    }


                    pitchToTargetValue = (float) (-Math.toDegrees(Math.atan2(vec.y + smoothMultiPitchY, hypot(vec.x, vec.z))));
                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
                    float yaw = Math.min(Math.max(Math.abs(yawDelta), 0), MathSystem.random(40, 70));
                    float pitch = Math.min(Math.max(Math.abs(pitchDelta), 0), MathSystem.random(5, 20));
                    float yaw33 = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);
                    float gcd = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);
                    float finalPitch = 0;

                    double xzDistance = Math.hypot(this.getTarget().getPosX() - mc.player.getPosX(), this.getTarget().getPosZ() - mc.player.getPosZ());
                    finalPitch = clamp(this.rotateVector.y + (pitchDelta > 0.0f ? pitch : -pitch), xzDistance < 1.7f ? -90 : -90, 90);

                    gcd -= (yaw33 - this.rotateVector.x) % GCDSensSystem.getGCDValue();
                    this.rotateVector = new Vector2f(gcd, finalPitch);
                    this.lastYaw = yaw33;
                    this.lastPitch = pitch;
                    break;
                }
                case "HolyWorld Rotation": {
                    pitchToTargetValue = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));
                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
                    float[] possibleValues = {35};
                    float randomyaw1 = MathSystem.random(0.5f, 2f);
                    float yaw = Math.min(Math.max(Math.abs(yawDelta), randomyaw1), 55);
                    float pitch = Math.min(Math.max(Math.abs(pitchDelta), randomyaw), 0    );
                    float yaw33 = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -(yaw));
                    float gcd = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);
                    if (getTimeCounterSetting().isReached(1)) {
                        pitch = Math.min(Math.max(Math.abs(pitchDelta), randomyaw), possibleValues[(int) MathSystem.random(0, possibleValues.length - 1)]);
                    }
//                    int down = 35;
//                    if (getTarget().getDistance(mc.player) > 2) {
//                        down = 90;
//                    }

                    pitch = clamp(this.rotateVector.y + (pitchDelta > 0.0f ? pitch : -pitch), -89.0f, 89);
                    gcd -= (yaw33 - this.rotateVector.x) % GCDSensSystem.getGCDValue();
                    pitch -= (pitch - this.rotateVector.y) % GCDSensSystem.getGCDValue();
                    this.rotateVector = new Vector2f(gcd, pitch);
                    this.lastYaw = yaw33;
                    this.lastPitch = pitch;
                    break;
                }
                case "Advanced Rotation": {
                    pitchToTargetValue = (float) (-Math.toDegrees(Math.atan2(vec.y + 0.3f, hypot(vec.x, vec.z))));
                    pitchDelta = wrapDegrees(pitchToTargetValue - this.rotateVector.y);
                    float[] possibleValues = {35};
                    float yaw = Math.min(Math.max(Math.abs(yawDelta), randomyaw), 45);
                    float pitch = Math.min(Math.max(Math.abs(pitchDelta), randomyaw), 0);
                    if (getTimeCounterSetting().isReached(-150)) {
                        pitch = Math.min(Math.max(Math.abs(pitchDelta), randomyaw), possibleValues[(int) MathSystem.random(0, possibleValues.length - 1)]);
                    }
                    float yaw33 = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -(yaw));
                    float gcd = this.rotateVector.x + (yawDelta > 0.0f ? yaw : -yaw);
                    pitch = clamp(this.rotateVector.y + (pitchDelta > 0.0f ? pitch : -pitch), -89.0f, 89);
                    gcd -= (yaw33 - this.rotateVector.x) % GCDSensSystem.getGCDValue();
                    pitch -= (pitch - this.rotateVector.y) % GCDSensSystem.getGCDValue();
                    this.rotateVector = new Vector2f(gcd, pitch);
                    this.lastYaw = yaw33;
                    this.lastPitch = pitch;
                    break;
                }
            }
        }
        if (((Boolean)this.options.is("Коррекция движения").getValue()).booleanValue()) {
            DDATTACK.mc.player.rotationYawOffset = this.rotateVector.x;
        }
    }

    private float calculateYawToTarget(Entity target) {
        Vector3d playerEyes = Minecraft.getInstance().player.getEyePosition(1.0f);
        Vector3d targetPos = target.getPositionVec().add(0, target.getHeight() / 2, 0);
        Vector3d vec = targetPos.subtract(playerEyes);
        return (float) wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
    }

    private float calculatePitchToTarget(Entity target) {
        Vector3d playerEyes = Minecraft.getInstance().player.getEyePosition(1.0f);
        Vector3d targetPos = target.getPositionVec().add(0, target.getHeight() / 2, 0);
        Vector3d vec = targetPos.subtract(playerEyes);
        return (float)(-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));
    }
    private void forceAttack() {
        if (!this.isAttacking && !this.canAttack()) {
            return;
        }
        this.isAttacking = true;
        try {
            this.preAttack();
            this.attack();
            this.postAttack();

        } finally {
            this.isAttacking = false;
        }
    }
    public float cooldownFromLastSwing() {
        return clamp(mc.player.ticksSinceLastSwing / randomLerp(8, 12), 0.0F, 1.0F);
    }
    public float randomLerp(float min, float max) {
        return Interpolator.lerp(max, min, new SecureRandom().nextFloat());
    }
    private boolean canAttack() {

        this.selected = MouseManager.getMouseOver(this.targetValue, this.rotateVector.x, this.rotateVector.y, this.attackDistance());
        if (DDATTACK.mc.player.getDistanceEyePos(this.targetValue) > (double)this.attackDistance()) {
            return false;
        }
        if (((Boolean)this.options.is("Проверка на хитбокс").getValue()).booleanValue() && !this.elytraTargetValueRule && this.selected == null) {
            return false;
        }
        if (!((Boolean)this.options.is("Бить через стены").getValue()).booleanValue() && !DDATTACK.mc.player.canEntityBeSeen(this.targetValue)) {
            return false;
        }
        if (((Boolean)this.options.is("Не бить если кушаешь").getValue()).booleanValue()) {
            if (mc.player.isHandActive() && (mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT || mc.player.getHeldItemMainhand().getUseAction() == UseAction.EAT)) {
                return false;
            }
        }
        return (Boolean)this.options.is("Не бить если в гуи").getValue() == false || DDATTACK.mc.currentScreen == null || DDATTACK.mc.currentScreen instanceof DropDown || DDATTACK.mc.currentScreen instanceof ChatScreen || DDATTACK.mc.currentScreen instanceof IngameMenuScreen;
    }

    private void preAttack() {

        if (DDATTACK.mc.player.isBlocking() && ((Boolean)this.options.is("Отжимать щит").getValue()).booleanValue()) {
            DDATTACK.mc.playerController.onStoppedUsingItem(DDATTACK.mc.player);
        }
    }


    private void postAttack() {

        LivingEntity livingEntity = this.targetValue;
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)livingEntity;
            if (((Boolean)this.options.is("Ломать щит").getValue()).booleanValue()) {
                this.breakShieldPlayer(player);
            }
        }
    }

    public boolean shouldPlayerFalling() {
        AttackSystem attackSystem = new AttackSystem();
        return attackSystem.isPlayerFallingP((Boolean)this.options.is("Только криты").getValue() != false && !DDCRIT.canUseDDCRIT(), (Boolean)this.smartCrits.getValue());
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) {
            return false;
        }
        if (entity.ticksExisted < 3) {
            return false;
        }
        if (AG.getInst().getModuleManager().getDDFRCM().isEnabled()) {
            DDFRCM ddfrcm = AG.getInst().getModuleManager().getDDFRCM();
            if (ddfrcm.fakePlayer.getDistanceEyePos(entity) > (double)this.maxRange()) {
                return false;
            }
        }
        if (!AG.getInst().getModuleManager().getDDFRCM().isEnabled()) {
            if (DDATTACK.mc.player.getDistanceEyePos(entity) > (double)this.maxRange()) {
                return false;
            }
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity p = (PlayerEntity)entity;
            if (DDBOT.isBot(entity)) {
                return false;
            }
            if (!((Boolean)this.targetValues.is("Друзья").getValue()).booleanValue() && FriendManager.isFriend(p.getName().getString())) {
                return false;
            }
            if (p.getName().getString().equalsIgnoreCase(DDATTACK.mc.player.getName().getString())) {
                return false;
            }
        }
        if (entity instanceof PlayerEntity && !((Boolean)this.targetValues.is("Игроки").getValue()).booleanValue()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !((Boolean)this.targetValues.is("Голые игроки").getValue()).booleanValue()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getTotalArmorValue() == 0 && !((Boolean)this.targetValues.is("Голые невидимки").getValue()).booleanValue()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && !((Boolean)this.targetValues.is("Невидимки").getValue()).booleanValue()) {
            return false;
        }
        if (entity instanceof PlayerEntity && ((PlayerEntity)entity).isCreative()) {
            return false;
        }
        if ((entity instanceof MonsterEntity || entity instanceof SlimeEntity || entity instanceof VillagerEntity|| entity instanceof IronGolemEntity|| entity instanceof SquidEntity||  entity instanceof BatEntity || entity instanceof WanderingTraderEntity || entity instanceof SalmonEntity||  entity instanceof DolphinEntity ||  entity instanceof CodEntity ||  entity instanceof TropicalFishEntity || entity instanceof PufferfishEntity|| entity instanceof VillagerEntity|| entity instanceof SilverfishEntity) && !((Boolean)this.targetValues.is("Мобы").getValue()).booleanValue()) {
            return false;
        }
        if (entity instanceof AnimalEntity && !((Boolean)this.targetValues.is("Животные").getValue()).booleanValue()) {
            return false;
        }
        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void breakShieldPlayer(PlayerEntity entity) {
        if (entity.isBlocking()) {
            int invSlot = InventoryOrigin.getInstance().getAxeInInventory(false);
            int hotBarSlot = InventoryOrigin.getInstance().getAxeInInventory(true);
            if (hotBarSlot == -1 && invSlot != -1) {
                int bestSlot = InventoryOrigin.getInstance().findBestSlotInHotBar();
                DDATTACK.mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, DDATTACK.mc.player);
                DDATTACK.mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, DDATTACK.mc.player);
                DDATTACK.mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlot));
                DDATTACK.mc.playerController.attackEntity(DDATTACK.mc.player, entity);
                DDATTACK.mc.player.swingArm(Hand.MAIN_HAND);
                DDATTACK.mc.player.connection.sendPacket(new CHeldItemChangePacket(DDATTACK.mc.player.inventory.currentItem));
                DDATTACK.mc.playerController.windowClick(0, bestSlot + 36, 0, ClickType.PICKUP, DDATTACK.mc.player);
                DDATTACK.mc.playerController.windowClick(0, invSlot, 0, ClickType.PICKUP, DDATTACK.mc.player);
            }
            if (hotBarSlot != -1) {
                DDATTACK.mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
                DDATTACK.mc.playerController.attackEntity(DDATTACK.mc.player, entity);
                DDATTACK.mc.player.swingArm(Hand.MAIN_HAND);
                DDATTACK.mc.player.connection.sendPacket(new CHeldItemChangePacket(DDATTACK.mc.player.inventory.currentItem));
            }
        }
    }
    private void smoothReset() {
        float targetRotation = mc.player.rotationYaw;
        float targetPitch = mc.player.rotationPitch;

        float speed = 0.385f;

        if (this.type.is("Custom Rotation")) {
            if (this.rejim.is("Снапы")) {
                if (this.resetSpeed.is("Плавно")) {
                    speed = this.resetSmooth.getValue();
                }
            }
        }

        if (this.type.is("FunTime Rotation")) {
            if (getTarget() !=null) {
                if (shakingRight) {
                    shakeYawOffset += shakeSpeed;
                    if (shakeYawOffset >= maxShake) shakingRight = false;
                } else {
                    shakeYawOffset -= shakeSpeed;
                    if (shakeYawOffset <= -maxShake) shakingRight = true;
                }
            }
        }

        if (this.type.is("FunTime Rotation")) {
            this.rotateVector.x = interpolate(this.rotateVector.x, targetRotation + shakeYawOffset, speed);


            if (Math.abs(this.rotateVector.x - targetRotation) < 0.1F &&
                    Math.abs(this.rotateVector.y - targetPitch) < 0.1F) {
                this.rotateVector.x = targetRotation;
                this.rotateVector.y = targetPitch;
                return;
            }
        }

        if (getTarget() == null) {
            reset();
        }

        if (!this.type.is("FunTime Rotation")) {
            this.rotateVector.x = interpolate(this.rotateVector.x, targetRotation, speed);
            this.rotateVector.y = interpolate(this.rotateVector.y, targetPitch, speed);

            if (Math.abs(this.rotateVector.x - targetRotation) < 0.1F &&
                    Math.abs(this.rotateVector.y - targetPitch) < 0.1F) {
                this.rotateVector.x = targetRotation;
                this.rotateVector.y = targetPitch;
                return;
            }
        }

        if (options.is("Коррекция движения").getValue()) {
            mc.player.rotationYawOffset = rotateVector.x;
        }
    }


    private float interpolate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
    private void reset() {
        if (options.is("Коррекция движения").getValue() ) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
        rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }



    @Override
    public void onEnable() {
        super.onEnable();
        this.reset();
        this.targetValue = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (!this.type.is("Grim Rotation") && !this.type.is("FunTime Rotation")) {
            if (this.fightBot.getValue()) {
                mc.gameSettings.keyBindRight.setPressed(false);
                mc.gameSettings.keyBindLeft.setPressed(false);
                mc.gameSettings.keyBindSprint.setPressed(false);
                mc.gameSettings.keyBindJump.setPressed(false);
                mc.gameSettings.keyBindForward.setPressed(false);
            }
        }
        this.reset();
        this.TimeCounterSetting.setLastMS(0L);
        this.targetValue = null;
        DDATTACK.mc.timer.timerSpeed = 1.0f;
    }

    public ModeSetting getType() {
        return this.type;
    }

    public ModeListSetting getOptions() {
        return this.options;
    }

    public ModeListSetting getoptions() {
        return this.options;
    }

    public TimeCounterSetting getTimeCounterSetting() {
        return this.TimeCounterSetting;
    }

    public LivingEntity getTarget() {
        return this.targetValue;
    }

    public void setTarget(LivingEntity targetValue) {
        this.targetValue = targetValue;
    }
}

