package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import melon.utils.inventory.slot.firstItem
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.world.getItem
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.math.RayTraceResult
import org.lwjgl.input.Mouse
import java.util.concurrent.ConcurrentLinkedQueue

@Module.Info(name = "MCP", category = Category.MISC)
object MCP : Module() {
    private var invSwapDelay = isetting("InvSwapDelay", 10, 0, 1000)
    private var task = ConcurrentLinkedQueue<CPacketClickWindow>()
    private var delay = TimerUtils()

    init {
        onLoop {
            if (mc.currentScreen is GuiContainer) return@onLoop
            if (task.isNotEmpty() && delay.passed(invSwapDelay.value)) {
                connection.sendPacket(task.poll())
                playerController.updateController()
            }
            if (Mouse.isButtonDown(2)) {
                if (mc.objectMouseOver.typeOfHit != RayTraceResult.Type.ENTITY && mc.objectMouseOver.typeOfHit != RayTraceResult.Type.BLOCK) {
                    val slot = player.hotbarSlots.firstItem(Items.ENDER_PEARL)
                    var intSlot = -1
                    if (slot == null) {
                        for (i in 9..35) {
                            if (player.inventory.getStackInSlot(i).getItem() != Items.ENDER_PEARL) continue
                            intSlot = i
                            break
                        }
                        if (intSlot == -1) return@onLoop
                        connection.sendPacket(
                            CPacketClickWindow(
                                player.inventoryContainer.windowId,
                                intSlot,
                                playerController.currentPlayerItem,
                                ClickType.SWAP,
                                player.inventory.getStackInSlot(intSlot),
                                player.openContainer.getNextTransactionID(player.inventory)
                            )
                        )
                        connection.sendPacket(CPacketPlayerTryUseItem(getItem(Items.ENDER_PEARL)))
                        task.add(
                            CPacketClickWindow(
                                player.inventoryContainer.windowId,
                                intSlot,
                                playerController.currentPlayerItem,
                                ClickType.SWAP,
                                player.inventory.getStackInSlot(intSlot),
                                player.openContainer.getNextTransactionID(player.inventory)
                            )
                        )
                        delay.reset()
                    } else {
                        spoofHotbar(slot) {
                            connection.sendPacket(CPacketPlayerTryUseItem(getItem(Items.ENDER_PEARL)))
                        }
                    }
                }
            }
        }
    }
}