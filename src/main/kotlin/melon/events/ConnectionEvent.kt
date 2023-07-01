package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting

sealed class ConnectionEvent : Event {
    internal object Connect : ConnectionEvent(), IEventPosting by EventBus()
    internal object Disconnect : ConnectionEvent(), IEventPosting by EventBus()
}