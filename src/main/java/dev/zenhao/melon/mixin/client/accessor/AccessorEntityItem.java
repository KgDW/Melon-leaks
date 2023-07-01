package dev.zenhao.melon.mixin.client.accessor;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityItem.class)
public interface AccessorEntityItem {
    @Accessor("health")
    int melonGetEntityItemHealth();
}
