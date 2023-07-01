package dev.zenhao.melon.utils.render;

import dev.zenhao.melon.module.modules.render.PopChams;
import dev.zenhao.melon.utils.gl.MelonTessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class TotemPopCham {
    private static final Minecraft mc = Minecraft.getMinecraft();
    EntityOtherPlayerMP player;
    ModelPlayer playerModel;
    Long startTime;
    double alphaFill;
    double alphaLine;

    public TotemPopCham(EntityOtherPlayerMP player, ModelPlayer playerModel, Long startTime, double alphaFill) {
        MinecraftForge.EVENT_BUS.register(this);
        this.player = player;
        this.playerModel = playerModel;
        this.startTime = startTime;
        this.alphaFill = alphaFill;
        this.alphaLine = alphaFill;
        if (player != null && mc.world != null && mc.player != null) {
            GL11.glLineWidth(1.0F);
            Color fillColorS = new Color(PopChams.INSTANCE.rF.getValue(), PopChams.INSTANCE.bF.getValue(), PopChams.INSTANCE.gF.getValue(), PopChams.INSTANCE.aF.getValue());
            int fillA = fillColorS.getAlpha();
            long time = System.currentTimeMillis() - this.startTime - ((Number) PopChams.INSTANCE.fadestart.getValue()).longValue();
            if (System.currentTimeMillis() - this.startTime > ((Number) PopChams.INSTANCE.fadestart.getValue()).longValue()) {
                double normal = this.normalize((double) time, 0.0D, ((Number) PopChams.INSTANCE.fadetime.getValue()).doubleValue());
                normal = MathHelper.clamp(normal, 0.0D, 1.0D);
                normal = -normal + 1.0D;
                fillA *= (int) normal;
            }

            Color fillColor = newAlpha(fillColorS, fillA);
            if (this.player != null && this.playerModel != null) {
                MelonTessellator.INSTANCE.prepare(GL_QUADS);
                GL11.glPushAttrib(1048575);
                GL11.glEnable(2881);
                GL11.glEnable(2848);
                if (alphaFill > 1.0D) {
                    alphaFill -= PopChams.INSTANCE.fadetime.getValue();
                }

                Color fillFinal = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int) alphaFill);

                glColor(fillFinal);
                GL11.glPolygonMode(1032, 6914);
                renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0F);
                GL11.glPolygonMode(1032, 6913);
                renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0F);
                GL11.glPolygonMode(1032, 6914);
                GL11.glPopAttrib();
                MelonTessellator.release();
            }

        }
    }

    public static void renderEntity(EntityLivingBase entity, ModelBase modelBase, float limbSwing, float limbSwingAmount, float scale) {
        if (mc.getRenderManager() != null) {
            float partialTicks = mc.getRenderPartialTicks();
            double x = entity.posX - mc.getRenderManager().viewerPosX;
            double y = entity.posY - mc.getRenderManager().viewerPosY;
            double z = entity.posZ - mc.getRenderManager().viewerPosZ;
            GlStateManager.pushMatrix();
            if (entity.isSneaking()) {
                y -= 0.125D;
            }

            renderLivingAt(x, y, z);
            prepareRotations(entity);
            float f9 = prepareScale(entity, scale);
            GlStateManager.enableAlpha();
            modelBase.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
            modelBase.setRotationAngles(limbSwing, limbSwingAmount, entity.rotationYaw, entity.rotationYawHead, entity.rotationPitch, f9, entity);
            modelBase.render(entity, limbSwing, limbSwingAmount, entity.rotationYaw, entity.rotationYawHead, entity.rotationPitch, f9);
            GlStateManager.popMatrix();
        }
    }

    public static void renderLivingAt(double x, double y, double z) {
        GlStateManager.translate((float) x, (float) y, (float) z);
    }

    public static float prepareScale(EntityLivingBase entity, float scale) {
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        double widthX = entity.getRenderBoundingBox().maxX - entity.getRenderBoundingBox().minX;
        double widthZ = entity.getRenderBoundingBox().maxZ - entity.getRenderBoundingBox().minZ;
        GlStateManager.scale((double) scale + widthX, scale * entity.height, (double) scale + widthZ);
        GlStateManager.translate(0.0F, -1.501F, 0.0F);
        return 0.0625F;
    }

    public static void prepareRotations(EntityLivingBase entityLivingBase) {
        GlStateManager.rotate(180.0F - entityLivingBase.rotationYaw, 0.0F, 1.0F, 0.0F);
    }

    public static Color newAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static void glColor(Color color) {
        GL11.glColor4f((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
    }

    public double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}
 
