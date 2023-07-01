package melon.system.render.component.impl.settings

import melon.system.config.setting.type.EnumSetting
import melon.system.render.RenderManager
import melon.system.render.component.impl.basic.Slider
import dev.zenhao.melon.utils.extension.readableName
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.render.font.renderer.MainFontRenderer
import kotlin.math.floor

class EnumSlider(val setting: EnumSetting<*>) : Slider(setting.name, setting.description, setting.visibility) {
    private val enumValues = setting.enumValues

    override val progress: Float
        get() {
            if (mouseState != MouseState.DRAG) {
                val settingValue = setting.value.ordinal
                if (roundInput(renderProgress.current) != settingValue) {
                    return (settingValue + settingValue / (enumValues.size - 1.0f)) / enumValues.size.toFloat()
                }
            }
            return Float.NaN
        }

    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (prevState != MouseState.DRAG) setting.nextValue()
    }

    override fun onDrag(mousePos: Vec2f, clickPos: Vec2f, buttonId: Int) {
        super.onDrag(mousePos, clickPos, buttonId)
        updateValue(mousePos)
    }

    private fun updateValue(mousePos: Vec2f) {
        setting.read(enumValues[roundInput(mousePos.x / width)].name)
    }

    private fun roundInput(input: Float) = floor(input * enumValues.size).toInt().coerceIn(0, enumValues.size - 1)

    override fun onRender(absolutePos: Vec2f) {
        val valueText = setting.value.readableName()
        protectedWidth = MainFontRenderer.getWidth(valueText, 0.75f)

        super.onRender(absolutePos)
        val posX = renderWidth - protectedWidth - 2.0f
        val posY = renderHeight - 2.0f - MainFontRenderer.getHeight(0.75f)
        MainFontRenderer.drawString(valueText, posX, posY, RenderManager.Color.text, 0.75f)
    }
}