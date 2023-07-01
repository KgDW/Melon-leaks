package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.render.GlStateUtils
import melon.events.Render3DEvent
import melon.events.render.RenderEntityEvent
import melon.system.event.safeEventListener
import melon.system.render.graphic.RenderUtils3D
import melon.utils.entity.EntityUtils.viewEntity
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import java.awt.Color

@Module.Info(name = "CrystalChams", category = Category.RENDER)
object CrystalChams : Module() {
    @JvmField
    var scale = fsetting("Scale", 1f, 0.1f, 5f)

    @JvmField
    var spinSpeed = fsetting("SpinSpeed", 1f, 0f, 5f)

    @JvmField
    var floatSpeed = fsetting("FloatSpeed", 1f, 0f, 5f)

    @JvmField
    var filled = bsetting("Filled", true)

    @JvmField
    var filledDepth = bsetting("FilledDepth", true).b(filled)

    @JvmField
    var filledColor = csetting("FilledColor", Color(133, 255, 200)).b(filled)

    @JvmField
    var filledAlpha = fsetting("FilledAlpha", 180f, 1f, 255f).b(filled)

    @JvmField
    var outline = bsetting("Outline", true)

    @JvmField
    var outlineDepth = bsetting("OutlineDepth", false).b(outline)

    @JvmField
    var outlineColor = csetting("OutlineColor", Color(133, 255, 200)).b(outline)

    @JvmField
    var outlineAlpha = fsetting("OutlineAlpha", 180f, 1f, 255f).b(outline)
    private var lineWidth = fsetting("Width", 2f, 0.25f, 5f)
    var range = fsetting("Range", 16f, 1f, 16f)

    init {
        safeEventListener<RenderEntityEvent.All.Pre> {
            if (it.entity is EntityEnderCrystal && player.getDistanceSq(it.entity) <= range.value.sq) {
                it.cancelled = true
            }
        }

        safeEventListener<Render3DEvent> {
            val partialTicks = RenderUtils3D.partialTicks
            val rangeSq = range.value.sq
            val renderer = mc.renderManager.getEntityClassRenderObject<EntityEnderCrystal>(EntityEnderCrystal::class.java)
                ?: return@safeEventListener

            GlStateUtils.alpha(true)
            GlStateManager.glLineWidth(lineWidth.value)
            GlStateUtils.useProgram(0)

            for (crystal in world.loadedEntityList) {
                if (crystal !is EntityEnderCrystal) continue
                if (viewEntity.getDistanceSq(crystal) > rangeSq) continue

                renderer.doRender(
                    crystal,
                    crystal.posX - mc.renderManager.renderPosX,
                    crystal.posY - mc.renderManager.renderPosY,
                    crystal.posZ - mc.renderManager.renderPosZ,
                    0.0f,
                    partialTicks
                )
            }

            GlStateUtils.depth(false)
            GlStateUtils.alpha(false)
        }
    }
}