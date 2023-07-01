package melon.system.render.component.impl.windows

import melon.system.render.component.WindowComponent

/**
 * Window with no rendering
 */
open class CleanWindow(
    name: String,
    posX: Float,
    posY: Float,
    width: Float,
    height: Float,
) : WindowComponent(name, posX, posY, width, height)