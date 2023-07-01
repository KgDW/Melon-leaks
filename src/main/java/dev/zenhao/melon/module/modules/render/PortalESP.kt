package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.render.RenderUtils3D
import dev.zenhao.melon.utils.threads.runAsyncThread
import net.minecraft.block.BlockPortal
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "PortalESP", category = Category.RENDER, description = "PortalEsp")
class PortalESP : Module() {
    private val blockPosArrayList = CopyOnWriteArrayList<BlockPos>()
    private val distance: Setting<Int> = isetting("Range", 50, 1, 100)
    private val box = bsetting("Box", true)
    private val boxAlpha: Setting<Int> = isetting("BoxAlpha", 150, 0, 255).b(box)
    private val outline = bsetting("Outline", true)
    private val lineWidth: Setting<Float> = fsetting("OutlineWidth", 1f, 0.1f, 5f).b(outline)
    var firstStart = false
    private var cooldownTicks = 0
    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        cooldownTicks = 0
        firstStart = true
    }

    @SubscribeEvent
    fun onTickEvent(event: ClientTickEvent?) {
        if (fullNullCheck()) {
            return
        }
        try {
            runAsyncThread {
                if (cooldownTicks < 1) {
                    blockPosArrayList.clear()
                    compileDL()
                    cooldownTicks = 80
                }
                --cooldownTicks
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        if (fullNullCheck()) {
            return
        }
        blockPosArrayList.forEach {
            RenderUtils3D.drawBoxESP(
                it,
                Color(204, 0, 153, 255),
                false,
                Color(204, 0, 153, 255),
                lineWidth.value,
                outline.value,
                box.value,
                boxAlpha.value,
                false
            )
        }
    }

    private fun compileDL() {
        if (mc.world == null || mc.player == null) {
            return
        }
        for (x in mc.player.posX.toInt() - distance.value..mc.player.posX.toInt() + distance.value) {
            for (y in mc.player.posY.toInt() - distance.value..mc.player.posY.toInt() + distance.value) {
                for (z in mc.player.posZ.toInt() - distance.value..mc.player.posZ.toInt() + distance.value) {
                    val pos = BlockPos(x, y, z)
                    val block = mc.world.getBlockState(pos).block
                    if (block is BlockPortal && !blockPosArrayList.contains(pos)) {
                        blockPosArrayList.add(pos)
                    }
                }
            }
        }
    }
}