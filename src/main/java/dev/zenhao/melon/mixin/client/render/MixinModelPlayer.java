package dev.zenhao.melon.mixin.client.render;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer extends ModelBiped {
    @Shadow
    public ModelRenderer bipedLeftArmwear;
    @Shadow
    public ModelRenderer bipedRightArmwear;
    @Shadow
    public ModelRenderer bipedLeftLegwear;
    @Shadow
    public ModelRenderer bipedRightLegwear;
    @Shadow
    public ModelRenderer bipedBodyWear;
}
