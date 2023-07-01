package dev.zenhao.melon.command.commands.mc;

import dev.zenhao.melon.command.Command;
import dev.zenhao.melon.command.syntax.SyntaxChunk;
import dev.zenhao.melon.event.events.client.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClipCommand extends Command {
    public ClipCommand() {
        super("clip", SyntaxChunk.EMPTY);
    }

    @Override
    public void call(String[] var1) {
        double x = new Double(var1[0]);
        double y = new Double(var1[1]);
        double z = new Double(var1[2]);
        for (String arg : var1) {
            if (arg == null) continue;
            Minecraft.getMinecraft().player.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
            Minecraft.getMinecraft().player.setPositionAndUpdate(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
            Minecraft.getMinecraft().player.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
        }
    }
}
