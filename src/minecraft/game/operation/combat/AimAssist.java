package minecraft.game.operation.combat;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import minecraft.game.advantage.advisee.AuraUtil;
import minecraft.game.advantage.figures.TimeCounterSetting;
import minecraft.game.advantage.luvvy.AttackSystem;
import minecraft.game.advantage.luvvy.MouseManager;
import minecraft.game.advantage.luvvy.PlayerSettingsModule;
import minecraft.game.operation.wamost.api.Category;
import minecraft.game.operation.wamost.api.Defuse;
import minecraft.game.operation.wamost.massa.elements.CheckBoxSetting;
import minecraft.game.operation.wamost.massa.elements.ModeListSetting;
import minecraft.game.operation.wamost.massa.elements.ModeSetting;
import minecraft.game.operation.wamost.massa.elements.SliderSetting;
import minecraft.game.transactions.EventRender2D;
import minecraft.game.transactions.EventUpdate;
import minecraft.system.AG;
import net.minecraft.block.material.Material;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.minecraft.util.math.MathHelper.wrapDegrees;

@Getter
@Defuse(name = "Aim Assist",description = "Penis", brand = Category.Combat)
public class AimAssist extends minecraft.game.operation.wamost.api.Module {

    public final SliderSetting attackRange = new SliderSetting("Дистанция наводки",
            3f, 3f, 6f, 0.1f
    );

    public final SliderSetting assistStrength = new SliderSetting("Сила",
            2f, 1f, 10f, 1f
    );

    public final SliderSetting smoothFactor = new SliderSetting("Плавность",
            0.4F, 0.1F, 1.0F, 0.1F
    );

    public final SliderSetting yawSpeed = new SliderSetting("Скорость Yaw",
            50f, 0f, 150f, 1f
    );

    public final SliderSetting pitchSpeed = new SliderSetting("Скорость Pitch",
            0f, 0f, 150f, 1f
    );

    public final ModeListSetting typeFilter = new ModeListSetting("Фильтр на",
            new CheckBoxSetting("Игроков", true),
            new CheckBoxSetting("Мобов", false),
            new CheckBoxSetting("Животных", false),
            new CheckBoxSetting("Жителей", false),
            new CheckBoxSetting("Друзей", true),
            new CheckBoxSetting("Невидимых", true),
            new CheckBoxSetting("Голых", true)
    );

    public final ModeSetting targetsSort = new ModeSetting("Сортировать по",
            "Здоровью",
            "Здоровью",
            "Броне",
            "Дистанции",
            "Полю зрения",
            "По всему"
    );

    public final CheckBoxSetting accelerateRotation = new CheckBoxSetting("Ускорять ротацию", false);
    public final CheckBoxSetting targetFocus = new CheckBoxSetting("Фокусировать одну цель", false);
    public final CheckBoxSetting fov = new CheckBoxSetting("Ограничить Fov", true);
    public final CheckBoxSetting sprintReset = new CheckBoxSetting("Сбрасывать спринт", true);

    public final SliderSetting radiusFov = new SliderSetting("Фов",
            90, 10, 180, 0.5f
    ).visibleIf(() -> fov.getValue());

    public final ModeListSetting noAttackCheck = new ModeListSetting("Не бить если",
            new CheckBoxSetting("Открыт инвентарь", true),
            new CheckBoxSetting("Используешь еду", true)
    );

    private final TimeCounterSetting timerUtil = new TimeCounterSetting();
    private final TimeCounterSetting focusTimer = new TimeCounterSetting();
    private Vector2f rotateVector = new Vector2f(0, 0);
    private LivingEntity target;
    private Entity selected;
    protected float lastYaw, lastPitch;

    @Subscribe
    public void onGui(EventRender2D event) {
        if (target != null) {
            setPlayerRotation(target, yawSpeed.getValue(), pitchSpeed.getValue(), smoothFactor.getValue());
        }
    }
    public AimAssist() {
        setEnabled(false, true);
        this.addSettings(this.attackRange, this.assistStrength, this.smoothFactor, this.yawSpeed, this.pitchSpeed, this.typeFilter, this.targetsSort, this.accelerateRotation, this.targetFocus, this.fov, this.sprintReset, this.radiusFov, this.noAttackCheck);
    }
    @Subscribe
    public void onUpdate(EventUpdate e) {
        updateTarget();

    }

    private boolean isAutoPotionActive() {
        return AG.getInst().getModuleManager().getAutopotion().isEnabled() && AG.getInst().getModuleManager().getAutopotion().isActive;
    }

