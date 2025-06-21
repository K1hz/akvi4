package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import java.util.Random;
import minecraft.system.AG;
import minecraft.system.managers.friend.FriendManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.HandSide;

public class PlayerModel<T extends LivingEntity> extends BipedModel<T> {
    private T entity;
    private List<ModelRenderer> modelRenderers = Lists.newArrayList();
    public final ModelRenderer bipedLeftArmwear;
    public final ModelRenderer bipedRightArmwear;
    public final ModelRenderer bipedLeftLegwear;
    public final ModelRenderer bipedRightLegwear;
    public final ModelRenderer bipedBodyWear;
    private final ModelRenderer bipedCape;
    private final ModelRenderer bipedDeadmau5Head;
    private final boolean smallArms;
    private final ModelRenderer rabbitBone;
    private final ModelRenderer rabbitRleg;
    private final ModelRenderer rabbitLarm;
    private final ModelRenderer rabbitRarm;
    private final ModelRenderer rabbitLleg;
    private final ModelRenderer rabbitHead;

    private final ModelRenderer head7;
    private final ModelRenderer left_horn;
    private final ModelRenderer right_horn;
    private final ModelRenderer body7;
    private final ModelRenderer left_wing;
    private final ModelRenderer right_wing;
    private final ModelRenderer left_arm7;
    private final ModelRenderer right_arm7;
    private final ModelRenderer left_leg7;
    private final ModelRenderer left_leg1;
    private final ModelRenderer bone2;
    private final ModelRenderer bone3;
    private final ModelRenderer bone7;
    private final ModelRenderer right_leg7;
    private final ModelRenderer right_leg3;
    private final ModelRenderer bone4;
    private final ModelRenderer bone5;
    private final ModelRenderer bone6;

    public ModelRenderer fredbody;
    public ModelRenderer torso;
    public ModelRenderer armRight;
    public ModelRenderer crotch;
    public ModelRenderer legRight;
    public ModelRenderer legLeft;
    public ModelRenderer armLeft;
    public ModelRenderer fredhead;
    public ModelRenderer armRightpad;
    public ModelRenderer armRight2;
    public ModelRenderer armRightpad2;
    public ModelRenderer handRight;
    public ModelRenderer legRightpad;
    public ModelRenderer legRight2;
    public ModelRenderer legRightpad2;
    public ModelRenderer footRight;
    public ModelRenderer legLeftpad;
    public ModelRenderer legLeft2;
    public ModelRenderer legLeftpad2;
    public ModelRenderer footLeft;
    public ModelRenderer armLeftpad;
    public ModelRenderer armLeft2;
    public ModelRenderer armLeftpad2;
    public ModelRenderer handLeft;
    public ModelRenderer jaw;
    public ModelRenderer frednose;
    public ModelRenderer earRight;
    public ModelRenderer earLeft;
    public ModelRenderer hat;
    public ModelRenderer earRightpad;
    public ModelRenderer earRightpad_1;
    public ModelRenderer hat2;

    private final ModelRenderer body;
    private final ModelRenderer eye;
    private final ModelRenderer left_leg;
    private final ModelRenderer right_leg;

    public PlayerModel(float modelSize, boolean smallArmsIn) {
        super(RenderType::getEntityTranslucent, modelSize, 0.0F, 64, 64);
        this.smallArms = smallArmsIn;
        this.bipedDeadmau5Head = new ModelRenderer(this, 24, 0);
        this.bipedDeadmau5Head.addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, modelSize);
        this.bipedCape = new ModelRenderer(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, modelSize);

