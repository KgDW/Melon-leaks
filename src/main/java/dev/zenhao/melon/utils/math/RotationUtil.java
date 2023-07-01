package dev.zenhao.melon.utils.math;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil {

    public static float yaw;
    public static float pitch;
    static Minecraft mc = Minecraft.getMinecraft();

    public static float[] getRotations(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((difX * difX + difZ * difZ));
        return new float[]{(float) MathHelper.wrapDegrees((Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
    }

    public static float[] getRotationsBlock(BlockPos block, EnumFacing face, boolean Legit) {
        double x = block.getX() + 0.5 - mc.player.posX + (double) face.getXOffset() / 2;
        double z = block.getZ() + 0.5 - mc.player.posZ + (double) face.getZOffset() / 2;
        double y = (block.getY() + 0.5);

        if (Legit)
            y += 0.5;

        double d1 = mc.player.posY + mc.player.getEyeHeight() - y;
        double d3 = MathHelper.sqrt(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (Math.atan2(d1, d3) * 180.0D / Math.PI);

        if (yaw < 0.0F) {
            yaw += 360f;
        }
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationsBlock(Vec3d block, EnumFacing face, boolean Legit) {
        double x = block.x + 0.5 - mc.player.posX + (double) face.getXOffset() / 2;
        double z = block.z + 0.5 - mc.player.posZ + (double) face.getZOffset() / 2;
        double y = (block.y + 0.5);

        if (Legit)
            y += 0.5;

        double d1 = mc.player.posY + mc.player.getEyeHeight() - y;
        double d3 = MathHelper.sqrt(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (Math.atan2(d1, d3) * 180.0D / Math.PI);

        if (yaw < 0.0F) {
            yaw += 360f;
        }
        return new float[]{yaw, pitch};
    }

    public static void updateRotations() {
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
    }

    public static void restoreRotations() {
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.rotationPitch = pitch;
    }

    public static void setPlayerRotations(float yaw, float pitch) {
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.rotationPitch = pitch;
    }

    public void setPlayerYaw(float yaw) {
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
    }

    public void lookAtPos(BlockPos pos) {
        float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
        setPlayerRotations(angle[0], angle[1]);
    }

    public void lookAtVec3d(Vec3d vec3d) {
        float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(vec3d.x, vec3d.y, vec3d.z));
        setPlayerRotations(angle[0], angle[1]);
    }

    public void lookAtVec3d(double x, double y, double z) {
        Vec3d vec3d = new Vec3d(x, y, z);
        this.lookAtVec3d(vec3d);
    }

    public void lookAtEntity(Entity entity) {
        float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionEyes(mc.getRenderPartialTicks()));
        setPlayerRotations(angle[0], angle[1]);
    }

    public void setPlayerPitch(float pitch) {
        mc.player.rotationPitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        RotationUtil.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        RotationUtil.pitch = pitch;
    }

    public int getDirection4D() {
        return getDirection4D();
    }

    public String getDirection4D(boolean northRed) {
        return getDirection4D(northRed);
    }
}
