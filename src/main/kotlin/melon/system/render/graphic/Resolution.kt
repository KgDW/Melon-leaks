package melon.system.render.graphic

import melon.system.render.RenderManager
import dev.zenhao.melon.utils.extension.fastCeil
import melon.system.event.AlwaysListening
import melon.system.util.interfaces.MinecraftWrapper

object Resolution : AlwaysListening, MinecraftWrapper {
    val widthI
        get() = mc.displayWidth

    val heightI
        get() = mc.displayHeight

    val heightF
        get() = mc.displayHeight.toFloat()

    val widthF
        get() = mc.displayWidth.toFloat()

}