        if (smallArmsIn) {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArm = new ModelRenderer(this, 40, 16);
            this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);
        } else {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
        }

        this.bipedLeftLeg = new ModelRenderer(this, 16, 48);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
        this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRenderer(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
        this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedBodyWear = new ModelRenderer(this, 16, 32);
        this.bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, modelSize + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);

        this.rabbitBone = new ModelRenderer(this);
        this.rabbitBone.setRotationPoint(0.0F, 24.0F, 0.0F);
        this.rabbitBone.setTextureOffset(28, 45).addBox(-5.0F, -13.0F, -5.0F, 10, 11, 8, 0.0F, false);

        this.rabbitRleg = new ModelRenderer(this);
        this.rabbitRleg.setRotationPoint(-3.0F, -2.0F, -1.0F);
        this.rabbitBone.addChild(this.rabbitRleg);
        this.rabbitRleg.addBox(-2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F);

        this.rabbitLarm = new ModelRenderer(this);
        this.rabbitLarm.setRotationPoint(5.0F, -13.0F, -1.0F);
        this.setRotationAngle(this.rabbitLarm, 0.0F, 0.0F, -0.0873F);
        this.rabbitBone.addChild(this.rabbitLarm);
        this.rabbitLarm.addBox(0.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F);

        this.rabbitRarm = new ModelRenderer(this);
        this.rabbitRarm.setRotationPoint(-5.0F, -13.0F, -1.0F);
        this.setRotationAngle(this.rabbitRarm, 0.0F, 0.0F, 0.0873F);
        this.rabbitBone.addChild(this.rabbitRarm);
        this.rabbitRarm.addBox(-2.0F, 0.0F, -2.0F, 2, 8, 4, 0.0F);

        this.rabbitLleg = new ModelRenderer(this);
        this.rabbitLleg.setRotationPoint(3.0F, -2.0F, -1.0F);
        this.rabbitBone.addChild(this.rabbitLleg);
        this.rabbitLleg.addBox(-2.0F, 0.0F, -2.0F, 4, 2, 4, 0.0F);

        this.rabbitHead = new ModelRenderer(this);
        this.rabbitHead.setRotationPoint(0.0F, -14.0F, -1.0F);
        this.rabbitBone.addChild(this.rabbitHead);
        this.rabbitHead.setTextureOffset(0, 0).addBox(-3.0F, 0.0F, -4.0F, 6, 1, 6, 0.0F, false);
        this.rabbitHead.setTextureOffset(56, 0).addBox(-5.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, false);
        this.rabbitHead.setTextureOffset(56, 0).addBox(3.0F, -9.0F, -5.0F, 2, 3, 2, 0.0F, true);
        this.rabbitHead.setTextureOffset(0, 45).addBox(-4.0F, -11.0F, -4.0F, 8, 11, 8, 0.0F, false);
        this.rabbitHead.setTextureOffset(46, 0).addBox(1.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false);
        this.rabbitHead.setTextureOffset(46, 0).addBox(-4.0F, -20.0F, 0.0F, 3, 9, 1, 0.0F, false);
        this.rabbitHead.showModel = true;

        this.head7 = new ModelRenderer(this);
        this.head7.setRotationPoint(0.0F, -6.0F, -1.0F);
        this.head7.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -3.0F, 8.0F, 8.0F, 8.0F, 0.3F, false);

        this.left_horn = new ModelRenderer(this);
        this.left_horn.setRotationPoint(-8.0F, 8.0F, 0.0F);
        this.head7.addChild(this.left_horn);
        this.setRotationAngle(this.left_horn, -0.3927F, 0.3927F, -0.5236F);
        this.left_horn.setTextureOffset(32, 8).addBox(13.4346F, -5.2071F, 2.7071F, 6.0F, 2.0F, 2.0F, 0.1F, false);
        this.left_horn.setTextureOffset(0, 0).addBox(17.4346F, -10.4071F, 2.7071F, 2.0F, 5.0F, 2.0F, 0.1F, false);

        this.right_horn = new ModelRenderer(this);
        this.right_horn.setRotationPoint(8.0F, 8.0F, 0.0F);
        this.head7.addChild(this.right_horn);
        this.setRotationAngle(this.right_horn, -0.3927F, -0.3927F, 0.5236F);
        this.right_horn.setTextureOffset(32, 8).addBox(-19.4346F, -5.2071F, 2.7071F, 6.0F, 2.0F, 2.0F, 0.1F, true);
        this.right_horn.setTextureOffset(0, 0).addBox(-19.4346F, -10.4071F, 2.7071F, 2.0F, 5.0F, 2.0F, 0.1F, true);

        this.body7 = new ModelRenderer(this);
        this.body7.setRotationPoint(0.5F, -0.1F, -3.5F);
        this.setRotationAngle(this.body7, 0.1745F, 0.0F, 0.0F);
        this.body7.setTextureOffset(0, 16).addBox(-4.5F, -1.7028F, 1.4696F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        this.left_wing = new ModelRenderer(this);
        this.left_wing.setRotationPoint(8.25F, -2.0F, 10.0F);
        this.body7.addChild(this.left_wing);
        this.setRotationAngle(this.left_wing, 0.0873F, -0.829F, 0.1745F);
        this.left_wing.setTextureOffset(40, 12).addBox(-7.0072F, -0.5972F, 0.7515F, 12.0F, 13.0F, 0.0F, 0.0F, false);

        this.right_wing = new ModelRenderer(this);
        this.right_wing.setRotationPoint(-9.25F, -2.0F, 10.0F);
        this.body7.addChild(this.right_wing);
        this.setRotationAngle(this.right_wing, 0.0873F, 0.829F, -0.1745F);
        this.right_wing.setTextureOffset(40, 12).addBox(-4.9928F, -0.5972F, 0.7515F, 12.0F, 13.0F, 0.0F, 0.0F, true);

        this.left_arm7 = new ModelRenderer(this);
        this.left_arm7.setRotationPoint(5.4F, -1.25F, -2.0F);
        this.setRotationAngle(this.left_arm7, 0.0F, 0.0F, -0.2182F);
        this.left_arm7.setTextureOffset(24, 16).addBox(-1.1F, -1.05F, 0.0F, 4.0F, 14.0F, 4.0F, 0.0F, false);

        this.right_arm7 = new ModelRenderer(this);
        this.right_arm7.setRotationPoint(-5.4F, -1.25F, -2.0F);
        this.setRotationAngle(this.right_arm7, 0.0F, 0.0F, 0.2182F);
        this.right_arm7.setTextureOffset(24, 16).addBox(-2.9F, -1.05F, 0.0F, 4.0F, 14.0F, 4.0F, 0.0F, true);

        this.left_leg7 = new ModelRenderer(this);
        this.left_leg7.setRotationPoint(3.0F, 10.0F, 0.0F);
        this.left_leg7.setTextureOffset(48, 22).addBox(-3.25F, -2.25F, -1.0F, 4.0F, 9.0F, 4.0F, 0.0F, false);

        this.left_leg1 = new ModelRenderer(this);
        this.left_leg1.setRotationPoint(-1.7F, -0.1F, -3.55F);
        this.left_leg7.addChild(this.left_leg1);
        this.setRotationAngle(this.left_leg1, -0.5236F, 0.0F, 0.0F);
        this.left_leg1.setTextureOffset(34, 34).addBox(0.95F, 4.6F, 8.0511F, 3.0F, 5.0F, 3.0F, 0.0F, false);

        this.bone2 = new ModelRenderer(this);
        this.bone2.setRotationPoint(1.4F, 15.0F, 0.25F);
        this.left_leg1.addChild(this.bone2);
        this.setRotationAngle(this.bone2, 0.5236F, 0.0F, 0.0F);
        this.bone2.setTextureOffset(26, 0).addBox(-0.7F, -1.15F, 9.3F, 4.0F, 2.0F, 4.0F, 0.0F, false);
        this.bone2.setTextureOffset(40, 0).addBox(-0.7F, -1.15F, 7.3F, 4.0F, 2.0F, 2.0F, 0.0F, false);

        this.bone3 = new ModelRenderer(this);
        this.bone3.setRotationPoint(-1.0F, 0.0F, -2.0F);
        this.left_leg1.addChild(this.bone3);
        this.setRotationAngle(this.bone3, 0.0F, -0.0873F, -0.2618F);

        this.bone7 = new ModelRenderer(this);
        this.bone7.setRotationPoint(1.9F, 12.0F, 0.25F);
        this.bone3.addChild(this.bone7);
        this.bone7.setTextureOffset(16, 34).addBox(-0.7911F, -10.1159F, 8.0029F, 4.0F, 4.0F, 5.0F, 0.0F, false);
        this.bone7.setTextureOffset(0, 32).addBox(-0.7911F, -15.1159F, 4.0029F, 4.0F, 9.0F, 4.0F, 0.0F, false);

        this.right_leg7 = new ModelRenderer(this);
        this.right_leg7.setRotationPoint(-3.0F, 10.0F, 0.0F);
        this.right_leg7.setTextureOffset(48, 22).addBox(-0.75F, -2.25F, -1.0F, 4.0F, 9.0F, 4.0F, 0.0F, true);

        this.right_leg3 = new ModelRenderer(this);
        this.right_leg3.setRotationPoint(1.7F, -0.1F, -3.55F);
        this.right_leg7.addChild(this.right_leg3);
        this.setRotationAngle(this.right_leg3, -0.5236F, 0.0F, 0.0F);
        this.right_leg3.setTextureOffset(34, 34).addBox(-3.95F, 4.6F, 8.0511F, 3.0F, 5.0F, 3.0F, 0.0F, true);

        this.bone4 = new ModelRenderer(this);
        this.bone4.setRotationPoint(-1.4F, 15.0F, 0.25F);
        this.right_leg3.addChild(this.bone4);
        this.setRotationAngle(this.bone4, 0.5236F, 0.0F, 0.0F);
        this.bone4.setTextureOffset(26, 0).addBox(-3.3F, -1.15F, 9.3F, 4.0F, 2.0F, 4.0F, 0.0F, true);
        this.bone4.setTextureOffset(40, 0).addBox(-3.3F, -1.15F, 7.3F, 4.0F, 2.0F, 2.0F, 0.0F, true);

        this.bone5 = new ModelRenderer(this);
        this.bone5.setRotationPoint(1.0F, 0.0F, -2.0F);
        this.right_leg3.addChild(this.bone5);
        this.setRotationAngle(this.bone5, 0.0F, 0.0873F, 0.2618F);

        this.bone6 = new ModelRenderer(this);
        this.bone6.setRotationPoint(-1.9F, 12.0F, 0.25F);
        this.bone5.addChild(this.bone6);
        this.bone6.setTextureOffset(16, 34).addBox(-3.2089F, -10.1159F, 8.0029F, 4.0F, 4.0F, 5.0F, 0.0F, true);
        this.bone6.setTextureOffset(0, 32).addBox(-3.2089F, -15.1159F, 4.0029F, 4.0F, 9.0F, 4.0F, 0.0F, true);

        // Freddy Bear model initialization
        this.textureWidth = 100;
        this.textureHeight = 80;

        this.fredbody = new ModelRenderer(this, 0, 0);
        this.fredbody.setRotationPoint(0.0F, -9.0F, 0.0F);
        this.fredbody.addBox(-1.0F, -14.0F, -1.0F, 2, 24, 2, 0.0F);

        this.torso = new ModelRenderer(this, 8, 0);
        this.torso.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.torso.addBox(-6.0F, -9.0F, -4.0F, 12, 18, 8, 0.0F);
        this.setRotationAngle(this.torso, (float)Math.PI / 180, 0.0F, 0.0F);

        this.armRight = new ModelRenderer(this, 48, 0);
        this.armRight.setRotationPoint(-6.5F, -8.0F, 0.0F);
        this.armRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
        this.setRotationAngle(this.armRight, 0.0F, 0.0F, 0.2617994F);

        this.crotch = new ModelRenderer(this, 56, 0);
        this.crotch.setRotationPoint(0.0F, 9.5F, 0.0F);
        this.crotch.addBox(-5.5F, 0.0F, -3.5F, 11, 3, 7, 0.0F);

        this.legRight = new ModelRenderer(this, 90, 8);
        this.legRight.setRotationPoint(-3.3F, 12.5F, 0.0F);
        this.legRight.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);

        this.legLeft = new ModelRenderer(this, 54, 10);
        this.legLeft.setRotationPoint(3.3F, 12.5F, 0.0F);
        this.legLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);

        this.armLeft = new ModelRenderer(this, 62, 10);
        this.armLeft.setRotationPoint(6.5F, -8.0F, 0.0F);
        this.armLeft.addBox(-1.0F, 0.0F, -1.0F, 2, 10, 2, 0.0F);
        this.setRotationAngle(this.armLeft, 0.0F, 0.0F, -0.2617994F);

        this.fredhead = new ModelRenderer(this, 39, 22);
        this.fredhead.setRotationPoint(0.0F, -13.0F, -0.5F);
        this.fredhead.addBox(-5.5F, -8.0F, -4.5F, 11, 8, 9, 0.0F);

        this.armRightpad = new ModelRenderer(this, 70, 10);
        this.armRightpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.armRightpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);

        this.armRight2 = new ModelRenderer(this, 90, 20);
        this.armRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
        this.armRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.setRotationAngle(this.armRight2, -0.17453292F, 0.0F, 0.0F);

        this.armRightpad2 = new ModelRenderer(this, 0, 26);
        this.armRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.armRightpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);

        this.handRight = new ModelRenderer(this, 20, 26);
        this.handRight.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.handRight.addBox(-2.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
        this.setRotationAngle(this.handRight, 0.0F, 0.0F, -0.05235988F);

        this.legRightpad = new ModelRenderer(this, 73, 33);
        this.legRightpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.legRightpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);

        this.legRight2 = new ModelRenderer(this, 20, 35);
        this.legRight2.setRotationPoint(0.0F, 9.6F, 0.0F);
        this.legRight2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.setRotationAngle(this.legRight2, (float)Math.PI / 90, 0.0F, 0.0F);

        this.legRightpad2 = new ModelRenderer(this, 0, 39);
        this.legRightpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.legRightpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);

        this.footRight = new ModelRenderer(this, 22, 39);
        this.footRight.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.footRight.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
        this.setRotationAngle(this.footRight, (float)(-Math.PI) / 90, 0.0F, 0.0F);

        this.legLeftpad = new ModelRenderer(this, 48, 39);
        this.legLeftpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.legLeftpad.addBox(-3.0F, 0.0F, -3.0F, 6, 9, 6, 0.0F);

        this.legLeft2 = new ModelRenderer(this, 72, 48);
        this.legLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
        this.legLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.setRotationAngle(this.legLeft2, (float)Math.PI / 90, 0.0F, 0.0F);

        this.legLeftpad2 = new ModelRenderer(this, 16, 50);
        this.legLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.legLeftpad2.addBox(-2.5F, 0.0F, -3.0F, 5, 7, 6, 0.0F);

        this.footLeft = new ModelRenderer(this, 72, 50);
        this.footLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.footLeft.addBox(-2.5F, 0.0F, -6.0F, 5, 3, 8, 0.0F);
        this.setRotationAngle(this.footLeft, (float)(-Math.PI) / 90, 0.0F, 0.0F);

        this.armLeftpad = new ModelRenderer(this, 38, 54);
        this.armLeftpad.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.armLeftpad.addBox(-2.5F, 0.0F, -2.5F, 5, 9, 5, 0.0F);

        this.armLeft2 = new ModelRenderer(this, 90, 48);
        this.armLeft2.setRotationPoint(0.0F, 9.6F, 0.0F);
        this.armLeft2.addBox(-1.0F, 0.0F, -1.0F, 2, 8, 2, 0.0F);
        this.setRotationAngle(this.armLeft2, -0.17453292F, 0.0F, 0.0F);

        this.armLeftpad2 = new ModelRenderer(this, 0, 58);
        this.armLeftpad2.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.armLeftpad2.addBox(-2.5F, 0.0F, -2.5F, 5, 7, 5, 0.0F);

        this.handLeft = new ModelRenderer(this, 58, 56);
        this.handLeft.setRotationPoint(0.0F, 8.0F, 0.0F);
        this.handLeft.addBox(-1.0F, 0.0F, -2.5F, 4, 4, 5, 0.0F);
        this.setRotationAngle(this.handLeft, 0.0F, 0.0F, 0.05235988F);

        this.jaw = new ModelRenderer(this, 49, 65);
        this.jaw.setRotationPoint(0.0F, 0.5F, 0.0F);
        this.jaw.addBox(-5.0F, 0.0F, -4.5F, 10, 3, 9, 0.0F);
        this.setRotationAngle(this.jaw, 0.08726646F, 0.0F, 0.0F);

        this.frednose = new ModelRenderer(this, 17, 67);
        this.frednose.setRotationPoint(0.0F, -2.0F, -4.5F);
        this.frednose.addBox(-4.0F, -2.0F, -3.0F, 8, 4, 3, 0.0F);

        this.earRight = new ModelRenderer(this, 8, 0);
        this.earRight.setRotationPoint(-4.5F, -5.5F, 0.0F);
        this.earRight.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
        this.setRotationAngle(this.earRight, 0.05235988F, 0.0F, -1.0471976F);

        this.earLeft = new ModelRenderer(this, 40, 0);
        this.earLeft.setRotationPoint(4.5F, -5.5F, 0.0F);
        this.earLeft.addBox(-1.0F, -3.0F, -0.5F, 2, 3, 1, 0.0F);
        this.setRotationAngle(this.earLeft, 0.05235988F, 0.0F, 1.0471976F);

        this.hat = new ModelRenderer(this, 70, 24);
        this.hat.setRotationPoint(0.0F, -8.4F, 0.0F);
        this.hat.addBox(-3.0F, -0.5F, -3.0F, 6, 1, 6, 0.0F);
        this.setRotationAngle(this.hat, (float)(-Math.PI) / 180, 0.0F, 0.0F);

        this.earRightpad = new ModelRenderer(this, 85, 0);
        this.earRightpad.setRotationPoint(0.0F, -1.0F, 0.0F);
        this.earRightpad.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);

        this.earRightpad_1 = new ModelRenderer(this, 40, 39);
        this.earRightpad_1.setRotationPoint(0.0F, -1.0F, 0.0F);
        this.earRightpad_1.addBox(-2.0F, -5.0F, -1.0F, 4, 4, 2, 0.0F);

        this.hat2 = new ModelRenderer(this, 78, 61);
        this.hat2.setRotationPoint(0.0F, 0.1F, 0.0F);
        this.hat2.addBox(-2.0F, -4.0F, -2.0F, 4, 4, 4, 0.0F);
        this.setRotationAngle(this.hat2, (float)(-Math.PI) / 180, 0.0F, 0.0F);

        this.legRight2.addChild(this.footRight);
        this.fredhead.addChild(this.earRight);
        this.legLeft.addChild(this.legLeftpad);
        this.earLeft.addChild(this.earRightpad_1);
        this.fredbody.addChild(this.legLeft);
        this.armRight2.addChild(this.armRightpad2);
        this.armLeft2.addChild(this.handLeft);
        this.fredbody.addChild(this.armLeft);
        this.fredbody.addChild(this.legRight);
        this.armLeft.addChild(this.armLeft2);
        this.legRight.addChild(this.legRight2);
        this.armLeft2.addChild(this.armLeftpad2);
        this.legLeft.addChild(this.legLeft2);
        this.fredhead.addChild(this.hat);
        this.earRight.addChild(this.earRightpad);
        this.fredbody.addChild(this.crotch);
        this.fredbody.addChild(this.torso);
        this.armRight.addChild(this.armRight2);
        this.armRight2.addChild(this.handRight);
        this.fredbody.addChild(this.fredhead);
        this.legRight.addChild(this.legRightpad);
        this.fredhead.addChild(this.frednose);
        this.legLeft2.addChild(this.legLeftpad2);
        this.armRight.addChild(this.armRightpad);
        this.armLeft.addChild(this.armLeftpad);
        this.hat.addChild(this.hat2);
        this.legRight2.addChild(this.legRightpad2);
        this.fredhead.addChild(this.jaw);
        this.fredbody.addChild(this.armRight);
        this.legLeft2.addChild(this.footLeft);
        this.fredhead.addChild(this.earLeft);

        this.body = new ModelRenderer(this);
        this.body.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.body.setTextureOffset(34, 8).addBox(-4.0F, 6.0F, -3.0F, 8, 12, 6);
        this.body.setTextureOffset(15, 10).addBox(-3.0F, 9.0F, 3.0F, 6, 8, 3);
        this.body.setTextureOffset(26, 0).addBox(-3.0F, 5.0F, -3.0F, 6, 1, 6);

        this.eye = new ModelRenderer(this);
        this.eye.setTextureOffset(0, 10).addBox(-3.0F, 7.0F, -4.0F, 6, 4, 1);

        this.left_leg = new ModelRenderer(this);
        this.left_leg.setRotationPoint(-2.0F, 18.0F, 0.0F);
        this.left_leg.setTextureOffset(0, 0).addBox(2.9F, 0.0F, -1.5F, 3, 6, 3, 0.0F);

        this.right_leg = new ModelRenderer(this);
        this.right_leg.setRotationPoint(2.0F, 18.0F, 0.0F);
        this.right_leg.setTextureOffset(13, 0).addBox(-5.9F, 0.0F, -1.5F, 3, 6, 3);
    }

    protected Iterable<ModelRenderer> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.bipedLeftLegwear, this.bipedRightLegwear, this.bipedLeftArmwear, this.bipedRightArmwear, this.bipedBodyWear));
    }

    public void renderEars(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn) {
        this.bipedDeadmau5Head.copyModelAngles(this.bipedHead);
        this.bipedDeadmau5Head.rotationPointX = 0.0F;
        this.bipedDeadmau5Head.rotationPointY = 0.0F;
        this.bipedDeadmau5Head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    public void renderCape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn) {
        this.bipedCape.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        boolean isLocalPlayer = mc.player != null && this.entity == mc.player;
        boolean isFriend = FriendManager.isFriend(this.entity.getName().getString());
        boolean shouldUseCustomModel = AG.getInst().getModuleManager().getCustomModels().isEnabled() &&
                (isLocalPlayer || (AG.getInst().getModuleManager().getCustomModels().friends.getValue() && isFriend));

        if (shouldUseCustomModel) {
            if (AG.getInst().getModuleManager().getCustomModels().mode.is("Crazy Rabbit")) {
                matrixStackIn.push();
                matrixStackIn.scale(1.25F, 1.25F, 1.25F);
                matrixStackIn.translate(0.0F, -0.3F, 0.0F);
                this.rabbitHead.rotateAngleX = this.bipedHead.rotateAngleX;
                this.rabbitHead.rotateAngleY = this.bipedHead.rotateAngleY;
                this.rabbitHead.rotateAngleZ = this.bipedHead.rotateAngleZ;
                this.rabbitLarm.rotateAngleX = this.bipedLeftArm.rotateAngleX;
                this.rabbitLarm.rotateAngleY = this.bipedLeftArm.rotateAngleY;
                this.rabbitLarm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ - 0.0873F;
                this.rabbitRarm.rotateAngleX = this.bipedRightArm.rotateAngleX;
                this.rabbitRarm.rotateAngleY = this.bipedRightArm.rotateAngleY;
                this.rabbitRarm.rotateAngleZ = this.bipedRightArm.rotateAngleZ + 0.0873F;
                this.rabbitRleg.rotateAngleX = this.bipedRightLeg.rotateAngleX;
                this.rabbitRleg.rotateAngleY = this.bipedRightLeg.rotateAngleY;
                this.rabbitRleg.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
                this.rabbitLleg.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
                this.rabbitLleg.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
                this.rabbitLleg.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
                this.rabbitBone.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                matrixStackIn.pop();
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("White Demon")) {
                matrixStackIn.push();
                this.head7.rotateAngleX = this.bipedHead.rotateAngleX;
                this.head7.rotateAngleY = this.bipedHead.rotateAngleY;
                this.head7.rotateAngleZ = this.bipedHead.rotateAngleZ;
                this.right_leg7.rotateAngleX = this.bipedRightLeg.rotateAngleX;
                this.right_leg7.rotateAngleY = this.bipedRightLeg.rotateAngleY;
                this.right_leg7.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
                this.left_leg7.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
                this.left_leg7.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
                this.left_leg7.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
                this.left_arm7.rotateAngleX = this.bipedLeftArm.rotateAngleX;
                this.left_arm7.rotateAngleY = this.bipedLeftArm.rotateAngleY;
                this.left_arm7.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
                this.right_arm7.rotateAngleX = this.bipedRightArm.rotateAngleX;
                this.right_arm7.rotateAngleY = this.bipedRightArm.rotateAngleY;
                this.right_arm7.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
                this.head7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_horn.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_horn.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.body7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_wing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_wing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_arm7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_arm7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_leg7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_leg7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                matrixStackIn.pop();
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("Red Demon")) {
                matrixStackIn.push();
                this.head7.rotateAngleX = this.bipedHead.rotateAngleX;
                this.head7.rotateAngleY = this.bipedHead.rotateAngleY;
                this.head7.rotateAngleZ = this.bipedHead.rotateAngleZ;
                this.right_leg7.rotateAngleX = this.bipedRightLeg.rotateAngleX;
                this.right_leg7.rotateAngleY = this.bipedRightLeg.rotateAngleY;
                this.right_leg7.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
                this.left_leg7.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
                this.left_leg7.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
                this.left_leg7.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
                this.left_arm7.rotateAngleX = this.bipedLeftArm.rotateAngleX;
                this.left_arm7.rotateAngleY = this.bipedLeftArm.rotateAngleY;
                this.left_arm7.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
                this.right_arm7.rotateAngleX = this.bipedRightArm.rotateAngleX;
                this.right_arm7.rotateAngleY = this.bipedRightArm.rotateAngleY;
                this.right_arm7.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
                this.head7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_horn.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_horn.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.body7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_wing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_wing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_arm7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_arm7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_leg7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_leg7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                matrixStackIn.pop();
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("Freddy Bear")) {
                matrixStackIn.push();
                this.fredhead.rotateAngleX = this.bipedHead.rotateAngleX;
                this.fredhead.rotateAngleY = this.bipedHead.rotateAngleY;
                this.fredhead.rotateAngleZ = this.bipedHead.rotateAngleZ;
                this.armLeft.rotateAngleX = this.bipedLeftArm.rotateAngleX;
                this.armLeft.rotateAngleY = this.bipedLeftArm.rotateAngleY;
                this.armLeft.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
                this.legRight.rotateAngleX = this.bipedRightLeg.rotateAngleX;
                this.legRight.rotateAngleY = this.bipedRightLeg.rotateAngleY;
                this.legRight.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
                this.legLeft.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
                this.legLeft.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
                this.legLeft.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
                this.armRight.rotateAngleX = this.bipedRightArm.rotateAngleX;
                this.armRight.rotateAngleY = this.bipedRightArm.rotateAngleY;
                this.armRight.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
                matrixStackIn.scale(0.75F, 0.65F, 0.75F);
                matrixStackIn.translate(0.0F, 0.85F, 0.0F);
                this.fredbody.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                matrixStackIn.pop();
            } else if (AG.getInst().getModuleManager().getCustomModels().mode.is("Amogus")) {
                matrixStackIn.push();
                matrixStackIn.scale(1.0F, 1.0F, 1.0F);
                matrixStackIn.translate(0.0F, -0.5F, 0.0F);
                this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.eye.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.left_leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.right_leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                matrixStackIn.pop();
            } else {
                matrixStackIn.push();
                super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
                this.bipedLeftLegwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedRightLegwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedLeftArmwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedRightArmwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedBodyWear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                matrixStackIn.pop();
            }
        } else {
            matrixStackIn.push();
            super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            this.bipedLeftLegwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.bipedRightLegwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.bipedLeftArmwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.bipedRightArmwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.bipedBodyWear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            matrixStackIn.pop();
        }
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entityIn.isSprinting() && AG.getInst().getModuleManager().getAnimation().isEnabled() && AG.getInst().getModuleManager().getAnimation().modes.is("Наруто")) {
            float backwardAngle = (float) (Math.PI * 0.5);
            this.bipedRightArm.rotateAngleX = backwardAngle;
            this.bipedLeftArm.rotateAngleX = backwardAngle;
            this.bipedRightArm.rotateAngleZ = 0.0F;
            this.bipedLeftArm.rotateAngleZ = 0.0F;
            this.bipedRightArm.rotateAngleY = 0.0F;
            this.bipedLeftArm.rotateAngleY = 0.0F;
            this.bipedHead.rotateAngleX += 0.3F;
            this.bipedHeadwear.rotateAngleX += 0.3F;
            this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
            this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
        }
        this.entity = entityIn; // Сохраняем сущность
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bipedLeftLegwear.copyModelAngles(this.bipedLeftLeg);
        this.bipedRightLegwear.copyModelAngles(this.bipedRightLeg);
        this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
        this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
        this.bipedBodyWear.copyModelAngles(this.bipedBody);

        if (entityIn.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty()) {
            if (entityIn.isCrouching()) {
                this.bipedCape.rotationPointZ = 1.4F;
                this.bipedCape.rotationPointY = 1.85F;
            } else {
                this.bipedCape.rotationPointZ = 0.0F;
                this.bipedCape.rotationPointY = 0.0F;
            }
        } else if (entityIn.isCrouching()) {
            this.bipedCape.rotationPointZ = 0.3F;
            this.bipedCape.rotationPointY = 0.8F;
        } else {
            this.bipedCape.rotationPointZ = -1.1F;
            this.bipedCape.rotationPointY = -0.85F;
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.bipedLeftArmwear.showModel = visible;
        this.bipedRightArmwear.showModel = visible;
        this.bipedLeftLegwear.showModel = visible;
        this.bipedRightLegwear.showModel = visible;
        this.bipedBodyWear.showModel = visible;
        this.bipedCape.showModel = visible;
        this.bipedDeadmau5Head.showModel = visible;
    }

    public void translateHand(HandSide sideIn, MatrixStack matrixStackIn) {
        ModelRenderer modelrenderer = this.getArmForSide(sideIn);
        if (AG.getInst().getModuleManager().getCustomModels().isEnabled()) {
            if (this.smallArms) {
                float f = 0.1F * (float)(sideIn == HandSide.RIGHT ? 1 : -1);
                modelrenderer.rotationPointX += f;
                modelrenderer.translateRotate(matrixStackIn);
                modelrenderer.rotationPointX -= f;
            } else {
                modelrenderer.translateRotate(matrixStackIn);
            }
        } else {
            if (this.smallArms) {
                float f = 0.5F * (float)(sideIn == HandSide.RIGHT ? 1 : -1);
                modelrenderer.rotationPointX += f;
                modelrenderer.translateRotate(matrixStackIn);
                modelrenderer.rotationPointX -= f;
            } else {
                modelrenderer.translateRotate(matrixStackIn);
            }
        }
    }

    public ModelRenderer getRandomModelRenderer(Random randomIn) {
        return this.modelRenderers.get(randomIn.nextInt(this.modelRenderers.size()));
    }

    public void accept(ModelRenderer p_accept_1_) {
        if (this.modelRenderers == null) {
            this.modelRenderers = Lists.newArrayList();
        }
        this.modelRenderers.add(p_accept_1_);
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}