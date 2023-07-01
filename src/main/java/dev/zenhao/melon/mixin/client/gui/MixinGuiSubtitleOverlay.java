package dev.zenhao.melon.mixin.client.gui;

import dev.zenhao.melon.event.events.render.Render2DEvent;
import net.minecraft.client.gui.GuiSubtitleOverlay;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSubtitleOverlay.class)
public abstract class MixinGuiSubtitleOverlay {

    @Inject(method = "renderSubtitles", at = @At(value = "HEAD"))
    private void renderSubtitlesHook(CallbackInfo info) {
        MinecraftForge.EVENT_BUS.post(new Render2DEvent());
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

}
