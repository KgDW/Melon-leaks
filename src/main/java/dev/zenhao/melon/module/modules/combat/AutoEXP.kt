package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.getItemSlot
import dev.zenhao.melon.module.modules.misc.XCarry
import dev.zenhao.melon.module.modules.player.AutoArmour
import dev.zenhao.melon.module.modules.player.Timer
import dev.zenhao.melon.setting.BooleanSetting
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.inventory.InventoryUtil
import dev.zenhao.melon.utils.math.DamageUtil
import dev.zenhao.melon.utils.math.MathUtil
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand
import org.lwjgl.input.Keyboard
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Module.Info(name = "AutoEXP", category = Category.COMBAT, description = "Automatically mends armour")
object AutoEXP : Module() {
    @JvmField
    var toggleMend: BooleanSetting = bsetting("ToggleMend", true)
    private var packetDelay = isetting("PacketDelay", 10, 0, 1000)
    private var delay: Setting<Int> = isetting("Delay", 50, 0, 500)
    private var mendingTakeOff = bsetting("AutoMend", true)
    private var closestEnemy: Setting<Int> = isetting("EnemyRange", 6, 1, 20).b(mendingTakeOff)
    private var mend_percentage: Setting<Int> = isetting("Mend%", 100, 1, 100).b(mendingTakeOff)
    private var updateController: Setting<Boolean> = bsetting("Update", false)
    private var shiftClick: Setting<Boolean> = bsetting("ShiftClick", true)
    private var xcarry = bsetting("XCarry", false)
    private var packet = bsetting("PacketClick", false)
    private var doneSlots: MutableList<Int> = ArrayList()
    private var timerUtils = TimerUtils()
    private var packetTimer = TimerUtils()
    private var elytraTimerUtils = TimerUtils()
    private var taskList: Queue<InventoryUtil.Task> = ConcurrentLinkedQueue()

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        timerUtils.reset()
        taskList.clear()
        doneSlots.clear()
        elytraTimerUtils.reset()
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        timerUtils.reset()
        packetTimer.reset()
        elytraTimerUtils.reset()
    }

    override fun onLogout() {
        taskList.clear()
        doneSlots.clear()
    }

    init {
        onPacketSend {
            when (it.packet) {
                is CPacketPlayer.Rotation -> {
                    it.packet.pitch = 90f
                }

                is CPacketPlayer.PositionRotation -> {
                    it.packet.pitch = 90f
                }
            }
        }

        onMotion {
            val slot = player.getItemSlot(Items.EXPERIENCE_BOTTLE) ?: return@onMotion
            if (!toggleMend.value) {
                if (!Keyboard.isKeyDown(getBind())) {
                    return@onMotion
                }
            }
            if (Timer.isDisabled || (Timer.isEnabled && !Timer.packetControl.value)) {
                it.setRotation(player.rotationYaw, 90f)
            }
            if (packetTimer.tickAndReset(packetDelay.value)) {
                spoofHotbar(slot) {
                    connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                }
            }
            if (taskList.isEmpty()) {
                try {
                    if (mendingTakeOff.value && (isSafe || EntityUtil.isSafe(player, 1, false, true))) {
                        val helm = player.inventoryContainer.getSlot(5).stack
                        if (!helm.isEmpty && DamageUtil.getRoundedDamage(helm) >= mend_percentage.value) {
                            takeOffSlot(5)
                        }
                        val chest2 = player.inventoryContainer.getSlot(6).stack
                        if (!chest2.isEmpty && DamageUtil.getRoundedDamage(chest2) >= mend_percentage.value) {
                            takeOffSlot(6)
                        }
                        val legging2 = player.inventoryContainer.getSlot(7).stack
                        if (!legging2.isEmpty && DamageUtil.getRoundedDamage(legging2) >= mend_percentage.value) {
                            takeOffSlot(7)
                        }
                        val feet2 = player.inventoryContainer.getSlot(8).stack
                        if (!feet2.isEmpty && DamageUtil.getRoundedDamage(feet2) >= mend_percentage.value) {
                            takeOffSlot(8)
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
            if (timerUtils.tickAndReset(delay.value.toLong())) {
                if (taskList.isNotEmpty()) {
                    val task = taskList.poll()
                    task.runTask()
                }
            }
        }
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
                if (updateController.value) {
                    taskList.add(InventoryUtil.Task())
                }
            }
        }
    }

    private val SafeClientEvent.isSafe: Boolean
        get() {
            val closest = EntityUtil.getClosestEnemy(closestEnemy.value.toDouble()) ?: return true
            return player.getDistanceSq(closest) >= MathUtil.square(closestEnemy.value.toDouble())
        }
}