package dev.zenhao.melon.module.hud

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11

object HudRenderUtil {
    var mc: Minecraft = Minecraft.getMinecraft()
    private fun preRender() {
        GL11.glPushMatrix()
        GL11.glDepthMask(true)
        GlStateManager.clear(256)
        GlStateManager.disableDepth()
        GlStateManager.enableDepth()
        RenderHelper.enableStandardItemLighting()
        GlStateManager.scale(1.0f, 1.0f, 0.01f)
    }

    private fun postRender() {
        GlStateManager.scale(1.0f, 1.0f, 1.0f)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.scale(0.5, 0.5, 0.5)
        GlStateManager.disableDepth()
        GlStateManager.enableDepth()
        GlStateManager.scale(2.0f, 2.0f, 2.0f)
        GL11.glPopMatrix()
    }

    fun itemRender(x: Int, y: Int, var1: ItemStack) {
        preRender()
        mc.getRenderItem().renderItemAndEffectIntoGUI(var1, x, y)
        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, var1, x, y)
        postRender()
    }
}