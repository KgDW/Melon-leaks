package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.Melon
import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.manager.FileManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.extension.synchronized
import dev.zenhao.melon.utils.render.RenderUtils
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import melon.events.render.Render2DEvent
import melon.system.event.safeEventListener
import melon.system.render.graphic.ProjectionUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.texture.DynamicTexture
import org.lwjgl.opengl.GL11
import java.io.File
import javax.imageio.ImageIO

@Module.Info(name = "AutoEsu", category = Category.RENDER)
object AutoEsu : Module() {
    private var dataList = ObjectArrayList<DataInfo>().synchronized()
    private var name = ssetting("Name", "eps_")
    private var autoScale = bsetting("AutoScale", false)
    private var customScale = bsetting("CustomScale", false)
    private var scaleSize = dsetting("ScaleSize", 0.5, 0.1, 2.0)
    val bufferedImage = ImageIO.read(Melon::class.java.getResourceAsStream("/assets/melon/esu/ruinan.png"))
    private val esuFile = File(FileManager.esuImageFile)
    val image = DynamicTexture(bufferedImage)

    init {
        safeEventListener<Render2DEvent.NMSL> {
            for (target in EntityManager.players) {
                if (target.name != name.value) continue
                val targetPos = target.positionVector
                val dist = player.getDistance(targetPos.x, targetPos.y, targetPos.z)
                var scale = 0.0018 + 0.003 * dist
                if (dist <= 8.0) scale = 0.245
                val screenPos = ProjectionUtils.toAbsoluteScreenPos(targetPos)
                //GlStateUtils.useProgramForce(0)
                GL11.glColor4f(1f, 1f, 1f, 1f)
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GlStateManager.bindTexture(image.glTextureId)
                //RenderUtils.bindTexture(image.glTextureId)
                val width = bufferedImage.width * (if (autoScale.value) scale else if (customScale.value) scaleSize.value else 1f).toFloat()
                val height = bufferedImage.height * (if (autoScale.value) scale else if (customScale.value) scaleSize.value else 1f).toFloat()
                RenderUtils.drawCompleteImage(
                    screenPos.x.toFloat() - width,
                    screenPos.y.toFloat() - height,
                    width,
                    height
                )
                //GlStateUtils.texture2d(true)
                GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE)
                GL11.glPopMatrix()
            }
        }
    }

    class DataInfo(var playerID: String, var image: DynamicTexture)
}