package dev.zenhao.melon.utils.info

import melon.events.RunGameLoopEvent
import melon.system.event.*

object McRenderInfo : AlwaysListening {
    var partialTicks = 0.0f

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            partialTicks = if (mc.isGamePaused) mc.renderPartialTicksPaused else mc.renderPartialTicks
        }
    }
}