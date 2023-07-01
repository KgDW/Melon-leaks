package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.entity.PushEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.event.events.render.RenderOverlayEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.setting.ModeSetting
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.math.RandomUtil
import melon.events.PacketEvents
import melon.events.PlayerMoveEvent
import melon.events.TickEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.system.event.safeEventListener
import net.minecraft.client.gui.GuiDownloadTerrain
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

@Module.Info(name = "PacketFlyRewrite", category = Category.MOVEMENT)
object PacketFlyRewrite : Module() {
    private val posLooks: MutableMap<Int, TimeVec> = ConcurrentHashMap()
    private var teleportID = 0
    private var packets = ArrayList<CPacketPlayer>()
    private var mode = msetting("Mode", Modes.Factor)
    private var phase: ModeSetting<*> = msetting("PhaseMode", PhaseMode.Full)
    private var type: Setting<*> = msetting("Type", Types.LimitJitter)
    private var TRStep: Setting<Boolean> = bsetting("ToggleRStep", false)
    private var betterResponse = bsetting("BetterResponse", false)
    private var confirmtp = bsetting("ConfirmTeleport", true)
    private var AntiKick: Setting<Boolean> = bsetting("AntiKick", true)
    private var lessReduction = bsetting("LessReduction", false)
    private var Reduction: Setting<Double> = dsetting("Reduction", 1.0, 0.1, 3.0).m(mode, Modes.Fast)
    private var FactorValue = fsetting("Factor", 1f, 1f, 5f)
    private var XZSpeed = fsetting("XZSpeed", 1f, 0f, 5f).m2(mode, Modes.Setback).m2(mode, Modes.Fast)
    private var YSpeedValue = fsetting("YSpeed", 1f, 0f, 3f).m2(mode, Modes.Setback).m2(mode, Modes.Fast)
    private var valueBounded = isetting("BoundedVal", 2, 1, 255)
    private var bypassXin = bsetting("BypassXin", false)
    private var packetCount = isetting("PacketCount", 3, 1, 10)
    private var resetTime = isetting("ResetTime", 3, 0, 10).b(bypassXin)
    private var moveFactor = dsetting("MoveFactor", 3.0, 1.0, 10.0)
    private var randomChat = bsetting("RandomChat", false).b(bypassXin)
    private var firstStart = false
    private var sent = 0
    private var otherids = 0
    private var lastFactor: Float = 1.0f
    private var resetTimer: TimerUtils = TimerUtils()
    private fun clearValues() {
        lastFactor = 1.0f
        otherids = 0
        teleportID = 0
        packets.clear()
        posLooks.clear()
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        if (TRStep.value) {
            ModuleManager.getModuleByClass(ReverseStep::class.java).disable()
        }
        if (mc.isSingleplayer) {
            ChatUtil.sendMessage(TextFormatting.RED.toString() + "Can't enable PacketFly in SinglePlayer!")
            disable()
        }
        firstStart = true
        sent = 0
        resetTimer.reset()
        clearValues()
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        if (TRStep.value) {
            ModuleManager.getModuleByClass(ReverseStep::class.java).enable()
        }
        otherids = 0
        packets.clear()
    }

    override fun getHudInfo(): String {
        return "" + TextFormatting.AQUA + mode.value
    }

    @SubscribeEvent
    fun onClientDisconnect(event: ClientDisconnectionFromServerEvent?) {
        disable()
    }

    private fun SafeClientEvent.doBetterResponse(event: SPacketPlayerPosLook) {
        var x = event.x
        var z = event.z
        var yaw = event.yaw
        var pitch = event.pitch
        if (event.flags.contains(SPacketPlayerPosLook.EnumFlags.X)) {
            x += player.posX
        }
        if (event.flags.contains(SPacketPlayerPosLook.EnumFlags.Z)) {
            z += player.posZ
        }
        if (event.flags.contains(SPacketPlayerPosLook.EnumFlags.X_ROT)) {
            pitch += player.rotationPitch
        }
        if (event.flags.contains(SPacketPlayerPosLook.EnumFlags.Y_ROT)) {
            yaw += player.rotationYaw
        }
        player.connection.sendPacket(PositionRotation(x, player.entityBoundingBox.minY, z, yaw, pitch, false))
    }

