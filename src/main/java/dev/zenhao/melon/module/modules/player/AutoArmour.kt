package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager.getModuleByClass
import dev.zenhao.melon.module.modules.combat.AutoEXP
import dev.zenhao.melon.module.modules.misc.XCarry
import dev.zenhao.melon.module.modules.movement.ElytraPlus
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.inventory.InventoryUtil
import dev.zenhao.melon.utils.math.DamageUtil
import dev.zenhao.melon.utils.math.MathUtil
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemExpBottle
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Keyboard
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Module.Info(name = "AutoArmour", category = Category.PLAYER, description = "Automatically equips armour")
object AutoArmour : Module() {
    private var packet = bsetting("PacketClick", false)
    private val delay: Setting<Int> = isetting("Delay", 10, 0, 500)
    private val mendingTakeOff = bsetting("TakeOffMend", true)
    private val closestEnemy: Setting<Int> = isetting("EnemyRange", 8, 1, 20).b(mendingTakeOff)
    private val mendPercent: Setting<Int> = isetting("Mend%", 95, 1, 100).b(mendingTakeOff)
    private val curse: Setting<Boolean> = bsetting("CurseOfBinding", false)
    private val shiftClick: Setting<Boolean> = bsetting("ShiftClick", true)
    private val xpcheck: Setting<Boolean> = bsetting("PacketXPCheck", true)
    private val elytraCheck = bsetting("ElytraCheck", true)
    private var xcarry = bsetting("XCarry", false)
    private val taskDelay = TimerUtils()
    private val elytraTimerUtils = TimerUtils()
    private val doneSlots: MutableList<Int> = ArrayList()
    private val taskList: Queue<InventoryUtil.Task> = ConcurrentLinkedQueue()
    private var mending = false
    override fun onEnable() {
        taskDelay.reset()
        taskList.clear()
        elytraTimerUtils.reset()
    }

    override fun onLogin() {
        taskDelay.reset()
        elytraTimerUtils.reset()
    }

    override fun onDisable() {
        taskDelay.reset()
        elytraTimerUtils.reset()
        taskList.clear()
        doneSlots.clear()
        mending = false
    }

