package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.entity.EntityEvent
import dev.zenhao.melon.event.events.entity.PushEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.chat.ChatUtil
import melon.events.PlayerMoveEvent
import melon.system.event.safeEventListener
import net.minecraft.entity.MoverType
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Created by 086 on 16/11/2017.
 *
 * @see MixinBlockLiquid
 */
@Module.Info(name = "Velocity", description = "Modify knockback impact", category = Category.MOVEMENT)
class Velocity : Module() {
    private val noPush: Setting<Boolean> = bsetting("NoPush", true)
    private val horizontal: Setting<Float> = fsetting("Horizontal", 0f, 0f, 100f)
    private val vertical: Setting<Float> = fsetting("Vertical", 0f, 0f, 100f)
    var cancelPiston = bsetting("CancelPiston", true)
    var ezLog = bsetting("EzLog", false)
    var logSendMsg = bsetting("LogMsg", false)
    var logMsg = ssetting("LogMessage", "你被活塞&l&cesu&f辣!")

    init {
        safeEventListener<PlayerMoveEvent> { event ->
            if (cancelPiston.value) {
                if (event.type == MoverType.PISTON) {
                    if (ezLog.value) {
                        if (logSendMsg.value) {
                            if (player.dimension == -1) {
                                player.sendChatMessage(
                                    Math.floor(player.posX / 8f)
                                        .toString() + " " + Math.floor(player.posY) + " " + Math.floor(
                                        player.posZ / 8f
                                    ) + "有活塞esu狗 气死我辣"
                                )
                            } else {
                                player.sendChatMessage(
                                    Math.floor(player.posX)
                                        .toString() + " " + Math.floor(player.posY) + " " + Math.floor(
                                        player.posZ
                                    ) + "有活塞esu狗 气死我辣"
                                )
                            }
                        }
                        player.connection.networkManager.closeChannel(
                            TextComponentString(
                                logMsg.value.replace(
                                    "&",
                                    ChatUtil.SECTIONSIGN
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun nmsl(event: EntityEvent) {
        if (fullNullCheck()) {
            return
        }
        if (event.getEntity() === mc.player) {
            if (horizontal.value == 0f && vertical.value == 0f || noPush.value) {
                event.isCanceled = true
                return
            }
            event.x = -event.x * horizontal.value
            event.y = 0.0
            event.z = -event.z * horizontal.value
        }
    }

    override fun getHudInfo(): String {
        return " H" + horizontal.value + "%" + " |" + "V" + vertical.value + "%"
    }

    @SubscribeEvent
    fun packet(event: PacketEvent.Receive) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is SPacketEntityVelocity) {
            val velocity = event.packet as SPacketEntityVelocity
            if (velocity.getEntityID() == mc.player.entityId) {
                if (horizontal.value == 0f && vertical.value == 0f) event.isCanceled = true
                velocity.motionX *= (horizontal.value / 100f).toInt()
                velocity.motionY *= (vertical.value / 100f).toInt()
                velocity.motionZ *= (horizontal.value / 100f).toInt()
            }
        } else if (event.packet is SPacketExplosion) {
            if (horizontal.value == 0f && vertical.value == 0f) event.isCanceled = true
            val velocity = event.packet as SPacketExplosion
            velocity.motionX *= horizontal.value / 100f
            velocity.motionY *= vertical.value / 100f
            velocity.motionZ *= horizontal.value / 100f
        }
    }

    @SubscribeEvent
    fun NoPush(event: PushEvent) {
        if (fullNullCheck()) {
            return
        }
        if (noPush.value) {
            event.isCanceled = true
        }
    }
}