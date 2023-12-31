package dev.zenhao.melon.manager

import melon.events.ConnectionEvent
import melon.events.TickEvent
import melon.events.WorldEvent
import melon.system.event.AlwaysListening
import melon.system.event.listener
import melon.system.event.safeParallelListener
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB

@Suppress("NOTHING_TO_INLINE")
object EntityManager: AlwaysListening {
    private var entity0 = emptyList<Entity>()
    val entity: List<Entity>
        get() = entity0

    private var livingBase0 = emptyList<EntityLivingBase>()
    val livingBase: List<EntityLivingBase>
        get() = livingBase0

    private var players0 = emptyList<EntityPlayer>()
    val players: List<EntityPlayer>
        get() = players0

    fun onInit() {
        listener<ConnectionEvent.Disconnect>(Int.MAX_VALUE, true) {
            entity0 = emptyList()
            livingBase0 = emptyList()
            players0 = emptyList()
        }

        listener<WorldEvent.Entity.Add>(Int.MAX_VALUE, true) {
            entity0 = entity0 + it.entity

            if (it.entity is EntityLivingBase) {
                livingBase0 = livingBase0 + it.entity

                if (it.entity is EntityPlayer) {
                    players0 = players0 + it.entity
                }
            }
        }

        listener<WorldEvent.Entity.Remove>(Int.MAX_VALUE, true) {
            entity0 = entity0 - it.entity

            if (it.entity is EntityLivingBase) {
                livingBase0 = livingBase0 - it.entity

                if (it.entity is EntityPlayer) {
                    players0 = players0 - it.entity
                }
            }
        }

        safeParallelListener<TickEvent.Post> {
            entity0 = world.loadedEntityList.toList()
            livingBase0 = world.loadedEntityList.filterIsInstance<EntityLivingBase>()
            players0 = world.playerEntities.toList()
        }
    }

    inline fun checkEntityCollision(box: AxisAlignedBB, noinline predicate: (Entity) -> Boolean): Boolean {
        return entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it.preventEntitySpawning }
            .filter { it.entityBoundingBox.intersects(box) }
            .filter(predicate)
            .none()
    }

    inline fun checkEntityCollision(box: AxisAlignedBB, ignoreEntity: Entity): Boolean {
        return entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it.preventEntitySpawning }
            .filter { it != ignoreEntity }
            .filter { it.entityBoundingBox.intersects(box) }
            .none()
    }

    inline fun checkEntityCollision(box: AxisAlignedBB): Boolean {
        return entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it.preventEntitySpawning }
            .filter { it.entityBoundingBox.intersects(box) }
            .none()
    }

    inline fun checkAnyEntity(box: AxisAlignedBB, noinline predicate: (Entity) -> Boolean): Boolean {
        return entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it.entityBoundingBox.intersects(box) }
            .filter(predicate)
            .none()
    }

    inline fun checkAnyEntity(box: AxisAlignedBB, ignoreEntity: Entity): Boolean {
        return entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it != ignoreEntity }
            .filter { it.entityBoundingBox.intersects(box) }
            .none()
    }

    inline fun checkAnyEntity(box: AxisAlignedBB): Boolean {
        return entity.asSequence()
            .filter { it.isEntityAlive }
            .filter { it.entityBoundingBox.intersects(box) }
            .none()
    }
}