    override fun onLogout() {
        taskList.clear()
        doneSlots.clear()
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            if (mc.currentScreen is GuiContainer && mc.currentScreen !is GuiInventory) {
                return@safeEventListener
            }
            if (xpcheck.value) {
                if (getModuleByClass(AutoEXP::class.java).isEnabled) {
                    if (!AutoEXP.toggleMend.value) {
                        if (!Keyboard.isKeyDown(AutoEXP.getBind())) {
                            return@safeEventListener
                        }
                    } else {
                        return@safeEventListener
                    }
                }
            }
            if (ElytraPlus.isEnabled && elytraCheck.value) {
                var slot: Int
                var elytraSlot = -1
                val chest = player.inventoryContainer.getSlot(6).stack
                for (findSlot in 0..35) {
                    if (player.inventory.getStackInSlot(findSlot).item != Items.ELYTRA) continue
                    elytraSlot = findSlot
                    break
                }
                if (!chest.getItem().equals(Items.ELYTRA) && elytraSlot != -1) {
                    if (InventoryUtil.findElytraSlot(
                            EntityEquipmentSlot.CHEST,
                            curse.value,
                            XCarry.getInstance().isEnabled
                        ).also { slot = it } != -1
                    ) {
                        taskList.add(InventoryUtil.Task(slot, packet.value))
                        taskList.add(InventoryUtil.Task(6, packet.value))
                        taskList.add(InventoryUtil.Task(slot, packet.value))
                    }
                } else {
                    return@safeEventListener
                }
            }
            if (taskList.isEmpty()) {
                var slot = 0
                var slot2 = 0
                var slot3: Int
                var chest: ItemStack
                var slot4 = 0
                if (mendingTakeOff.value && InventoryUtil.holdingItem(ItemExpBottle::class.java) && mc.gameSettings.keyBindUseItem.isKeyDown && (isSafe || EntityUtil.isSafe(
                        player, 1, false, true
                    ))
                ) {
                    mending = true
                    val helm = player.inventoryContainer.getSlot(5).stack
                    if (!helm.isEmpty && DamageUtil.getRoundedDamage(helm) >= mendPercent.value) {
                        takeOffSlot(5)
                        mending = true
                    }
                    val chest2 = player.inventoryContainer.getSlot(6).stack
                    if (!chest2.isEmpty && DamageUtil.getRoundedDamage(chest2) >= mendPercent.value) {
                        takeOffSlot(6)
                        mending = true
                    }
                    val legging2 = player.inventoryContainer.getSlot(7).stack
                    if (!legging2.isEmpty && DamageUtil.getRoundedDamage(legging2) >= mendPercent.value) {
                        takeOffSlot(7)
                        mending = true
                    }
                    val feet2 = player.inventoryContainer.getSlot(8).stack
                    if (!feet2.isEmpty && DamageUtil.getRoundedDamage(feet2) >= mendPercent.value) {
                        takeOffSlot(8)
                        mending = true
                    }
                    return@safeEventListener
                }
                val helm = player.inventoryContainer.getSlot(5).stack
                if (helm.getItem() === Items.AIR && InventoryUtil.findArmorSlot(
                        EntityEquipmentSlot.HEAD,
                        curse.value,
                        XCarry.getInstance().isEnabled
                    ).also { slot4 = it } != -1
                ) {
                    getSlotOn(5, slot4)
                }
                if (player.inventoryContainer.getSlot(6).stack.also { chest = it }.getItem() === Items.AIR) {
                    if (taskList.isEmpty()) {
                        if (InventoryUtil.findArmorSlot(
                                EntityEquipmentSlot.CHEST,
                                curse.value,
                                XCarry.getInstance().isEnabled
                            ).also { slot3 = it } != -1
                        ) {
                            getSlotOn(6, slot3)
                            mending = true
                        }
                    }
                }
                if (chest.getItem() === Items.ELYTRA && elytraTimerUtils.passedMs(500L) && taskList.isEmpty()) {
                    slot3 =
                        InventoryUtil.findItemInventorySlot(
                            Items.DIAMOND_CHESTPLATE,
                            false,
                            XCarry.getInstance().isEnabled
                        )
                    if (slot3 == -1 && InventoryUtil.findItemInventorySlot(
                            Items.IRON_CHESTPLATE,
                            false,
                            XCarry.getInstance().isEnabled
                        )
                            .also { slot3 = it } == -1 && InventoryUtil.findItemInventorySlot(
                            Items.GOLDEN_CHESTPLATE,
                            false,
                            XCarry.getInstance().isEnabled
                        )
                            .also { slot3 = it } == -1 && InventoryUtil.findItemInventorySlot(
                            Items.CHAINMAIL_CHESTPLATE,
                            false,
                            XCarry.getInstance().isEnabled
                        ).also { slot3 = it } == -1
                    ) {
                        slot3 = InventoryUtil.findItemInventorySlot(
                            Items.LEATHER_CHESTPLATE,
                            false,
                            XCarry.getInstance().isEnabled
                        )
                    }
                    if (slot3 != -1) {
                        taskList.add(InventoryUtil.Task(slot3, packet.value))
                        taskList.add(InventoryUtil.Task(6, packet.value))
                        taskList.add(InventoryUtil.Task(slot3, packet.value))
                    }
                    elytraTimerUtils.reset()
                }
                if (player.inventoryContainer.getSlot(7).stack.getItem() === Items.AIR && InventoryUtil.findArmorSlot(
                        EntityEquipmentSlot.LEGS,
                        curse.value,
                        XCarry.getInstance().isEnabled
                    ).also { slot2 = it } != -1
                ) {
                    getSlotOn(7, slot2)
                }
                if (player.inventoryContainer.getSlot(8).stack.getItem() === Items.AIR && InventoryUtil.findArmorSlot(
                        EntityEquipmentSlot.FEET,
                        curse.value,
                        XCarry.getInstance().isEnabled
                    ).also { slot = it } != -1
                ) {
                    getSlotOn(8, slot)
                }
            }
            if (taskDelay.tickAndReset(delay.value.toLong())) {
                if (taskList.isNotEmpty()) {
                    val task = taskList.poll()
                    task?.runTask()
                }
            }
            mending = false
        }
    }

    override fun getHudInfo(): String? {
        return if (mending) {
            TextFormatting.BLUE.toString() + "[" + TextFormatting.RED + "Mending" + TextFormatting.BLUE + "]"
        } else null
    }

    private val SafeClientEvent.isSafe: Boolean
        get() {
            val closest = EntityUtil.getClosestEnemy(closestEnemy.value.toDouble()) ?: return true
            return player.getDistanceSq(closest) >= MathUtil.square(closestEnemy.value.toDouble())
        }

    private fun takeOffSlot(slot: Int) {
        if (taskList.isEmpty()) {
            var target = -1
            for (i in InventoryUtil.findEmptySlots(XCarry.getInstance().isEnabled && xcarry.value)) {
                if (doneSlots.contains(target)) continue
                target = i
                doneSlots.add(i)
            }
            if (target != -1) {
                if (target in 1..4 || !shiftClick.value) {
                    taskList.add(InventoryUtil.Task(slot, packet.value))
                    taskList.add(InventoryUtil.Task(target, packet.value))
                } else {
                    taskList.add(InventoryUtil.Task(slot, true, packet.value))
                }
            }
        }
    }

    private fun getSlotOn(slot: Int, target: Int) {
        if (taskList.isEmpty()) {
            doneSlots.remove(target as Any)
            if (target in 1..4 || !shiftClick.value) {
                taskList.add(InventoryUtil.Task(target, packet.value))
                taskList.add(InventoryUtil.Task(slot, packet.value))
            } else {
                taskList.add(InventoryUtil.Task(target, true, packet.value))
            }
        }
    }
}