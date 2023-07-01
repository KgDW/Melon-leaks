package dev.zenhao.melon.mixin.client.accessor;

import net.minecraft.network.play.server.SPacketConfirmTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketConfirmTransaction.class)
public interface AccessorSPacketConfirmTransaction {
    @Accessor("windowId")
    int SWindowID();

    @Accessor("actionNumber")
    short SActionNumber();

    @Accessor("accepted")
    boolean SAccepted();
}
