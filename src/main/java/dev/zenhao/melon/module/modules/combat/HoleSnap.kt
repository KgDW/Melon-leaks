package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.movement.Speed
import dev.zenhao.melon.module.modules.movement.Step
import dev.zenhao.melon.setting.BooleanSetting
import dev.zenhao.melon.setting.FloatSetting
import dev.zenhao.melon.setting.IntegerSetting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.toRadian
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.entity.HoleUtil
import dev.zenhao.melon.utils.math.RandomUtil
import dev.zenhao.melon.utils.vector.Vec2f
import dev.zenhao.melon.utils.vector.VectorUtils
import dev.zenhao.melon.utils.vector.VectorUtils.distanceTo
import melon.events.PlayerMoveEvent
import melon.system.event.safeEventListener
import melon.utils.hole.HoleInfo
import melon.utils.hole.SurroundUtils
import melon.utils.hole.SurroundUtils.betterPosition
import melon.utils.hole.SurroundUtils.checkHole
import melon.utils.math.vector.toVec3d
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.MovementInput
import net.minecraft.util.MovementInputFromOptions
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.*

@Module.Info(name = "HoleSnap", category = Category.COMBAT)
object HoleSnap : Module() {
    private var range: IntegerSetting = isetting("Range", 5, 1, 50)
    private var timerVal: FloatSetting = fsetting("TimerVal", 3.4f, 1f, 4f)
    private var timeoutTicks: IntegerSetting = isetting("TimeOutTicks", 60, 0, 1000)
    private var toggleStep: BooleanSetting = bsetting("EnableStep", true)
    private var disableStrafe: BooleanSetting = bsetting("DisableSpeed", false)
    private var antiAim: BooleanSetting = bsetting("AntiAim", true)
    private var packetListReset: TimerUtils = TimerUtils()
    private var holePos: Vec3d? = null
    private var timerBypassing = false
    private var normalLookPos = 0
    private var rotationMode = 1
    private var enabledTicks = 0
    private var stuckTicks = 0
    private var lastPitch = 0f
    private var lastYaw = 0f
    private var normalPos = 0
    private var ranTicks = 0
    var holeInfo: HoleInfo? = null; private set

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        lastYaw = mc.player.rotationYaw
        lastPitch = mc.player.rotationPitch
    }

    override fun onDisable() {
        holePos = null
        stuckTicks = 0
        ranTicks = 0
        enabledTicks = 0
        rotationMode = 1
        timerBypassing = false
        packetListReset.reset()
        mc.timer.tickLength = 50f
        if (toggleStep.value && ModuleManager.getModuleByClass(Step::class.java).isEnabled) {
            ModuleManager.getModuleByClass(Step::class.java).disable()
        }
    }

    val Entity.speed get() = hypot(motionX, motionZ)
    private val EntityPlayer.isFlying: Boolean
        get() = this.isElytraFlying || this.capabilities.isFlying

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is SPacketPlayerPosLook) {
            disable()
        }
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is CPacketPlayer.Position && rotationMode == 1) {
            normalPos++
            if (normalPos > 20) {
                rotationMode = if (normalLookPos > 20) {
                    3
                } else {
                    2
                }
            }
        } else if (event.packet is CPacketPlayer.PositionRotation && rotationMode == 2) {
            normalLookPos++
            if (normalLookPos > 20) {
                rotationMode = if (normalPos > 20) {
                    3
                } else {
                    1
                }
            }
        }
    }

    @SubscribeEvent
    fun onInput(event: InputUpdateEvent) {
        if (fullNullCheck()) {
            return
        }
        if (event.movementInput is MovementInputFromOptions && holePos != null) {
            event.movementInput.resetMove()
        }
    }

    private fun MovementInput.resetMove() {
        moveForward = 0.0f
        moveStrafe = 0.0f
        forwardKeyDown = false
        backKeyDown = false
        leftKeyDown = false
        rightKeyDown = false
    }

    override fun getHudInfo(): String {
        return TextFormatting.RED.toString() + "" + rotationMode + ""
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            if (packetListReset.passed(1000)) {
                normalPos = 0
                normalLookPos = 0
                lastYaw = mc.player.rotationYaw
                lastPitch = mc.player.rotationPitch
                packetListReset.reset()
            }
            if (timerBypassing && antiAim.value) {
                when (rotationMode) {
                    1 -> {
                        //Pos
                        if (EntityUtil.isMoving()) {
                            it.setRotation(lastYaw, lastPitch)
                        }
                    }

                    2 -> {
                        //PosLook
                        it.setRotation(
                            lastYaw + RandomUtil.nextFloat(1f, 3f),
                            lastPitch + RandomUtil.nextFloat(1f, 5f)
                        )
                    }

                    3 -> {
                        //Mixed
                        it.setRotation(lastYaw, lastPitch)
                        it.setRotation(
                            lastYaw + RandomUtil.nextFloat(1f, 3f),
                            lastPitch + RandomUtil.nextFloat(1f, 5f)
                        )
                    }
                }
            }
        }

        safeEventListener<PlayerMoveEvent> { event ->
            if (++enabledTicks > timeoutTicks.value) {
                disable()
                return@safeEventListener
            }

            if (!player.isEntityAlive || player.isFlying) return@safeEventListener

            val currentSpeed = player.speed

            if (shouldDisable(currentSpeed)) {
                timerBypassing = false
                disable()
                return@safeEventListener
            }
            getHole()?.let {
                mc.timer.tickLength = 50f / timerVal.value
                if (disableStrafe.value && ModuleManager.getModuleByClass(Speed::class.java).isEnabled) {
                    ModuleManager.getModuleByClass(Speed::class.java).disable()
                }
                if (toggleStep.value && ModuleManager.getModuleByClass(Step::class.java).isDisabled) {
                    ModuleManager.getModuleByClass(Step::class.java).enable()
                }
                if (!player.isCentered(it)) {
                    timerBypassing = true
                    val playerPos = player.positionVector
                    val targetPos =
                        Vec3d(it.x + 0.5, player.posY, it.z + 0.5)
                    /*: Vec3d = if (HoleUtil.is2HoleB(it)) {
                        //Vec3d(it.x.toDouble() + ((it.x - playerPos.x) / 2), mc.player.posY, it.z.toDouble() + ((it.z - playerPos.z) / 2))
                        Vec3d(it.toVec3dCenter().x, mc.player.posY, it.toVec3dCenter().z)
                    } else {
                        Vec3d(it.x + 0.5, mc.player.posY, it.z + 0.5)
                    }
                     */

                    val yawRad = getRotationTo(playerPos, targetPos).x.toRadian()
                    val dist = hypot(targetPos.x - playerPos.x, targetPos.z - playerPos.z)
                    val baseSpeed = player.applySpeedPotionEffects(0.2873)
                    val speed = if (player.onGround) baseSpeed else max(currentSpeed + 0.02, baseSpeed)
                    val cappedSpeed = min(speed, dist)

                    event.x = -sin(yawRad) * cappedSpeed
                    event.z = cos(yawRad) * cappedSpeed

                    if (player.collidedHorizontally) stuckTicks++
                    else stuckTicks = 0
                }
            }
        }
    }

    private fun EntityLivingBase.applySpeedPotionEffects(speed: Double): Double {
        return this.getActivePotionEffect(MobEffects.SPEED)?.let {
            speed * this.speedEffectMultiplier
        } ?: speed
    }

    private val EntityLivingBase.speedEffectMultiplier: Double
        get() = this.getActivePotionEffect(MobEffects.SPEED)?.let {
            1.0 + (it.amplifier + 1.0) * 0.2
        } ?: 1.0

    private fun getRotationTo(posFrom: Vec3d, posTo: Vec3d): Vec2f {
        return getRotationFromVec(posTo.subtract(posFrom))
    }

    private fun getRotationFromVec(vec: Vec3d): Vec2f {
        val xz = hypot(vec.x, vec.z)
        val yaw = normalizeAngle(Math.toDegrees(atan2(vec.z, vec.x)) - 90.0)
        val pitch = normalizeAngle(Math.toDegrees(-atan2(vec.y, xz)))
        return Vec2f(yaw.toFloat(), pitch.toFloat())
    }

    private fun normalizeAngle(angleIn: Double): Double {
        var angle = angleIn
        angle %= 360.0
        if (angle >= 180.0) {
            angle -= 360.0
        }
        if (angle < -180.0) {
            angle += 360.0
        }
        return angle
    }

    private fun shouldDisable(currentSpeed: Double) =
        holePos?.let { mc.player.posY < it.y } ?: false
                || stuckTicks > 5 && currentSpeed < 0.1
                || currentSpeed < 0.01 && getHole()?.let { mc.player.isCentered(it) } == true || (checkHole(mc.player) != SurroundUtils.HoleType.NONE)

    /*
    private fun shouldDisable(currentSpeed: Double) =
        hole?.let { mc.player.posY < it.origin.y } ?: false
                || stuckTicks > 5 && currentSpeed < 0.05
                || mc.player.onGround && HoleManager.getHoleInfo(mc.player).let {
            it.isHole && mc.player.isCentered(it.center.toBlockPos())
        }
     */

    private fun EntityPlayerSP.isCentered(center: Vec3d): Boolean {
        return this.isCentered(center.x + 0.5, center.z + 0.5)
    }

    private fun EntityPlayerSP.isCentered(x: Double, z: Double): Boolean {
        return abs(this.posX - x) < 0.2
                && abs(this.posZ - z) < 0.2
    }

    private fun getHole() =
        if (mc.player.ticksExisted % 10 == 0 && mc.player.betterPosition != holePos) findHole()
        else holePos ?: findHole()

    private fun findHole(): Vec3d? {
        var closestHole = Pair(69.69, Vec3d.ZERO)
        val playerPos = mc.player.betterPosition
        val ceilRange = range.value
        val posList = VectorUtils.getBlockPositionsInArea(
            playerPos.add(ceilRange, -1, ceilRange),
            playerPos.add(-ceilRange, -1, -ceilRange)
        )

        for (posXZ in posList) {
            val dist = mc.player.distanceTo(posXZ)
            if (dist > range.value || dist > closestHole.first) continue

            for (posY in 0..5) {
                val pos = posXZ.add(0, -posY, 0)
                if (!mc.world.isAirBlock(pos.up())) break
                if (HoleUtil.is2HoleB(pos)) {
                    closestHole = dist to pos.toVec3d()
                    continue
                } else {
                    if (checkHole(pos) == SurroundUtils.HoleType.NONE) continue
                    closestHole = dist to pos.toVec3d()
                }
            }
        }

        return if (closestHole.second != Vec3d.ZERO) closestHole.second.also { holePos = it }
        else null
    }

    /*
    private fun findHole(): HoleInfo? {
        val playerPos = mc.player.betterPosition
        val hRangeSq = 8f * 8f
        return HoleManager.holeInfos.asSequence()
            .filterNot { it.isTrapped }
            .filter { playerPos.y > it.origin.y }
            .filter { playerPos.y - it.origin.y <= 5f }
            .filter { distanceSq(mc.player.posX, mc.player.posZ, it.center.x, it.center.z) <= hRangeSq }
            .filter { it.canEnter(mc.world, playerPos) }
            .minByOrNull { distanceSq(mc.player.posX, mc.player.posZ, it.center.x, it.center.z) }
    }
     */
}