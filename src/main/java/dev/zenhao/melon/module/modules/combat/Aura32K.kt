package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.manager.FriendManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.BooleanSetting
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.math.RandomUtil
import dev.zenhao.melon.utils.math.deneb.LagCompensator
import melon.system.event.safeEventListener
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagList
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "32kAuraNew", category = Category.COMBAT)
class Aura32K : Module() {
    val server: Setting<*> = msetting("Server", Server.Normal)
    val hitRange: Setting<Double> = dsetting("HitRange", 6.0, 0.1, 8.0)
    val hitChance: Setting<Int> = isetting("HitChange", 100, 1, 100)
    val randomSpeed: Setting<Boolean> = bsetting("RandomSpeed", true)
    val randomSpeedValueMin: Setting<Int> = isetting("RandomSpeedMin", 2, 0, 30).b(randomSpeed as BooleanSetting)
    val randomSpeedValueMax: Setting<Int> = isetting("RandomSpeedMax", 6, 0, 30).b(randomSpeed as BooleanSetting)
    val delay: Setting<Int> = isetting("Delay", 2, 0, 20).b(randomSpeed as BooleanSetting)
    val lethalMode: Setting<Boolean> = bsetting("LethalMode", true)
    val lethalCPS: Setting<Int> = isetting("LethalCPS", 25, 0, 500).b(lethalMode as BooleanSetting)
    val CriticalCPT: Setting<Boolean> = bsetting("CriticalsHit", true)
    val CritCPS: Setting<Int> = isetting("CritCps", 12, 0, 30).b(CriticalCPT as BooleanSetting)
    val CritCPT: Setting<Int> = isetting("CritCpt", 1, 0, 30).b(CriticalCPT as BooleanSetting)
    val CritDelay: Setting<Int> = isetting("CritDelay", 1, 0, 2000).b(CriticalCPT as BooleanSetting)
    val CPTAttack: Setting<Boolean> = bsetting("NormalAttack", true)
    val NCPS: Setting<Int> = isetting("NormalCps", 15, 0, 100).b(CPTAttack as BooleanSetting)
    val NCPT: Setting<Int> = isetting("NormalCpt", 0, 0, 30).b(CPTAttack as BooleanSetting)
    val rotate: Setting<Boolean> = bsetting("Rotate", false)
    val noLag: Setting<Boolean> = bsetting("NoLag", true)
    val cpsTimerUtils = TimerUtils()
    val delayTimerUtils = TimerUtils()
    var cpsValue = 0
    var cpsCritValue = 0
    var lethalCPSValue = 0

    init {
        safeEventListener<PlayerMotionEvent> {
            if (fullNullCheck()) {
                return@safeEventListener
            }
            for (target in CopyOnWriteArrayList(mc.world.loadedEntityList)) {
                if (target == null) {
                    lethalCPSValue = 0
                }
                if (target !is EntityLivingBase ||
                    target === mc.player
                ) continue
                if (mc.player.getDistance(target) > hitRange.value || target.health <= 0.0f || target !is EntityPlayer || isSuperWeapon(
                        mc.player.heldItemMainhand
                    )
                ) {
                    continue
                }
                if (target === mc.player || FriendManager.isFriend(target.getName()) || mc.player.isDead || mc.player.health + mc.player.absorptionAmount <= 0.0f || target.isDead || isSuperWeapon(
                        mc.player.heldItemMainhand
                    )
                ) continue
                if (mc.player.heldItemMainhand.getItem() != Items.DIAMOND_SWORD) {
                    return@safeEventListener
                }
                if (rotate.value) {
                    it.setRotation(BlockInteractionHelper.getLegitRotations(Vec3d(target.posX, target.posY, target.posZ))[0], BlockInteractionHelper.getLegitRotations(Vec3d(target.posX, target.posY, target.posZ))[1])
                }
                if (CPTAttack.value) {
                    attack(target)
                }
                if (lethalMode.value) {
                    lethalAttack(target)
                }
            }
        }
    }

