package melon.system.render.component.impl.settings

import melon.system.config.setting.type.BindSetting
import melon.system.render.RenderManager
import melon.system.render.component.impl.basic.Slider
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.render.font.renderer.MainFontRenderer
import org.lwjgl.input.Keyboard

class BindButton(
    private val setting: BindSetting
) : Slider(setting.name, setting.description, setting.visibility) {
    override fun onRelease(mousePos: Vec2f, buttonId: Int) {
        super.onRelease(mousePos, buttonId)
        if (listening) {
            setting.value.apply {
                if (buttonId > 1) setBind(-buttonId - 1)
            }
        }

        listening = !listening
    }

    override fun onKeyInput(keyCode: Int, keyState: Boolean) {
        super.onKeyInput(keyCode, keyState)
        if (listening && keyCode != Keyboard.KEY_NONE && !keyState) {
            setting.value.apply {
                if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) clear()
                else setBind(keyCode)
                inputField = setting.name
                listening = false
            }
        }
    }

    override fun onRender(absolutePos: Vec2f) {
        super.onRender(absolutePos)

        val valueText = if (listening) "Listening" else setting.value.toString()

        protectedWidth = MainFontRenderer.getWidth(valueText, 0.75f)
        val posX = renderWidth - protectedWidth - 2.0f
        val posY = renderHeight - 2.0f - MainFontRenderer.getHeight(0.75f)
        MainFontRenderer.drawString(valueText, posX, posY, RenderManager.Color.text, 0.75f)
    }
}