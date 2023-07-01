package dev.zenhao.melon.mixin.client.entity;

import dev.zenhao.melon.event.events.render.RenderLiquidVisionEvent;
import dev.zenhao.melon.event.events.render.RenderTotemPopEvent;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.player.NoEntityTrace;
import dev.zenhao.melon.module.modules.render.CameraClip;
import dev.zenhao.melon.module.modules.render.NoRender;
import melon.events.render.Render2DEvent;
import melon.utils.Wrapper;
import melon.system.render.graphic.GlStateUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
@Mixin(value = {EntityRenderer.class}, priority = 8888)
public abstract class MixinEntityRenderer {
    @Shadow
    @Final
    public Minecraft mc = Minecraft.getMinecraft();

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V", shift = At.Shift.AFTER))
    public void updateCameraAndRender(float partialTicks, long nanoTime, CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("melonRender2D");

        GlStateUtils.INSTANCE.alpha(false);
        GlStateUtils.INSTANCE.pushMatrixAll();

        Render2DEvent.Mc.INSTANCE.post();
        GlStateUtils.INSTANCE.rescaleActual();
        Render2DEvent.Absolute.INSTANCE.post();

        GlStateUtils.INSTANCE.popMatrixAll();
        GlStateUtils.INSTANCE.alpha(true);

        GlStateUtils.INSTANCE.useProgramForce(0);
        GlStateUtils.INSTANCE.pushMatrixAll();
        GlStateUtils.INSTANCE.rescaleActual();
        Render2DEvent.NMSL.INSTANCE.post();
        GlStateUtils.INSTANCE.popMatrixAll();
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Redirect(method = {"setupFog"}, at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/ActiveRenderInfo.getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"))
    public IBlockState onSettingUpFogWhileInLiquid(World worldIn, Entity entityIn, float p_186703_2_) {
        IBlockState iBlockState = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, entityIn, p_186703_2_);
        RenderLiquidVisionEvent event = new RenderLiquidVisionEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled() && (iBlockState.getMaterial() == Material.LAVA || iBlockState.getMaterial() == Material.WATER)) {
            return Blocks.AIR.getDefaultState();
        }
        return iBlockState;
    }

    @Inject(method = "displayItemActivation", at = @At(value = "HEAD"), cancellable = true)
    public void onDisplayItemActivationPre(ItemStack stack, CallbackInfo ci) {
        RenderTotemPopEvent event = new RenderTotemPopEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderItemActivation", at = @At(value = "HEAD"), cancellable = true)
    public void onRenderItemActivationPre(int p_190563_1_, int p_190563_2_, float p_190563_3_, CallbackInfo ci) {
        RenderTotemPopEvent event = new RenderTotemPopEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = {"updateLightmap"}, at = {@At("HEAD")}, cancellable = true)
    private void updateLightmap(float partialTicks, CallbackInfo info) {
        if (NoRender.INSTANCE.isEnabled() && ((NoRender.INSTANCE).skylight.getValue() == NoRender.Skylight.ENTITY || (NoRender.INSTANCE).skylight.getValue() == NoRender.Skylight.ALL))
            info.cancel();
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float ticks, CallbackInfo info) {
        if (ModuleManager.getModuleByClass(NoRender.class).isEnabled()) {
            if (NoRender.INSTANCE.nohurtCam.getValue()) {
                info.cancel();
            }
        }
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, com.google.common.base.Predicate<? super Entity> predicate) {
        if (ModuleManager.getModuleByClass(NoEntityTrace.class).isEnabled()) {
            return new ArrayList<>();
        } else {
            return worldClient.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate::test);
        }
    }

    @ModifyVariable(method = {"orientCamera"}, ordinal = 3, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double changeCameraDistanceHook(final double range) {
        return (CameraClip.getInstance().isEnabled() && CameraClip.getInstance().extend.getValue()) ? CameraClip.getInstance().distance.getValue() : range;
    }

    @ModifyVariable(method = {"orientCamera"}, ordinal = 7, at = @At(value = "STORE", ordinal = 0), require = 1)
    public double orientCameraHook(final double range) {
        return (CameraClip.getInstance().isEnabled() && CameraClip.getInstance().extend.getValue()) ? CameraClip.getInstance().distance.getValue() : ((CameraClip.getInstance().isEnabled() && !CameraClip.getInstance().extend.getValue()) ? 4.0 : range);
    }
}

