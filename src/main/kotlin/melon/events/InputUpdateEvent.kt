package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import melon.system.event.WrappedForgeEvent
import net.minecraftforge.client.event.InputUpdateEvent

class InputUpdateEvent(override val event: InputUpdateEvent) : Event, WrappedForgeEvent, IEventPosting by Companion {
    val movementInput
        get() = event.movementInput

    companion object : EventBus()
}