package dev.zenhao.melon.module.modules.render.chorus

import dev.zenhao.melon.event.events.player.ChorusUseEvent
import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.ColorSetting
import dev.zenhao.melon.setting.IntegerSetting
import dev.zenhao.melon.utils.chat.ChatUtil
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

@Module.Info(name = "ChorusDetect", category = Category.RENDER)
class ChorusDetect : Module() {
    var tpMap: ConcurrentHashMap<ChorusUseEvent, Long> = ConcurrentHashMap()
    private var color: ColorSetting = csetting("Color", Color(255, 198, 206))
    private var alpha: IntegerSetting = isetting("Alpha", 80, 1, 255)

    @SubscribeEvent
    fun onTeleport(event: ChorusUseEvent) {
        if (fullNullCheck()) {
            return
        }
        if (!tpMap.contains(event)) {
            tpMap[event] = System.currentTimeMillis()
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        if (fullNullCheck() || tpMap.isEmpty()) {
            return
        }
        tpMap.forEach {
            if (abs(it.value - System.currentTimeMillis()) >= 3500f) {
                ChatUtil.sendMessage("Removed")
                tpMap.remove(it.key, it.value)
            }
            /*
            MelonTessellator.drawBoundingFullBox(
                it.key.pos,
                color.value.red,
                color.value.green,
                color.value.blue,
                alpha.value
            )

             */
            if (!it.key.chat) {
                ChatUtil.sendMessage(it.key.player.name + " Teleported: " + it.key.pos.x + it.key.pos.y + it.key.pos.z)
                it.key.chat = true
            }
        }
    }
}
