package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.event.events.render.TransformSideFirstPersonEvent
import dev.zenhao.melon.event.events.render.item.RenderItemAnimationEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import melon.system.event.safeEventListener
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @Author GL_DONT_CARE (Viewmodel Transformations)
 * @Author NekoPvP (Item FOV)
 */
@Module.Info(name = "ViewModel", category = Category.RENDER)
class ViewModel : Module() {
    private val type = msetting("Type", Mode.BOTH)
    private val xRight: Setting<Double> = dsetting("RightX", 0.2, -2.0, 2.0)
    private val yRight: Setting<Double> = dsetting("RightY", 0.2, -2.0, 2.0)
    private val zRight: Setting<Double> = dsetting("RightZ", 0.2, -2.0, 2.0)
    private val xLeft: Setting<Double> = dsetting("LeftX", 0.2, -2.0, 2.0)
    private val yLeft: Setting<Double> = dsetting("LeftY", 0.2, -2.0, 2.0)
    private val zLeft: Setting<Double> = dsetting("LeftZ", 0.2, -2.0, 2.0)
    private val fov: Setting<Float> = fsetting("ItemFov", 150f, 70f, 200f)
    private val rotate = bsetting("Rotate", false)
    @JvmField
    var cancelEating: Setting<Boolean> = bsetting("CancelEating", false)
    var rotateVal = 0

    init {
        safeEventListener<PlayerMotionEvent> {
            if (rotate.value) {
                var oao = 0
                while (oao < 360) {
                    oao = ++rotateVal
                    oao++
                }
            }
        }
    }

    @SubscribeEvent
    fun onTransform(event: TransformSideFirstPersonEvent) {
        if (type.value == Mode.Value || type.value == Mode.BOTH) {
            if (event.enumHandSide == EnumHandSide.RIGHT) {
                GlStateManager.translate(xRight.value, yRight.value, zRight.value)
            } else if (event.enumHandSide == EnumHandSide.LEFT) {
                GlStateManager.translate(xLeft.value, yLeft.value, zLeft.value)
            }
        }
    }

    @SubscribeEvent
    fun onFov(event: FOVModifier) {
        if (type.value == Mode.FOV || type.value == Mode.BOTH) {
            event.fov = fov.value
        }
    }

    @SubscribeEvent
    fun onTransformItem(event: RenderItemAnimationEvent.Transform) {
        if (fullNullCheck()) return
        if (event.hand == EnumHand.MAIN_HAND && rotate.value) {
            val i: Float = if (mc.player.primaryHand == EnumHandSide.RIGHT) {
                1f
            } else {
                -1f
            }
            GlStateManager.translate(0.15f * i, 0.3f, 0.0f)
            GlStateManager.rotate(5f * i, 0.0f, 0.0f, 0.0f)
            if (i > 0f) GlStateManager.translate(0.56f, -0.52f, -0.72f * i) else GlStateManager.translate(
                0.56f,
                -0.52f,
                0.5f
            )
            GlStateManager.translate(0.0f, 0.2f * 0.6f, 0.0f)
            GlStateManager.rotate(rotateVal.inc().toFloat(), rotateVal / 2f, rotateVal / i * 2f, rotateVal / 2f)
            GlStateManager.scale(1.625f, 1.625f, 1.625f)
        }
    }

    private enum class Mode {
        Value, FOV, BOTH
    }
}