package dev.zenhao.melon.module.modules.extra

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.calcDamage
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.getPredictedTarget
import dev.zenhao.melon.module.modules.crystal.MelonAura2
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.entity.HoleUtil
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.input.Mouse

@Module.Info(name = "SmartOffHand", category = Category.XDDD, description = "StupidOffHand")
class SmartOffHand : Module() {
    private var totems = 0
    private var count = 0
    private var cmode = msetting("Case", Case.Crystal)
    private var strict = bsetting("Strict", false)
    private var mode = msetting("Mode", Mode.Crystal)
    private var delay: Setting<Int> = isetting("Delay", 0, 0, 1000)
    private var totem = bsetting("SwitchTotem", true)
    private var sbHealth = dsetting("Health", 11.0, 0.0, 36.0)
    private var autoSwitch = bsetting("SwitchGap", true)
    private var switchMode = msetting("GapWhen", SMode.RClick).b(autoSwitch)
    private var elytra = bsetting("CheckElytra", true)
    private var holeCheck = bsetting("CheckHole", false)
    private var holeSwitch = dsetting("HoleHealth", 8.0, 0.0, 36.0).b(holeCheck)
    private var crystalCalculate = bsetting("CalculateDmg", true)
    private var maxSelfDmg = dsetting("MaxSelfDmg", 26.0, 0.0, 36.0).b(crystalCalculate)
    private var predictTicks = isetting("PredictTicks", 0, 0, 20)

