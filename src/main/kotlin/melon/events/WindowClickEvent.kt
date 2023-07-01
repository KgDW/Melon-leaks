package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType

class WindowClickEvent(var windowID: Int, var currentSlot: Int, var targetSlot: Int, var clickType: ClickType, var player: EntityPlayer) : Event, IEventPosting by Companion {
    companion object : EventBus()
}