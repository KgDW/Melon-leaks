package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.entity.PushEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.movement.ReverseStep
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.math.MathUtil
import melon.events.PlayerMoveEvent
import melon.system.event.safeEventListener
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.CPacketInput
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Created by 086 on 22/12/2017.
 */
@Module.Info(
    name = "Freecam",
    category = Category.PLAYER,
    description = "Leave your body and trascend into the realm of the gods"
)
object Freecam : Module() {
    var CancelPackes: Setting<Boolean> = bsetting("CancelPackets", true)
    var toggleRStep: Setting<Boolean> = bsetting("ToggleRStep", true)
    var rotate = bsetting("Rotate", false)
    var speed: Setting<Double> = dsetting("Speed", 1.0, 0.1, 10.0)
    var firstStart = false
    var posX = 0.0
    var posY = 0.0
    var posZ = 0.0
    var clonedPlayer: EntityOtherPlayerMP? = null
    var isRidingEntity = false
    var ridingEntity: Entity? = null

    @SubscribeEvent
    fun PacketSend(event: PacketEvent.Send) {
        if (!CancelPackes.value || fullNullCheck()) {
            return
        }
        if (event.packet is CPacketPlayer || event.packet is CPacketInput) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onPush(event: PushEvent) {
        event.isCanceled = true
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        firstStart = true
        if (toggleRStep.value) {
            ModuleManager.getModuleByClass(ReverseStep::class.java).disable()
        }
        if (mc.player != null) {
            isRidingEntity = mc.player.getRidingEntity() != null
            if (mc.player.getRidingEntity() == null) {
                posX = mc.player.posX
                posY = mc.player.posY
                posZ = mc.player.posZ
            } else {
                ridingEntity = mc.player.getRidingEntity()
                mc.player.dismountRidingEntity()
            }
            clonedPlayer = EntityOtherPlayerMP(mc.world, mc.getSession().profile)
            clonedPlayer!!.entityBoundingBox = AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
            clonedPlayer!!.copyLocationAndAnglesFrom(mc.player)
            clonedPlayer!!.rotationYawHead = mc.player.rotationYawHead
            mc.world.addEntityToWorld(-101, clonedPlayer)
            mc.player.capabilities.isFlying = true
            mc.player.capabilities.flySpeed = (speed.value / 100f).toFloat()
            mc.player.noClip = true
        }
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        if (toggleRStep.value) {
            ModuleManager.getModuleByClass(ReverseStep::class.java).enable()
        }
        mc.player.setPositionAndRotation(posX, posY, posZ, clonedPlayer!!.rotationYaw, clonedPlayer!!.rotationPitch)
        mc.world.removeEntityFromWorld(-101)
        clonedPlayer = null
        posZ = 0.0
        posY = posZ
        posX = posY
        mc.player.capabilities.isFlying = false
        mc.player.capabilities.flySpeed = 0.05f
        mc.player.noClip = false
        mc.player.motionZ = 0.0
        mc.player.motionY = mc.player.motionZ
        mc.player.motionX = mc.player.motionY
        if (isRidingEntity) {
            mc.player.startRiding(ridingEntity!!, true)
        }
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            if (toggleRStep.value) {
                if (ModuleManager.getModuleByClass(ReverseStep::class.java).isEnabled) {
                    return@safeEventListener
                }
            }
            mc.player.noClip = true
            mc.player.setVelocity(0.0, 0.0, 0.0)
            val dir = MathUtil.directionSpeed(speed.value)
            if (mc.player.movementInput.moveStrafe != 0f || mc.player.movementInput.moveForward != 0f) {
                mc.player.motionX = dir[0]
                mc.player.motionZ = dir[1]
            } else {
                mc.player.motionX = 0.0
                mc.player.motionZ = 0.0
            }
            mc.player.isSprinting = false
            if (rotate.value) {
                clonedPlayer!!.prevRotationPitch = mc.player.prevRotationPitch
                mc.player.prevRotationPitch = clonedPlayer!!.prevRotationPitch
                clonedPlayer!!.rotationPitch = mc.player.rotationPitch
                mc.player.rotationPitch = clonedPlayer!!.rotationPitch
                clonedPlayer!!.rotationYaw = mc.player.rotationYaw
                mc.player.rotationYaw = clonedPlayer!!.rotationYaw
                clonedPlayer!!.renderYawOffset = mc.player.renderYawOffset
                mc.player.renderYawOffset = clonedPlayer!!.renderYawOffset
                clonedPlayer!!.rotationYawHead = mc.player.rotationYawHead
                mc.player.rotationYawHead = clonedPlayer!!.rotationYawHead
                it.setRotation(clonedPlayer!!.rotationYaw, clonedPlayer!!.rotationPitch)
            }
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                mc.player.motionY += speed.value
            }
            if (mc.gameSettings.keyBindSneak.isKeyDown) {
                mc.player.motionY -= speed.value
            }
        }

        safeEventListener<PlayerMoveEvent> { event ->
            if (firstStart) {
                event.x = 0.0
                event.y = 0.0
                event.z = 0.0
                firstStart = false
            }
            player.noClip = true
        }
    }

    @SubscribeEvent
    fun onWorldEvent(event: EntityJoinWorldEvent) {
        if (fullNullCheck()) {
            return
        }
        if (event.entity === mc.player) {
            toggle()
        }
    }
}