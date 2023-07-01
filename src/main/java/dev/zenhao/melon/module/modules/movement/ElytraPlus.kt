package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.entity.EventPlayerTravel
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.math.MathUtil
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.util.math.MathHelper
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

@Module.Info(name = "ElytraFly", description = "Allows you to fly with elytra", category = Category.MOVEMENT)
internal object ElytraPlus : Module() {
    private val mode: Setting<*> = msetting("Mode", Mode.Superior)
    private val speed: Setting<Float> = fsetting("Speed", 18f, 0f, 50f)
    private val downSpeed: Setting<Float> = fsetting("DownSpeed", 1.8f, 0f, 10f)
    private val glideSpeed: Setting<Float> = fsetting("GlideSpeed", 0.0001f, 0f, 10f)
    private val upSpeed: Setting<Float> = fsetting("UpSpeed", 5f, 0f, 10f)
    private val directUp = bsetting("DirectUp", false)
    private val accelerate: Setting<Boolean> = bsetting("Accelerate", true)
    private val vAccelerationTimer: Setting<Int> = isetting("AccTime", 1000, 0, 10000)
    private val rotationPitch: Setting<Float> = fsetting("RotationPitch", 45f, 0f, 90f)
    private val cancelInWater: Setting<Boolean> = bsetting("CancelInWater", true)
    private val cancelAtHeight: Setting<Int> = isetting("CancelHeight", 0, 0, 10)
    private val instantFly: Setting<Boolean> = bsetting("FastBoost", true)
    private val onEnableEquipElytra: Setting<Boolean> = bsetting("AutoEnableWhileElytra", false)
    private val pitchSpoof: Setting<Boolean> = bsetting("PitchSpoof", false)
    private val accelerationTimer = TimerUtils()
    private val accelerationResetTimer = TimerUtils()
    private val instantFlyTimer = TimerUtils()
    private var sendMessage = false
    private var elytraSlot = -1

