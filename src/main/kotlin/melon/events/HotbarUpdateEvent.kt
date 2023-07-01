package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting

class HotbarUpdateEvent(val oldSlot: Int, val newSlot: Int) : Event, IEventPosting by Companion {
    companion object : EventBus()
}