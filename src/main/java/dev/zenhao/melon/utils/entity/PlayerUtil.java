package dev.zenhao.melon.utils.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class PlayerUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean MovementInput() {
        return (mc.gameSettings.keyBindForward).isPressed()
                || (mc.gameSettings.keyBindLeft).isPressed()
                || (mc.gameSettings.keyBindRight).isPressed()
                || (mc.gameSettings.keyBindBack).isPressed();
    }

    public static BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static BlockPos GetLocalPlayerPosFloored() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static boolean IsEating() {
        return mc.player != null && (mc.player.getHeldItemMainhand().getItem() instanceof ItemFood || mc.player.getHeldItemOffhand().getItem() instanceof ItemFood) && mc.player.isHandActive();
    }

    public static boolean isAirUnder(Entity ent) {
        return mc.world.getBlockState(new BlockPos(ent.posX, ent.posY - 1, ent.posZ)).getBlock() == Blocks.AIR;
    }

    public static void damageHypixel() {
        if (mc.getConnection() == null) return;

        if (mc.player.onGround) {
            final double x = mc.player.posX;
            final double y = mc.player.posY;
            final double z = mc.player.posZ;
            for (int i = 0; i < 9; i++) {
                mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 0.4122222218322211111111F, z, false));
                mc.getConnection().sendPacket(new CPacketPlayer.Position(x, y + 0.000002737272, z, false));
                mc.getConnection().sendPacket(new CPacketPlayer(false));
            }
            mc.getConnection().sendPacket(new CPacketPlayer(true));
        }
    }
}