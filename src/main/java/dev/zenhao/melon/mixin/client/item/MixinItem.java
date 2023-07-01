package dev.zenhao.melon.mixin.client.item;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(value = {Item.class}, priority = Integer.MAX_VALUE)
public class MixinItem {
    @Shadow
    protected static Random itemRand = new Random();
}
