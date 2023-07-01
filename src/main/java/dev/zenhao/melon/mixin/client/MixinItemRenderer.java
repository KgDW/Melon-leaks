package dev.zenhao.melon.mixin.client;

import dev.zenhao.melon.event.events.render.RenderOverlayEvent;
import dev.zenhao.melon.event.events.render.TransformSideFirstPersonEvent;
import dev.zenhao.melon.event.events.render.item.RenderItemAnimationEvent;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.render.ViewModel;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = {"renderWaterOverlayTexture"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void preRenderWaterOverlayTexture(float partialTicks, CallbackInfo ci) {
        RenderOverlayEvent event = new RenderOverlayEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = {"renderFireInFirstPerson"}, at = {@At(value = "HEAD")}, cancellable = true)
    public void preRenderFireInFirstPerson(CallbackInfo ci) {
        RenderOverlayEvent event = new RenderOverlayEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = {"renderSuffocationOverlay"}, at = {@At(value = "HEAD")}, cancellable = true)
    private void onRenderSuffocationOverlay(TextureAtlasSprite sprite, CallbackInfo ci) {
        RenderOverlayEvent event = new RenderOverlayEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
    public void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_, CallbackInfo callbackInfo) {
        TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
    public void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack, CallbackInfo callbackInfo) {
        TransformSideFirstPersonEvent event = new TransformSideFirstPersonEvent(hand);
        MinecraftForge.EVENT_BUS.post(event);
        if (ModuleManager.getModuleByName("ViewModel").isEnabled() && ((ViewModel) ModuleManager.getModuleByName("ViewModel")).cancelEating.getValue()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At("HEAD"), cancellable = true)
    private void onRenderItemAnimationPre(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo ci) {
        RenderItemAnimationEvent.Render uwu = new RenderItemAnimationEvent.Render(stack, hand);
        if (uwu.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemSide(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Z)V"))
    private void onRenderItemTransformAnimationPre(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
        MinecraftForge.EVENT_BUS.post(new RenderItemAnimationEvent.Transform(stack, hand, p_187457_5_));
    }
}