package melon.system.render.graphic

import melon.system.event.AlwaysListening
import melon.system.util.color.ColorRGB
import melon.events.Render3DEvent
import melon.events.RunGameLoopEvent
import melon.system.event.safeEventListener
import melon.system.render.graphic.buffer.DynamicVAO
import melon.system.render.graphic.mask.EnumFacingMask
import melon.system.render.graphic.shaders.Shader
import net.minecraft.client.renderer.ActiveRenderInfo
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL32.GL_DEPTH_CLAMP

object RenderUtils3D : AlwaysListening {
    var vertexSize = 0
    var translationX = 0.0
        private set
    var translationY = 0.0
        private set
    var translationZ = 0.0
        private set

    fun setTranslation(x: Double, y: Double, z: Double) {
        translationX = x
        translationY = y
        translationZ = z
    }

    fun resetTranslation() {
        translationX = 0.0
        translationY = 0.0
        translationZ = 0.0
    }

    fun drawBox(box: AxisAlignedBB, color: ColorRGB, sides: Int) {
        if (sides and EnumFacingMask.DOWN != 0) {
            putVertex(box.minX, box.minY, box.maxZ, color)
            putVertex(box.minX, box.minY, box.minZ, color)
            putVertex(box.maxX, box.minY, box.minZ, color)
            putVertex(box.maxX, box.minY, box.maxZ, color)
        }

        if (sides and EnumFacingMask.UP != 0) {
            putVertex(box.minX, box.maxY, box.minZ, color)
            putVertex(box.minX, box.maxY, box.maxZ, color)
            putVertex(box.maxX, box.maxY, box.maxZ, color)
            putVertex(box.maxX, box.maxY, box.minZ, color)
        }

        if (sides and EnumFacingMask.NORTH != 0) {
            putVertex(box.minX, box.minY, box.minZ, color)
            putVertex(box.minX, box.maxY, box.minZ, color)
            putVertex(box.maxX, box.maxY, box.minZ, color)
            putVertex(box.maxX, box.minY, box.minZ, color)
        }

        if (sides and EnumFacingMask.SOUTH != 0) {
            putVertex(box.maxX, box.minY, box.maxZ, color)
            putVertex(box.maxX, box.maxY, box.maxZ, color)
            putVertex(box.minX, box.maxY, box.maxZ, color)
            putVertex(box.minX, box.minY, box.maxZ, color)
        }

        if (sides and EnumFacingMask.WEST != 0) {
            putVertex(box.minX, box.minY, box.maxZ, color)
            putVertex(box.minX, box.maxY, box.maxZ, color)
            putVertex(box.minX, box.maxY, box.minZ, color)
            putVertex(box.minX, box.minY, box.minZ, color)
        }

        if (sides and EnumFacingMask.EAST != 0) {
            putVertex(box.maxX, box.minY, box.minZ, color)
            putVertex(box.maxX, box.maxY, box.minZ, color)
            putVertex(box.maxX, box.maxY, box.maxZ, color)
            putVertex(box.maxX, box.minY, box.maxZ, color)
        }
    }

    fun drawLineTo(position: Vec3d, color: ColorRGB) {
        putVertex(camPos.x, camPos.y, camPos.z, color)
        putVertex(position.x, position.y, position.z, color)
    }

    fun drawOutline(box: AxisAlignedBB, color: ColorRGB) {
        putVertex(box.minX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.maxZ, color)
        putVertex(box.maxX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.minZ, color)

        putVertex(box.minX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.maxZ, color)
        putVertex(box.maxX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.minZ, color)

        putVertex(box.minX, box.minY, box.minZ, color)
        putVertex(box.minX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.maxZ, color)
        putVertex(box.maxX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.maxZ, color)
    }

    fun putVertex(posX: Double, posY: Double, posZ: Double, color: ColorRGB) {
        DynamicVAO.buffer.apply {
            putFloat((posX + translationX).toFloat())
            putFloat((posY + translationY).toFloat())
            putFloat((posZ + translationZ).toFloat())
            putInt(color.rgba)
        }
        vertexSize++
    }

    fun draw(mode: Int) {
        if (vertexSize == 0) return

        DynamicVAO.POS3_COLOR.upload(vertexSize)

        DrawShader.bind()
        DynamicVAO.POS3_COLOR.useVao { glDrawArrays(mode, 0, vertexSize) }
        DrawShader.unbind()

        vertexSize = 0
    }

    fun prepareGL() {
        GlStateManager.pushMatrix()
        GlStateManager.glLineWidth(1f)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_CLAMP)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        GlStateManager.disableAlpha()
        GlStateManager.shadeModel(GL_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
    }

    fun releaseGL() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL_FLAT)
        GlStateManager.enableAlpha()
        GlStateManager.depthMask(true)
        glDisable(GL_DEPTH_CLAMP)
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.color(1f, 1f, 1f)
        GlStateManager.glLineWidth(1f)
        GlStateManager.popMatrix()
    }

    private object DrawShader :
        Shader(
            "/assets/melon/shaders/general/Pos3Color.vsh",
            "/assets/melon/shaders/general/Pos3Color.fsh"
        )

    @JvmStatic
    var partialTicks = 0.0f
        private set
    var camPos: Vec3d = Vec3d.ZERO
        private set

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            partialTicks =
                if (mc.isGamePaused) mc.renderPartialTicksPaused else mc.renderPartialTicks
        }

        safeEventListener<Render3DEvent> {
            setTranslation(
                -mc.renderManager.renderPosX,
                -mc.renderManager.renderPosY,
                -mc.renderManager.renderPosZ
            )
        }

        safeEventListener<Render3DEvent>(Int.MAX_VALUE, true) {
            val entity = mc.renderViewEntity ?: player
            val ticks = partialTicks
            val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * ticks
            val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ticks
            val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * ticks
            val camOffset = ActiveRenderInfo.getCameraPosition()

            camPos = Vec3d(x + camOffset.x, y + camOffset.y, z + camOffset.z)
        }
    }
}