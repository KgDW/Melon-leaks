package melon.system.render.graphic.scala

import dev.zenhao.melon.utils.Wrapper
import melon.system.render.graphic.GlStateUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager

object ScreenCenterScalaHelper {

    @JvmStatic
    fun glScalePush(scala: Float) {
        GlStateUtils.pushMatrixAll()
        val resolution = ScaledResolution(Wrapper.mc)
        GlStateManager.translate(resolution.scaledWidth / 2.0, resolution.scaledHeight / 2.0, 0.0)
        GlStateManager.scale(scala, scala, 0F)
        GlStateManager.translate(-resolution.scaledWidth / 2.0, -resolution.scaledHeight / 2.0, 0.0)
    }

    @JvmStatic
    fun glScalePop() {
        GlStateUtils.popMatrixAll()
    }
}