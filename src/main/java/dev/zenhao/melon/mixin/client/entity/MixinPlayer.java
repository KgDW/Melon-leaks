package dev.zenhao.melon.mixin.client.entity;

import dev.zenhao.melon.event.events.entity.EventPlayerTravel;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {EntityPlayer.class})
public abstract class MixinPlayer extends MixinEntity {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void travel(float strafe, float vertical, float forward, CallbackInfo info) {
        EventPlayerTravel event = new EventPlayerTravel(strafe, vertical, forward);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            info.cancel();
        }
    }

    @Inject(method = {"isEntityInsideOpaqueBlock"}, at = {@At("HEAD")}, cancellable = true)
    private void isEntityInsideOpaqueBlockHook(final CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(false);
    }

}

