package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.mixin.client.accessor.AccessorSPacketConfirmTransaction
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import net.minecraft.network.play.client.CPacketConfirmTransaction
import net.minecraft.network.play.server.SPacketConfirmTransaction

@Module.Info(name = "Disabler", category = Category.MISC)
object Disabler : Module() {
    private var c0f = bsetting("C0F", false)
    private var s0f = bsetting("S0F", true)
    init {
        safeEventListener<PacketEvents.Receive> {
            when (it.packet) {
                is SPacketConfirmTransaction -> {
                    val packet = it.packet as AccessorSPacketConfirmTransaction
                    val windowID = packet.SWindowID()
                    val actionNumber = packet.SActionNumber()
                    if (actionNumber < 0 && windowID == 0 && s0f.value) {
                        it.cancelled = true
                        connection.sendPacket(CPacketConfirmTransaction(windowID, actionNumber, false))
                        connection.sendPacket(
                            CPacketConfirmTransaction(
                                player.inventoryContainer.windowId,
                                actionNumber,
                                false
                            )
                        )
                    }
                }
            }
        }

        safeEventListener<PacketEvents.Send> {
            when (it.packet) {
                is CPacketConfirmTransaction -> {
                    if (c0f.value) {
                        it.cancelled = true
                    }
                }
            }
        }
    }
}