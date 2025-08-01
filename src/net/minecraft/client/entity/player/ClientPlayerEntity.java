package net.minecraft.client.entity.player;

import com.google.common.collect.Lists;

import minecraft.system.AG;
import minecraft.game.enjoin.interfaces.CommandDispatcher;
import minecraft.game.enjoin.api.DispatchResult;
import minecraft.game.transactions.*;
import minecraft.game.operation.wamost.api.ModuleManager;
import minecraft.game.operation.misc.SelfDestruct;
import minecraft.game.operation.movement.AutoSprint;
import minecraft.game.operation.misc.DDPUSH;
import minecraft.game.advantage.figures.TimeCounterSetting;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;


public class ClientPlayerEntity extends AbstractClientPlayerEntity {
    public final ClientPlayNetHandler connection;
    private final StatisticsManager stats;
    private final ClientRecipeBook recipeBook;
    private final List<IAmbientSoundHandler> ambientSoundHandlers = Lists.newArrayList();
    private int permissionLevel = 0;
    public List<WindowClickMemory> windowClickMemory = new ArrayList<>();

    /**
     * The last X position which was transmitted to the server, used to determine
     * when the X position changes and needs
     * to be re-trasmitted
     */
    private double lastReportedPosX;

    /**
     * The last Y position which was transmitted to the server, used to determine
     * when the Y position changes and needs
     * to be re-transmitted
     */
    private double lastReportedPosY;

    /**
     * The last Z position which was transmitted to the server, used to determine
     * when the Z position changes and needs
     * to be re-transmitted
     */
    private double lastReportedPosZ;

    /**
     * The last yaw value which was transmitted to the server, used to determine
     * when the yaw changes and needs to be
     * re-transmitted
     */
    @Getter
    public float lastReportedYaw;
    public float lastReportedPreYaw;
    public float lastReportedPrePitch;
    public float prePitch;
    public float preYaw;

    /**
     * The last pitch value which was transmitted to the server, used to determine
     * when the pitch changes and needs to
     * be re-transmitted
     */
    @Getter
    public float lastReportedPitch;
    private boolean prevOnGround;
    private boolean isCrouching;
    private boolean clientSneakState;

    /**
     * the last sprinting state sent to the server
     */
    public boolean serverSprintState;

    /**
     * Reset to 0 every time position is sent to the server, used to send periodic
     * updates every 20 ticks even when the
     * player is not moving.
     */
    private int positionUpdateTicks;
    private boolean hasValidHealth;
    private String serverBrand;
    public MovementInput movementInput;
    protected final Minecraft mc;
    protected int sprintToggleTimer;
    public int sprintingTicksLeft;
    public float renderArmYaw;
    public float renderArmPitch;
    public float prevRenderArmYaw;
    public float prevRenderArmPitch;
    private int horseJumpPowerCounter;
    private float horseJumpPower;
    public float timeInPortal;
    public float prevTimeInPortal;
    private boolean handActive;
    private Hand activeHand;
    private boolean rowingBoat;
    private boolean autoJumpEnabled = true;
    private int autoJumpTime;
    private boolean wasFallFlying;
    private int counterInWater;
    private boolean showDeathScreen = true;