    private void processRotationLogic() {
        if (target == null || mc.player.getDistance(target) > attackRange.getValue()) {
            updateTarget();
        }

        boolean isInventoryOpen = noAttackCheck.get(Integer.parseInt("Открыт инвентарь")).getValue() && mc.currentScreen instanceof InventoryScreen;
        boolean isUsingFood = noAttackCheck.get(Integer.parseInt("Используешь еду")).getValue() && mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().isFood();

        if (isInventoryOpen || isUsingFood) {
            rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
            return;
        }


        setPlayerRotation(target, yawSpeed.getValue(), pitchSpeed.getValue(), smoothFactor.getValue());
    }

    private void setPlayerRotation(LivingEntity target, float yawSpeed, float pitchSpeed, float smoothFactor) {
        if (target == null || mc.player == null || !LookTarget(target)) return;

        Vector3d targetVec = target.getPositionVec().add(0, target.getEyeHeight() / 2.0, 0);
        Vector3d playerVec = mc.player.getEyePosition(1.0F);
        Vector3d directionVec = targetVec.subtract(playerVec).normalize();

        float targetYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(directionVec.z, directionVec.x)) - 90.0F);
        float targetPitch = (float) -Math.toDegrees(Math.atan2(directionVec.y, Math.sqrt(directionVec.x * directionVec.x + directionVec.z * directionVec.z)));

        float yawDelta = wrapDegrees(targetYaw - mc.player.rotationYaw);
        float pitchDelta = wrapDegrees(targetPitch - mc.player.rotationPitch);

        float clampedYaw = Math.min(Math.abs(yawDelta), yawSpeed) * Math.signum(yawDelta);
        float clampedPitch = Math.min(Math.abs(pitchDelta), pitchSpeed) * Math.signum(pitchDelta);

        float assistFactor = assistStrength.getValue() / 20.0F;
        clampedYaw *= assistFactor;
        clampedPitch *= assistFactor;

        float smoothYaw = mc.player.rotationYaw + clampedYaw * smoothFactor;
        float smoothPitch = mc.player.rotationPitch + clampedPitch * smoothFactor;

        smoothPitch = MathHelper.clamp(smoothPitch, -90.0F, 90.0F);

        mc.player.rotationYaw = smoothYaw;
        mc.player.rotationPitch = smoothPitch;
        mc.player.rotationYawHead = smoothYaw;
        mc.player.renderYawOffset = PlayerSettingsModule.calculateCorrectYawOffset(smoothYaw);;

        rotateVector = new Vector2f(smoothYaw, smoothPitch);
        lastYaw = smoothYaw;
        lastPitch = smoothPitch;
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity living && isValid(living)) {
                targets.add(living);
            }
        }

        if (targetFocus.getValue() && target != null) {
            if (isValid(target)) {
                if (!focusTimer.isReached(5000)) {
                    return;
                } else {
                    focusTimer.reset();
                    target = null;
                }
            } else {
                focusTimer.reset();
                target = null;
            }
        }

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (targets.size() == 1) {
            target = targets.get(0);
            focusTimer.reset();
            return;
        }

        sortTargets(targets);
        target = targets.get(0);
        focusTimer.reset();
    }

    private void sortTargets(List<LivingEntity> targets) {
        Comparator<LivingEntity> comparator = (object1, object2) -> 0;

        if (targetsSort.is("Здоровью")) {
            comparator = Comparator.comparingDouble(this::getEntityHealth).reversed();
        }
        if (targetsSort.is("Броне")) {
            comparator = comparator.thenComparing(Comparator.comparingDouble(o -> getEntityArmor((PlayerEntity) o)).reversed());
        }
        if (targetsSort.is("Дистанции")) {
            comparator = comparator.thenComparingDouble(mc.player::getDistance);
        }
        if (targetsSort.is("Полю зрения")) {
            comparator = comparator.thenComparingDouble(this::getFieldOfView).reversed();
        }

        if (targetsSort.is("По всему")) {
            comparator = Comparator.comparingDouble((LivingEntity o) -> mc.player.getDistance(o)).thenComparingDouble(this::getFieldOfView).thenComparingDouble(this::getEntityHealth).thenComparingDouble(o -> getEntityArmor((PlayerEntity) o));
        }
        targets.sort(comparator);
    }

    private double getFieldOfView(LivingEntity entity) {
        Vector3d playerVec = mc.player.getLookVec();
        Vector3d targetVec = entity.getPositionVec().subtract(mc.player.getPositionVec()).normalize();
        double dotProduct = playerVec.dotProduct(targetVec);
        return Math.acos(dotProduct) * (180 / Math.PI);
    }

    private boolean LookTarget(LivingEntity target) {
        if (target == null) return false;
        Vector3d playerDirection = mc.player.getLook(1.0F).normalize();
        Vector3d targetDirection = target.getPositionVec()
                .subtract(mc.player.getEyePosition(1.0F))
                .normalize();
        double dotProduct = playerDirection.dotProduct(targetDirection);
        double angle = Math.toDegrees(Math.acos(MathHelper.clamp(dotProduct, -1.0, 1.0)));
        double maxFov = fov.getValue() ? radiusFov.getValue() : 360.0;
        return angle <= maxFov || !fov.getValue();
    }

    private final TimeCounterSetting sprintTimer = new TimeCounterSetting();
    private void resetSprintDirectly() {
        if (sprintReset.getValue() && mc.player.isSprinting()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
            mc.player.setSprinting(false);
        }

        if (sprintReset.getValue() && !mc.player.isSprinting() && mc.gameSettings.keyBindForward.isKeyDown()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
            mc.player.setSprinting(true);
        }
    }

    private void updateAttack() {
        if (target == null) return;

        selected = MouseManager.getMouseOver(target, rotateVector.x, rotateVector.y, attackRange.getValue());

        boolean isInventoryOpen = noAttackCheck.is("Открыт инвентарь").getValue() && mc.currentScreen instanceof InventoryScreen;
        boolean isUsingFood = noAttackCheck.is("Используешь еду").getValue() && mc.player.isHandActive() && mc.player.getActiveItemStack().getItem().isFood();
        if (isInventoryOpen || isUsingFood) {
            return;
        }

        resetSprintDirectly();

        if (!mc.player.isElytraFlying() && (selected == null || selected != target)) {
            return;
        }

        setPlayerRotation(target, yawSpeed.getValue(), pitchSpeed.getValue(), smoothFactor.getValue());

        timerUtil.setLastMS(500);
        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);
    }


    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;
        if (entity.ticksExisted < 3) return false;
        if (AuraUtil.getDistanceEyePos(entity) > attackRange.getValue()) return false;


        if (entity instanceof PlayerEntity p) {
            if (DDBOT.isBot(entity)) return false;

            if (!typeFilter.is("Друзей").getValue() && AG.getInst().getFriendManager().isFriend(p.getName().getString())) {
                return false;
            }

            if (p.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;

            if (!typeFilter.is("Игроков").getValue()) {
                return false;
            }

            if (p.getTotalArmorValue() == 0 && !typeFilter.is("Голых").getValue()) {
                return false;
            }

            if (p.isInvisible()) {
                if (!typeFilter.is("Невидимых").getValue()) {
                    return false;
                }

                if (p.getTotalArmorValue() == 0 && !typeFilter.is("Голых").getValue()) {
                    return false;
                }
            }
        }

        if (entity instanceof MonsterEntity) {
            if (!typeFilter.is("Мобов").getValue()) {
                return false;
            }
        }

        if (entity instanceof VillagerEntity) {
            if (!typeFilter.is("Жителей").getValue()) {
                return false;
            }
        }

        if (entity instanceof AnimalEntity) {
            if (!typeFilter.is("Животных").getValue()) {
                return false;
            }
        }

        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void reset() {
        rotateVector = new Vector2f(mc.player != null ? mc.player.rotationYaw : 0, mc.player != null ? mc.player.rotationPitch : 0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            reset();
            target = null;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        timerUtil.setLastMS(0);
        target = null;
    }

    private double getEntityArmor(PlayerEntity entityPlayer2) {
        double d2 = 0.0;
        for (int i2 = 0; i2 < 4; ++i2) {
            ItemStack is = entityPlayer2.inventory.armorInventory.get(i2);
            if (!(is.getItem() instanceof ArmorItem)) continue;
            d2 += getProtectionLvl(is);
        }
        return d2;
    }

    private double getProtectionLvl(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem i) {
            double damageReduceAmount = i.getDamageReduceAmount();
            if (stack.isEnchanted()) {
                damageReduceAmount += (double) EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack) * 0.25;
            }
            return damageReduceAmount;
        }
        return 0;
    }

    private double getEntityHealth(LivingEntity ent) {
        if (ent instanceof PlayerEntity player) {
            return (double) (player.getHealth() + player.getAbsorptionAmount()) * (getEntityArmor(player) / 20.0);
        }
        return ent.getHealth() + ent.getAbsorptionAmount();
    }

    public LivingEntity getTarget() {
        return AG.getInst().getModuleManager().getAimAssist().isEnabled() ? AG.getInst().getModuleManager().getAimAssist().target : null;
    }
}