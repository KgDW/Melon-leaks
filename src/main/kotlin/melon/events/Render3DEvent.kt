package melon.events

import melon.system.event.Event
import melon.system.event.IEventPosting
import melon.system.event.NamedProfilerEventBus

object Render3DEvent : Event, IEventPosting by NamedProfilerEventBus("melonRender3D")