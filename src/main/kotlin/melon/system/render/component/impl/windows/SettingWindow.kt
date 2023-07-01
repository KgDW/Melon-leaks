package melon.system.render.component.impl.windows


import melon.system.config.setting.AbstractSetting
import melon.system.render.component.impl.basic.Button
import melon.system.render.component.impl.basic.Slider
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.config.setting.type.*
import melon.system.render.component.impl.settings.EnumSlider
import melon.system.render.component.impl.settings.SettingButton
import melon.system.render.component.impl.settings.SettingSlider
import melon.system.render.component.impl.settings.StringButton
import org.lwjgl.input.Keyboard

abstract class SettingWindow<T : Any>(
    name: String,
    val element: T,
    posX: Float,
    posY: Float,
) : ListWindow(name, posX, posY, 150.0f, 200.0f) {

    override val minWidth: Float get() = 100.0f
    override val minHeight: Float get() = draggableHeight

    override val minimizable get() = false

    var listeningChild: Slider? = null; private set
    private var initialized = false

    protected abstract fun getSettingList(): List<AbstractSetting<*>>

    override fun onGuiInit() {
        super.onGuiInit()
        if (!initialized) {
            for (setting in getSettingList()) {
                when (setting) {
                    is BooleanSetting -> SettingButton(setting)
                    is NumberSetting -> SettingSlider(setting)
                    is EnumSetting -> EnumSlider(setting)
                    is ColorSetting -> Button(setting.name, { displayColorPicker(setting) }, setting.description, setting.visibility)
                    is StringSetting -> StringButton(setting)
                    is BindSetting -> melon.system.render.component.impl.settings.BindButton(setting)
                    else -> null
                }?.also {
                    children.add(it)
                }
            }
            initialized = true
        }
    }

    private fun displayColorPicker(colorSetting: ColorSetting) {
        ColorPicker.visible = true
        ColorPicker.setting = colorSetting
        ColorPicker.onDisplayed()
    }

    override fun onDisplayed() {
        super.onDisplayed()
        lastActiveTime = System.currentTimeMillis() + 1000L
    }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        (hoveredChild as? Slider)?.let {
            if (it != listeningChild) {
                listeningChild?.onStopListening(false)
                listeningChild = it.takeIf { it.listening }
            }
        }
    }

    override fun onTick() {
        super.onTick()
        if (listeningChild?.listening == false) listeningChild = null
        Keyboard.enableRepeatEvents(listeningChild != null)
    }

    override fun onClosed() {
        super.onClosed()
        listeningChild = null
        ColorPicker.visible = false
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        listeningChild?.onKeyInput(keyCode, keyState)
    }

}