    init {
        safeEventListener<PacketEvents.Receive> { event ->
            if (event.packet is SPacketPlayerPosLook) {
                if (player.isEntityAlive && mode.value !== Modes.Setback && world.isBlockLoaded(
                        BlockPos(player),
                        false
                    ) && mc.currentScreen !is GuiDownloadTerrain
                ) {
                    val vec = posLooks.remove(event.packet.getTeleportId())
                    if (vec != null && vec.x == event.packet.getX() && vec.y == event.packet.getY() && vec.z == event.packet.getZ()) {
                        event.cancelled = true
                        return@safeEventListener
                    }
                    teleportID = event.packet.getTeleportId()
                    if (betterResponse.value) {
                        doBetterResponse(event.packet)
                    }
                }
            }
        }

        safeEventListener<PacketEvents.Send> { event ->
            if (event.packet is CPacketPlayer && !packets.remove(event.packet)) {
                event.cancelled = true
            }
        }

        safeEventListener<PlayerMoveEvent> { event ->
            if (firstStart) {
                event.cancelled = true
                firstStart = false
            } else {
                event.cancelled = false
            }
            if (bypassXin.value) {
                if (sent >= packetCount.value) {
                    event.cancelled = true
                }
            }
            if (teleportID != 0 || mode.value == Modes.Setback) {
                event.x = player.motionX
                event.y = player.motionY
                event.z = player.motionZ
                if (checkHitBoxes() || phase.value == PhaseMode.Semi) {
                    player.noClip = true
                }
            }
        }

        safeEventListener<PlayerMotionEvent> {
            posLooks.entries.removeIf { (_, value): Map.Entry<Int, TimeVec> ->
                System.currentTimeMillis() - value.time > TimeUnit.SECONDS.toMillis(
                    30L
                )
            }
            player.setVelocity(0.0, 0.0, 0.0)
            if (bypassXin.value) {
                if (sent >= packetCount.value) {
                    if (!resetTimer.passed(resetTime.value * 1000f)) {
                        return@safeEventListener
                    } else {
                        if (randomChat.value) {
                            player.sendChatMessage(RandomUtil.randomString(1, 5))
                        }
                        resetTimer.reset()
                        sent = 0
                    }
                }
            }
            if (mode.value !== Modes.Setback && teleportID == 0 && lessReduction.value) {
                sendTP(player.positionVector)
                if (resetTicks(6)) {
                    sendPackets(0.0, 0.0, 0.0)
                }
                return@safeEventListener
            }
            if (TRStep.value) {
                if (ModuleManager.getModuleByClass(ReverseStep::class.java).isEnabled) {
                    return@safeEventListener
                }
            }
            val isPhasing = checkHitBoxes()
            var ySpeed: Double = if (player.movementInput.jump && (isPhasing || !EntityUtil.isMoving())) {
                if (AntiKick.value && !isPhasing) {
                    if (resetTicks(if (mode.value === Modes.Setback) 10 else 20)) -0.032 else 0.062
                } else {
                    0.062
                }
            } else if (player.movementInput.sneak) {
                -0.062
            } else {
                if (!isPhasing) (if (resetTicks(4)) (if (AntiKick.value) -0.04 else 0.0) else 0.0) else 0.0
            }
            if (phase.value == PhaseMode.Full && isPhasing && EntityUtil.isMoving() && ySpeed != 0.0) {
                ySpeed /= 2.5
            }
            if (mode.value === Modes.Increment) {
                if (lastFactor >= FactorValue.value) {
                    lastFactor = 1.0f
                } else if (++lastFactor > FactorValue.value) {
                    lastFactor = FactorValue.value
                }
            } else {
                lastFactor = FactorValue.value
            }
            val dirSpeed = directionSpeed(if (phase.value == PhaseMode.Full && isPhasing) 0.031 else 0.26)
            var i = 1
            while (i <= if (mode.value === Modes.Factor || mode.value === Modes.Increment) lastFactor else 1f) {
                player.motionX = dirSpeed[0] * 1f * i * XZSpeed.value
                player.motionY = ySpeed * 1f * i * YSpeedValue.value
                player.motionZ = dirSpeed[1] * 1f * i * XZSpeed.value
                sendPackets(player.motionX, player.motionY, player.motionZ)
                i++
            }
            //Fast Mode
            var fastSpeedY =
                if (player.movementInput.jump && (checkHitBoxes() || !EntityUtil.isMoving())) (if (AntiKick.value && !checkHitBoxes()) (if (resetTicks(
                        10
                    )
                ) -0.032 else 0.062) else if (resetTicks(20)) -0.032 else 0.062) else if (player.movementInput.sneak) -0.062 else if (!checkHitBoxes()) (if (resetTicks(
                        4
                    )
                ) (if (AntiKick.value) -0.04 else 0.0) else 0.0) else 0.0
            if (checkHitBoxes() && EntityUtil.isMoving() && fastSpeedY != 0.0) {
                fastSpeedY /= Reduction.value
            }
            if (mode.value == Modes.Fast) {
                sendPackets(dirSpeed[0], fastSpeedY, dirSpeed[1])
            }
            if (mode.value == Modes.XinBypass) {
                var tempBoost = 0
                for (boost in 1..moveFactor.value.toInt()) {
                    tempBoost = boost * boost
                }
                sendPackets(dirSpeed[0] * tempBoost, ySpeed * tempBoost, dirSpeed[1] * tempBoost)
            }
        }
    }

    @SubscribeEvent
    fun onPush(event: PushEvent) {
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderOverlayEvent) {
        event.isCanceled = true
    }

    private fun SafeClientEvent.checkHitBoxes(): Boolean {
        return world.getCollisionBoxes(player, player.entityBoundingBox).isNotEmpty()
    }

    private fun resetTicks(ticks: Int): Boolean {
        if (++otherids >= ticks) {
            otherids = 0
            return true
        }
        return false
    }

