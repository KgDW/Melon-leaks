package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import java.util.*

@Module.Info(name = "Blink", category = Category.PLAYER, description = "Cancels server side packets")
object Blink : Module() {
    var mode: Setting<*> = msetting("Mode", Modes.All)
    var packets: Queue<Packet<*>> = LinkedList()
    private var clonedPlayer: EntityOtherPlayerMP? = null
    override fun onDisable() {
        runSafe {
            while (packets.isNotEmpty()) {
                connection.sendPacket(packets.poll())
            }
            world.removeEntityFromWorld(-1600)
            clonedPlayer = null
        }
    }

    override fun onEnable() {
        runSafe {
            EntityOtherPlayerMP(world, mc.getSession().profile).also { clonedPlayer = it }
                .copyLocationAndAnglesFrom(player)
            clonedPlayer!!.rotationYawHead = player.rotationYawHead
            world.addEntityToWorld(-1600, clonedPlayer!!)
        }
    }

    init {
        safeEventListener<PacketEvents.Send> {
            if (isEnabled && (mode.value === Modes.All && it.packet !is CPacketPlayerTryUseItem || it.packet is CPacketPlayer)) {
                it.cancelled = true
                packets.add(it.packet)
            }
        }
    }

    override fun getHudInfo(): String {
        return packets.size.toString()
    }

    enum class Modes {
        CPacketPlayer, All
    }
}