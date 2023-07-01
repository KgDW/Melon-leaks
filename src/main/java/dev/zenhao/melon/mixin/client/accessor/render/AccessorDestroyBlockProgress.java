package dev.zenhao.melon.mixin.client.accessor.render;

import net.minecraft.client.renderer.DestroyBlockProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DestroyBlockProgress.class)
public interface AccessorDestroyBlockProgress {
    @Accessor("miningPlayerEntId")
    int MGetEntityID();
}
