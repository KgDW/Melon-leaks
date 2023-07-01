package dev.zenhao.melon.mixin.client.render;

import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.render.Nametags;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 19/12/2017.
 */
@Mixin(RenderPlayer.class)
public class MixinRenderPlayer extends RenderLivingBase<AbstractClientPlayer> {
    public MixinRenderPlayer(RenderManager p_i46156_1_, ModelBase p_i46156_2_, float p_i46156_3_) {
        super(p_i46156_1_, p_i46156_2_, p_i46156_3_);
    }

    @Shadow
    private void setModelVisibilities(AbstractClientPlayer p_177137_1_) {
    }

    @Inject(method = "renderEntityName*", at = @At("HEAD"), cancellable = true)
    public void renderLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        if (ModuleManager.getModuleByClass(Nametags.class).isEnabled()) {
            info.cancel();
        }
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(AbstractClientPlayer abstractClientPlayer) {
        return null;
    }
}
