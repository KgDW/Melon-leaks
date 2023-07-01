package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack

@Module.Info(name = "ChestStealer", category = Category.PLAYER)
class ChestStealer : Module() {
    private val delayTimer = TimerUtils()
    private var mode = msetting("Mode", Mode.Steal)
    private var entityChest = bsetting("EntityChest", true)
    private var pause = bsetting("Pause", false)
    private var delay = dsetting("Delay", 170.0, 0.0, 1000.0)

    override fun onEnable() {
        delayTimer.reset()
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            if (delayTimer.tickAndReset(delay.value)) {
                if (mc.currentScreen is GuiChest) {
                    val guiChest = mc.currentScreen as GuiChest
                    if (!pause.value) {
                    handleClick(guiChest, guiChest.lowerChestInventory.sizeInventory)
                    } else {
                        playerController.windowClick(
                            player.inventoryContainer.windowId,
                            player.inventory.currentItem,
                            0,
                            ClickType.PICKUP,
                            player
                        )
                        playerController.windowClick(guiChest.inventorySlots.windowId, 1, 0, ClickType.PICKUP, player)
                    }
                    playerController.updateController()
                } else if (mc.currentScreen is GuiScreenHorseInventory && entityChest.value) {
                    val horseInventory = mc.currentScreen as GuiScreenHorseInventory
                    handleClick(horseInventory, horseInventory.horseInventory.sizeInventory)
                    playerController.updateController()
                }
            }
        }
    }

    private fun SafeClientEvent.handleClick(guiContainer: GuiContainer?, size: Int) {
        for (i in 0 until size) {
            val stack = guiContainer!!.inventorySlots.inventory[i]
            if (doMode(guiContainer.inventorySlots.windowId, i, stack)) break
        }
    }

    private fun SafeClientEvent.doMode(windowId: Int, i: Int, stack: ItemStack): Boolean {
        if (stack.isEmpty() && mode.value != Mode.Drop) return false
        when (mode.value) {
            Mode.Steal -> {
                playerController.windowClick(windowId, i, 0, ClickType.QUICK_MOVE, player)
                return true
            }

            Mode.Swap -> {
                for (a in 0..35) {
                    if (player.inventory.getStackInSlot(a).getItem() === Items.AIR) {
                        playerController.windowClick(windowId, i, a, ClickType.SWAP, player)
                    }
                }
                return true
            }

            Mode.Drop -> {
                playerController.windowClick(windowId, i, 1, ClickType.THROW, player)
                return true
            }
        }
        return false
    }

    enum class Mode {
        Steal, Swap, Drop
    }
}