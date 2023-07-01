package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.combat.AutoEXP
import dev.zenhao.melon.module.modules.crystal.MelonAura2
import dev.zenhao.melon.setting.BooleanSetting
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.math.RandomUtil
import dev.zenhao.melon.utils.threads.runAsyncThread
import dev.zenhao.melon.utils.vector.Vec2f
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.runSafe
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.text.TextFormatting
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max

@Module.Info(name = "Timer", category = Category.PLAYER, description = "Changes your client tick speed")
object Timer : Module() {
    val tickNormal: Setting<Float> = fsetting("Speed", 1.2f, 1f, 10f)
    val packetControl: BooleanSetting = bsetting("PacketControl", false)
    private val newBypass = bsetting("NewBypass", false)
    private var disablerBypass = bsetting("Disabler", false)
    private var calcPacketList = CopyOnWriteArrayList<CPacketPlayer>()
    private var sendList = ConcurrentLinkedQueue<CPacketPlayer>()
    private var packetListReset: TimerUtils = TimerUtils()
    private var resetTimer = TimerUtils()
    private var normalLookPos = 0
    private var rotationMode = 1
    private var normalPos = 0
    private var lastPitch = 0f
    private var lastYaw = 0f

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        mc.timer.tickLength = 50.0f
        packetListReset.reset()
        resetTimer.reset()
        calcPacketList.clear()
        sendList.clear()
    }

    override fun onEnable() {
        runSafe {
            val caRotationNeeded = MelonAura2.isEnabled && MelonAura2.rotationInfo.rotation != Vec2f.ZERO && MelonAura2.crystalState.get() != MelonAura2.CurrentState.Waiting
            val caRotation = MelonAura2.rotationInfo
            mc.timer.tickLength = 50.0f
            packetListReset.reset()
            lastYaw = if (caRotationNeeded) caRotation.rotation.x else player.rotationYaw
            lastPitch = if (caRotationNeeded) caRotation.rotation.y else player.rotationPitch
            resetTimer.reset()
        }
    }

    override fun getHudInfo(): String {
        return TextFormatting.RED.toString() + "" + rotationMode + ""
    }

    init {
        safeEventListener<PacketEvents.Receive> { event ->
            if (event.packet is SPacketPlayerPosLook) {
                if (disablerBypass.value) {
                    connection.sendPacket(CPacketConfirmTeleport(++event.packet.teleportId))
                }
            }
        }

        safeEventListener<PacketEvents.Send> { event ->
            if (newBypass.value && event.packet is CPacketPlayer && event.packet !is CPacketPlayer.Rotation) {
                event.cancelled = resetTimer.tickAndReset(150)
                calcPacketList.add(event.packet)
                if (sendList.isNotEmpty()) {
                    val packet = sendList.poll()
                    player.connection.sendPacket(packet)
                    //player.setPositionAndRotation(packet.x, packet.y, packet.z, packet.yaw, packet.pitch)
                    //ChatUtil.sendMessage(packet.toString())
                }
            }
            when (event.packet) {
                is CPacketPlayer.Position -> {
                    if (rotationMode == 1) {
                        normalPos++
                        if (normalPos > 1) {
                            rotationMode = 2
                        }
                    }
                }

                is CPacketPlayer.PositionRotation -> {
                    if (rotationMode == 2) {
                        normalLookPos++
                        if (normalLookPos > 1) {
                            rotationMode = 1
                        }
                    }
                }
            }
        }

        safeEventListener<PlayerMotionEvent> {
            if (calcPacketList.isNotEmpty() && calcPacketList.size > 2 && newBypass.value) {
                runAsyncThread {
                    val finalX = max(calcPacketList[0].x, calcPacketList[1].x)
                    val finalY = max(calcPacketList[0].y, calcPacketList[1].y)
                    val finalZ = max(calcPacketList[0].z, calcPacketList[1].z)
                    val finalYaw: Float
                    val finalPitch: Float
                    val onGround = calcPacketList[0].isOnGround && calcPacketList[1].isOnGround
                    if (calcPacketList[0] is CPacketPlayer.PositionRotation && calcPacketList[1] is CPacketPlayer.PositionRotation) {
                        finalYaw = max(calcPacketList[0].yaw, calcPacketList[1].yaw)
                        finalPitch = max(calcPacketList[0].pitch, calcPacketList[1].pitch)
                        sendList.add(
                            CPacketPlayer.PositionRotation(
                                finalX,
                                finalY,
                                finalZ,
                                finalYaw,
                                finalPitch,
                                onGround
                            )
                        )
                        calcPacketList.removeAt(0)
                        calcPacketList.removeAt(1)
                        return@runAsyncThread
                    }
                    sendList.add(CPacketPlayer.Position(finalX, finalY, finalZ, onGround))
                    calcPacketList.removeAt(0)
                    calcPacketList.removeAt(1)
                }
            }
            val caRotationNeeded = MelonAura2.isEnabled && MelonAura2.rotationInfo.rotation != Vec2f.ZERO && MelonAura2.crystalState.get() != MelonAura2.CurrentState.Waiting
            val caRotation = MelonAura2.rotationInfo
            if (packetListReset.tickAndReset(50)) {
                normalPos = 0
                normalLookPos = 0
                rotationMode = 1
                lastYaw = if (caRotationNeeded) caRotation.rotation.x else player.rotationYaw
                lastPitch = if (caRotationNeeded) caRotation.rotation.y else player.rotationPitch
            }
            if (packetControl.value) {
                when (rotationMode) {
                    1 -> {
                        //Pos
                        if (EntityUtil.isMoving()) {
                            if (ModuleManager.getModuleByClass(AutoEXP.javaClass).isEnabled) {
                                RotationManager.addRotations(lastYaw, 90f)
                            } else {
                                RotationManager.addRotations(lastYaw, lastPitch)
                            }
                        }
                    }

                    2 -> {
                        //PosLook
                        if (ModuleManager.getModuleByClass(AutoEXP.javaClass).isEnabled) {
                            RotationManager.addRotations(
                                player.rotationYaw + RandomUtil.nextFloat(1f, 2f),
                                90f
                            )
                        } else {
                            if (caRotationNeeded) {
                                RotationManager.addRotations(
                                    caRotation.rotation.x + RandomUtil.nextFloat(1f, 2f),
                                    caRotation.rotation.y + RandomUtil.nextFloat(1f, 2f)
                                )
                            } else {
                                RotationManager.addRotations(
                                    player.rotationYaw + RandomUtil.nextFloat(1f, 2f),
                                    player.rotationPitch + RandomUtil.nextFloat(1f, 2f)
                                )
                            }
                        }
                    }
                }
            }
            mc.timer.tickLength = 50.0f / tickNormal.value
        }
    }
}