package dev.zenhao.melon.mixin.client.render;

import melon.events.render.RenderEntityEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderLivingBase.class, priority = 114514)
public class MixinRenderLivingBase<T extends EntityLivingBase> {
    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", shift = At.Shift.BEFORE))
    public void renderModelHead(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent.Model.Pre eventModel = RenderEntityEvent.Model.Pre.of(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, mainModel);
        eventModel.post();
    }

    @Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBase;render(Lnet/minecraft/entity/Entity;FFFFFF)V", shift = At.Shift.AFTER))
    public void renderEntityReturn(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        if (entity == null || !RenderEntityEvent.getRenderingEntities()) return;

        RenderEntityEvent.Model.Post eventModel = RenderEntityEvent.Model.Post.of(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, mainModel);
        eventModel.post();
    }
}
