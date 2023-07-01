package dev.zenhao.melon.event.events.client;

import dev.zenhao.melon.event.EventStage;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PacketEvent extends EventStage {
    private final Packet<?> packet;

    public PacketEvent(int stage, Packet<?> packet) {
        super(stage);
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    @Cancelable
    public static class Send extends PacketEvent {
        public Send(int stage, Packet<?> packet) {
            super(stage, packet);
        }
    }

    @Cancelable
    public static class Receive extends PacketEvent {
        public Receive(int stage, Packet<?> packet) {
            super(stage, packet);
        }
    }
}

