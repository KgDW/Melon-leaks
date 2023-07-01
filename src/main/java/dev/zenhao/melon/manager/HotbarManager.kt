package dev.zenhao.melon.manager

import dev.zenhao.melon.utils.inventory.HotbarSlot
import dev.zenhao.melon.utils.inventory.action
import dev.zenhao.melon.utils.inventory.inventoryTaskNow
import dev.zenhao.melon.utils.inventory.swapWith
import melon.events.HotbarUpdateEvent
import melon.events.PacketEvents
import melon.system.event.AlwaysListening
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.inventory.slot.hotbarSlots
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketHeldItemChange

@Suppress("NOTHING_TO_INLINE")
object HotbarManager : AlwaysListening {
    var serverSideHotbar = 0; private set
    var swapTime = 0L; private set

    val EntityPlayerSP.serverSideItem: ItemStack
        get() = inventory.mainInventory[serverSideHotbar]

    fun onInit() {
        safeEventListener<PacketEvents.Send>(Int.MIN_VALUE) {
            if (it.cancelled || it.packet !is CPacketHeldItemChange) return@safeEventListener

            val prev = serverSideHotbar
            synchronized(playerController) {
                serverSideHotbar = it.packet.slotId
                swapTime = System.currentTimeMillis()
            }

            if (prev != serverSideHotbar) {
                HotbarUpdateEvent(prev, serverSideHotbar).post()
            }
        }
    }

    inline fun onSpoof(slot: Int) {
        try {
            if (Minecraft.getMinecraft().player.inventory.currentItem == slot || slot < 0) {
                return
            }
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread) {
                Minecraft.getMinecraft().connection!!.sendPacket(CPacketHeldItemChange(slot))
                Minecraft.getMinecraft().player.inventory.currentItem = slot
                Minecraft.getMinecraft().playerController.updateController()
            }
        } catch (_: Exception) {
        }
    }

    inline fun SafeClientEvent.spoofHotbarBypass(slot: HotbarSlot, crossinline block: () -> Unit) {
        synchronized(playerController) {
            val swap = slot.hotbarSlot != serverSideHotbar
            if (swap) {
                inventoryTaskNow {
                    val hotbarSlot = player.hotbarSlots[serverSideHotbar]
                    swapWith(slot, hotbarSlot)
                    action { block.invoke() }
                    swapWith(slot, hotbarSlot)
                }
            } else {
                block.invoke()
            }
        }
    }

    inline fun SafeClientEvent.spoofHotbarBypass(slot: Int, crossinline block: () -> Unit) {
        synchronized(playerController) {
            val swap = slot != serverSideHotbar
            if (swap) {
                inventoryTaskNow {
                    val hotbarSlot = player.hotbarSlots[serverSideHotbar]
                    val currentSlot = HotbarSlot(player.inventoryContainer.inventorySlots[slot])
                    swapWith(currentSlot, hotbarSlot)
                    action { block.invoke() }
                    swapWith(currentSlot, hotbarSlot)
                }
            } else {
                block.invoke()
            }
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot) {
        return spoofHotbar(slot.hotbarSlot)
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int) {
        if (serverSideHotbar != slot && slot >= 0) {
            connection.sendPacket(CPacketHeldItemChange(slot))
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: HotbarSlot, crossinline block: () -> Unit) {
        synchronized(playerController) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.spoofHotbar(slot: Int, crossinline block: () -> Unit) {
        synchronized(playerController) {
            spoofHotbar(slot)
            block.invoke()
            resetHotbar()
        }
    }

    inline fun SafeClientEvent.resetHotbar() {
        val slot = playerController.currentPlayerItem
        if (serverSideHotbar != slot) {
            spoofHotbar(slot)
        }
    }
}