    fun shouldSwing(): Boolean {
        return isEnabled && mc.player.isElytraFlying
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Send) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is CPacketPlayer && pitchSpoof.value) {
            if (!mc.player.isElytraFlying) return
            if (event.packet is PositionRotation && pitchSpoof.value) {
                val rotation = event.packet as PositionRotation
                Objects.requireNonNull(mc.connection)!!.sendPacket(CPacketPlayer.Position(rotation.x, rotation.y, rotation.z, rotation.onGround))
                event.isCanceled = true
            } else if (event.packet is CPacketPlayer.Rotation && pitchSpoof.value) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onTravel(event: EventPlayerTravel) {
        if (fullNullCheck()) {
            return
        }
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() !== Items.ELYTRA) return
        if (!mc.player.isElytraFlying) {
            if (!mc.player.onGround && instantFly.value) {
                if (!instantFlyTimer.passed(500)) return
                instantFlyTimer.reset()
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player,
                        CPacketEntityAction.Action.START_FALL_FLYING
                    )
                )
            }
            return
        }
        if (mode.value == Mode.Packet) {
            onPacketMode(event)
        } else if (mode.value == Mode.Superior) {
            onControl(event)
        }
    }

    override fun getHudInfo(): String {
        return mode.value.toString()
    }

    override fun onEnable() {
        elytraSlot = -1
        if (onEnableEquipElytra.value) {
            if (mc.player != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST)
                    .getItem() !== Items.ELYTRA
            ) {
                for (i in 0..43) {
                    val stacktemp = mc.player.inventory.getStackInSlot(i)
                    if (stacktemp.isEmpty() || stacktemp.getItem() !== Items.ELYTRA) continue
                    elytraSlot = i
                    break
                }
                if (elytraSlot != -1) {
                    val hasArmorAtChest =
                        mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() !== Items.AIR
                    mc.playerController.windowClick(
                        mc.player.inventoryContainer.windowId,
                        elytraSlot,
                        0,
                        ClickType.PICKUP,
                        mc.player
                    )
                    mc.playerController.windowClick(
                        mc.player.inventoryContainer.windowId,
                        6,
                        0,
                        ClickType.PICKUP,
                        mc.player
                    )
                    if (hasArmorAtChest) mc.playerController.windowClick(
                        mc.player.inventoryContainer.windowId,
                        elytraSlot,
                        0,
                        ClickType.PICKUP,
                        mc.player
                    )
                }
            }
        }
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        if (elytraSlot != -1) {
            val hasItem =
                !mc.player.inventory.getStackInSlot(elytraSlot).isEmpty() || mc.player.inventory.getStackInSlot(
                    elytraSlot
                ).getItem() !== Items.AIR
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player)
            mc.playerController.windowClick(
                mc.player.inventoryContainer.windowId,
                elytraSlot,
                0,
                ClickType.PICKUP,
                mc.player
            )
            if (hasItem) {
                mc.playerController.windowClick(
                    mc.player.inventoryContainer.windowId,
                    6,
                    0,
                    ClickType.PICKUP,
                    mc.player
                )
            }
        }
    }

    private fun onPacketMode(p_Travel: EventPlayerTravel) {
        val yHeight = mc.player.posY
        if (yHeight <= cancelAtHeight.value) {
            if (!sendMessage) {
                ChatUtil.NoSpam.sendMessage("WARNING, you must scaffold up or use fireworks, as YHeight <= CancelAtHeight!")
                sendMessage = true
            }
            return
        }
        val isMoveKeyDown =
            (mc.gameSettings.keyBindForward.isKeyDown || mc.gameSettings.keyBindLeft.isKeyDown || mc.gameSettings.keyBindRight.isKeyDown
                    || mc.gameSettings.keyBindBack.isKeyDown)
        val cancelInWater = !mc.player.isInWater && !mc.player.isInLava && cancelInWater.value
        if (!isMoveKeyDown) {
            accelerationTimer.resetTimeSkipTo(-vAccelerationTimer.value.toLong())
        } else if (mc.player.rotationPitch <= rotationPitch.value && cancelInWater) {
            if (accelerate.value) {
                if (accelerationTimer.passed(vAccelerationTimer.value)) {
                    accelerate()
                    return
                }
            }
            return
        }
        p_Travel.isCanceled = true
        accelerate()
    }

    private fun onControl(p_Travel: EventPlayerTravel) {
        p_Travel.isCanceled = true
        val moveForward = mc.gameSettings.keyBindForward.isKeyDown
        val moveBackward = mc.gameSettings.keyBindBack.isKeyDown
        val moveLeft = mc.gameSettings.keyBindLeft.isKeyDown
        val moveRight = mc.gameSettings.keyBindRight.isKeyDown
        val moveUp = mc.gameSettings.keyBindJump.isKeyDown
        val moveDown = mc.gameSettings.keyBindSneak.isKeyDown
        val moveForwardFactor = if (moveForward) 1.0f else (if (moveBackward) -1 else 0).toFloat()
        var yawDeg = mc.player.rotationYaw
        if (moveLeft && (moveForward || moveBackward)) {
            yawDeg -= 40.0f * moveForwardFactor
        } else if (moveRight && (moveForward || moveBackward)) {
            yawDeg += 40.0f * moveForwardFactor
        } else if (moveLeft) {
            yawDeg -= 90.0f
        } else if (moveRight) {
            yawDeg += 90.0f
        }
        if (moveBackward) {
            yawDeg -= 180.0f
        }
        val yaw = Math.toRadians(yawDeg.toDouble()).toFloat()
        val motionAmount = sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ)
        if (moveUp && !moveForward && !moveBackward && !moveLeft && !moveRight && !moveDown && directUp.value) {
            mc.player.motionY = upSpeed.value.toDouble()
            return
        }
        if (moveUp || moveForward || moveBackward || moveLeft || moveRight) {
            if (moveUp && motionAmount > 1.0) {
                if (mc.player.motionX == 0.0 && mc.player.motionZ == 0.0) {
                    mc.player.motionY = upSpeed.value.toDouble()
                } else {
                    val calcMotionDiff = motionAmount * 0.008
                    mc.player.motionY += calcMotionDiff * 3.2
                    mc.player.motionX -= (-MathHelper.sin(yaw)).toDouble() * calcMotionDiff
                    mc.player.motionZ -= MathHelper.cos(yaw).toDouble() * calcMotionDiff
                    mc.player.motionX *= 0.99
                    mc.player.motionY *= 0.98
                    mc.player.motionZ *= 0.99
                }
            } else { /* runs when pressing wasd */
                mc.player.motionX = (-MathHelper.sin(yaw)).toDouble() * (speed.value / 10)
                mc.player.motionY = -glideSpeed.value.toDouble()
                mc.player.motionZ = MathHelper.cos(yaw).toDouble() * (speed.value / 10)
            }
        } else { /* Stop moving if no inputs are pressed */
            mc.player.motionX = 0.0
            mc.player.motionY = 0.0
            mc.player.motionZ = 0.0
        }
        if (moveDown) {
            mc.player.motionY = -downSpeed.value.toDouble()
        }
    }

    private fun accelerate() {
        if (accelerationResetTimer.passed(vAccelerationTimer.value)) {
            accelerationResetTimer.reset()
            accelerationTimer.reset()
            sendMessage = false
        }
        val speedacc = speed.value / 10f
        val dir = MathUtil.directionSpeed(speedacc.toDouble())
        mc.player.motionY = -glideSpeed.value.toDouble()
        if (mc.player.movementInput.moveStrafe != 0f || mc.player.movementInput.moveForward != 0f) {
            mc.player.motionX = dir[0]
            mc.player.motionZ = dir[1]
            mc.player.motionX -= mc.player.motionX * (abs(mc.player.rotationPitch) + 90) / 90 - mc.player.motionX
            mc.player.motionZ -= mc.player.motionZ * (abs(mc.player.rotationPitch) + 90) / 90 - mc.player.motionZ
        } else {
            mc.player.motionX = 0.0
            mc.player.motionZ = 0.0
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown) {
            mc.player.motionY = -downSpeed.value.toDouble()
        }
    }

    private enum class Mode {
        Superior, Packet
    }
}