    public ClientPlayerEntity(Minecraft mc, ClientWorld world, ClientPlayNetHandler connection, StatisticsManager stats, ClientRecipeBook recipeBook, boolean clientSneakState, boolean clientSprintState) {
        super(world, connection.getGameProfile());
        this.mc = mc;
        this.connection = connection;
        this.stats = stats;
        this.recipeBook = recipeBook;
        this.clientSneakState = clientSneakState;
        this.serverSprintState = clientSprintState;
        this.ambientSoundHandlers.add(new UnderwaterAmbientSoundHandler(this, mc.getSoundHandler()));
        this.ambientSoundHandlers.add(new BubbleColumnAmbientSoundHandler(this));
        this.ambientSoundHandlers.add(new BiomeSoundHandler(this, mc.getSoundHandler(), world.getBiomeManager()));
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    /**
     * Heal living entity (param: amount of half-hearts)
     */
    @Override
    public void heal(float healAmount) {
    }

    @Override
    public boolean startRiding(Entity entityIn, boolean force) {
        if (!super.startRiding(entityIn, force)) {
            return false;
        } else {
            if (entityIn instanceof AbstractMinecartEntity) {
                this.mc.getSoundHandler().play(new RidingMinecartTickableSound(this, (AbstractMinecartEntity) entityIn));
            }

            if (entityIn instanceof BoatEntity) {
                this.prevRotationYaw = entityIn.rotationYaw;
                this.rotationYaw = entityIn.rotationYaw;
                this.setRotationYawHead(entityIn.rotationYaw);
            }

            return true;
        }
    }

    public EventSprint eventSprint = new EventSprint();

    @Override
    public void dismount() {
        super.dismount();
        this.rowingBoat = false;
    }

    /**
     * Gets the current pitch of the entity.
     */
    @Override
    public float getPitch(float partialTicks) {
        return this.rotationPitch;
    }

    /**
     * Gets the current yaw of the entity
     */
    @Override
    public float getYaw(float partialTicks) {
        return this.isPassenger() ? super.getYaw(partialTicks) : this.rotationYaw;
    }

    /**
     * Called to update the entity's position/logic.
     */

    EventUpdate eventUpdate = new EventUpdate();

    @Override
    public void tick() {
        if (this.world.isBlockLoaded(new BlockPos(this.getPosX(), 0.0D, this.getPosZ()))) {
            if (!AG.getInst().playerOnServer) {
                AG.getInst().playerOnServer = true;
            }

            AG.getInst().getEventBus().post(eventUpdate);
            super.tick();

            if (this.isPassenger()) {
                this.connection.sendPacket(new CPlayerPacket.RotationPacket(this.rotationYaw, this.rotationPitch, this.onGround));
                this.connection.sendPacket(new CInputPacket(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneaking));
                Entity entity = this.getLowestRidingEntity();

                if (entity != this && entity.canPassengerSteer()) {
                    this.connection.sendPacket(new CMoveVehiclePacket(entity));
                }
            } else {
                this.onUpdateWalkingPlayer();
            }

            for (IAmbientSoundHandler iambientsoundhandler : this.ambientSoundHandlers) {
                iambientsoundhandler.tick();
            }
        }
    }

    public float getDarknessAmbience() {
        for (IAmbientSoundHandler iambientsoundhandler : this.ambientSoundHandlers) {
            if (iambientsoundhandler instanceof BiomeSoundHandler) {
                return ((BiomeSoundHandler) iambientsoundhandler).getDarknessAmbienceChance();
            }
        }

        return 0.0F;
    }

    /**
     * called every tick when the player is on foot. Performs all the things that
     * normally happen during movement.
     */

    private final EventMotion eventMotion = new EventMotion(0, 0, 0, 0, 0, false, null);

    private void onUpdateWalkingPlayer() {
        if (!windowClickMemory.isEmpty()) {
            Iterator<WindowClickMemory> iterator = windowClickMemory.iterator();
            while (iterator.hasNext()) {
                WindowClickMemory memory = iterator.next();
                if (memory != null && memory.timerWaitAction.isReached(memory.timeWait)) {
                    this.mc.playerController.windowClick(memory.windowId, memory.slotId, memory.mouseButton, memory.type, memory.player);
                    iterator.remove();
                }
            }
        }

        boolean flag = this.isSprinting();
        ActionEvent ea = new ActionEvent(flag);
        AG.getInst().getEventBus().post(ea);

        preYaw = rotationYaw;
        prePitch = rotationPitch;

        if (flag != this.serverSprintState) {
            CEntityActionPacket.Action centityactionpacket$action = flag ? CEntityActionPacket.Action.START_SPRINTING : CEntityActionPacket.Action.STOP_SPRINTING;
            this.connection.sendPacket(new CEntityActionPacket(this, centityactionpacket$action));
            this.serverSprintState = flag;
        }

        if (ea.isSprintState() != this.serverSprintState) {
            CEntityActionPacket.Action centityactionpacket$action = ea.isSprintState() ? CEntityActionPacket.Action.START_SPRINTING : CEntityActionPacket.Action.STOP_SPRINTING;
            this.connection.sendPacket(new CEntityActionPacket(this, centityactionpacket$action));
            this.serverSprintState = ea.isSprintState();
        }

        boolean flag3 = this.isSneaking();

        if (flag3 != this.clientSneakState) {
            CEntityActionPacket.Action centityactionpacket$action1 = flag3 ? CEntityActionPacket.Action.START_SNEAKING : CEntityActionPacket.Action.STOP_SNEAKING;
            this.connection.sendPacket(new CEntityActionPacket(this, centityactionpacket$action1));
            this.clientSneakState = flag3;
        }

        if (this.isCurrentViewEntity()) {

            eventMotion.setX(this.getPosX());
            eventMotion.setY(this.getPosY());
            eventMotion.setZ(this.getPosZ());
            eventMotion.setYaw(this.rotationYaw);
            eventMotion.setPitch(this.rotationPitch);
            eventMotion.setOnGround(this.onGround);


            AG.getInst().getEventBus().post(eventMotion);

            if (eventMotion.isCancel()) {
                eventMotion.open();
                return;
            }

            double d4 = eventMotion.getX() - this.lastReportedPosX;
            double d0 = eventMotion.getY() - this.lastReportedPosY;
            double d1 = eventMotion.getZ() - this.lastReportedPosZ;
            double d2 = eventMotion.getYaw() - this.lastReportedYaw;
            double d3 = eventMotion.getPitch() - this.lastReportedPitch;
            ++this.positionUpdateTicks;
            boolean flag1 = MathHelper.lengthSquared(d4, d0, d1) > MathHelper.square(2.0E-4D) || this.positionUpdateTicks >= 20;
            boolean flag2 = d2 != 0.0D || d3 != 0.0D;

            if (this.isPassenger()) {
                Vector3d vector3d = this.getMotion();
                this.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(vector3d.x, -999.0D, vector3d.z, this.rotationYaw, this.rotationPitch, this.onGround));
                flag1 = false;
            } else if (flag1 && flag2) {
                this.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(eventMotion.getX(), eventMotion.getY(), eventMotion.getZ(), eventMotion.getYaw(), eventMotion.getPitch(), eventMotion.isOnGround()));
            } else if (flag1) {
                this.connection.sendPacket(new CPlayerPacket.PositionPacket(eventMotion.getX(), eventMotion.getY(), eventMotion.getZ(), eventMotion.isOnGround()));
            } else if (flag2) {
                this.connection.sendPacket(new CPlayerPacket.RotationPacket(eventMotion.getYaw(), eventMotion.getPitch(), eventMotion.isOnGround()));
            } else if (this.prevOnGround != eventMotion.isOnGround()) {
                this.connection.sendPacket(new CPlayerPacket(eventMotion.isOnGround()));
            }

            if (flag1) {
                this.lastReportedPosX = eventMotion.getX();
                this.lastReportedPosY = eventMotion.getY();
                this.lastReportedPosZ = eventMotion.getZ();
                this.positionUpdateTicks = 0;
            }

            if (flag2) {
                this.lastReportedYaw = eventMotion.getYaw();
                this.lastReportedPitch = eventMotion.getPitch();
            }

            this.prevOnGround = eventMotion.isOnGround();
            this.autoJumpEnabled = this.mc.gameSettings.autoJump;
        }

