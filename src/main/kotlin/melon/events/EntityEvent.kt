package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.entity.EntityLivingBase

sealed class EntityEvent(val entity: EntityLivingBase) : Event {
    class UpdateHealth(entity: EntityLivingBase, val prevHealth: Float, val health: Float) : EntityEvent(entity), IEventPosting by Companion {
        companion object : EventBus()
    }

    class Death(entity: EntityLivingBase) : EntityEvent(entity), IEventPosting by Companion {
        companion object : EventBus()
    }
}