    private fun SafeClientEvent.sendPackets(x: Double, y: Double, z: Double) {
        val vec = Vec3d(x, y, z)
        val position = player.positionVector.add(vec)
        val outOfBoundsVec = sendInvalidPacket(position)
        packetSender(CPacketPlayer.Position(position.x, position.y, position.z, false))
        packetSender(
            PositionRotation(
                outOfBoundsVec.x,
                outOfBoundsVec.y,
                outOfBoundsVec.z,
                player.rotationYaw,
                player.rotationPitch,
                false
            )
        )
        player.setPosition(position.x, position.y, position.z)
        sendTP(position)
        sent++
    }

    private fun SafeClientEvent.sendTP(position: Vec3d) {
        if (confirmtp.value && teleportID != 0) {
            val id = ++teleportID
            player.connection.sendPacket(CPacketConfirmTeleport(id))
            posLooks[id] = TimeVec(position)
        }
    }

    private fun sendInvalidPacket(position: Vec3d): Vec3d {
        //左右极限 = 6
        //上下极限 =-150 || =300
        //尝试不非法发包
        var spoofX = position.x
        var spoofY = position.y
        var spoofZ = position.z
        when (type.value) {
            Types.Up -> {
                spoofY += 1337.0
            }

            Types.Down -> {
                spoofY -= 1337.0
            }

            Types.DownStrict -> {
                spoofY -= 256.0
            }

            Types.Bounded -> {
                spoofY += (if (spoofY < 127.5) 255 else 0) - position.y
            }

            Types.Conceal -> {
                spoofX += RandomUtil.nextInt(-100000, 100000).toDouble()
                spoofY += 2.0
                spoofZ += RandomUtil.nextInt(-100000, 100000).toDouble()
            }

            Types.Limit -> {
                spoofX += RandomUtil.nextDouble(-50.0, 50.0)
                spoofY += if (RandomUtil.getRandom().nextBoolean()) RandomUtil.nextDouble(
                    -80.0,
                    -50.0
                ) else RandomUtil.nextDouble(50.0, 80.0)
                spoofZ += RandomUtil.nextDouble(-50.0, 50.0)
            }

            Types.LimitJitter -> {
                spoofX += RandomUtil.nextDouble(-10.0, 10.0)
                spoofY += if (RandomUtil.getRandom().nextBoolean()) RandomUtil.nextDouble(
                    -100.0,
                    -80.0
                ) else RandomUtil.nextDouble(80.0, 100.0)
                spoofZ += RandomUtil.nextDouble(-10.0, 10.0)
            }

            Types.Preserve -> {
                spoofX += RandomUtil.getRandom().nextInt(100000).toDouble()
                spoofZ += RandomUtil.getRandom().nextInt(100000).toDouble()
            }

            Types.LimitPreserve -> {
                spoofX += RandomUtil.nextDouble(45.0, 85.0)
                spoofY += if (RandomUtil.getRandom().nextBoolean()) RandomUtil.nextDouble(
                    -95.0,
                    -40.0
                ) else RandomUtil.nextDouble(40.0, 95.0)
                spoofZ += RandomUtil.nextDouble(-85.0, -45.0)
            }

            Types.Xin -> {
                spoofX += valueBounded.value.toDouble()
                spoofY += 0.0
                spoofZ += valueBounded.value.toDouble()
            }

            Types.OrgStrict -> {
                spoofX -= RandomUtil.nextInt(-10, 10).toDouble()
                spoofY += RandomUtil.nextInt(-2, 2).toDouble()
                spoofZ -= RandomUtil.nextInt(-10, 10).toDouble()
            }
        }
        return Vec3d(spoofX, spoofY, spoofZ)
    }

    private fun SafeClientEvent.packetSender(packet: CPacketPlayer) {
        packets.add(packet)
        player.connection.sendPacket(packet)
    }

    private fun SafeClientEvent.directionSpeed(speed: Double): DoubleArray {
        var forward = player.movementInput.moveForward
        var side = player.movementInput.moveStrafe
        var yaw =
            player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * mc.renderPartialTicks
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (if (forward > 0.0f) -45 else 45).toFloat()
            } else if (side < 0.0f) {
                yaw += (if (forward > 0.0f) 45 else -45).toFloat()
            }
            side = 0.0f
            if (forward > 0.0f) {
                forward = 1.0f
            } else if (forward < 0.0f) {
                forward = -1.0f
            }
        }
        val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
        val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val posX = forward.toDouble() * speed * cos + side.toDouble() * speed * sin
        val posZ = forward.toDouble() * speed * sin - side.toDouble() * speed * cos
        return doubleArrayOf(posX, posZ)
    }

    enum class Types {
        Up, Down, DownStrict, Bounded, Conceal, Limit, LimitJitter, Preserve, LimitPreserve, Xin, OrgStrict
    }

    @Suppress("unused")
    enum class PhaseMode {
        Off, Semi, Full
    }

    enum class Modes {
        Factor, Setback, Fast, Increment, XinBypass
    }

    class TimeVec(xIn: Double, yIn: Double, zIn: Double, val time: Long) : Vec3d(xIn, yIn, zIn) {
        constructor(vec3d: Vec3d) : this(vec3d.x, vec3d.y, vec3d.z, System.currentTimeMillis())
    }
}