        lastReportedPreYaw = preYaw;
        lastReportedPrePitch = prePitch;

        AG.getInst().getEventBus().post(new EventPostMotion());
        if (eventMotion.getPostMotion() != null) {
            eventMotion.getPostMotion().run();
        }
    }

    @Override
    public boolean drop(boolean p_225609_1_) {
        CPlayerDiggingPacket.Action cplayerdiggingpacket$action = p_225609_1_ ? CPlayerDiggingPacket.Action.DROP_ALL_ITEMS : CPlayerDiggingPacket.Action.DROP_ITEM;
        this.connection.sendPacket(new CPlayerDiggingPacket(cplayerdiggingpacket$action, BlockPos.ZERO, Direction.DOWN));
        return this.inventory.decrStackSize(this.inventory.currentItem, p_225609_1_ && !this.inventory.getCurrentItem().isEmpty() ? this.inventory.getCurrentItem().getCount() : 1) != ItemStack.EMPTY;
    }

    /**
     * Sends a chat message from the player.
     */
    public void sendChatMessage(String message) {
        ModuleManager moduleManager = AG.getInst().getModuleManager();
        CommandDispatcher commandDispatcher = AG.getInst().getCommandDispatcher();

        SelfDestruct selfDestruct = moduleManager.getSelfDestruct();

        if (commandDispatcher.dispatch(message) == DispatchResult.DISPATCHED) {
            return;
        }

        if (selfDestruct.unhooked) {
            if (message.equals(selfDestruct.secret)) {
                selfDestruct.hook();
                return;
            }
        }

        this.connection.sendPacket(new CChatMessagePacket(message));
    }

    @Override
    public void swingArm(Hand hand) {
        super.swingArm(hand);
        this.connection.sendPacket(new CAnimateHandPacket(hand));
    }

    @Override
    public void respawnPlayer() {
        this.connection.sendPacket(new CClientStatusPacket(CClientStatusPacket.State.PERFORM_RESPAWN));
    }

    /**
     * Deals damage to the entity. This will take the armor of the entity into
     * consideration before damaging the health
     * bar.
     */
    @Override
    protected void damageEntity(DamageSource damageSrc, float damageAmount) {
        if (!this.isInvulnerableTo(damageSrc)) {
            this.setHealth(this.getHealth() - damageAmount);
        }
    }

    /**
     * set current crafting inventory back to the 2x2 square
     */
    @Override
    public void closeScreen() {
        EventInventoryClose event = new EventInventoryClose(this.openContainer.windowId);
        AG.getInst().getEventBus().post(event);
        if (!event.isCancel()) {
            this.connection.sendPacket(new CCloseWindowPacket(this.openContainer.windowId));
        }
        this.closeScreenAndDropStack();
    }

    public void closeScreenAndDropStack() {
        this.inventory.setItemStack(ItemStack.EMPTY);
        super.closeScreen();
        this.mc.displayGuiScreen(null);
    }

    /**
     * Updates health locally.
     */
    public void setPlayerSPHealth(float health) {
        if (this.hasValidHealth) {
            float f = this.getHealth() - health;

            if (f <= 0.0F) {
                this.setHealth(health);

                if (f < 0.0F) {
                    this.hurtResistantTime = 10;
                }
            } else {
                this.lastDamage = f;
                this.setHealth(this.getHealth());
                this.hurtResistantTime = 20;
                this.damageEntity(DamageSource.GENERIC, f);
                this.maxHurtTime = 10;
                this.hurtTime = this.maxHurtTime;
            }
        } else {
            this.setHealth(health);
            this.hasValidHealth = true;
        }
    }

    /**
     * Sends the player's abilities to the server (if there is one).
     */
    @Override
    public void sendPlayerAbilities() {
        this.connection.sendPacket(new CPlayerAbilitiesPacket(this.abilities));
    }

    /**
     * returns true if this is an EntityPlayerSP, or the logged in player.
     */
    @Override
    public boolean isUser() {
        return true;
    }

    @Override
    public boolean hasStoppedClimbing() {
        return !this.abilities.isFlying && super.hasStoppedClimbing();
    }

    @Override
    public boolean func_230269_aK_() {
        return !this.abilities.isFlying && super.func_230269_aK_();
    }

    @Override
    public boolean getMovementSpeed() {
        return !this.abilities.isFlying && super.getMovementSpeed();
    }

    protected void sendHorseJump() {
        this.connection.sendPacket(new CEntityActionPacket(this, CEntityActionPacket.Action.START_RIDING_JUMP, MathHelper.floor(this.getHorseJumpPower() * 100.0F)));
    }

    public void sendHorseInventory() {
        this.connection.sendPacket(new CEntityActionPacket(this, CEntityActionPacket.Action.OPEN_INVENTORY));
    }

    /**
     * Sets the brand of the currently connected server. Server brand information is
     * sent over the {@code MC|Brand}
     * plugin channel, and is used to identify modded servers in crash reports.
     */
    public void setServerBrand(String brand) {
        this.serverBrand = brand;
    }

    /**
     * Gets the brand of the currently connected server. May be null if the server
     * hasn't yet sent brand information.
     * Server brand information is sent over the {@code MC|Brand} plugin channel,
     * and is used to identify modded servers
     * in crash reports.
     */
    public String getServerBrand() {
        return this.serverBrand;
    }

    public StatisticsManager getStats() {
        return this.stats;
    }

    public ClientRecipeBook getRecipeBook() {
        return this.recipeBook;
    }

    public void removeRecipeHighlight(IRecipe<?> recipe) {
        if (this.recipeBook.isNew(recipe)) {
            this.recipeBook.markSeen(recipe);
            this.connection.sendPacket(new CMarkRecipeSeenPacket(recipe));
        }
    }

    @Override
    protected int getPermissionLevel() {
        return this.permissionLevel;
    }

    public void setPermissionLevel(int permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    @Override
    public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {
        if (actionBar) {
            this.mc.ingameGUI.setOverlayMessage(chatComponent, false);
        } else {
            this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponent);
        }
    }

    private void setPlayerOffsetMotion(double x, double z) {
        BlockPos blockpos = new BlockPos(x, this.getPosY(), z);
        if (this.shouldBlockPushPlayer(blockpos)) {
            double d0 = x - (double) blockpos.getX();
            double d1 = z - (double) blockpos.getZ();
            Direction direction = null;
            double d2 = Double.MAX_VALUE;
            Direction[] adirection = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

            for (Direction direction1 : adirection) {
                double d3 = direction1.getAxis().getCoordinate(d0, 0.0D, d1);
                double d4 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d3 : d3;

                if (d4 < d2 && !this.shouldBlockPushPlayer(blockpos.offset(direction1))) {
                    d2 = d4;
                    direction = direction1;
                }
            }

            if (direction != null) {
                Vector3d vector3d = this.getMotion();

                if (direction.getAxis() == Direction.Axis.X) {
                    this.setMotion(0.1D * (double) direction.getXOffset(), vector3d.y, vector3d.z);
                } else {
                    this.setMotion(vector3d.x, vector3d.y, 0.1D * (double) direction.getZOffset());
                }
            }
        }
    }

    private boolean shouldBlockPushPlayer(BlockPos pos) {
        ModuleManager moduleManager = AG.getInst().getModuleManager();
        DDPUSH DDPUSH = moduleManager.getDDPUSH();

        if (DDPUSH.isEnabled() && DDPUSH.modes.is("Блоки").getValue()) {
            return false;
        }

        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        AxisAlignedBB axisalignedbb1 = (new AxisAlignedBB(pos.getX(), axisalignedbb.minY, pos.getZ(), (double) pos.getX() + 1.0D, axisalignedbb.maxY, (double) pos.getZ() + 1.0D)).shrink(1.0E-7D);
        return !this.world.func_242405_a(this, axisalignedbb1, (state, pos2) -> {
            return state.isSuffocating(this.world, pos2);
        });
    }

    /**
     * Set sprinting switch for Entity.
     */
    @Override
    public void setSprinting(boolean sprinting) {
        AG.getInst().getEventBus().post(eventSprint);
        if (eventSprint.isCancel()) {
            super.setSprinting(false);
            this.sprintingTicksLeft = 0;
            eventSprint.open();
            return;
        }
        super.setSprinting(sprinting);
        this.sprintingTicksLeft = 0;
    }

    /**
     * Sets the current XP, total XP, and level number.
     */
    public void setXPStats(float currentXP, int maxXP, int level) {
        this.experience = currentXP;
        this.experienceTotal = maxXP;
        this.experienceLevel = level;
    }

    /**
     * Send a chat message to the CommandSender
     */
    @Override
    public void sendMessage(ITextComponent component, UUID senderUUID) {
        this.mc.ingameGUI.getChatGUI().printChatMessage(component);
    }

    /**
     * Handler for {@link World#setEntityState}
     */
    @Override
    public void handleStatusUpdate(byte id) {
        if (id >= 24 && id <= 28) {
            this.setPermissionLevel(id - 24);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public void setShowDeathScreen(boolean show) {
        this.showDeathScreen = show;
    }

    public boolean isShowDeathScreen() {
        return this.showDeathScreen;
    }

    @Override
    public void playSound(SoundEvent soundIn, float volume, float pitch) {
        this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), soundIn, this.getSoundCategory(), volume, pitch, false);
    }

    @Override
    public void playSound(SoundEvent p_213823_1_, SoundCategory p_213823_2_, float p_213823_3_, float p_213823_4_) {
        this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), p_213823_1_, p_213823_2_, p_213823_3_, p_213823_4_, false);
    }

    /**
     * Returns whether the entity is in a server world
     */
    @Override
    public boolean isServerWorld() {
        return true;
    }

    @Override
    public void setActiveHand(Hand hand) {
        ItemStack itemstack = this.getHeldItem(hand);

        if (!itemstack.isEmpty() && !this.isHandActive()) {
            super.setActiveHand(hand);
            this.handActive = true;
            this.activeHand = hand;
        }
    }

    @Override
    public boolean isHandActive() {
        return this.handActive;
    }

    @Override
    public void resetActiveHand() {
        super.resetActiveHand();
        this.handActive = false;
    }

    @Override
    public Hand getActiveHand() {
        return this.activeHand;
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);

        if (LIVING_FLAGS.equals(key)) {
            boolean flag = (this.dataManager.get(LIVING_FLAGS) & 1) > 0;
            Hand hand = (this.dataManager.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;

            if (flag && !this.handActive) {
                this.setActiveHand(hand);
            } else if (!flag && this.handActive) {
                this.resetActiveHand();
            }
        }

        if (FLAGS.equals(key) && this.isElytraFlying() && !this.wasFallFlying) {
            this.mc.getSoundHandler().play(new ElytraSound(this));
        }
    }

    public boolean isRidingHorse() {
        Entity entity = this.getRidingEntity();
        return this.isPassenger() && entity instanceof IJumpingMount && ((IJumpingMount) entity).canJump();
    }

    public float getHorseJumpPower() {
        return this.horseJumpPower;
    }

    @Override
    public void openSignEditor(SignTileEntity signTile) {
        this.mc.displayGuiScreen(new EditSignScreen(signTile));
    }

    @Override
    public void openMinecartCommandBlock(CommandBlockLogic commandBlock) {
        this.mc.displayGuiScreen(new EditMinecartCommandBlockScreen(commandBlock));
    }

    @Override
    public void openCommandBlock(CommandBlockTileEntity commandBlock) {
        this.mc.displayGuiScreen(new CommandBlockScreen(commandBlock));
    }

    @Override
    public void openStructureBlock(StructureBlockTileEntity structure) {
        this.mc.displayGuiScreen(new EditStructureScreen(structure));
    }

    @Override
    public void openJigsaw(JigsawTileEntity p_213826_1_) {
        this.mc.displayGuiScreen(new JigsawScreen(p_213826_1_));
    }

    @Override
    public void openBook(ItemStack stack, Hand hand) {
        Item item = stack.getItem();

        if (item == Items.WRITABLE_BOOK) {
            this.mc.displayGuiScreen(new EditBookScreen(this, stack, hand));
        }
    }

    /**
     * Called when the entity is dealt a critical hit.
     */
    @Override
    public void onCriticalHit(Entity entityHit) {
        this.mc.particles.addParticleEmitter(entityHit, ParticleTypes.CRIT);
    }

    @Override
    public void onEnchantmentCritical(Entity entityHit) {
        this.mc.particles.addParticleEmitter(entityHit, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public boolean isSneaking() {
        return this.movementInput != null && this.movementInput.sneaking;
    }

    @Override
    public boolean isCrouching() {
        return this.isCrouching;
    }

    public boolean isForcedDown() {
        return this.isCrouching() || this.isVisuallySwimming();
    }

    @Override
    public void updateEntityActionState() {
        super.updateEntityActionState();

        if (this.isCurrentViewEntity()) {
            this.moveStrafing = this.movementInput.moveStrafe;
            this.moveForward = this.movementInput.moveForward;
            this.isJumping = this.movementInput.jump;
            this.prevRenderArmYaw = this.renderArmYaw;
            this.prevRenderArmPitch = this.renderArmPitch;
            this.renderArmPitch = (float) ((double) this.renderArmPitch + (double) (this.rotationPitch - this.renderArmPitch) * 0.5D);
            this.renderArmYaw = (float) ((double) this.renderArmYaw + (double) (this.rotationYaw - this.renderArmYaw) * 0.5D);
        }
    }

    protected boolean isCurrentViewEntity() {
        return this.mc.getRenderViewEntity() == this;
    }

    /**
     * Called frequently so the entity can update its state every tick as required.
     * For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */


    public EventLivingTick tick = new EventLivingTick();
    EventNoSlow eventNoSlow = new EventNoSlow();

    @Override
    public void livingTick() {

        ++this.sprintingTicksLeft;

        if (this.sprintToggleTimer > 0) {
            --this.sprintToggleTimer;
        }

        this.handlePortalTeleportation();
        boolean flag = this.movementInput.jump;
        boolean flag1 = this.movementInput.sneaking;
        boolean flag2 = this.isUsingSwimmingAnimation();
        this.isCrouching = !this.abilities.isFlying && !this.isSwimming() && this.isPoseClear(Pose.CROUCHING) && (this.isSneaking() || !this.isSleeping() && !this.isPoseClear(Pose.STANDING));
        this.movementInput.tickMovement(this.isForcedDown());
        this.mc.getTutorial().handleMovement(this.movementInput);

        if (this.isHandActive() && !this.isPassenger()) {
            AG.getInst().getEventBus().post(eventNoSlow);
            if (!eventNoSlow.isCancel()) {
                this.movementInput.moveStrafe *= 0.2F;
                this.movementInput.moveForward *= 0.2F;
            } else {
                eventNoSlow.open();
            }
            this.sprintToggleTimer = 0;
        }

        boolean flag3 = false;

        if (this.autoJumpTime > 0) {
            --this.autoJumpTime;
            flag3 = true;
            this.movementInput.jump = true;
        }

        if (!this.noClip) {
            this.setPlayerOffsetMotion(this.getPosX() - (double) this.getWidth() * 0.35D, this.getPosZ() + (double) this.getWidth() * 0.35D);
            this.setPlayerOffsetMotion(this.getPosX() - (double) this.getWidth() * 0.35D, this.getPosZ() - (double) this.getWidth() * 0.35D);
            this.setPlayerOffsetMotion(this.getPosX() + (double) this.getWidth() * 0.35D, this.getPosZ() - (double) this.getWidth() * 0.35D);
            this.setPlayerOffsetMotion(this.getPosX() + (double) this.getWidth() * 0.35D, this.getPosZ() + (double) this.getWidth() * 0.35D);
        }

        if (flag1) {
            this.sprintToggleTimer = 0;
        }

        boolean flag4 = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.abilities.allowFlying;

        if ((this.onGround || this.canSwim()) && !flag1 && !flag2 && this.isUsingSwimmingAnimation() && !this.isSprinting() && flag4 && !this.isHandActive() && !this.isPotionActive(Effects.BLINDNESS)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && (!this.isInWater() || this.canSwim()) && this.isUsingSwimmingAnimation() && flag4 && !this.isHandActive() && !this.isPotionActive(Effects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }


        ModuleManager moduleManager = AG.getInst().getModuleManager();
        AutoSprint autoSprint = moduleManager.getAutoSprint();

        if (autoSprint.isEnabled() && !this.isSprinting() &&
                (!this.isInWater() || this.canSwim()) && this.isUsingSwimmingAnimation()
                && flag4 && !this.isHandActive() && !this.isPotionActive(Effects.BLINDNESS)) {
            mc.player.setSprinting(mc.player.movementInput.forwardKeyDown);
        }

        if (this.isSprinting()) {
            boolean flag5 = !this.movementInput.isMovingForward() || !flag4;
            boolean flag6 = flag5 || this.collidedHorizontally || this.isInWater() && !this.canSwim();

            if (this.isSwimming()) {
                if (!this.onGround && !this.movementInput.sneaking && flag5 || !this.isInWater()) {
                    this.setSprinting(false);
                }
            } else if (flag6) {
                this.setSprinting(false);
            }
        }

        boolean flag7 = false;

        if (this.abilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                if (!this.abilities.isFlying) {
                    this.abilities.isFlying = true;
                    flag7 = true;
                    this.sendPlayerAbilities();
                }
            } else if (!flag && this.movementInput.jump && !flag3) {
                if (this.flyToggleTimer == 0) {
                    this.flyToggleTimer = 7;
                } else if (!this.isSwimming()) {
                    this.abilities.isFlying = !this.abilities.isFlying;
                    flag7 = true;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.movementInput.jump && !flag7 && !flag && !this.abilities.isFlying && !this.isPassenger() && !this.isOnLadder()) {
            ItemStack itemstack = this.getItemStackFromSlot(EquipmentSlotType.CHEST);

            if (itemstack.getItem() == Items.ELYTRA && ElytraItem.isUsable(itemstack) && this.tryToStartFallFlying()) {
                this.connection.sendPacket(new CEntityActionPacket(this, CEntityActionPacket.Action.START_FALL_FLYING));
            }
        }

        this.wasFallFlying = this.isElytraFlying();

        if (this.isInWater() && this.movementInput.sneaking && this.func_241208_cS_()) {
            this.handleFluidSneak();
        }

        if (this.areEyesInFluid(FluidTags.WATER)) {
            int i = this.isSpectator() ? 10 : 1;
            this.counterInWater = MathHelper.clamp(this.counterInWater + i, 0, 600);
        } else if (this.counterInWater > 0) {
            this.areEyesInFluid(FluidTags.WATER);
            this.counterInWater = MathHelper.clamp(this.counterInWater - 10, 0, 600);
        }

        if (this.abilities.isFlying && this.isCurrentViewEntity()) {
            int j = 0;

            if (this.movementInput.sneaking) {
                --j;
            }

            if (this.movementInput.jump) {
                ++j;
            }

            if (j != 0) {
                this.setMotion(this.getMotion().add(0.0D, (float) j * this.abilities.getFlySpeed() * 3.0F, 0.0D));
            }
        }

        if (this.isRidingHorse()) {
            IJumpingMount ijumpingmount = (IJumpingMount) this.getRidingEntity();

            if (this.horseJumpPowerCounter < 0) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0) {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (flag && !this.movementInput.jump) {
                this.horseJumpPowerCounter = -10;
                ijumpingmount.setJumpPower(MathHelper.floor(this.getHorseJumpPower() * 100.0F));
                this.sendHorseJump();
            } else if (!flag && this.movementInput.jump) {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            } else if (flag) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10) {
                    this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
                } else {
                    this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            this.horseJumpPower = 0.0F;
        }

        super.livingTick();

        if (this.onGround && this.abilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
            this.abilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }

    private void handlePortalTeleportation() {
        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal) {
            if (this.mc.currentScreen != null && !this.mc.currentScreen.isPauseScreen()) {
                if (this.mc.currentScreen instanceof ContainerScreen) {
                    this.closeScreen();
                }

                this.mc.displayGuiScreen(null);
            }

            if (this.timeInPortal == 0.0F) {
                this.mc.getSoundHandler().play(SimpleSound.ambientWithoutAttenuation(SoundEvents.BLOCK_PORTAL_TRIGGER, this.rand.nextFloat() * 0.4F + 0.8F, 0.25F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F) {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        } else if (this.isPotionActive(Effects.NAUSEA) && this.getActivePotionEffect(Effects.NAUSEA).getDuration() > 60) {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F) {
                this.timeInPortal = 1.0F;
            }
        } else {
            if (this.timeInPortal > 0.0F) {
                this.timeInPortal -= 0.05F;
            }

            if (this.timeInPortal < 0.0F) {
                this.timeInPortal = 0.0F;
            }
        }

        this.decrementTimeUntilPortal();
    }

    /**
     * Handles updating while riding another entity
     */
    @Override
    public void updateRidden() {
        super.updateRidden();
        this.rowingBoat = false;

        if (this.getRidingEntity() instanceof BoatEntity boatentity) {
            boatentity.updateInputs(this.movementInput.leftKeyDown, this.movementInput.rightKeyDown, this.movementInput.forwardKeyDown, this.movementInput.backKeyDown);
            this.rowingBoat |= this.movementInput.leftKeyDown || this.movementInput.rightKeyDown || this.movementInput.forwardKeyDown || this.movementInput.backKeyDown;
        }
    }

    public boolean isRowingBoat() {
        return this.rowingBoat;
    }

    @Override
    @Nullable

    /**
     * Removes the given potion effect from the active potion map and returns it.
     * Does not call cleanup callbacks for
     * the end of the potion effect.
     */ public EffectInstance removeActivePotionEffect(@Nullable Effect potioneffectin) {
        if (potioneffectin == Effects.NAUSEA) {
            this.prevTimeInPortal = 0.0F;
            this.timeInPortal = 0.0F;
        }

        return super.removeActivePotionEffect(potioneffectin);
    }

    @Override
    public void move(MoverType typeIn, Vector3d pos) {
        double d0 = this.getPosX();
        double d1 = this.getPosZ();
        super.move(typeIn, pos);
        this.updateAutoJump((float) (this.getPosX() - d0), (float) (this.getPosZ() - d1));
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    protected void updateAutoJump(float movementX, float movementZ) {
        if (this.canAutoJump()) {
            Vector3d vector3d = this.getPositionVec();
            Vector3d vector3d1 = vector3d.add(movementX, 0.0D, movementZ);
            Vector3d vector3d2 = new Vector3d(movementX, 0.0D, movementZ);
            float f = this.getAIMoveSpeed();
            float f1 = (float) vector3d2.lengthSquared();

            if (f1 <= 0.001F) {
                Vector2f vector2f = this.movementInput.getMoveVector();
                float f2 = f * vector2f.x;
                float f3 = f * vector2f.y;
                float f4 = MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F));
                float f5 = MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F));
                vector3d2 = new Vector3d(f2 * f5 - f3 * f4, vector3d2.y, f3 * f5 + f2 * f4);
                f1 = (float) vector3d2.lengthSquared();

                if (f1 <= 0.001F) {
                    return;
                }
            }

            float f12 = MathHelper.fastInvSqrt(f1);
            Vector3d vector3d12 = vector3d2.scale(f12);
            Vector3d vector3d13 = this.getForward();
            float f13 = (float) (vector3d13.x * vector3d12.x + vector3d13.z * vector3d12.z);

            if (!(f13 < -0.15F)) {
                ISelectionContext iselectioncontext = ISelectionContext.forEntity(this);
                BlockPos blockpos = new BlockPos(this.getPosX(), this.getBoundingBox().maxY, this.getPosZ());
                BlockState blockstate = this.world.getBlockState(blockpos);

                if (blockstate.getCollisionShape(this.world, blockpos, iselectioncontext).isEmpty()) {
                    blockpos = blockpos.up();
                    BlockState blockstate1 = this.world.getBlockState(blockpos);

                    if (blockstate1.getCollisionShape(this.world, blockpos, iselectioncontext).isEmpty()) {
                        float f6 = 7.0F;
                        float f7 = 1.2F;

                        if (this.isPotionActive(Effects.JUMP_BOOST)) {
                            f7 += (float) (this.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) * 0.75F;
                        }

                        float f8 = Math.max(f * 7.0F, 1.0F / f12);
                        Vector3d vector3d4 = vector3d1.add(vector3d12.scale(f8));
                        float f9 = this.getWidth();
                        float f10 = this.getHeight();
                        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(vector3d, vector3d4.add(0.0D, f10, 0.0D))).grow(f9, 0.0D, f9);
                        Vector3d lvt_19_1_ = vector3d.add(0.0D, 0.51F, 0.0D);
                        vector3d4 = vector3d4.add(0.0D, 0.51F, 0.0D);
                        Vector3d vector3d5 = vector3d12.crossProduct(new Vector3d(0.0D, 1.0D, 0.0D));
                        Vector3d vector3d6 = vector3d5.scale(f9 * 0.5F);
                        Vector3d vector3d7 = lvt_19_1_.subtract(vector3d6);
                        Vector3d vector3d8 = vector3d4.subtract(vector3d6);
                        Vector3d vector3d9 = lvt_19_1_.add(vector3d6);
                        Vector3d vector3d10 = vector3d4.add(vector3d6);
                        Iterator<AxisAlignedBB> iterator = this.world.func_234867_d_(this, axisalignedbb, (entity) -> true).flatMap((shape) -> shape.toBoundingBoxList().stream()).iterator();
                        float f11 = Float.MIN_VALUE;

                        while (iterator.hasNext()) {
                            AxisAlignedBB axisalignedbb1 = iterator.next();

                            if (axisalignedbb1.intersects(vector3d7, vector3d8) || axisalignedbb1.intersects(vector3d9, vector3d10)) {
                                f11 = (float) axisalignedbb1.maxY;
                                Vector3d vector3d11 = axisalignedbb1.getCenter();
                                BlockPos blockpos1 = new BlockPos(vector3d11);

                                for (int i = 1; (float) i < f7; ++i) {
                                    BlockPos blockpos2 = blockpos1.up(i);
                                    BlockState blockstate2 = this.world.getBlockState(blockpos2);
                                    VoxelShape voxelshape;

                                    if (!(voxelshape = blockstate2.getCollisionShape(this.world, blockpos2, iselectioncontext)).isEmpty()) {
                                        f11 = (float) voxelshape.getEnd(Direction.Axis.Y) + (float) blockpos2.getY();

                                        if ((double) f11 - this.getPosY() > (double) f7) {
                                            return;
                                        }
                                    }

                                    if (i > 1) {
                                        blockpos = blockpos.up();
                                        BlockState blockstate3 = this.world.getBlockState(blockpos);

                                        if (!blockstate3.getCollisionShape(this.world, blockpos, iselectioncontext).isEmpty()) {
                                            return;
                                        }
                                    }
                                }

                                break;
                            }
                        }

                        if (f11 != Float.MIN_VALUE) {
                            float f14 = (float) ((double) f11 - this.getPosY());

                            if (!(f14 <= 0.5F) && !(f14 > f7)) {
                                this.autoJumpTime = 1;
                            }
                        }
                    }
                }
            }
        }
    }
    public org.joml.Vector2f getPreviousRotation() {
        return new org.joml.Vector2f(lastReportedYaw, lastReportedPitch);
    }
    private boolean canAutoJump() {
        return this.isAutoJumpEnabled() && this.autoJumpTime <= 0 && this.onGround && !this.isStayingOnGroundSurface() && !this.isPassenger() && this.isMoving() && (double) this.getJumpFactor() >= 1.0D;
    }

    public boolean isMoving() {
        Vector2f vector2f = this.movementInput.getMoveVector();
        return vector2f.x != 0.0F || vector2f.y != 0.0F;
    }

    private boolean isUsingSwimmingAnimation() {
        double d0 = 0.8D;
        return this.canSwim() ? this.movementInput.isMovingForward() : (double) this.movementInput.moveForward >= 0.8D;
    }

    public float getWaterBrightness() {
        if (!this.areEyesInFluid(FluidTags.WATER)) {
            return 0.0F;
        } else {
            float f = 600.0F;
            float f1 = 100.0F;

            if ((float) this.counterInWater >= 600.0F) {
                return 1.0F;
            } else {
                float f2 = MathHelper.clamp((float) this.counterInWater / 100.0F, 0.0F, 1.0F);
                float f3 = (float) this.counterInWater < 100.0F ? 0.0F : MathHelper.clamp(((float) this.counterInWater - 100.0F) / 500.0F, 0.0F, 1.0F);
                return f2 * 0.6F + f3 * 0.39999998F;
            }
        }
    }

    @Override
    public boolean canSwim() {
        return this.eyesInWaterPlayer;
    }

    @Override
    protected boolean updateEyesInWaterPlayer() {
        boolean flag = this.eyesInWaterPlayer;
        boolean flag1 = super.updateEyesInWaterPlayer();

        if (this.isSpectator()) {
            return this.eyesInWaterPlayer;
        } else {
            if (!flag && flag1) {
                this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
                this.mc.getSoundHandler().play(new UnderwaterAmbientSounds.UnderWaterSound(this));
            }

            if (flag && !flag1) {
                this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
            }

            return this.eyesInWaterPlayer;
        }
    }

    @Override
    public Vector3d getLeashPosition(float partialTicks) {
        if (this.mc.gameSettings.getPointOfView().func_243192_a()) {
            float f = MathHelper.lerp(partialTicks * 0.5F, this.rotationYaw, this.prevRotationYaw) * ((float) Math.PI / 180F);
            float f1 = MathHelper.lerp(partialTicks * 0.5F, this.rotationPitch, this.prevRotationPitch) * ((float) Math.PI / 180F);
            double d0 = this.getPrimaryHand() == HandSide.RIGHT ? -1.0D : 1.0D;
            Vector3d vector3d = new Vector3d(0.39D * d0, -0.6D, 0.3D);
            return vector3d.rotatePitch(-f1).rotateYaw(-f).add(this.getEyePosition(partialTicks));
        } else {
            return super.getLeashPosition(partialTicks);
        }
    }

    public static class WindowClickMemory {
        public int windowId;
        public int slotId;
        public int mouseButton;
        public int timeWait;
        public ClickType type;
        public PlayerEntity player;
        public TimeCounterSetting timerWaitAction = new TimeCounterSetting();

        public WindowClickMemory(int windowId, int slotId, int mouseButton, ClickType type, PlayerEntity player, int timeWait) {
            this.windowId = windowId;
            this.slotId = slotId;
            this.mouseButton = mouseButton;
            this.type = type;
            this.player = player;
            this.timerWaitAction.reset();
            this.timeWait = timeWait;
        }
    }
}
