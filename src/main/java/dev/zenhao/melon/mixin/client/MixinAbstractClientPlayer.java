package dev.zenhao.melon.mixin.client;

import dev.zenhao.melon.mixin.client.entity.MixinPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {AbstractClientPlayer.class}, priority = 0x7FFFFFFE)
public abstract class MixinAbstractClientPlayer extends MixinPlayer {
}

