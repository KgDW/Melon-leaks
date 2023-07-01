package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.combat.AutoTotem.getInventorySlots
import melon.events.TickEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketClickWindow
import java.util.concurrent.atomic.AtomicInteger

@Module.Info(name = "Anti32kTotem", description = "null", category = Category.COMBAT)
object Anti32kTotem : Module() {
    private var packetClick = bsetting("PacketClick", false)
    private var numOfTotems = 0
    private var preferredTotemSlot = 0

    override fun getHudInfo(): String {
        return numOfTotems.toString()
    }

    private fun SafeClientEvent.findTotems(): Boolean {
        numOfTotems = 0
        val preferredTotemSlotStackSize = AtomicInteger()
        preferredTotemSlotStackSize.set(Int.MIN_VALUE)
        inventoryAndHotbarSlots.forEach { (slotKey: Int, slotValue: ItemStack) ->
            var numOfTotemsInStack = 0
            if (slotValue.getItem() == Items.TOTEM_OF_UNDYING) {
                numOfTotemsInStack = slotValue.count
                if (preferredTotemSlotStackSize.get() < numOfTotemsInStack) {
                    preferredTotemSlotStackSize.set(numOfTotemsInStack)
                    preferredTotemSlot = slotKey
                }
            }
            numOfTotems += numOfTotemsInStack
        }
        if (player.heldItemOffhand.getItem() == Items.TOTEM_OF_UNDYING) {
            numOfTotems += player.heldItemOffhand.count
        }
        return numOfTotems != 0
    }

    init {
        safeEventListener<TickEvent.Pre> {
            if (mc.currentScreen is GuiContainer || !findTotems() || player.inventory.getStackInSlot(0)
                    .getItem() === Items.TOTEM_OF_UNDYING
            ) {
                return@safeEventListener
            }
            for (i in 9..36) {
                if (player.inventory.getStackInSlot(i).getItem() === Items.TOTEM_OF_UNDYING) {
                    if (packetClick.value) {
                        connection.sendPacket(CPacketClickWindow(player.inventoryContainer.windowId, i, 0, ClickType.SWAP, player.inventory.getStackInSlot(i), player.openContainer.getNextTransactionID(player.inventory)))
                    } else {
                        playerController.windowClick(
                            player.inventoryContainer.windowId,
                            i,
                            0,
                            ClickType.SWAP,
                            player
                        )
                    }
                    playerController.updateController()
                    break
                }
            }
        }
    }

    private val SafeClientEvent.inventoryAndHotbarSlots: Map<Int, ItemStack>
        get() = getInventorySlots(9)
}