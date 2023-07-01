package dev.zenhao.melon.mixin.client.render;

import dev.zenhao.melon.Melon;
import dev.zenhao.melon.module.modules.client.NewCustomFont;
import dev.zenhao.melon.utils.animations.MathKt;
import melon.system.render.font.renderer.MainFontRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    @Shadow
    public int FONT_HEIGHT;
    @Shadow
    protected float posX;
    @Shadow
    protected float posY;
    @Shadow
    private float alpha;
    @Shadow
    private float red;
    @Shadow
    private float green;
    @Shadow
    private float blue;

    @Shadow
    protected abstract void renderStringAtPos(String text, boolean shadow);

    @Inject(method = "drawString(Ljava/lang/String;FFIZ)I", at = @At("HEAD"), cancellable = true)
    private void drawString$Inject$HEAD(String text, float x, float y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        handleDrawString(text, x, y, color, dropShadow, cir);
    }

    @Inject(method = "renderString", at = @At("HEAD"), cancellable = true)
    private void renderString$Inject$HEAD(String text, float x, float y, int color, boolean shadow, CallbackInfoReturnable<Integer> cir) {
        handleDrawString(text, x, y, color, false, cir);
    }

    private void handleDrawString(String text, float x, float y, int color, boolean drawShadow, CallbackInfoReturnable<Integer> cir) {
        if (Melon.isReady() && NewCustomFont.INSTANCE.getOverrideMinecraft().getValue()) {
            posX = x;
            posY = y;

            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            if (text != null) {
                MainFontRenderer.INSTANCE.drawStringJava(text, x, y, color, 1.0f, drawShadow);
                cir.setReturnValue(MathKt.fastCeil(x + MainFontRenderer.INSTANCE.getWidth(text)));
            }
        }
    }

    @Inject(method = "getStringWidth", at = @At("HEAD"), cancellable = true)
    public void getStringWidth$Inject$HEAD(String text, CallbackInfoReturnable<Integer> cir) {
        if (Melon.isReady() && NewCustomFont.INSTANCE.getOverrideMinecraft().getValue()) {
            cir.setReturnValue(MathKt.fastCeil(MainFontRenderer.INSTANCE.getWidth(text)));
        }
    }

    @Inject(method = "getCharWidth", at = @At("HEAD"), cancellable = true)
    public void getCharWidth$Inject$HEAD(char character, CallbackInfoReturnable<Integer> cir) {
        if (Melon.isReady() && NewCustomFont.INSTANCE.getOverrideMinecraft().getValue()) {
            cir.setReturnValue(MathKt.fastCeil(MainFontRenderer.INSTANCE.getWidth(character)));
        }
    }
}
