package dev.zenhao.melon.event.events.render

import melon.system.event.*

class ResolutionUpdateEvent(val width: Int, val height: Int) : Event, IEventPosting by Companion {
    companion object : EventBus()
}
