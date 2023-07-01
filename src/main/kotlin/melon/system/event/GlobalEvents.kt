package melon.system.event

import melon.events.ConnectionEvent
import melon.events.RunGameLoopEvent
import melon.events.WorldEvent
import melon.system.util.interfaces.MinecraftWrapper
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraftforge.fml.common.eventhandler.Event

abstract class AbstractClientEvent {
    val mc = MinecraftWrapper.mc
    abstract val world: WorldClient?
    abstract val player: EntityPlayerSP?
    abstract val playerController: PlayerControllerMP?
    abstract val connection: NetHandlerPlayClient?
}

open class ClientEvent : AbstractClientEvent() {
    final override val world: WorldClient? = mc.world
    final override val player: EntityPlayerSP? = mc.player
    final override val playerController: PlayerControllerMP? = mc.playerController
    final override val connection: NetHandlerPlayClient? = mc.connection

    inline operator fun <T> invoke(block: ClientEvent.() -> T) = run(block)
}

open class SafeClientEvent internal constructor(
    override val world: WorldClient,
    override val player: EntityPlayerSP,
    override val playerController: PlayerControllerMP,
    override val connection: NetHandlerPlayClient
) : AbstractClientEvent() {
    inline operator fun <T> invoke(block: SafeClientEvent.() -> T) = run(block)

    companion object : ListenerOwner(), MinecraftWrapper {
        var instance: SafeClientEvent? = null; private set

        fun initListener() {
            listener<ConnectionEvent.Disconnect>(Int.MAX_VALUE, true) {
                reset()
            }

            listener<WorldEvent.Unload>(Int.MAX_VALUE, true) {
                reset()
            }

            listener<RunGameLoopEvent.Tick>(Int.MAX_VALUE, true) {
                update()
            }
        }

        fun update() {
            val world = mc.world ?: return
            val player = mc.player ?: return
            val playerController = mc.playerController ?: return
            val connection = mc.connection ?: return

            instance = SafeClientEvent(world, player, playerController, connection)
        }

        fun reset() {
            instance = null
        }
    }
}

fun Event.cancel() {
    this.isCanceled = true
}