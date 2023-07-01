package dev.zenhao.melon.utils.entity;

import dev.zenhao.melon.utils.Wrapper;
import dev.zenhao.melon.utils.inventory.InventoryUtil;
import dev.zenhao.melon.utils.render.GlStateUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CrystalUtil {
    static Minecraft mc = Minecraft.getMinecraft();

    public static List<BlockPos> getSphere(BlockPos loc, double r, double h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = loc.x;
        int cy = loc.y;
        int cz = loc.z;
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static List<BlockPos> getSphereVec(Vec3d loc, double r, double h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = (int) loc.x;
        int cy = (int) loc.y;
        int cz = (int) loc.z;
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static void targetHUD(EntityLivingBase entity, float red, float green, float blue, float alpha, float width) {
        boolean b = Wrapper.getMinecraft().getRenderManager().options.thirdPersonView == 2;
        float playerViewY = Wrapper.getMinecraft().getRenderManager().playerViewY;
        Vec3d interpolatedPos = EntityUtil.getInterpolatedPos(entity, mc.getRenderPartialTicks());
        GlStateManager.pushMatrix();
        GlStateManager.translate(interpolatedPos.x - mc.getRenderManager().renderPosX, interpolatedPos.y - mc.getRenderManager().renderPosY, interpolatedPos.z - mc.getRenderManager().renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) (b ? -1 : 1), 1.0f, 0.0f, 0.0f);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GL11.glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(width);
        GL11.glEnable(2848);
        GL11.glBegin(2);
        GL11.glVertex2d(-entity.width, 0.0);
        GL11.glVertex2d(-entity.width, entity.height);
        GL11.glVertex2d(entity.width, entity.height);
        GL11.glVertex2d(entity.width, 0.0);
        GL11.glEnd();
        GlStateManager.popMatrix();
    }

    public static Vec3d getPlayerPos(EntityPlayer player) {
        return new Vec3d(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
    }

    protected static double getDirection2D(double dx, double dy) {
        double d;
        if (dy == 0) {
            if (dx > 0) {
                d = 90;
            } else {
                d = -90;
            }
        } else {
            d = Math.atan(dx / dy) * 57.2957796;
            if (dy < 0) {
                if (dx > 0) {
                    d += 180;
                } else {
                    if (dx < 0) {
                        d -= 180;
                    } else {
                        d = 180;
                    }
                }
            }
        }
        return d;
    }

    protected static Vec3d getVectorForRotation(double pitch, double yaw) {
        float f = MathHelper.cos((float) (-yaw * 0.017453292F - (float) Math.PI));
        float f1 = MathHelper.sin((float) (-yaw * 0.017453292F - (float) Math.PI));
        float f2 = -MathHelper.cos((float) (-pitch * 0.017453292F));
        float f3 = MathHelper.sin((float) (-pitch * 0.017453292F));
        return new Vec3d(f1 * f2, f3, f * f2);
    }

    public static void placeCrystal(BlockPos pos) {
        if (pos == null) {
            return;
        }
        pos.offset(EnumFacing.DOWN);
        double dx = (pos.getX() + 0.5 - mc.player.posX);
        double dy = (pos.getY() + 0.5 - mc.player.posY) - .5 - mc.player.getEyeHeight();
        double dz = (pos.getZ() + 0.5 - mc.player.posZ);

        double x = getDirection2D(dz, dx);
        double y = getDirection2D(dy, Math.sqrt(dx * dx + dz * dz));

        Vec3d vec = getVectorForRotation(-y, x - 90);
        if (mc.player.inventory.offHandInventory.get(0).getItem().getClass().equals(Item.getItemById(426).getClass())) {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos.offset(EnumFacing.DOWN), EnumFacing.UP, vec, EnumHand.OFF_HAND);
        } else if (InventoryUtil.pickItem(426, false) != -1) {
            InventoryUtil.setSlot(InventoryUtil.pickItem(426, false));
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos.offset(EnumFacing.DOWN), EnumFacing.UP, vec, EnumHand.MAIN_HAND);
        }
    }

    public static double getDamage(Vec3d pos, Entity target) {
        Entity entity = target == null ? mc.player : target;
        float damage = 6.0F;
        float f3 = damage * 2.0F;

        if (!entity.isImmuneToExplosions()) {
            double d12 = entity.getDistance(pos.x, pos.y, pos.z) / (double) f3;

            if (d12 <= 1.0D) {
                double d5 = entity.posX - pos.x;
                double d7 = entity.posY + (double) entity.getEyeHeight() - pos.y;
                double d9 = entity.posZ - pos.z;
                double d13 = MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);

                if (d13 != 0.0D) {
                    double d14 = mc.world.getBlockDensity(pos, entity.getEntityBoundingBox());
                    double d10 = (1.0D - d12) * d14;
                    return (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f3 + 1.0D));
                }
            }
        }
        return 0;
    }

    public static void doFilledChams(ModelBase modelBase, Entity entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch, float scale, Color visibleColour, Color invisibleColour, boolean renderEntity) {
        if (renderEntity) {
            modelBase.render(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch, scale);
        }
        int combinedLight = mc.world.getCombinedLight(entity.getPosition(), 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) (combinedLight % 65536), (float) (combinedLight / 65536));
        GlStateUtils.blend(true);
        GlStateUtils.lighting(true);
        GlStateUtils.alpha(false);
        RenderUtils.glColor(invisibleColour.getRGB());
        GlStateUtils.colorLock(true);
        GlStateUtils.depth(false);
        GlStateUtils.depthMask(false);
        GL11.glEnable(32823);
        GlStateManager.doPolygonOffset(1.0f, -1000000.0f);
        modelBase.render(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch, scale);
        GlStateManager.doPolygonOffset(1.0f, 1000000.0f);
        GL11.glDisable(32823);
        GlStateUtils.depthMask(true);
        GlStateUtils.alpha(true);
        GlStateUtils.blend(false);
        GlStateUtils.resetColour();
        GlStateUtils.blend(true);
        GlStateUtils.depth(true);
        RenderUtils.glColor(visibleColour.getRGB());
        modelBase.render(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch, scale);
        GlStateUtils.blend(false);
        GlStateUtils.colorLock(false);
        GlStateUtils.resetColour();
    }

}
