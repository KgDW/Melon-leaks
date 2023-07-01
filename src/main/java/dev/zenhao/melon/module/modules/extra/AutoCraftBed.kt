package dev.zenhao.melon.module.modules.extra

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.inventory.InventoryUtil
import melon.system.event.safeEventListener
import net.minecraft.block.Block
import net.minecraft.block.BlockPlanks
import net.minecraft.block.material.Material
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemBlock
import net.minecraft.item.crafting.CraftingManager
import net.minecraft.network.play.client.CPacketPlaceRecipe
import net.minecraft.util.ResourceLocation
import java.util.concurrent.CopyOnWriteArrayList


@Module.Info(name = "AutoCraftBed", category = Category.XDDD)
class AutoCraftBed : Module() {
    private var craftMode = msetting("CraftMode", Mode.Smart)
    private var craftDelay = isetting("CraftDelay", 5, 0, 1000)
    private var color = ssetting("BedColor", "white")
    private var slotList: CopyOnWriteArrayList<Int> = CopyOnWriteArrayList()
    private var craftTimer: TimerUtils? = TimerUtils()
    private var shouldCraft = false

    override fun getHudInfo(): String {
        return slotList.size.toString()
    }

    init {
        safeEventListener<PlayerMotionEvent> { event ->
            val woolSlot: Int = findInventoryWool()
            val woodSlot: Int = InventoryUtil.findInInventory(
                { it!!.getItem() is ItemBlock && (it.getItem() as ItemBlock).block is BlockPlanks },
                true
            )
            for (slot in 0..36) {
                if (player.inventory.getStackInSlot(slot).item != null) {
                    slotList.remove(slot)
                } else {
                    if (!slotList.contains(slot) && slotList.size < slot) {
                        slotList.add(slot)
                    }
                }
            }
            if (event.stage == 0) {
                if (mc.currentScreen is GuiCrafting) {
                    if (woolSlot == -1 || woodSlot == -1 || woolSlot == -2 || woodSlot == -2) {
                        //displayGuiScreen(null)
                        //currentScreen = null
                        shouldCraft = false
                        return@safeEventListener
                    }
                    if (craftTimer!!.tickAndReset(craftDelay.value)) {
                        player.connection.sendPacket(
                            CPacketPlaceRecipe(
                                player.openContainer.windowId,
                                CraftingManager.getRecipe(ResourceLocation(color.value.lowercase() + "_bed"))!!,
                                true
                            )
                        )
                        slotList.forEach {
                            when (craftMode.value) {
                                Mode.Semi -> {
                                    if (it != woodSlot && it != woolSlot) {
                                        playerController.windowClick(
                                            player.openContainer.windowId,
                                            0,
                                            it,
                                            ClickType.QUICK_MOVE,
                                            player
                                        )
                                        playerController.updateController()
                                        slotList.remove(it)
                                    }
                                }

                                Mode.Auto -> {
                                    playerController.windowClick(
                                        player.openContainer.windowId,
                                        0,
                                        0,
                                        ClickType.QUICK_MOVE,
                                        player
                                    )
                                    playerController.updateController()
                                    slotList.remove(it)
                                }

                                Mode.Smart -> {
                                    if (it != woodSlot && it != woolSlot) {
                                        playerController.windowClick(
                                            player.openContainer.windowId,
                                            0,
                                            0,
                                            ClickType.QUICK_MOVE,
                                            player
                                        )
                                        playerController.updateController()
                                        slotList.remove(it)
                                    }
                                }
                            }
                            //displayGuiScreen(null)
                        }
                    }
                }
            }
        }
    }

    private fun findInventoryWool(): Int {
        return InventoryUtil.findInInventory({ s ->
            if (s!!.getItem() is ItemBlock) {
                val block: Block = (s.getItem() as ItemBlock).block
                return@findInInventory block.defaultState.material === Material.CLOTH
            }
            false
        }, true)
    }

    enum class Mode {
        Auto, Semi, Smart
    }
}