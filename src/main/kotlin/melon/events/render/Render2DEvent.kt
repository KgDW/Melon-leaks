package melon.events.render

import melon.system.event.Event
import melon.system.event.IEventPosting
import melon.system.event.NamedProfilerEventBus

sealed class Render2DEvent : Event {
    object Mc : Render2DEvent(), IEventPosting by NamedProfilerEventBus("mc")
    object Absolute : Render2DEvent(), IEventPosting by NamedProfilerEventBus("absolute")
    object NMSL : Render2DEvent(), IEventPosting by NamedProfilerEventBus("NMSL")
}