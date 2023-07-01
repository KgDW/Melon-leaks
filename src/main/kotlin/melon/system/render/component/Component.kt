package melon.system.render.component

import dev.zenhao.melon.Melon
import melon.system.config.configs.GuiConfig.setting
import dev.zenhao.melon.utils.FALSE_BLOCK
import dev.zenhao.melon.utils.animations.MathUtils
import dev.zenhao.melon.utils.info.McRenderInfo
import dev.zenhao.melon.utils.delegate.AutoUpdateValue
import dev.zenhao.melon.utils.render.HAlign
import dev.zenhao.melon.utils.render.VAlign
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.util.interfaces.Nameable
import net.minecraft.client.Minecraft
import kotlin.math.max

open class Component(
    final override val name: String,
    posXIn: Float,
    posYIn: Float,
    widthIn: Float,
    heightIn: Float
) : Nameable {
    protected val mc: Minecraft = Minecraft.getMinecraft()

    var settingGroup: SettingGroup = SettingGroup.NONE

    // Basic info
    protected val visibleSetting = setting("Visible", true, FALSE_BLOCK, { _, it -> it || !closeable })
    var visible by visibleSetting

    protected val dockingHSetting = setting("Docking H", HAlign.LEFT)
    protected val dockingVSetting = setting("Docking V", VAlign.TOP)

    var width by setting("Width", widthIn, 0.0f..69420.914f, 0.1f, FALSE_BLOCK, { _, it -> it.coerceIn(minWidth, max(scaledDisplayWidth, minWidth)) })
    var height by setting("Height", heightIn, 0.0f..69420.914f, 0.1f, FALSE_BLOCK, { _, it -> it.coerceIn(minHeight, max(scaledDisplayHeight, minHeight)) })

    protected var relativePosX by setting("Pos X", posXIn, -69420.914f..69420.914f, 0.1f, FALSE_BLOCK,
        { _, it -> if (this is WindowComponent && Melon.ready) absToRelativeX(relativeToAbsX(it).coerceIn(0.0f, max(scaledDisplayWidth - width, 0.0f))) else it })
    protected var relativePosY by setting("Pos Y", posYIn, -69420.914f..69420.914f, 0.1f, FALSE_BLOCK,
        { _, it -> if (this is WindowComponent && Melon.ready) absToRelativeY(relativeToAbsY(it).coerceIn(0.0f, max(scaledDisplayHeight - height, 0.0f))) else it })

    var dockingH by dockingHSetting
    var dockingV by dockingVSetting

    var posX: Float
        get() {
            return relativeToAbsX(relativePosX)
        }
        set(value) {
            if (!Melon.ready) return
            relativePosX = absToRelativeX(value)
        }

    var posY: Float
        get() {
            return relativeToAbsY(relativePosY)
        }
        set(value) {
            if (!Melon.ready) return
            relativePosY = absToRelativeY(value)
        }


    // Extra info
    open val minWidth = 1.0f
    open val minHeight = 1.0f
    open val maxWidth = -1.0f
    open val maxHeight = -1.0f
    open val closeable: Boolean get() = true

    // Rendering info
    var prevPosX = 0.0f; protected set
    var prevPosY = 0.0f; protected set
    val renderPosX by AutoUpdateValue { MathUtils.lerp(prevPosX + prevDockWidth, posX + dockWidth, McRenderInfo.partialTicks) - dockWidth }
    val renderPosY by AutoUpdateValue { MathUtils.lerp(prevPosY + prevDockHeight, posY + dockHeight, McRenderInfo.partialTicks) - dockHeight }

    var prevWidth = 0.0f; protected set
    var prevHeight = 0.0f; protected set
    val renderWidth by AutoUpdateValue { MathUtils.lerp(prevWidth, width, McRenderInfo.partialTicks) }
    open val renderHeight by AutoUpdateValue { MathUtils.lerp(prevHeight, height, McRenderInfo.partialTicks) }


    private fun relativeToAbsX(xIn: Float) = xIn + scaledDisplayWidth * dockingH.multiplier - dockWidth
    private fun relativeToAbsY(yIn: Float) = yIn + scaledDisplayHeight * dockingV.multiplier - dockHeight
    private fun absToRelativeX(xIn: Float) = xIn - scaledDisplayWidth * dockingH.multiplier + dockWidth
    private fun absToRelativeY(yIn: Float) = yIn - scaledDisplayHeight * dockingV.multiplier + dockHeight

    protected val scaledDisplayWidth get() = mc.displayWidth / 1F//GuiSetting.scaleFactorFloat //TODO
    protected val scaledDisplayHeight get() = mc.displayHeight / 1F//GuiSetting.scaleFactorFloat
    private val dockWidth get() = width * dockingH.multiplier
    private val dockHeight get() = height * dockingV.multiplier
    private val prevDockWidth get() = prevWidth * dockingH.multiplier
    private val prevDockHeight get() = prevHeight * dockingV.multiplier

    // Update methods
    open fun onDisplayed() {
        updatePrevPos()
        updatePrevSize()
    }

    open fun onClosed() {}

    open fun onGuiInit() {}

    open fun onTick() {
        updatePrevPos()
        updatePrevSize()
    }

    private fun updatePrevPos() {
        prevPosX = posX
        prevPosY = posY
    }

    private fun updatePrevSize() {
        prevWidth = width
        prevHeight = height
    }

    open fun onRender(absolutePos: Vec2f) {}

    open fun onPostRender(absolutePos: Vec2f) {}

    enum class SettingGroup(val groupName: String) {
        NONE(""),
        CLICK_GUI("click_gui"),
        HUD_GUI("hud_gui")
    }
}