package dev.zenhao.melon.mixin.client.network;

import dev.zenhao.melon.event.events.client.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import melon.events.PacketEvents;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {NetworkManager.class})
public abstract class MixinNetworkManager {
    @Inject(method = {"sendPacket(Lnet/minecraft/network/Packet;)V"}, at = @At("HEAD"), cancellable = true)
    public void onSendPacketPre2(final Packet<?> packet, final CallbackInfo info) {
        PacketEvent.Send event = new PacketEvent.Send(0, packet);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
        if (packet != null) {
            PacketEvents.Send event2 = new PacketEvents.Send(packet);
            event2.post();

            if (event2.getCancelled()) {
                info.cancel();
            }
        }
    }

    @Inject(method = {"sendPacket(Lnet/minecraft/network/Packet;)V"}, at = @At(value = "RETURN"), cancellable = true)
    public void onSendPacketPost(Packet<?> packet, CallbackInfo info) {
        PacketEvent.Send event = new PacketEvent.Send(1, packet);
        MinecraftForge.EVENT_BUS.post(event);
        if (packet != null) {
            PacketEvents.PostSend event2 = new PacketEvents.PostSend(packet);
            event2.post();
        }
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = {"channelRead0*"}, at = @At("HEAD"), cancellable = true)
    public void onChannelReadPre2(final ChannelHandlerContext context, final Packet<?> packet, final CallbackInfo info) {
        if (packet != null) {
            PacketEvents.Receive event2 = new PacketEvents.Receive(packet);
            event2.post();

            if (event2.getCancelled()) {
                info.cancel();
            }
        }
        PacketEvent.Receive event = new PacketEvent.Receive(0, packet);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "channelRead0*", at = @At("RETURN"))
    private void channelReadPost(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
        if (packet != null) {
            PacketEvents.PostReceive event = new PacketEvents.PostReceive(packet);
            event.post();
        }
    }

    //NoPacketKick
    @Inject(method = {"exceptionCaught"}, at = @At(value = "HEAD"), cancellable = true)
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci2) {
        ci2.cancel();
    }
}