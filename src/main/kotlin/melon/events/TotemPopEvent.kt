package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.entity.player.EntityPlayer

sealed class TotemPopEvent(val name: String, val count: Int) : Event {
    class Pop(val entity: EntityPlayer, count: Int) : TotemPopEvent(entity.name, count), IEventPosting by Companion {
        companion object : EventBus()
    }

    class Death(val entity: EntityPlayer, count: Int) : TotemPopEvent(entity.name, count), IEventPosting by Companion {
        companion object : EventBus()
    }

    class Clear(name: String, count: Int) : TotemPopEvent(name, count), IEventPosting by Companion {
        companion object : EventBus()
    }
}