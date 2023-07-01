package melon.events

import melon.system.event.Event
import melon.system.event.EventBus
import melon.system.event.IEventPosting
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

sealed class WorldEvent : Event {
    internal object Unload : WorldEvent(), IEventPosting by EventBus()
    internal object Load : WorldEvent(), IEventPosting by EventBus()

    sealed class Entity(val entity: net.minecraft.entity.Entity) : WorldEvent() {
        class Add(entity: net.minecraft.entity.Entity) : Entity(entity), IEventPosting by Companion {
            companion object : EventBus()
        }

        class Remove(entity: net.minecraft.entity.Entity) : Entity(entity), IEventPosting by Companion {
            companion object : EventBus()
        }
    }

    class ServerBlockUpdate(
        val pos: BlockPos,
        val oldState: IBlockState,
        val newState: IBlockState
    ) : WorldEvent(), IEventPosting by Companion {
        companion object : EventBus()
    }

    class ClientBlockUpdate(
        val pos: BlockPos,
        val oldState: IBlockState,
        val newState: IBlockState
    ) : WorldEvent(), IEventPosting by Companion {
        companion object : EventBus()
    }

    class RenderUpdate(
        val x1: Int,
        val y1: Int,
        val z1: Int,
        val x2: Int,
        val y2: Int,
        val z2: Int
    ) : WorldEvent(), IEventPosting by Companion {
        companion object : EventBus()
    }
}