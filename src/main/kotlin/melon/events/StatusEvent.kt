package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting

class StatusEvent(var status: Int, var glow: Boolean) : Event, IEventPosting by Companion {
    companion object: EventBus()
}
