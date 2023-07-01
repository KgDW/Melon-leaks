package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.MathHelper
import kotlin.math.floor

@Module.Info(name = "Phase", category = Category.MOVEMENT)
class Phase : Module() {
    private var ctp = bsetting("ConfirmTP", false)
    private val timeout = isetting("Timeout", 5, 1, 10)
    private val growOffset = dsetting("GrowOffset", 0.01, 0.01, 0.5, 0.01)
    private var teleportID = 0

    override fun onEnable() {
        teleportID = 0
    }

    init {
        safeEventListener<PacketEvents.Receive> {
            if (it.packet is SPacketPlayerPosLook && ctp.value) {
                teleportID = it.packet.getTeleportId()
            }
        }

        safeEventListener<PlayerMotionEvent> {
            if ((world.getCollisionBoxes(player, player.entityBoundingBox.grow(growOffset.value, 0.0, growOffset.value)).size < 2)) {
                player.setPosition(
                    roundToClosest(
                        player.posX,
                        floor(player.posX) + 0.301,
                        floor(player.posX) + 0.699
                    ),
                    player.posY,
                    roundToClosest(player.posZ, floor(player.posZ) + 0.301, floor(player.posZ) + 0.699)
                )
            } else if (player.ticksExisted % timeout.value == 0) {
                player.setPosition(
                    player.posX + MathHelper.clamp(
                        roundToClosest(
                            player.posX,
                            floor(player.posX) + 0.241,
                            floor(player.posX) + 0.759
                        ) - player.posX, -0.03, 0.03
                    ),
                    player.posY,
                    player.posZ + MathHelper.clamp(
                        roundToClosest(
                            player.posZ,
                            floor(player.posZ) + 0.241,
                            floor(player.posZ) + 0.759
                        ) - player.posZ, -0.03, 0.03
                    )
                )
                connection.sendPacket(
                    CPacketPlayer.Position(
                        player.posX,
                        player.posY,
                        player.posZ,
                        true
                    )
                )
                connection.sendPacket(
                    CPacketPlayer.Position(
                        roundToClosest(
                            player.posX,
                            floor(player.posX) + 0.23,
                            floor(player.posX) + 0.77
                        ),
                        player.posY,
                        roundToClosest(
                            player.posZ,
                            floor(player.posZ) + 0.23,
                            floor(player.posZ) + 0.77
                        ),
                        true
                    )
                )
            }
            if (ctp.value && teleportID != 0) {
                val id = ++teleportID
                connection.sendPacket(CPacketConfirmTeleport(id))
            }
        }
    }

    private fun roundToClosest(num: Double, low: Double, high: Double): Double {
        val d1 = num - low
        val d2 = high - num
        return if (d2 > d1) {
            low
        } else {
            high
        }
    }
}