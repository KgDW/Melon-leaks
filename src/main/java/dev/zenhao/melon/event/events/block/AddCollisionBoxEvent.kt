package dev.zenhao.melon.event.events.block

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

class AddCollisionBoxEvent(
    val entity: Entity?,
    val entityBox: AxisAlignedBB,
    val pos: BlockPos,
    val block: Block,
    val collidingBoxes: MutableList<AxisAlignedBB>
) : Event, IEventPosting by Companion {
    companion object : EventBus()
}