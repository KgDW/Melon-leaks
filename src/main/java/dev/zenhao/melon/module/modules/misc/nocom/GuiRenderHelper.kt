package dev.zenhao.melon.module.modules.misc.nocom

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11

object GuiRenderHelper {
    fun drawRect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        val right = x + w
        val bottom = y + h
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.color(red, green, blue, alpha)
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION)
        bufferBuilder.pos(x.toDouble(), bottom.toDouble(), 0.0).endVertex() // top left
        bufferBuilder.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex() // top right
        bufferBuilder.pos(right.toDouble(), y.toDouble(), 0.0).endVertex() // bottom right
        bufferBuilder.pos(x.toDouble(), y.toDouble(), 0.0).endVertex() // bottom left
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun drawOutlineRect(x: Float, y: Float, w: Float, h: Float, lineWidth: Float, color: Int) {
        val right = x + w
        val bottom = y + h
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.color(red, green, blue, alpha)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GlStateManager.glLineWidth(lineWidth)
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        bufferBuilder.pos(x.toDouble(), bottom.toDouble(), 0.0).endVertex() // top left
        bufferBuilder.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex() // top right
        bufferBuilder.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex() // top right
        bufferBuilder.pos(right.toDouble(), y.toDouble(), 0.0).endVertex() // bottom right
        bufferBuilder.pos(right.toDouble(), y.toDouble(), 0.0).endVertex() // bottom right
        bufferBuilder.pos(x.toDouble(), y.toDouble(), 0.0).endVertex() // bottom left
        bufferBuilder.pos(x.toDouble(), y.toDouble(), 0.0).endVertex() // bottom left
        bufferBuilder.pos(x.toDouble(), bottom.toDouble(), 0.0).endVertex() // top left
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }
}