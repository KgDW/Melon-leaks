package dev.zenhao.melon.mixin.client.world;

import melon.events.EntityAddToWorldEvent;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldClient.class)
public class MixinWorldClient {
    @Inject(method = "addEntityToWorld", at = @At("HEAD"))
    public void onEntityAddedToWorld(int entityID, Entity entity, CallbackInfo ci) {
        new EntityAddToWorldEvent(entity).post();
    }
}
