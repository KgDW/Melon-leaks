package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.chat.ChatUtil
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import net.minecraft.init.Items
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.network.play.server.SPacketPlayerPosLook

@Module.Info(name = "FlyFactor", category = Category.MOVEMENT)
class FlyFactor: Module() {
    private var level = isetting("FlyLevel", 7, 0, 50)
    private var auto = bsetting("CheckAuto", false)
    private var heightTime = isetting("HeightUpdateTime",400,0,2000)
    private var heightUpdateTimer = TimerUtils()
    private var flight = false

    init {
        safeEventListener<PacketEvents.Receive> {
            if (it.packet is SPacketPlayerPosLook) {
                mc.player.setFlag(1,true)
                flight = false
                heightUpdateTimer.reset()
                ChatUtil.NoSpam.sendMessage("LagBack Detected! Updating Status!")
            }
        }

        safeEventListener<PlayerMotionEvent> {
            if (mc.player.inWater || mc.player.isInLava) {
                heightUpdateTimer.reset()
                return@safeEventListener
            }
            if (!mc.player.onGround && !flight) {
                if (!mc.player.isElytraFlying && mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
                    if (heightUpdateTimer.tickAndReset(heightTime.value) && auto.value) {
                        mc.player.setFlag(level.value, true)
                        ChatUtil.NoSpam.sendMessage("Changed Status!")
                        flight = true
                    } else if (!auto.value) {
                        mc.player.setFlag(level.value, true)
                        ChatUtil.NoSpam.sendMessage("Changed Status!")
                        flight = true
                    }
                }
            } else {
                heightUpdateTimer.reset()
            }
            if (!mc.player.getFlag(7) && flight) {
                mc.player.setFlag(3, true)
                flight = false
                ChatUtil.NoSpam.sendMessage("Status Updated!")
            }
        }
    }

    override fun getHudInfo(): String {
        return level.value.toString()
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        flight = false
        heightUpdateTimer.reset()
    }
}