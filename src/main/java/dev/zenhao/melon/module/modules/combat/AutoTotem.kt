package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.manager.CrystalManager
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import melon.events.PacketEvents
import melon.events.RunGameLoopEvent
import melon.events.TickEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.extension.packetClick
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketEntityStatus
import java.lang.StrictMath.toDegrees
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.atan2

@Module.Info(name = "AutoTotem", description = "null", category = Category.COMBAT)
object AutoTotem : Module() {
    private var strict = bsetting("Strict", false)
    private var packetRotate = bsetting("PacketRotate", true).b(strict)
    private var rotateAngle = isetting("RotateAngle", 180, 0, 360).b(strict)
    private var disablerBypass = bsetting("DisablerBypass", false).b(strict)
    private var packetClick = bsetting("PacketClick", false)
    private var packetListen = false
    private var preferredTotemSlot = 0
    private var numOfTotems = 0
    private var fixedYaw = 0f

    init {
        safeEventListener<PacketEvents.Send> {
            when (it.packet) {
                is CPacketClickWindow -> {
                    if (strict.value && disablerBypass.value) {
                        if (!findTotems() || mc.currentScreen is GuiContainer && mc.currentScreen !is GuiInventory) {
                            return@safeEventListener
                        }
                        if (it.packet.clickedItem.item == Items.TOTEM_OF_UNDYING) {
                            if (player.rotationYaw != fixedYaw && EntityUtil.isMoving()) {
                                val clickedSlot = it.packet.slotId
                                packetListen = true
                                it.cancelled = true
                                legitBypass(fixedYaw, clickedSlot)
                                it.cancelled = false
                            } else {
                                it.cancelled = false
                            }
                        }
                    }
                }

                is CPacketPlayer -> {
                    if (strict.value && EntityUtil.isMoving() && packetListen) {
                        it.packet.yaw = fixedYaw
                    }
                }
            }
        }

        safeEventListener<PacketEvents.Receive> {
            if (it.packet is SPacketEntityStatus) {
                val packet = it.packet
                if (packet.opCode.toInt() == 35 && packet.getEntity(world) == player && strict.value) {
                    if (!findTotems() || mc.currentScreen is GuiContainer && mc.currentScreen !is GuiInventory) {
                        return@safeEventListener
                    }
                    doRotate(fixedYaw) {
                        packetListen = true
                        ChatUtil.sendMessage("PacketReceive Listened!")
                    }
                }
            }
        }

        safeEventListener<RunGameLoopEvent.Tick> {
            val pX = player.lastReportedPosX
            val pZ = player.lastReportedPosZ
            val eX = player.posX
            val eZ = player.posZ
            val dX = pX - eX
            val dZ = pZ - eZ
            fixedYaw = (toDegrees(atan2(dZ, dX)) - rotateAngle.value).toFloat()
            if (packetListen && strict.value) {
                doRotate(fixedYaw)
            }
        }

        safeEventListener<TickEvent.Pre> {
            if (!findTotems() || (mc.currentScreen is GuiContainer && mc.currentScreen !is GuiInventory)) {
                packetListen = false
                return@safeEventListener
            }
            if (player.heldItemOffhand.getItem() != Items.TOTEM_OF_UNDYING) {
                packetListen = true
                val offhandEmptyPreSwitch = player.heldItemOffhand.getItem() == Items.AIR
                legitBypass(fixedYaw, preferredTotemSlot)
                legitBypass(fixedYaw, 45)
                if (!offhandEmptyPreSwitch) {
                    legitBypass(fixedYaw, preferredTotemSlot)
                }
            } else {
                packetListen = false
            }
        }
    }

    private fun SafeClientEvent.doRotate(fixedYaw: Float, unit: (SafeClientEvent.(Float) -> Unit)? = null) {
        if (packetRotate.value) {
            connection.sendPacket(CPacketPlayer.Rotation(fixedYaw, CrystalManager.rotation.y, false))
        } else {
            RotationManager.addRotations(fixedYaw, CrystalManager.rotation.y)
        }
        unit?.invoke(this, fixedYaw)
    }

    private fun SafeClientEvent.legitBypass(fixedYaw: Float, slot: Int) {
        runCatching {
            if (strict.value) {
                doRotate(fixedYaw)
            }
            if (packetClick.value) {
                connection.sendPacket(packetClick(slot))
            } else {
                playerController.windowClick(0, slot, 0, ClickType.PICKUP, player)
            }
            playerController.updateController()
        }
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

    override fun getHudInfo(): String {
        return numOfTotems.toString() + ""
    }

    private val SafeClientEvent.inventoryAndHotbarSlots: Map<Int, ItemStack>
        get() = getInventorySlots(9)

    fun SafeClientEvent.getInventorySlots(current: Int): Map<Int, ItemStack> {
        var currentSlot = current
        val fullInventorySlots: MutableMap<Int, ItemStack> = HashMap()
        while (currentSlot <= 44) {
            fullInventorySlots[currentSlot] = player.inventoryContainer.inventory[currentSlot]
            currentSlot++
        }
        return fullInventorySlots
    }
}