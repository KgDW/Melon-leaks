package dev.zenhao.melon.mixin.client.item;

import net.minecraft.item.ItemEnderPearl;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {ItemEnderPearl.class}, priority = Integer.MAX_VALUE)
public class MixinItemEnderPearl extends MixinItem {
}