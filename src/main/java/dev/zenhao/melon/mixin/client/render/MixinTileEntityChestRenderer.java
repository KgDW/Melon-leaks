package dev.zenhao.melon.mixin.client.render;

import dev.zenhao.melon.event.events.render.RenderChestEvent;
import dev.zenhao.melon.module.modules.render.NoRender;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {TileEntityChestRenderer.class})
public class MixinTileEntityChestRenderer {
    @Inject(method = {"render*"}, at = @At(value = "INVOKE"), cancellable = true)
    private void renderChest(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        RenderChestEvent event = new RenderChestEvent();
        IBlockState blockState = Blocks.SNOW_LAYER.defaultBlockState.withProperty(BlockSnow.LAYERS, 8);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            Minecraft.getMinecraft().world.setBlockState(te.getPos(), blockState);
            Minecraft.getMinecraft().world.markTileEntityForRemoval(te);
        }
    }

    @Inject(method = "render*", at = @At("INVOKE"), cancellable = true)
    public void onRenderTileEntityPre(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        if (NoRender.INSTANCE.isEnabled()) {
            if (NoRender.INSTANCE.tryReplaceChest(te)) {
                ci.cancel();
            }
        }
    }

}
