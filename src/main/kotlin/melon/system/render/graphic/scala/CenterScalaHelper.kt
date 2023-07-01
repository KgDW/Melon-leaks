package melon.system.render.graphic.scala

import melon.system.render.graphic.GlStateUtils
import net.minecraft.client.renderer.GlStateManager

class CenterScalaHelper(private val cx: Int, private val cy: Int, private val scala: Float) {

    fun glScalePush() {
        GlStateUtils.pushMatrixAll()
        GlStateManager.translate(cx.toDouble(), cy.toDouble(), 0.0)
        GlStateManager.scale(scala, scala, 0F)
        GlStateManager.translate(-cx.toDouble(), -cy.toDouble(), 0.0)
    }

    fun glScalePop() {
        GlStateUtils.popMatrixAll()
    }
}