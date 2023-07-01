package dev.zenhao.melon.mixin.client;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {Block.class}, priority = Integer.MAX_VALUE)
public abstract class MixinBlock {
}
