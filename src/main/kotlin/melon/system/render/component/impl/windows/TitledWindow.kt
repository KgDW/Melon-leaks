package melon.system.render.component.impl.windows

import melon.system.render.RenderManager
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.render.font.renderer.MainFontRenderer

/**
 * Window with rectangle and title rendering
 */
open class TitledWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
) : BasicWindow(name, posX, posY, width, height) {
    override val draggableHeight: Float get() = MainFontRenderer.getHeight() + 5.0f

    override val minimizable get() = true

    override fun onRender(absolutePos: Vec2f) {
        super.onRender(absolutePos)
        MainFontRenderer.drawString(name, 3.0f, 1.5f, RenderManager.Color.text)
    }
}