package dev.zenhao.melon.event.events.block

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.util.math.BlockPos

class BlockBreakEvent(val breakerID: Int, val position: BlockPos, val progress: Int) : Event, IEventPosting by Companion {
    companion object : EventBus()
}