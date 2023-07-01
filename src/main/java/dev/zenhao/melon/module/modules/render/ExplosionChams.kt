package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.gl.MelonTessellator
import melon.system.event.safeConcurrentListener
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "ExplosionChams", category = Category.RENDER)
object ExplosionChams : Module() {
    private val range = isetting("Range", 12, 0, 30)
    private val reversePercent = isetting("ReversePercent", 85,1, 100)
    private val fadeTime = isetting("FadeTime", 300, 1, 1000)
    private val lineWidth = fsetting("LineWidth", 1.5f, 0.1f, 10f)
    private val color = csetting("Color", Color(255, 255, 255))
    private val cryList = ConcurrentHashMap<EntityEnderCrystal, RenderInfo>()

    init {
        safeConcurrentListener<PlayerMotionEvent> {
            if (cryList.isNotEmpty()) {
                cryList.forEach {
                    if (!world.loadedEntityList.contains(it.key) || player.getDistanceSq(it.key) > range.value.sq) {
                        cryList.remove(it.key, it.value)
                    }
                }
            }
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        if (fullNullCheck()) return
        for (e in CopyOnWriteArrayList(mc.world.loadedEntityList)) {
            if (e !is EntityEnderCrystal) continue
            if (mc.player.getDistanceSq(e) > range.value.sq) continue
            if (!cryList.containsKey(e)) {
                cryList[e] = RenderInfo(e, System.currentTimeMillis(), fadeTime.value, false)
            }
        }
        if (cryList.isNotEmpty()) {
            cryList.forEach { (_: EntityEnderCrystal, renderInfo: RenderInfo) ->
                if (renderInfo.entity.isEntityAlive && mc.player.getDistanceSq(renderInfo.entity) < range.value.sq) {
                    val calcTime = MathHelper.clamp((System.currentTimeMillis() - renderInfo.time).toDouble(), 0.0, renderInfo.saveTime.toDouble()) / renderInfo.saveTime.toDouble()
                    var renderTime =
                        MathHelper.clamp(calcTime, 0.0, 0.8)
                    if ((renderTime / renderInfo.saveTime.toDouble()) * 100 > reversePercent.value && !renderInfo.reverse) {
                        renderInfo.reverse = true
                    } else if (renderInfo.reverse) {
                        if (renderTime in 0.0..0.01) {
                            renderInfo.reverse = false
                            renderTime = MathHelper.clamp(calcTime, 0.0, 0.8)
                        }
                    }
                    val renderHeight = if (!renderInfo.reverse) {
                        renderTime.toFloat()
                    } else {
                        renderTime -= (0.01 * renderInfo.saveTime)
                        renderTime.toFloat()
                    }
                    MelonTessellator.drawCircle(
                        renderInfo.entity,
                        renderTime,
                        renderHeight,
                        lineWidth.value,
                        color.value
                    )
                }
            }
        }
    }

    class RenderInfo(var entity: EntityEnderCrystal, var time: Long, val saveTime: Int, var reverse: Boolean)
}