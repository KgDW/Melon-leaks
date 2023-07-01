package melon.system.render.component.impl.windows

import melon.system.render.RenderManager
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.render.graphic.RenderUtils2D
import melon.system.render.shader.WindowBlurShader

/**
 * Window with rectangle rendering
 */
open class BasicWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
) : CleanWindow(name, posX, posY, width, height) {

    fun getRadius(): Int { return 0 }

    override fun onRender(absolutePos: Vec2f) {
        super.onRender(absolutePos)
        // Blur
        if (RenderManager.Gui.Window.windowBlur) {
            WindowBlurShader.render(renderWidth, renderHeight)
        }
        val needRounding = RenderManager.Gui.Window.windowRadius > 0.0f
        // Body
        if (RenderManager.Gui.Window.titleBar) {
            if (needRounding)
                RenderUtils2D.drawHalfRoundedRectFilled(0.0f, draggableHeight, renderWidth, renderHeight - draggableHeight, RenderManager.Gui.Window.windowRadius, RenderUtils2D.HalfRoundedDirection.BOTTOM, RenderManager.Color.backGround)
            else
                RenderUtils2D.drawRectFilled(0.0f, draggableHeight, renderWidth, renderHeight, RenderManager.Color.backGround)
        } else {
            if (needRounding)
                RenderUtils2D.drawRoundedRectFilled(0.0f, 0.0f, renderWidth, renderHeight, RenderManager.Gui.Window.windowRadius, RenderManager.Color.backGround)
            else
                RenderUtils2D.drawRectFilled(0.0f, 0.0f, renderWidth, renderHeight, RenderManager.Color.backGround)
        }
        // Outline
        if (RenderManager.Gui.Window.windowOutline) {
            RenderUtils2D.drawRectOutline(0.0f, 0.0f, renderWidth, renderHeight, 1.0f, RenderManager.Color.primary.alpha(255))
        }
        // TitleBar
        if (RenderManager.Gui.Window.titleBar) {
            if (needRounding)
                RenderUtils2D.drawHalfRoundedRectFilled(0.0f, 0.0f, renderWidth, draggableHeight, RenderManager.Gui.Window.windowRadius, RenderUtils2D.HalfRoundedDirection.TOP, RenderManager.Color.primary)
            else
                RenderUtils2D.drawRectFilled(0.0f, 0.0f, renderWidth, draggableHeight, RenderManager.Color.primary)
        }
    }

}