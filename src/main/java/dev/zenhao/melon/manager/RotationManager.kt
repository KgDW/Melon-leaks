package dev.zenhao.melon.manager

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.extension.synchronized
import dev.zenhao.melon.utils.math.RotationUtils.getRotationTo
import dev.zenhao.melon.utils.vector.Vec2f
import dev.zenhao.melon.utils.vector.VectorUtils.toVec3d
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import melon.system.event.AlwaysListening
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.runSafe
import melon.utils.concurrent.threads.runSynchronized
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object RotationManager : AlwaysListening {
    private val rotationMap = ObjectArrayList<Vec2f>().synchronized()
    fun onInit() {
        safeEventListener<PlayerMotionEvent> { event ->
            rotationMap.runSynchronized {
                if (rotationMap.isNotEmpty()) {
                    rotationMap.forEach {
                        val packet = Vec2f(it.x, it.y)
                        event.setRotation(packet.x, packet.y)
                        rotationMap.remove(packet)
                    }
                }
            }
        }
    }

    @JvmStatic
    fun addRotations(yaw: Float, pitch: Float) {
        rotationMap.add(Vec2f(yaw, pitch))
    }

    @JvmStatic
    fun addRotations(rotation: Vec2f) {
        rotationMap.add(rotation)
    }

    @JvmStatic
    fun addRotations(blockPos: BlockPos) {
        rotationMap.add(
            Vec2f(
                BlockInteractionHelper.getLegitRotations(Vec3d(blockPos))[0],
                BlockInteractionHelper.getLegitRotations(Vec3d(blockPos))[1]
            )
        )
    }

    @JvmStatic
    fun addRotationsNew(blockPos: BlockPos) {
        runSafe {
            rotationMap.add(getRotationTo(blockPos.toVec3d()))
        }
    }

    @JvmStatic
    fun addRotations(vec3d: Vec3d) {
        rotationMap.add(
            Vec2f(
                BlockInteractionHelper.getLegitRotations(vec3d)[0],
                BlockInteractionHelper.getLegitRotations(vec3d)[1]
            )
        )
    }
}