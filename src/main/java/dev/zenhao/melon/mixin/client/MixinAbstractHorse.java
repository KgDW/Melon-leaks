package dev.zenhao.melon.mixin.client;

import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.movement.EntitySpeed;
import net.minecraft.entity.passive.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {AbstractHorse.class})
public class MixinAbstractHorse {
    @Inject(method = {"isHorseSaddled"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void isHorseSaddled(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.getModuleByClass(EntitySpeed.class).isEnabled()) {
            cir.setReturnValue(true);
        }
    }
}

