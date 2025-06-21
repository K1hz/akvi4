package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import minecraft.system.capes.renderlayers.CustomCapeRenderLayer;
import minecraft.game.operation.misc.BadTrip;
import minecraft.system.AG;
import minecraft.game.transactions.EventModelRender;
import minecraft.game.operation.combat.DDATTACK;
import minecraft.game.advantage.advisee.ClientReceive;
import minecraft.game.advantage.figures.PlayerPositionTracker;
import minecraft.system.managers.friend.FriendManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class PlayerRenderer extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>
{

    public PlayerRenderer(EntityRendererManager renderManager)
    {
        this(renderManager, false);
    }
    public PlayerRenderer(EntityRendererManager renderManager, boolean useSmallArms)
    {
        super(renderManager, new PlayerModel<>(0.0F, useSmallArms), 0.5F);
            this.addLayer(new BipedArmorLayer<>(this, new BipedModel(0.5F), new BipedModel(1.0F)));
            this.addLayer(new HeldItemLayer<>(this));
            this.addLayer(new ArrowLayer<>(this));
            this.addLayer(new CustomCapeRenderLayer(this));
            this.addLayer(new HeadLayer<>(this));
            this.addLayer(new ElytraLayer<>(this));
            this.addLayer(new ParrotVariantLayer<>(this));
            this.addLayer(new SpinAttackEffectLayer<>(this));
            this.addLayer(new BeeStingerLayer<>(this));
    }

    public void render(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        this.setModelVisibilities(entityIn);
        if (AG.getInst().getModuleManager().getWallHack().isEnabled()) {
            GlStateManager.enablePolygonOffset();
            GlStateManager.polygonOffset(1.0F, -1000000F);
            super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            GlStateManager.disablePolygonOffset();
            GlStateManager.polygonOffset(1.0F, 1000000F);
        } else {
            if (PlayerPositionTracker.isInView(entityIn)) {
                EventModelRender modelRender = new EventModelRender(this, () -> {
                    super.renderFixed(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
                });
                if (entityIn instanceof PlayerEntity) {
                    AG.getInst().getEventBus().post(modelRender);
                    if (!modelRender.isCancel()) {
                        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
                    }
                } else {
                    super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
                }
            }
        }
    }

    public Vector3d getRenderOffset(AbstractClientPlayerEntity entityIn, float partialTicks)
    {
        return entityIn.isCrouching() ? new Vector3d(0.0D, -0.125D, 0.0D) : super.getRenderOffset(entityIn, partialTicks);
    }

    private void setModelVisibilities(AbstractClientPlayerEntity clientPlayer) {
        PlayerModel<AbstractClientPlayerEntity> playermodel = this.getEntityModel();

        if (clientPlayer.isSpectator()) {
            playermodel.setVisible(false);
            playermodel.bipedHead.showModel = true;
            playermodel.bipedHeadwear.showModel = true;
        } else {
            Minecraft mc = Minecraft.getInstance();
            boolean isLocalPlayer = clientPlayer == mc.player;
            boolean isFriend = FriendManager.isFriend(clientPlayer.getName().getString());
            boolean customModelsEnabled = AG.getInst().getModuleManager().getCustomModels().isEnabled();
            boolean applyToFriends = AG.getInst().getModuleManager().getCustomModels().friends.getValue();
            boolean shouldHideArmor = customModelsEnabled && (isLocalPlayer || (applyToFriends && isFriend));

            playermodel.setVisible(true); // Устанавливаем базовую видимость
            playermodel.bipedHeadwear.showModel = clientPlayer.isWearing(PlayerModelPart.HAT);

            // Управляем видимостью брони
            if (shouldHideArmor) {
                playermodel.bipedBodyWear.showModel = false;
                playermodel.bipedLeftLegwear.showModel = false;
                playermodel.bipedRightLegwear.showModel = false;
                playermodel.bipedLeftArmwear.showModel = false;
                playermodel.bipedRightArmwear.showModel = false;
            } else {
                playermodel.bipedBodyWear.showModel = clientPlayer.isWearing(PlayerModelPart.JACKET);
                playermodel.bipedLeftLegwear.showModel = clientPlayer.isWearing(PlayerModelPart.LEFT_PANTS_LEG);
                playermodel.bipedRightLegwear.showModel = clientPlayer.isWearing(PlayerModelPart.RIGHT_PANTS_LEG);
                playermodel.bipedLeftArmwear.showModel = clientPlayer.isWearing(PlayerModelPart.LEFT_SLEEVE);
                playermodel.bipedRightArmwear.showModel = clientPlayer.isWearing(PlayerModelPart.RIGHT_SLEEVE);
            }

            playermodel.isSneak = clientPlayer.isCrouching();
            BipedModel.ArmPose bipedmodel$armpose = func_241741_a_(clientPlayer, Hand.MAIN_HAND);
            BipedModel.ArmPose bipedmodel$armpose1 = func_241741_a_(clientPlayer, Hand.OFF_HAND);

            if (bipedmodel$armpose.func_241657_a_()) {
                bipedmodel$armpose1 = clientPlayer.getHeldItemOffhand().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
            }

            if (clientPlayer.getPrimaryHand() == HandSide.RIGHT) {
                playermodel.rightArmPose = bipedmodel$armpose;
                playermodel.leftArmPose = bipedmodel$armpose1;
            } else {
                playermodel.rightArmPose = bipedmodel$armpose1;
                playermodel.leftArmPose = bipedmodel$armpose;
            }
        }
    }

    private static BipedModel.ArmPose func_241741_a_(AbstractClientPlayerEntity p_241741_0_, Hand p_241741_1_)
    {
        ItemStack itemstack = p_241741_0_.getHeldItem(p_241741_1_);

        if (itemstack.isEmpty())
        {
            return BipedModel.ArmPose.EMPTY;
        }
        else
        {
            if (p_241741_0_.getActiveHand() == p_241741_1_ && p_241741_0_.getItemInUseCount() > 0)
            {
                UseAction useaction = itemstack.getUseAction();

                if (useaction == UseAction.BLOCK)
                {
                    return BipedModel.ArmPose.BLOCK;
                }

                if (useaction == UseAction.BOW)
                {
                    return BipedModel.ArmPose.BOW_AND_ARROW;
                }

                if (useaction == UseAction.SPEAR)
                {
                    return BipedModel.ArmPose.THROW_SPEAR;
                }

                if (useaction == UseAction.CROSSBOW && p_241741_1_ == p_241741_0_.getActiveHand())
                {
                    return BipedModel.ArmPose.CROSSBOW_CHARGE;
                }
            }
            else if (!p_241741_0_.isSwingInProgress && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack))
            {
                return BipedModel.ArmPose.CROSSBOW_HOLD;
            }

            return BipedModel.ArmPose.ITEM;
        }
    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getEntityTexture(AbstractClientPlayerEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        boolean isLocalPlayer = entity == mc.player;
        boolean isFriend = FriendManager.isFriend(entity.getName().getString());
        boolean shouldUseCustomModel = AG.getInst().getModuleManager().getCustomModels().isEnabled() &&
                (isLocalPlayer || (AG.getInst().getModuleManager().getCustomModels().friends.getValue() && isFriend));

        if (shouldUseCustomModel) {
            if (AG.getInst().getModuleManager().getCustomModels().mode.is("Crazy Rabbit")) {
                return new ResourceLocation("render/images/skins/3d/rabbit.png");
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("White Demon")) {
                return new ResourceLocation("render/images/skins/3d/whitedemon.png");
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("Red Demon")) {
                return new ResourceLocation("render/images/skins/3d/reddemon.png");
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("Freddy Bear")) {
                return new ResourceLocation("render/images/skins/3d/freddy.png");
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("Amogus")) {
                return new ResourceLocation("render/images/skins/3d/amogus.png");
            }
        }
        return entity.getLocationSkin();
    }

    @Override
    protected void preRenderCallback(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f) {
        if (!BadTrip.flexx) {
            matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
        } else {
            float timeFactor = (float) ((Math.sin((double) System.currentTimeMillis() / 300.0 * Math.PI) * 2 + 0.5));
            float widthHeightFactor = (float) (0.9375f + 0.8999998569488525 * timeFactor);

            float minY = 0.3375f;
            float maxY = 0.9375f;
            float smoothY = maxY - (maxY - minY) * ((float) Math.sin(System.currentTimeMillis() / 100.0) * 0.5f + 0.5f);

            matrixStack.scale(widthHeightFactor + 3, smoothY, widthHeightFactor + 3);
        }
    }


    protected void renderName(AbstractClientPlayerEntity entityIn, ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn)
    {
        double d0 = this.renderManager.squareDistanceTo(entityIn);
        matrixStackIn.push();

        DDATTACK DDATTACK = AG.getInst().getModuleManager().getDDATTACK();

        if (d0 < 100.0D) {
            Scoreboard scoreboard = entityIn.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null) {
                Score score = scoreboard.getOrCreateScore(entityIn.getScoreboardName(), scoreobjective);
                super.renderName(entityIn, (new StringTextComponent(Integer.toString(score.getScorePoints()))).appendString(" ").append(scoreobjective.getDisplayName()), matrixStackIn, bufferIn, packedLightIn);
                matrixStackIn.translate(0.0D, (double) (9.0F * 1.15F * 0.025F), 0.0D);
                if (DDATTACK.isEnabled() && DDATTACK.getTarget() != null && ClientReceive.isConnectedToServer("reallyworld") && ClientReceive.isPvP()) {
                    Score score2 = scoreboard.getOrCreateScore(DDATTACK.getTarget().getScoreboardName(), scoreobjective);
                    String scoreText = Integer.toString(score2.getScorePoints()) + " " + scoreobjective.getDisplayName();
                    String scoreNumber = scoreText.replaceAll("[^0-9]", "");
                    try {
                        int hp = Integer.parseInt(scoreNumber);
                        if (hp <= DDATTACK.getTarget().getMaxHealth()) {
                            DDATTACK.getTarget().setHealth((float) hp);
                        } else {
                        }
                    } catch (NumberFormatException e) {
                    }
                } else if (DDATTACK.isEnabled() && DDATTACK.getTarget() != null && ClientReceive.isConnectedToServer("reallyworld") && ClientReceive.isPvP()) {
                    DDATTACK.getTarget().setHealth(DDATTACK.getTarget().getMaxHealth());
                }
            }
        }

        super.renderName(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn);
        matrixStackIn.pop();
    }

    public void renderRightArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn)
    {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (this.entityModel).bipedRightArm, (this.entityModel).bipedRightArmwear);
    }

    public void renderLeftArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn)
    {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (this.entityModel).bipedLeftArm, (this.entityModel).bipedLeftArmwear);
    }

    private void renderItem(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn, ModelRenderer rendererArmIn, ModelRenderer rendererArmwearIn)
    {
        PlayerModel<AbstractClientPlayerEntity> playermodel = this.getEntityModel();
        this.setModelVisibilities(playerIn);
        playermodel.swingProgress = 0.0F;
        playermodel.isSneak = false;
        playermodel.swimAnimation = 0.0F;
        playermodel.setRotationAngles(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rendererArmIn.rotateAngleX = 0.0F;
        rendererArmIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntitySolid(playerIn.getLocationSkin())), combinedLightIn, OverlayTexture.NO_OVERLAY);
        rendererArmwearIn.rotateAngleX = 0.0F;
        rendererArmwearIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.getEntityTranslucent(playerIn.getLocationSkin())), combinedLightIn, OverlayTexture.NO_OVERLAY);
    }

    protected void applyRotations(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks)
    {
        float f = entityLiving.getSwimAnimation(partialTicks);

        if (entityLiving.isElytraFlying())
        {
            super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
            float f1 = (float)entityLiving.getTicksElytraFlying() + partialTicks;
            float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);

            if (!entityLiving.isSpinAttacking())
            {
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(f2 * (-90.0F - entityLiving.rotationPitch)));
            }

            Vector3d vector3d = entityLiving.getLook(partialTicks);
            Vector3d vector3d1 = entityLiving.getMotion();
            double d0 = Entity.horizontalMag(vector3d1);
            double d1 = Entity.horizontalMag(vector3d);

            if (d0 > 0.0D && d1 > 0.0D)
            {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStackIn.rotate(Vector3f.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
            }
        }
        else if (f > 0.0F)
        {
            super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
            float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.rotationPitch : -90.0F;
            float f4 = MathHelper.lerp(f, 0.0F, f3);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(f4));

            if (entityLiving.isActualySwimming())
            {
                matrixStackIn.translate(0.0D, -1.0D, (double)0.3F);
            }
        }
        else
        {
            super.applyRotations(entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks);
        }
    }
}
