package melon.events.render

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.client.gui.ScaledResolution

class Render2DOverlayEvent(var partialTicks: Float, var scaledResolution: ScaledResolution) : Event,
    IEventPosting by Companion {
    companion object : EventBus()
}