    init {
        safeEventListener<PlayerMotionEvent> {
            try {
                if (!getItemStackAvailable(Items.TOTEM_OF_UNDYING) && !getItemStackAvailable(Items.END_CRYSTAL) && !getItemStackAvailable(Items.GOLDEN_APPLE)) {
                    return@safeEventListener
                }
                totems = mc.player.inventory.mainInventory.stream()
                    .filter { it.getItem() === Items.TOTEM_OF_UNDYING }
                    .mapToInt { it.count }.sum()
                if (mc.player.heldItemOffhand.getItem() === Items.TOTEM_OF_UNDYING) {
                    totems++
                }
                when (cmode.value) {
                    Case.Crystal -> {
                        var crystals = mc.player.inventory.mainInventory.stream()
                            .filter { it.getItem() === Items.END_CRYSTAL }
                            .mapToInt { it.count }.sum()
                        if (mc.player.heldItemOffhand.getItem() === Items.END_CRYSTAL) {
                            crystals += mc.player.heldItemOffhand.count
                        }
                        var gapple = mc.player.inventory.mainInventory.stream()
                            .filter { it.getItem() === Items.GOLDEN_APPLE }
                            .mapToInt { it.count }.sum()
                        if (mc.player.heldItemOffhand.getItem() === Items.GOLDEN_APPLE) {
                            gapple += mc.player.heldItemOffhand.count
                        }
                        var item: Item? = null
                        if (!mc.player.heldItemOffhand.isEmpty()) {
                            item = mc.player.heldItemOffhand.getItem()
                        }
                        count = if (item != null) {
                            when (item) {
                                Items.END_CRYSTAL -> {
                                    crystals
                                }

                                Items.TOTEM_OF_UNDYING -> {
                                    totems
                                }

                                else -> {
                                    gapple
                                }
                            }
                        } else {
                            0
                        }
                        val handItem = mc.player.heldItemMainhand.getItem()
                        val offhandItem = if (mode.value == Mode.Crystal) Items.END_CRYSTAL else Items.GOLDEN_APPLE
                        val sOffhandItem = if (mode.value == Mode.Crystal) Items.GOLDEN_APPLE else Items.END_CRYSTAL
                        val shouldSwitch: Boolean = if (switchMode.value == SMode.Sword) {
                            mc.player.heldItemMainhand.getItem() is ItemSword && Mouse.isButtonDown(1) && autoSwitch.value
                        } else {
                            (Mouse.isButtonDown(1)
                                    && autoSwitch.value
                                    && handItem !is ItemFood
                                    && handItem !is ItemExpBottle
                                    && handItem !is ItemBlock)
                        }
                        if (shouldTotem() && getItemSlot(Items.TOTEM_OF_UNDYING) != -1) {
                            switchTotem()
                        } else {
                            if (shouldSwitch && getItemSlot(sOffhandItem) != -1) {
                                if (mc.player.heldItemOffhand.getItem() != sOffhandItem) {
                                    val slot =
                                        if (getItemSlot(sOffhandItem) < 9) getItemSlot(sOffhandItem) + 36 else getItemSlot(
                                            sOffhandItem
                                        )
                                    switchTo(slot)
                                }
                            } else if (getItemSlot(offhandItem) != -1 && ModuleManager.getModuleByClass(MelonAura2::class.java).isEnabled) {
                                val slot =
                                    if (getItemSlot(offhandItem) < 9) getItemSlot(offhandItem) + 36 else getItemSlot(
                                        offhandItem
                                    )
                                if (mc.player.heldItemOffhand.getItem() != offhandItem) {
                                    switchTo(slot)
                                }
                            } else {
                                switchTotem()
                            }
                        }
                    }

                    Case.Gap -> {
                        val offhandItem = Items.GOLDEN_APPLE
                        val slot =
                            if (getItemSlot(offhandItem) < 9) getItemSlot(offhandItem) + 36 else getItemSlot(offhandItem)
                        if (mc.player.heldItemMainhand.getItem() != Items.CHORUS_FRUIT) {
                            if (mc.player.heldItemOffhand.getItem() != offhandItem) {
                                switchTo(slot)
                            }
                        } else {
                            switchTotem()
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun SafeClientEvent.getItemStackAvailable(item: Item): Boolean {
        return player.inventory.mainInventory.stream()
            .filter { it.getItem() === item }
            .mapToInt { it.count }.sum() > 0
    }

    private fun SafeClientEvent.shouldTotem(): Boolean {
        return if (totem.value) {
            checkHealth() || mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)
                .getItem() === Items.ELYTRA && elytra.value || mc.player.fallDistance >= 5.0f || HoleUtil.isPlayerInHole() && holeCheck.value && mc.player.health + mc.player.absorptionAmount <= holeSwitch.value || crystalCalculate.value && calcHealth()
        } else {
            false
        }
    }

    private fun SafeClientEvent.calcHealth(): Boolean {
        var maxDmg = 0.5
        for (entity in ArrayList(mc.world.loadedEntityList)) {
            if (entity !is EntityEnderCrystal) continue
            if (mc.player.getDistance(entity) > 12f) continue
            val predictionTarget =
                if (predictTicks.value > 0) getPredictedTarget(entity, predictTicks.value) else Vec3d(
                    0.0, 0.0, 0.0
                )
            val d = calcDamage(
                mc.player,
                mc.player.positionVector.add(predictionTarget),
                mc.player.entityBoundingBox,
                entity.posX + 0.5,
                (entity.posY + 1),
                entity.posZ + 0.5,
                MutableBlockPos()
            ).toDouble()

            if (d > maxDmg) maxDmg = d
        }
        return if (maxDmg - 0.5 > mc.player.health + mc.player.absorptionAmount) true else maxDmg > maxSelfDmg.value
    }

    private fun checkHealth(): Boolean {
        val lowHealth = mc.player.health + mc.player.absorptionAmount <= sbHealth.value
        val notInHoleAndLowHealth = lowHealth && !HoleUtil.isPlayerInHole()
        return if (holeCheck.value) notInHoleAndLowHealth else lowHealth
    }

    private fun switchTotem() {
        if (totems != 0) {
            if (mc.player.heldItemOffhand.getItem() != Items.TOTEM_OF_UNDYING) {
                val slot =
                    if (getItemSlot(Items.TOTEM_OF_UNDYING) < 9) getItemSlot(Items.TOTEM_OF_UNDYING) + 36 else getItemSlot(
                        Items.TOTEM_OF_UNDYING
                    )
                switchTo(slot)
            }
        }
    }

    private fun switchTo(slot: Int) {
        try {
            if (strict.value) {
                mc.player.setVelocity(0.0, 0.0, 0.0)
            }
            if (timerUtils.passed(delay.value)) {
                mc.playerController.windowClick(
                    mc.player.inventoryContainer.windowId,
                    slot,
                    0,
                    ClickType.PICKUP,
                    mc.player
                )
                mc.playerController.windowClick(
                    mc.player.inventoryContainer.windowId,
                    45,
                    0,
                    ClickType.PICKUP,
                    mc.player
                )
                mc.playerController.windowClick(
                    mc.player.inventoryContainer.windowId,
                    slot,
                    0,
                    ClickType.PICKUP,
                    mc.player
                )
                timerUtils.reset()
            }
            if (strict.value) {
                mc.player.setVelocity(0.0, 0.0, 0.0)
            }
        } catch (ignored: Exception) {
        }
    }

    private fun getItemSlot(input: Item): Int {
        var itemSlot = -1
        for (i in 45 downTo 1) {
            if (mc.player.inventory.getStackInSlot(i).getItem() !== input) continue
            itemSlot = i
            break
        }
        return itemSlot
    }

    override fun getHudInfo(): String {
        if (mc.player.heldItemOffhand.getItem() === Items.TOTEM_OF_UNDYING) {
            return "Totem"
        }
        if (mc.player.heldItemOffhand.getItem() === Items.END_CRYSTAL) {
            return "Crystal"
        }
        if (mc.player.heldItemOffhand.getItem() === Items.GOLDEN_APPLE) {
            return "Gapple"
        }
        return if (mc.player.heldItemOffhand.getItem() === Items.BED) {
            "Bed"
        } else "None"
    }

    enum class Case {
        Crystal, Gap
    }

    enum class Mode {
        Crystal, Gap
    }

    enum class SMode {
        RClick, Sword
    }

    companion object {
        var timerUtils = TimerUtils()
    }
}