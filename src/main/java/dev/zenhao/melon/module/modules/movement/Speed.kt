package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.combat.HoleSnap
import dev.zenhao.melon.module.modules.player.Timer
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.extension.sq
import dev.zenhao.melon.utils.math.RandomUtil
import melon.events.PacketEvents
import melon.events.PlayerMoveEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.runSafe
import net.minecraft.init.MobEffects
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketPlayerPosLook
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.hypot
import kotlin.math.sqrt

@Module.Info(name = "Speed", category = Category.MOVEMENT)
object Speed : Module() {
    private var damageBoost = bsetting("DamageBoost", true)
    private var boostDelay = isetting("BoostDelay", 750, 1, 3000).b(damageBoost)
    private var lavaBoost = bsetting("LavaBoost", true)
    private var speedInWater = bsetting("SpeedInWater", true)
    private var step = bsetting("SetStep", true)
    private var bbtt = bsetting("2b2t", false)
    private var strictBoost = bsetting("StrictBoost", false).b(bbtt).b(damageBoost)
    private var useTimer = bsetting("UseTimer", true)
    private var boostTimer = TimerUtils()
    private var boostSpeed = 0.0
    private var lastDist = 0.0
    private var stage = 1
    private var level = 1
    private var moveSpeed = 0.0

    private val SafeClientEvent.baseMoveSpeed: Double
        get() {
            var n = 0.2873
            if (player.isPotionActive(MobEffects.SPEED)) {
                player.getActivePotionEffect(MobEffects.SPEED)?.let {
                    n *= 1.0 + 0.2 * (it.amplifier + 1)
                }
            }
            return n
        }

    init {
        safeEventListener<PacketEvents.Receive> { event ->
            when (event.packet) {
                is SPacketPlayerPosLook -> {
                    boostTimer.reset()
                    lastDist = 0.0
                    moveSpeed = baseMoveSpeed.coerceAtMost(baseMoveSpeed)
                    stage = 2
                }

                is SPacketEntityVelocity -> {
                    if (event.packet.getEntityID() == player.getEntityId()) {
                        boostSpeed = hypot(
                            (event.packet.motionX / 8000f).toDouble(),
                            (event.packet.motionZ / 8000f).toDouble()
                        )
                    }
                }
            }
        }
        safeEventListener<PlayerMotionEvent> {
            if (HoleSnap.isEnabled || (ElytraPlus.isEnabled && player.isElytraFlying)) {
                return@safeEventListener
            }
            if (useTimer.value) {
                mc.timer.tickLength =
                    if (Timer.isEnabled) 50f / Timer.tickNormal.value else 45.955883f
            }
            if (it.stage == 1) {
                lastDist = sqrt((player.posX - player.prevPosX).sq + (player.posZ - player.prevPosZ).sq)
            }
        }

        safeEventListener<PlayerMoveEvent> { event ->
            if (HoleSnap.isEnabled || (ElytraPlus.isEnabled && player.isElytraFlying)) {
                return@safeEventListener
            }
            if (!speedInWater.value) {
                if (shouldReturn()) {
                    return@safeEventListener
                }
            }
            player.stepHeight = if (Step.isEnabled) Step.stepheight.value.toFloat() else 0.0f
            if (!speedInWater.value) {
                if (shouldReturn()) {
                    return@safeEventListener
                }
            }
            if (player.onGround) {
                level = 2
            }
            if (step.value) {
                player.stepHeight = if (Step.isEnabled) Step.stepheight.value.toFloat() else 0.6f
            }
            if (round(player.posY - player.posY.toInt()) == round(0.138)) {
                player.motionY -= 0.07
                event.y -= 0.08316090325960147
                player.posY -= 0.08316090325960147
            }
            if (level != 1 || player.moveForward == 0.0f && player.moveStrafing == 0.0f) {
                if (level == 2) {
                    level = 3
                    if (EntityUtil.isMoving()) {
                        if (!player.isInLava && player.onGround) {
                            val motY = 0.399999986886978 + EntityUtil.getJumpEffect() * 0.1
                            player.motionY = motY
                            event.y = motY
                        }
                        moveSpeed *= if (bbtt.value) {
                            1.548
                        } else {
                            if (!player.isSneaking) {
                                1.6755
                            } else {
                                1.433
                            }
                        }
                    }
                } else if (level == 3) {
                    level = 4
                    moveSpeed =
                        lastDist - 0.6553 * (lastDist - baseMoveSpeed + RandomUtil.nextDouble(0.0139871, 0.037181238))
                } else {
                    if (player.onGround && (world.getCollisionBoxes(
                            player,
                            player.boundingBox.offset(0.0, player.motionY, 0.0)
                        ).size > 0 || player.collidedVertically)
                    ) {
                        level = 1
                    }
                    moveSpeed = lastDist - (lastDist / RandomUtil.nextDouble(110.0, 156.0))
                }
            } else {
                level = 2
                moveSpeed = if (bbtt.value) {
                    1.548
                } else {
                    if (!player.isSneaking) {
                        1.7103
                    } else {
                        1.578
                    }
                } * baseMoveSpeed
            }
            if (damageBoost.value && EntityUtil.isMoving() && boostSpeed != 0.0) {
                if (boostTimer.tickAndReset(boostDelay.value)) {
                    moveSpeed = if (bbtt.value) {
                        if (strictBoost.value) {
                            (moveSpeed + 1 / 10f / 1.5f).coerceAtLeast(baseMoveSpeed)
                        } else {
                            boostSpeed
                        }
                    } else {
                        boostSpeed
                    }
                }
                boostSpeed = 0.0
            }
            moveSpeed = moveSpeed.coerceAtLeast(baseMoveSpeed)
            player.stepHeight = if (Step.isEnabled) Step.stepheight.value.toFloat() else 0.6f
            if (!player.isInLava) {
                event.setSpeed(moveSpeed)
            } else if (lavaBoost.value && player.isInLava) {
                event.x *= 3.1
                event.z *= 3.1
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    event.y *= 3.0
                }
            }
            if (player.movementInput.moveForward == 0.0f && player.movementInput.moveStrafe == 0.0f) {
                event.setSpeed(0.0)
            }
        }
    }

    override fun onEnable() {
        if (mc.player == null) {
            disable()
            return
        }
        runSafe {
            boostSpeed = 0.0
            boostTimer.reset()
            moveSpeed = baseMoveSpeed
        }
    }

    private fun SafeClientEvent.shouldReturn(): Boolean {
        return player.isInLava || player.isInWater || isDisabled || player.isInWeb
    }

    override fun onDisable() {
        moveSpeed = 0.0
        stage = 2
        runSafe {
            player.stepHeight = 0.6f
            mc.timer.tickLength = 50.0f
        }
    }

    private fun round(n: Double): Double {
        require(3 >= 0)
        return BigDecimal(n).setScale(3, RoundingMode.HALF_UP).toDouble()
    }
}