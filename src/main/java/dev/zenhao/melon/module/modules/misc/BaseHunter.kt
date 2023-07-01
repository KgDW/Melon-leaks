package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.system.event.safeEventListener

@Module.Info(name = "BaseHunter", category = Category.MISC)
object BaseHunter: Module() {
    init {
        safeEventListener<PlayerMotionEvent> {

        }
    }

    private fun getSpiralCoords(n: Int): IntArray {
        var n0 = n
        var x = 0
        var z = 0
        var d = 1
        var lineNumber = 1
        var coords = intArrayOf(0, 0)
        for (i in 0 until n0) {
            if (2 * x * d < lineNumber) {
                x += d
                coords = intArrayOf(x, z)
            } else if (2 * z * d < lineNumber) {
                z += d
                coords = intArrayOf(x, z)
            } else {
                d *= -1
                lineNumber++
                n0++
            }
        }
        return coords
    }
}