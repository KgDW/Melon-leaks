package dev.zenhao.melon.mixin.client.entity;

import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.movement.EntitySpeed;
import net.minecraft.entity.passive.EntityPig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created by 086 on 16/12/2017.
 */
@Mixin(EntityPig.class)
public class MixinEntityPig {

    @Inject(method = "canBeSteered", at = @At("HEAD"), cancellable = true)
    public void canBeSteered(CallbackInfoReturnable<Boolean> returnable) {
        if (ModuleManager.getModuleByClass(EntitySpeed.class).isEnabled()) {
            returnable.setReturnValue(true);
            returnable.cancel();
        }
    }

}
