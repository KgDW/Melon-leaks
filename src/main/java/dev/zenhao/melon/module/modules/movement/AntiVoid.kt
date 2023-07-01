package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import melon.events.PlayerMoveEvent
import melon.system.event.safeEventListener
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.atomic.AtomicInteger

@Module.Info(name = "AntiVoid", category = Category.MOVEMENT, description = "Fuck OFF 2b2t.org void")
class AntiVoid : Module() {
    private val teleportID = AtomicInteger()
    var mode = msetting("Mode", Mode.BOUNCE)
    var groundTimerUtils = TimerUtils()
    var lastGroundPos: BlockPos? = null

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is SPacketPlayerPosLook) {
            teleportID.set((event.packet as SPacketPlayerPosLook).getTeleportId())
        }
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            if (mode.value == Mode.MOTION) {
                if (mc.player.onGround) {
                    lastGroundPos = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
                }
                if (mc.player.posY <= 0.95 && !Flight.INSTANCE.isEnabled) {
                    it.y = 6.0
                }
            }
        }

        safeEventListener<PlayerMoveEvent> { event ->
            val yLevel = player.posY
            runCatching {
                if (yLevel <= 0.95) {
                    if (mode.value == Mode.BOUNCE) {
                        player.moveVertical = 10.0f
                        player.jump()
                    } else if (mode.value == Mode.CANCEL) {
                        player.jump()
                        event.cancelled = true
                    }
                } else {
                    player.moveVertical = 0.0f
                }
            }
        }
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        mc.player.moveVertical = 0.0f
        groundTimerUtils.reset()
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        mc.player.moveVertical = 0.0f
        groundTimerUtils.reset()
    }

    override fun getHudInfo(): String? {
        if (mode.value == Mode.BOUNCE) {
            return "Bounce"
        } else if (mode.value == Mode.CANCEL) {
            return "Cancel"
        } else if (mode.value == Mode.MOTION) {
            return "Motion"
        }
        return null
    }

    enum class Mode {
        BOUNCE, CANCEL, MOTION
    }
}