    fun isSuperWeapon(item: ItemStack?): Boolean {
        if (item == null) return true
        if (item.tagCompound == null) return true
        if (item.enchantmentTagList.tagType == 0) return true
        val enchants = item.tagCompound!!.getTag("ench") as NBTTagList
        var i = 0
        if (server.value === Server.Normal) {
            while (i < enchants.tagCount()) {
                val enchant = enchants.getCompoundTagAt(i)
                if (enchant.getInteger("id") == 16) {
                    val lvl = enchant.getInteger("lvl")
                    if (lvl >= 16) return false
                    break
                }
                i++
            }
        } else if (server.value === Server.Xin) {
            while (i < enchants.tagCount()) {
                val enchant = enchants.getCompoundTagAt(i)
                if (!(enchant.getInteger("id") == 34 && enchant.getInteger("id") == 20)) {
                    return false
                }
                i++
            }
        }
        return true
    }

    fun attack(entity: Entity?) {
        var i: Int
        var delay2 = RandomUtil.nextInt(randomSpeedValueMin.value, randomSpeedValueMax.value)
        if (!randomSpeed.value) {
            delay2 = delay.value
        }
        if (!delayTimerUtils.passedTicks(delay2)) {
            return
        }
        i = 0
        while (i < NCPT.value) {
            if (RandomUtil.nextInt(0, 100) >= hitChance.value) {
                ++i
                continue
            }
            if (noLag.value) {
                try {
                    mc.player.connection.sendPacket(CPacketUseEntity(entity))
                } catch (ignored: Exception) {
                }
            } else {
                mc.playerController.attackEntity(mc.player, entity)
            }
            mc.player.swingArm(EnumHand.MAIN_HAND)
            mc.player.resetCooldown()
            ++i
        }
        ++cpsValue
        if (cpsValue.toFloat() >= LagCompensator.INSTANCE.tickRate / NCPS.value.toFloat()) {
            if (RandomUtil.nextInt(0, 100) < hitChance.value) {
                if (noLag.value) {
                    try {
                        mc.player.connection.sendPacket(CPacketUseEntity(entity))
                    } catch (ignored: Exception) {
                    }
                } else {
                    mc.playerController.attackEntity(mc.player, entity)
                }
            }
            mc.player.swingArm(EnumHand.MAIN_HAND)
            mc.player.resetCooldown()
            cpsValue = 0
        }
        if (CriticalCPT.value) {
            if (cpsTimerUtils.passed(CritDelay.value)) {
                i = 0
                while (i < CritCPT.value) {
                    if (RandomUtil.nextInt(0, 100) >= hitChance.value) {
                        ++i
                        continue
                    }
                    if (noLag.value) {
                        mc.player.connection.sendPacket(CPacketUseEntity(entity))
                        ++i
                        continue
                    }
                    mc.playerController.attackEntity(mc.player, entity)
                    ++i
                }
                mc.player.swingArm(EnumHand.MAIN_HAND)
                mc.player.resetCooldown()
                cpsTimerUtils.reset()
            }
            ++cpsCritValue
            if (cpsCritValue.toFloat() >= LagCompensator.INSTANCE.tickRate / CritCPS.value.toFloat()) {
                if (RandomUtil.nextInt(0, 100) < hitChance.value) {
                    if (noLag.value) {
                        try {
                            mc.player.connection.sendPacket(CPacketUseEntity(entity))
                        } catch (ignored: Exception) {
                        }
                    } else {
                        mc.playerController.attackEntity(mc.player, entity)
                    }
                }
                mc.player.swingArm(EnumHand.MAIN_HAND)
                mc.player.resetCooldown()
                cpsCritValue = 0
            }
        }
        delayTimerUtils.reset()
    }

    fun lethalAttack(entity: Entity?) {
        ++lethalCPSValue
        if (lethalCPSValue.toFloat() >= LagCompensator.INSTANCE.tickRate / lethalCPS.value.toFloat()) {
            if (noLag.value) {
                try {
                    mc.player.connection.sendPacket(CPacketUseEntity(entity))
                } catch (ignored: Exception) {
                }
            } else {
                mc.playerController.attackEntity(mc.player, entity)
            }
            mc.player.swingArm(EnumHand.MAIN_HAND)
            mc.player.resetCooldown()
            lethalCPSValue = 0
        }
    }

    enum class Server {
        Xin, Normal
    }
}