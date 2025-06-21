package net.minecraft.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import minecraft.game.advantage.make.color.ColoringSystem;
import minecraft.game.operation.visual.ChinaHat;
import minecraft.game.operation.visual.Hud;
import minecraft.system.AG;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import static com.mojang.blaze3d.systems.RenderSystem.*;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR;
import static net.optifine.util.MathUtils.PI2;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11C.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLE_FAN;

public class HeadLayer<T extends LivingEntity, M extends EntityModel<T> & IHasHead> extends LayerRenderer<T, M> {
    private final float field_239402_a_;
    private final float field_239403_b_;
    private final float field_239404_c_;

    public HeadLayer(IEntityRenderer<T, M> p_i50946_1_) {
        this(p_i50946_1_, 1.0F, 1.0F, 1.0F);
    }

    public HeadLayer(IEntityRenderer<T, M> p_i232475_1_, float p_i232475_2_, float p_i232475_3_, float p_i232475_4_) {
        super(p_i232475_1_);
        this.field_239402_a_ = p_i232475_2_;
        this.field_239403_b_ = p_i232475_3_;
        this.field_239404_c_ = p_i232475_4_;
    }

    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EquipmentSlotType.HEAD);

        if (!itemstack.isEmpty()) {
            Item item = itemstack.getItem();
            matrixStackIn.push();
            matrixStackIn.scale(this.field_239402_a_, this.field_239403_b_, this.field_239404_c_);
            boolean flag = entitylivingbaseIn instanceof VillagerEntity || entitylivingbaseIn instanceof ZombieVillagerEntity;

            if (entitylivingbaseIn.isChild() && !(entitylivingbaseIn instanceof VillagerEntity)) {
                matrixStackIn.translate(0.0D, 0.03125D, 0.0D);
                matrixStackIn.scale(0.7F, 0.7F, 0.7F);
                matrixStackIn.translate(0.0D, 1.0D, 0.0D);
            }

            this.getEntityModel().getModelHead().translateRotate(matrixStackIn);
            if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof AbstractSkullBlock) {
                float f3 = 1.1875F;
                matrixStackIn.scale(f3, -f3, -f3);

                if (flag) {
                    matrixStackIn.translate(0.0D, 0.0625D, 0.0D);
                }

                GameProfile gameprofile = null;

                if (itemstack.hasTag()) {
                    CompoundNBT compoundnbt = itemstack.getTag();

                    if (compoundnbt.contains("SkullOwner", 10)) {
                        gameprofile = NBTUtil.readGameProfile(compoundnbt.getCompound("SkullOwner"));
                    } else if (compoundnbt.contains("SkullOwner", 8)) {
                        String s = compoundnbt.getString("SkullOwner");

                        if (!StringUtils.isBlank(s)) {
                            gameprofile = SkullTileEntity.updateGameProfile(new GameProfile(null, s));
                            compoundnbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
                        }
                    }
                }

                matrixStackIn.translate(-0.5D, 0.0D, -0.5D);
                SkullTileEntityRenderer.render(null, 180.0F, ((AbstractSkullBlock) ((BlockItem) item).getBlock()).getSkullType(), gameprofile, limbSwing, matrixStackIn, bufferIn, packedLightIn);
            } else if (!(item instanceof ArmorItem) || ((ArmorItem) item).getEquipmentSlot() != EquipmentSlotType.HEAD) {
                float f2 = 0.625F;
                matrixStackIn.translate(0.0D, -0.25D, 0.0D);
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F));
                matrixStackIn.scale(f2, -f2, -f2);

                if (flag) {
                    matrixStackIn.translate(0.0D, 0.1875D, 0.0D);
                }

                Minecraft.getInstance().getFirstPersonRenderer().renderItemSide(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.HEAD, false, matrixStackIn, bufferIn, packedLightIn);
            }

            matrixStackIn.pop();
        }
        if (ChinaHat.type.is("Новый")) {
            ChinaHat chinaHat = AG.getInst().getModuleManager().getChinaHat();
            if (chinaHat.isEnabled() && entitylivingbaseIn instanceof PlayerEntity player && ((player instanceof ClientPlayerEntity) || AG.getInst().getFriendManager().isFriend(TextFormatting.getTextWithoutFormattingCodes(player.getName().getString())))) {
                float width = player.getWidth();

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                enableDepthTest();
                disableTexture();
                enableBlend();
                defaultBlendFunc();
                disableCull();
                shadeModel(GL_SMOOTH);
                GL11.glEnable(GL_LINE_SMOOTH);
                lineWidth(0.03f);

                matrixStackIn.push();
                {
                    float offset = player.inventory.armorInventory.get(3).isEmpty() ? -.43f : -.519f;
                    getEntityModel().getModelHead().translateRotate(matrixStackIn);
                    matrixStackIn.translate(0, offset, 0);
                    matrixStackIn.rotate(Vector3f.ZN.rotationDegrees(180f));
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));

                    buffer.begin(GL_TRIANGLE_FAN, POSITION_COLOR);
                    {
                        buffer.pos(matrixStackIn.getLast().getMatrix(), 0, .225f, 0)
                                .color(ColoringSystem.setAlpha(Hud.getColor(1), 200))
                                .endVertex();
                        for (int i = 0, size = 360; i <= size; i++) {
                            buffer.pos(matrixStackIn.getLast().getMatrix(),
                                            -MathHelper.sin(i * PI2 / size) * 0.555, 0,
                                            MathHelper.cos(i * PI2 / size) * 0.555)
                                    .color(ColoringSystem.setAlpha(Hud.getColor(960), 255))
                                    .endVertex();
                        }
                    }
                    tessellator.draw();
                    buffer.begin(GL_LINE_LOOP, POSITION_COLOR);
                    {
                        for (int i = 0, size = 360; i <= size; i++) {
                            buffer.pos(matrixStackIn.getLast().getMatrix(),
                                            -MathHelper.sin(i * PI2 / size) * 0, 0,
                                            MathHelper.cos(i * PI2 / size) * 0)
                                    .color(ColoringSystem.setAlpha(Hud.getColor(2048), 200))
                                    .endVertex();
                        }
                    }
                    depthMask(false);
                    tessellator.draw();
                    depthMask(true);

                }
                matrixStackIn.pop();
                disableDepthTest();
                disableBlend();
                enableTexture();
                shadeModel(GL_FLAT);
                enableCull();
            }
        }
        if (ChinaHat.type.is("Обычный")) {
            if (AG.getInst().getModuleManager().getChinaHat().isEnabled() && entitylivingbaseIn instanceof PlayerEntity player && ((player instanceof ClientPlayerEntity) || AG.getInst().getFriendManager().isFriend(TextFormatting.getTextWithoutFormattingCodes(player.getName().getString())))) {
                float width = player.getWidth() + 0.02f;
                float time = (System.currentTimeMillis() % 5000) / 5000f * 360;

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                enableDepthTest();
                disableTexture();
                enableBlend();
                defaultBlendFunc();
                disableCull();
                shadeModel(GL_SMOOTH);
                GL11.glEnable(GL_LINE_SMOOTH);
                lineWidth(2);

                matrixStackIn.push();
                {
                    float offset = player.inventory.armorInventory.get(3).isEmpty() ? -.41f : -.5f;
                    if (AG.getInst().getModuleManager().getCustomModels().isEnabled()) {
                        if (AG.getInst().getModuleManager().getCustomModels().mode.is("Freddy Bear")) {
                            offset = player.inventory.armorInventory.get(3).isEmpty() ? -.41f : -.6f;
                        }
                        if (AG.getInst().getModuleManager().getCustomModels().mode.is("White Demon")) {
                            offset = player.inventory.armorInventory.get(3).isEmpty() ? -.41f : -.6f;
                        }
                        if (AG.getInst().getModuleManager().getCustomModels().mode.is("Red Demon")) {
                            offset = player.inventory.armorInventory.get(3).isEmpty() ? -.41f : -.6f;
                        }
                        if (AG.getInst().getModuleManager().getCustomModels().mode.is("Crazy Rabbit")) {
                            offset = player.inventory.armorInventory.get(3).isEmpty() ? -.41f : -.4f;
                        }
                    }
                    getEntityModel().getModelHead().translateRotate(matrixStackIn);
                    matrixStackIn.translate(0, offset, 0);
                    matrixStackIn.rotate(Vector3f.ZN.rotationDegrees(180f));
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));

                    buffer.begin(GL_TRIANGLE_FAN, POSITION_COLOR);
                    {
                        int centerColor = ColoringSystem.getColorBlack(0);
                        buffer.pos(matrixStackIn.getLast().getMatrix(), 0, .3f, 0)
                                .color(ColoringSystem.setAlpha(centerColor, 245))
                                .endVertex();
                        for (int i = 0, size = 360; i <= size; i++) {
                            int color = (i < 180) ? ColoringSystem.getColorBlack(i + (int) time) : ColoringSystem.getColorBlack(i + (int) time);
                            buffer.pos(matrixStackIn.getLast().getMatrix(),
                                            -MathHelper.sin((i + time) * PI2 / size) * width, 0,
                                            MathHelper.cos((i + time) * PI2 / size) * width)
                                    .color(ColoringSystem.setAlpha(color, 245))
                                    .endVertex();
                        }
                    }
                    tessellator.draw();
                    buffer.begin(GL_LINE_LOOP, POSITION_COLOR);
                    {
                        for (int i = 0, size = 360; i <= size; i++) {
                            int color = (i < 180) ? ColoringSystem.getColorBlack(i + (int) time) : ColoringSystem.getColorBlack(i + (int) time);
                            buffer.pos(matrixStackIn.getLast().getMatrix(),
                                            -MathHelper.sin((i + time) * PI2 / size) * width, 0,
                                            MathHelper.cos((i + time) * PI2 / size) * width)
                                    .color(ColoringSystem.setAlpha(color, 245))
                                    .endVertex();
                        }
                    }
                    depthMask(false);
                    tessellator.draw();
                    depthMask(true);
                }
                matrixStackIn.pop();
                disableDepthTest();
                disableBlend();
                enableTexture();
                shadeModel(GL_FLAT);
                enableCull();
            }
        }

        if (ChinaHat.type.is("Кастомный")) {
            if (AG.getInst().getModuleManager().getChinaHat().isEnabled() && entitylivingbaseIn instanceof PlayerEntity player && ((player instanceof ClientPlayerEntity) || AG.getInst().getFriendManager().isFriend(TextFormatting.getTextWithoutFormattingCodes(player.getName().getString())))) {
                float width = player.getWidth();

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                enableDepthTest();
                disableTexture();
                enableBlend();
                defaultBlendFunc();
                disableCull();
                shadeModel(GL_SMOOTH);
                GL11.glEnable(GL_LINE_SMOOTH);
                lineWidth(ChinaHat.толщина.getValue());

                matrixStackIn.push();
                {
                    float offset = player.inventory.armorInventory.get(3).isEmpty() ? -ChinaHat.posY.getValue() / 10 - 0.4f : -.5f - ChinaHat.posY.getValue() / 10;
                    getEntityModel().getModelHead().translateRotate(matrixStackIn);
                    matrixStackIn.translate(ChinaHat.posX.getValue(), offset, ChinaHat.posZ.getValue());
                    matrixStackIn.rotate(Vector3f.ZN.rotationDegrees(180f));
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90));

                    float alpha = ChinaHat.aaaa.getValue();
                    buffer.begin(GL_TRIANGLE_FAN, POSITION_COLOR);
                    {
                        buffer.pos(matrixStackIn.getLast().getMatrix(), 0, ChinaHat.высота.getValue() / 10, 0)
                                .color(ColoringSystem.setAlpha(Hud.getColor(0), (int) alpha))
                                .endVertex();
                        for (int i = 0, size = 360; i <= size; i++) {
                            buffer.pos(matrixStackIn.getLast().getMatrix(),
                                            -MathHelper.sin(i * PI2 / size) * ChinaHat.ширина.getValue() / 10, 0,
                                            MathHelper.cos(i * PI2 / size) * ChinaHat.ширина.getValue() / 10)
                                    .color(ColoringSystem.setAlpha(Hud.getColor(90), (int) alpha))
                                    .endVertex();
                        }
                    }
                    tessellator.draw();
                    buffer.begin(GL_LINE_LOOP, POSITION_COLOR);
                    {
                        for (int i = 0, size = 360; i <= size; i++) {
                            buffer.pos(matrixStackIn.getLast().getMatrix(),
                                            -MathHelper.sin(i * PI2 / size) * ChinaHat.ширина.getValue() / 10, 0,
                                            MathHelper.cos(i * PI2 / size) * ChinaHat.ширина.getValue() / 10)
                                    .color(ColoringSystem.setAlpha(Hud.getColor(90), (int) alpha))
                                    .endVertex();
                        }
                    }
                    depthMask(false);
                    tessellator.draw();
                    depthMask(true);

                }
                matrixStackIn.pop();
                disableDepthTest();
                disableBlend();
                enableTexture();
                shadeModel(GL_FLAT);
                enableCull();
            }
        }
    }
}
