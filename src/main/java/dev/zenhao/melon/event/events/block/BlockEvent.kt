package dev.zenhao.melon.event.events.block

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class BlockEvent(var pos: BlockPos, var facing: EnumFacing) : Event, IEventPosting by Companion {
    companion object: EventBus()
}