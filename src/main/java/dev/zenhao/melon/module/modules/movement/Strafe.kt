package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.player.JumpEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.utils.entity.EntityUtil
import melon.events.PlayerMoveEvent
import melon.system.event.safeEventListener
import net.minecraft.init.MobEffects
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

@Module.Info(name = "Strafe", category = Category.MOVEMENT)
class Strafe : Module() {
    private val mode = msetting("Mode", Mode.NORMAL)
    private var boost = bsetting("DamageBoost", false)
    private var boostSpeed = 0.0
    private var stage = 1
    private var lastDist = 0.0
    private var moveSpeed = 0.0

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is SPacketEntityVelocity) {
            if ((event.packet as SPacketEntityVelocity).getEntityID() == mc.player.getEntityId()) {
                if (ModuleManager.getModuleByClass(Speed::class.java).isDisabled) {
                    boostSpeed = hypot(
                        ((event.packet as SPacketEntityVelocity).motionX / 8000f).toDouble(),
                        ((event.packet as SPacketEntityVelocity).motionZ / 8000f).toDouble()
                    )
                }
            }
        }
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            if (it.stage == 1) {
                lastDist =
                    sqrt((mc.player.posX - mc.player.prevPosX) * (mc.player.posX - mc.player.prevPosX) + (mc.player.posZ - mc.player.prevPosZ) * (mc.player.posZ - mc.player.prevPosZ))
            }
        }

        safeEventListener<PlayerMoveEvent> { event ->
            val motionY = 0.0
            if (shouldReturn() && !player.isInWater && !player.isInLava) {
                if (player.onGround) {
                    stage = 2
                }
                when (stage) {
                    0 -> {
                        ++stage
                        lastDist = 0.0
                    }

                    2 -> {
                        if (player.onGround && mc.gameSettings.keyBindJump.isKeyDown) {
                            if (player.isPotionActive(MobEffects.JUMP_BOOST)) {
                                event.y = motionY.also { player.motionY = it }
                                moveSpeed *= if (mode.value == Mode.NORMAL) 1.7 else 2.149
                            }
                        }
                    }

                    3 -> {
                        moveSpeed =
                            lastDist - (if (mode.value == Mode.NORMAL) 0.6901 else 0.795) * (lastDist - baseMoveSpeed)
                    }

                    else -> {
                        if ((world.getCollisionBoxes(
                                player,
                                player.entityBoundingBox.offset(0.0, player.motionY, 0.0)
                            ).size > 0 || player.collidedVertically) && stage > 0
                        ) {
                            stage = if (player.moveForward != 0.0f || player.moveStrafing != 0.0f) 1 else 0
                        }
                        moveSpeed = lastDist - lastDist / 209.0
                    }
                }
                if (boost.value && boostSpeed != 0.0 && EntityUtil.isMoving()) {
                    //moveSpeed = moveSpeed + Math.abs(moveSpeed - boostSpeed);
                    moveSpeed = boostSpeed
                    boostSpeed = 0.0
                }
                moveSpeed = if (!mc.gameSettings.keyBindJump.isKeyDown && player.onGround) {
                    baseMoveSpeed
                } else {
                    moveSpeed.coerceAtLeast(baseMoveSpeed)
                }
                if (player.movementInput.moveForward.toDouble() == 0.0 && player.movementInput.moveStrafe.toDouble() == 0.0) {
                    event.setSpeed(0.0)
                } else if (player.movementInput.moveForward.toDouble() != 0.0 && player.movementInput.moveStrafe.toDouble() != 0.0) {
                    player.movementInput.moveForward *= sin(0.7853981633974483).toFloat()
                    player.movementInput.moveStrafe *= cos(0.7853981633974483).toFloat()
                }
                event.x = (player.movementInput.moveForward * moveSpeed * -sin(
                    Math.toRadians(player.rotationYaw.toDouble())
                ) + player.movementInput.moveStrafe * moveSpeed * cos(
                    Math.toRadians(
                        player.rotationYaw.toDouble()
                    )
                )) * if (mode.value == Mode.NORMAL) 0.993 else 0.99
                event.z = (player.movementInput.moveForward * moveSpeed * cos(
                    Math.toRadians(player.rotationYaw.toDouble())
                ) - player.movementInput.moveStrafe * moveSpeed * -sin(
                    Math.toRadians(
                        player.rotationYaw.toDouble()
                    )
                )) * if (mode.value == Mode.NORMAL) 0.993 else 0.99
                ++stage
            }
        }
    }

    @SubscribeEvent
    fun onJump(event: JumpEvent) {
        if (fullNullCheck()) {
            return
        }
        if (shouldReturn() && !mc.player.isInWater && !mc.player.isInLava) {
            event.isCanceled = true
        }
    }

    override fun getHudInfo(): String {
        return if (mode.value == Mode.NORMAL) "Normal" else "Strict"
    }

    private val baseMoveSpeed: Double
        get() {
            var n = 0.2873
            if (mc.player.isPotionActive(MobEffects.SPEED)) {
                n *= 1.0 + 0.2 * (Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED))!!.amplifier + 1)
            }
            return n
        }

    private fun shouldReturn(): Boolean {
        return Speed.isDisabled
    }

    @Suppress("unused")
    enum class Mode {
        STRICT, NORMAL
    }
}