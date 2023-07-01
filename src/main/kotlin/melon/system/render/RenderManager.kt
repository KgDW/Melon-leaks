package melon.system.render

import dev.zenhao.melon.utils.extension.fastCeil
import dev.zenhao.melon.utils.vector.Vec2f
import melon.events.TickEvent
import melon.system.event.AlwaysListening
import melon.system.event.safeParallelListener
import melon.system.render.graphic.GlStateUtils
import melon.system.render.graphic.Resolution
import melon.system.util.color.ColorRGB
import melon.system.util.delegate.FrameValue
import melon.utils.TickTimer
import melon.utils.Wrapper
import melon.system.util.interfaces.MinecraftWrapper
import org.lwjgl.input.Mouse
import kotlin.math.round

internal object RenderManager {
    //val fontRenderer =
    val tooltips get() = true
    val antiAliasSampleLevel get() = 1f

    object Color {
        val alphaHover get() = 32
        val primary get() = ColorRGB(255, 160, 240, 220)
        val idle get() = if (primary.lightness < 0.9f) ColorRGB(255, 255, 255, 0) else ColorRGB(0, 0, 0, 0)
        val hover get() = idle.alpha(alphaHover)
        val click get() = idle.alpha(alphaHover * 2)
        val backGround get() = ColorRGB(36, 40, 48, 160)
        val outline get() = ColorRGB(240, 250, 255, 48)
        val text get() = ColorRGB(255, 250, 253, 255)
    }

    object Gui {

        val fadeInTime  get() = 0.4f
        val fadeOutTime get() = 0.4f

        val blur get() = 0.25f
        val darkness get() = 0.25f
        val particle get() = false


        object Window {
            val windowBlur get() = true
            val windowOutline get() = false
            val windowRadius get() = 6.5f
            val titleBar get() = false
        }

        object Component {
            val blur get() = true
            val outline get() = false
        }
    }


    object ScaleInfo : AlwaysListening, MinecraftWrapper {
        private var scale = 100
            set(value) {
                settingTimer.reset()
                field = value
            }

        private var prevScale = scale / 100.0f
        private var scale0 = prevScale

        private val settingTimer = TickTimer()

        val scaleFactorFloat by FrameValue { (prevScale + (scale0 - prevScale) * mc.renderPartialTicks) * 2.0f }
        val scaleFactor      by FrameValue { (prevScale + (scale0 - prevScale) * mc.renderPartialTicks) * 2.0  }

        val widthF get() = Resolution.widthF / scaleFactorFloat

        val heightF get() = Resolution.heightF / scaleFactorFloat

        val widthI get() = widthF.fastCeil()

        val heightI get() = heightF.fastCeil()

        init {
            safeParallelListener<TickEvent.Post> {
                prevScale = scale0
                if (settingTimer.tick(500L)) {
                    val diff = scale0 - getRoundedScale()
                    when {
                        diff < -0.025 -> scale0 += 0.025f
                        diff > 0.025 -> scale0 -= 0.025f
                        else -> scale0 = getRoundedScale()
                    }
                }
            }
        }

        private fun getRoundedScale(): Float = round((scale / 100.0f) / 0.1f) * 0.1f

        fun resetScale() {
            scale = 100
            prevScale = 1.0f
            scale0 = 1.0f
        }


        fun rescale() {
            GlStateUtils.rescale(Wrapper.minecraft.displayWidth / scaleFactor, Wrapper.minecraft.displayHeight / scaleFactor)
        }
    }


    fun getRealMousePos(): Vec2f {
        val scaleFactor = ScaleInfo.scaleFactorFloat
        return Vec2f(
            Mouse.getX() / scaleFactor - 1.0f,
            (Wrapper.minecraft.displayHeight - 1 - Mouse.getY()) / scaleFactor
        )
    }
}