package dev.zenhao.melon.manager

import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator
import dev.zenhao.melon.utils.vector.Vec2f
import melon.events.PacketEvents
import melon.events.RunGameLoopEvent
import melon.system.event.AlwaysListening
import melon.system.event.safeEventListener
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec3d

object CrystalManager : AlwaysListening {
    var position: Vec3d = Vec3d.ZERO; private set
    var eyePosition: Vec3d = Vec3d.ZERO; private set
    var rotation = Vec2f.ZERO; private set

    fun onInit() {
        safeEventListener<RunGameLoopEvent.Tick> {
            for (entity in ArrayList(world.loadedEntityList)) {
                if (entity !is EntityLivingBase) continue
                CrystalDamageCalculator.reductionMap[entity] = CrystalDamageCalculator.DamageReduction(entity)
            }
        }

        safeEventListener<PacketEvents.PostSend> {
            if (it.packet !is CPacketPlayer) return@safeEventListener
            if (it.packet.moving) {
                position = Vec3d(it.packet.x, it.packet.y, it.packet.z)
                eyePosition = Vec3d(it.packet.x, it.packet.y + player.getEyeHeight(), it.packet.z)
            }
            if (it.packet.rotating) {
                rotation = Vec2f(it.packet.yaw, it.packet.pitch)
            }
        }
    }
}