package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import melon.events.PacketEvents
import melon.events.RunGameLoopEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketEntity
import net.minecraft.network.play.server.SPacketEntity.*
import net.minecraft.network.play.server.SPacketMoveVehicle
import net.minecraft.network.play.server.SPacketSetPassengers
import net.minecraft.util.EnumHand

@Module.Info(name = "BoatFly", category = Category.MOVEMENT, description = "Weeeeeeee")
object BoatFly : Module() {
    var noKick: Setting<Boolean> = bsetting("NoKick", false)
    var packet: Setting<Boolean> = bsetting("Packet", false)
    var remount = bsetting("Remount", false)
    var remountPackets = bsetting("RemountPackets", false)
    var fixYaw = bsetting("FixYaw", false)
    var noForce = bsetting("NoForceMove", false)
    var bypass = bsetting("Bypass", false)
    var speed: Setting<Double> = dsetting("Speed", 2.9, 0.1, 10.0)
    var upSpeed = dsetting("UpSpeed", 2.0, 0.0, 10.0)
    var downSpeed = dsetting("DownSpeed", 1.0, 0.0, 10.0)
    var glideSpeed = fsetting("GlideSpeed", 1f, 0f, 10f)

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            if (player.getRidingEntity() == null) {
                return@safeEventListener
            }
            player.getRidingEntity()!!.setNoGravity(true)
            if (glideSpeed.value != 0f) {
                player.getRidingEntity()!!.motionY -= glideSpeed.value.toDouble()
            } else {
                player.getRidingEntity()!!.motionY = 0.0
            }
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                player.getRidingEntity()!!.onGround = false
                player.getRidingEntity()!!.motionY = upSpeed.value
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown) {
                player.getRidingEntity()!!.onGround = false
                player.getRidingEntity()!!.motionY -= downSpeed.value
            }
            val normalDir = directionSpeed(speed.value / 2.0)
            if (player.movementInput.moveStrafe != 0.0f || player.movementInput.moveForward != 0.0f) {
                player.getRidingEntity()!!.motionX = normalDir[0]
                player.getRidingEntity()!!.motionZ = normalDir[1]
            } else {
                player.getRidingEntity()!!.motionX = 0.0
                player.getRidingEntity()!!.motionZ = 0.0
            }
            if (fixYaw.value) {
                player.getRidingEntity()!!.rotationYaw = player.rotationYaw
            }
            if (noKick.value) {
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    if (player.ticksExisted % 8 < 2) {
                        player.getRidingEntity()!!.motionY = -0.03999999910593033
                    }
                } else if (player.ticksExisted % 8 < 4) {
                    player.getRidingEntity()!!.motionY = -0.07999999821186066
                }
            }
        }

        safeEventListener<PacketEvents.Receive> { event ->
            if (event.packet is SPacketMoveVehicle && player.isRiding) {
                event.cancelled = true
            }
            if (event.packet is SPacketEntity) {
                if (player.getRidingEntity() != null && noForce.value) {
                    if (event.packet.getEntity(world) === player.getRidingEntity()) {
                        event.cancelled = true
                    }
                }
            }
            if (event.packet is S15PacketEntityRelMove) {
                if (player.getRidingEntity() != null && noForce.value) {
                    if (event.packet.getEntity(world) === player.getRidingEntity()) {
                        event.cancelled = true
                    }
                }
            }
            if (event.packet is S16PacketEntityLook) {
                if (player.getRidingEntity() != null && noForce.value) {
                    if (event.packet.getEntity(world) === player.getRidingEntity()) {
                        event.cancelled = true
                    }
                }
            }
            if (event.packet is S17PacketEntityLookMove) {
                if (player.getRidingEntity() != null && noForce.value) {
                    if (event.packet.getEntity(world) === player.getRidingEntity()) {
                        event.cancelled = true
                    }
                }
            }
            if (event.packet is SPacketSetPassengers) {
                val riding = player.getRidingEntity()
                if (riding != null && event.packet.entityId == riding.getEntityId() && remount.value) {
                    event.cancelled = true
                    remove(event.packet, player, riding)
                }
            }
        }

        safeEventListener<PacketEvents.Send> { event ->
            if ((event.packet is CPacketPlayer.Rotation || event.packet is CPacketInput) && player.isRiding) {
                event.cancelled = true
            }
            if (event.packet is CPacketVehicleMove) {
                val riding = player.getRidingEntity()
                if (riding != null && bypass.value) {
                    sendPackets(riding)
                }
            }
        }
    }

    fun SafeClientEvent.sendPackets(riding: Entity) {
        player.connection.sendPacket(CPacketUseEntity(riding, EnumHand.MAIN_HAND))
        player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
    }

    private fun SafeClientEvent.remove(packet: SPacketSetPassengers, player: Entity, riding: Entity) {
        for (id in packet.passengerIds) {
            if (id == player.getEntityId()) {
                if (remountPackets.value) {
                    sendPackets(riding)
                }
            } else {
                try {
                    val entity = world.getEntityByID(id)
                    entity?.dismountRidingEntity()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun SafeClientEvent.directionSpeed(speed: Double): DoubleArray {
        var forward = player.movementInput.moveForward
        var side = player.movementInput.moveStrafe
        var yaw =
            player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * mc.renderPartialTicks
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (if (forward > 0.0f) -45 else 45).toFloat()
            } else if (side < 0.0f) {
                yaw += (if (forward > 0.0f) 45 else -45).toFloat()
            }
            side = 0.0f
            if (forward > 0.0f) {
                forward = 1.0f
            } else if (forward < 0.0f) {
                forward = -1.0f
            }
        }
        val sin = Math.sin(Math.toRadians((yaw + 90.0f).toDouble()))
        val cos = Math.cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val posX = forward * speed * cos + side * speed * sin
        val posZ = forward * speed * sin - side * speed * cos
        return doubleArrayOf(posX, posZ)
    }
}