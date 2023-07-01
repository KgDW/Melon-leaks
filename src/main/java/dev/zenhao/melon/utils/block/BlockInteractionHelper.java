package dev.zenhao.melon.utils.block;

import dev.zenhao.melon.utils.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hub on 15 June 2019
 * Last Updated 12 January 2019 by hub
 */
public class BlockInteractionHelper {
    public static List<Block> blackList = Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL,
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.TRAPDOOR,
            Blocks.ENCHANTING_TABLE
    );
    public static List<Block> shulkerList = Arrays.asList(
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
    );
    public static Minecraft mc = Minecraft.getMinecraft();

    public static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();

        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        if (pitch > 90.0f) {
            pitch = 90.0f;
        } else if (pitch < -90.0f) {
            pitch = -90.0f;
        }

        return new float[]{
                Wrapper.getPlayer().rotationYaw
                        + MathHelper.wrapDegrees(yaw - Wrapper.getPlayer().rotationYaw),
                Wrapper.getPlayer().rotationPitch + MathHelper
                        .wrapDegrees(pitch - Wrapper.getPlayer().rotationPitch)};
    }

    public static Vec3d getEyesPos() {
        return new Vec3d(Wrapper.getPlayer().posX,
                Wrapper.getPlayer().posY + Wrapper.getPlayer().getEyeHeight(),
                Wrapper.getPlayer().posZ);
    }

    public static boolean canBeClicked(BlockPos pos) {
        return getBlock(pos).canCollideCheck(getState(pos), false);
    }

    public static Block getBlock(BlockPos pos) {
        return getState(pos).getBlock();
    }

    public static PlayerControllerMP getPlayerController() {
        return Minecraft.getMinecraft().playerController;
    }

    public static IBlockState getState(BlockPos pos) {
        return Wrapper.getWorld().getBlockState(pos);
    }

    public static boolean checkForNeighbours(BlockPos blockPos) {
        // check if we don't have a block adjacent to blockpos
        if (!hasNeighbour(blockPos)) {
            // find air adjacent to blockpos that does have a block adjacent to it, let's fill this first as to form a bridge between the player and the original blockpos. necessary if the player is going diagonal.
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = blockPos.offset(side);
                if (hasNeighbour(neighbour)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean hasNeighbour(BlockPos blockPos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if (!Wrapper.getWorld().getBlockState(neighbour).getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }
}
