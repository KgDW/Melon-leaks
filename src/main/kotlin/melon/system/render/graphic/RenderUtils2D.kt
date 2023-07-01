package melon.system.render.graphic

import dev.zenhao.melon.utils.animations.MathUtils
import melon.system.util.color.ColorRGB
import dev.zenhao.melon.utils.extension.toRadian
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.render.graphic.buffer.DynamicVAO
import melon.system.render.graphic.shaders.Shader
import melon.system.util.interfaces.MinecraftWrapper
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11.*
import kotlin.math.*

/** Utils for basic 2D shapes rendering */
object RenderUtils2D : MinecraftWrapper {
    var vertexSize = 0

    fun drawItem(
        itemStack: ItemStack,
        x: Int,
        y: Int,
        text: String? = null,
        drawOverlay: Boolean = true
    ) {
        GlStateUtils.useProgram(0)
        GlStateUtils.blend(true)
        GlStateUtils.depth(true)
        RenderHelper.enableGUIStandardItemLighting()

        mc.renderItem.zLevel = 0.0f
        mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x, y)
        if (drawOverlay)
            mc.renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x, y, text)
        mc.renderItem.zLevel = 0.0f

        RenderHelper.disableStandardItemLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)

        GlStateUtils.depth(false)
        GlStateUtils.texture2d(true)
    }

    fun drawCircleOutline(
        center: Vec2f = Vec2f.ZERO,
        radius: Float,
        segments: Int = 0,
        lineWidth: Float = 1f,
        color: ColorRGB
    ) {
        drawArcOutline(center, radius, Pair(0f, 360f), segments, lineWidth, color)
    }

    fun drawCircleFilled(
        center: Vec2f = Vec2f.ZERO,
        radius: Float,
        segments: Int = 0,
        color: ColorRGB
    ) {
        drawArcFilled(center, radius, Pair(0f, 360f), segments, color)
    }

    fun drawArcOutline(
        center: Vec2f = Vec2f.ZERO,
        radius: Float,
        angleRange: Pair<Float, Float>,
        segments: Int = 0,
        lineWidth: Float = 1f,
        color: ColorRGB
    ) {
        val arcVertices = getArcVertices(center, radius, angleRange, segments)
        drawLineStrip(arcVertices, lineWidth, color)
    }

    fun drawArcFilled(
        center: Vec2f = Vec2f.ZERO,
        radius: Float,
        angleRange: Pair<Float, Float>,
        segments: Int = 0,
        color: ColorRGB
    ) {
        val arcVertices = getArcVertices(center, radius, angleRange, segments)
        drawTriangleFan(center, arcVertices, color)
    }

    fun drawRectOutline(width: Float, height: Float, lineWidth: Float = 1.0f, color: ColorRGB) {
        drawRectOutline(0.0f, 0.0f, width, height, lineWidth, color)
    }

    fun drawRectOutline(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        lineWidth: Float = 1.0f,
        color: ColorRGB
    ) {
        prepareGl()
        GlStateManager.glLineWidth(lineWidth)

        putVertex(x1, y2, color)
        putVertex(x1, y1, color)
        putVertex(x2, y1, color)
        putVertex(x2, y2, color)
        draw(GL_LINE_LOOP)

        releaseGl()
    }

    fun drawRectFilled(width: Float, height: Float, color: ColorRGB) {
        drawRectFilled(0.0f, 0.0f, width, height, color)
    }

    fun drawRectFilled(x1: Float, y1: Float, x2: Float, y2: Float, color: ColorRGB) {
        prepareGl()

        putVertex(x1, y2, color)
        putVertex(x2, y2, color)
        putVertex(x2, y1, color)
        putVertex(x1, y1, color)

        draw(GL_QUADS)

        releaseGl()
    }

    fun drawRoundedRectFilled(x: Float, y: Float, width: Float, height: Float, radius: Float, color: ColorRGB){
        drawHalfRoundedRectFilled(x, y, width, radius, radius, HalfRoundedDirection.TOP, color)
        drawRectFilled(x, y + radius, x + width, y + height - radius, color)
        drawHalfRoundedRectFilled(x, y + height - radius, width, radius, radius, HalfRoundedDirection.BOTTOM, color)
    }

    fun drawHalfRoundedRectFilled(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        direction: HalfRoundedDirection,
        color: ColorRGB
    ) {
        var radius = radius
        if (radius > height) {
            radius = height
        }
        when (direction) {
            HalfRoundedDirection.TOP -> {
                drawArcFilled(Vec2f(x + width - radius, y + radius) , radius, Pair(  0F,  90F), color = color)
                drawArcFilled(Vec2f(x + radius        , y + radius) , radius, Pair(270F, 360F), color = color)
                drawRectFilled(
                    x + radius,
                    y,
                    x + width - radius,
                    y + radius,
                    color
                )
                drawRectFilled(
                    x,
                    y + radius,
                    x + width,
                    y + height,
                    color
                )
            }
            HalfRoundedDirection.BOTTOM -> {
                drawArcFilled(Vec2f(x + width - radius, y + height - radius), radius, Pair( 90F, 180F), color = color)
                drawArcFilled(Vec2f(x + radius        , y + height - radius), radius, Pair(180F, 270F), color = color)
                drawRectFilled(
                    x + radius,
                    y + height - radius,
                    x + width - radius,
                    y + height,
                    color
                )
                drawRectFilled(
                    x,
                    y,
                    x + width,
                    y + height - radius,
                    color
                )
            }
            HalfRoundedDirection.LEFT -> {
                drawArcFilled(Vec2f(x + radius, y + height - radius), radius, Pair(270F, 180F), color = color)
                drawArcFilled(Vec2f(x + radius, y + radius         ), radius, Pair(270F, 360F), color = color)
                drawRectFilled(
                    x,
                    y + radius,
                    x + radius,
                    y + height - radius,
                    color
                )
                drawRectFilled(
                    x + radius,
                    y,
                    x + width,
                    y + height,
                    color
                )
            }
            HalfRoundedDirection.RIGHT -> {
                drawArcFilled(Vec2f(x + width - radius, y + height - radius), radius, Pair( 90F, 180F), color = color)
                drawArcFilled(Vec2f(x + width - radius, y + radius         ), radius, Pair(  0F,  90F), color = color)
                drawRectFilled(
                    x + width - radius,
                    y + radius,
                    x + width,
                    y + height - radius,
                    color
                )
                drawRectFilled(
                    x,
                    y + radius,
                    x + width - radius,
                    y + height,
                    color
                )
            }
            HalfRoundedDirection.TOP_LEFT -> {

            }
            HalfRoundedDirection.TOP_RIGHT -> {

            }
            HalfRoundedDirection.BOTTOM_LEFT -> {

            }
            HalfRoundedDirection.BOTTOM_RIGHT -> {

            }
        }
    }

    fun drawQuad(pos1: Vec2f, pos2: Vec2f, pos3: Vec2f, pos4: Vec2f, color: ColorRGB) {
        prepareGl()

        putVertex(pos1, color)
        putVertex(pos2, color)
        putVertex(pos4, color)
        putVertex(pos3, color)

        draw(GL_TRIANGLE_STRIP)

        releaseGl()
    }

    fun drawTriangleOutline(
        pos1: Vec2f,
        pos2: Vec2f,
        pos3: Vec2f,
        lineWidth: Float = 1f,
        color: ColorRGB
    ) {
        val vertices = arrayOf(pos1, pos2, pos3)
        drawLineLoop(vertices, lineWidth, color)
    }

    fun drawTriangleFilled(pos1: Vec2f, pos2: Vec2f, pos3: Vec2f, color: ColorRGB) {
        prepareGl()

        putVertex(pos1, color)
        putVertex(pos2, color)
        putVertex(pos3, color)
        draw(GL_TRIANGLES)

        releaseGl()
    }

    fun drawTriangleFan(center: Vec2f, vertices: Array<Vec2f>, color: ColorRGB) {
        prepareGl()

        putVertex(center, color)
        for (vertex in vertices) {
            putVertex(vertex, color)
        }
        draw(GL_TRIANGLE_FAN)

        releaseGl()
    }

    fun drawTriangleStrip(vertices: Array<Vec2f>, color: ColorRGB) {
        prepareGl()

        for (vertex in vertices) {
            putVertex(vertex, color)
        }
        draw(GL_TRIANGLE_STRIP)

        releaseGl()
    }

    fun drawLineLoop(vertices: Array<Vec2f>, lineWidth: Float = 1f, color: ColorRGB) {
        prepareGl()
        GlStateManager.glLineWidth(lineWidth)

        for (vertex in vertices) {
            putVertex(vertex, color)
        }
        draw(GL_LINE_LOOP)

        releaseGl()
        GlStateManager.glLineWidth(1f)
    }

    fun drawLineStrip(vertices: Array<Vec2f>, lineWidth: Float = 1f, color: ColorRGB) {
        prepareGl()
        GlStateManager.glLineWidth(lineWidth)

        for (vertex in vertices) {
            putVertex(vertex, color)
        }
        draw(GL_LINE_STRIP)

        releaseGl()
        GlStateManager.glLineWidth(1f)
    }

    fun drawLine(posBegin: Vec2f, posEnd: Vec2f, lineWidth: Float = 1f, color: ColorRGB) {
        prepareGl()
        GlStateManager.glLineWidth(lineWidth)

        putVertex(posBegin, color)
        putVertex(posEnd, color)
        draw(GL_LINES)

        releaseGl()
        GlStateManager.glLineWidth(1f)
    }

    fun putVertex(pos: Vec2f, color: ColorRGB) {
        putVertex(pos.x, pos.y, color)
    }

    fun putVertex(posX: Float, posY: Float, color: ColorRGB) {
        DynamicVAO.buffer.apply {
            putFloat(posX)
            putFloat(posY)
            putInt(color.rgba)
        }
        vertexSize++
    }

    fun draw(mode: Int) {
        DynamicVAO.POS2_COLOR.upload(vertexSize)

        DrawShader.bind()
        DynamicVAO.POS2_COLOR.useVao { glDrawArrays(mode, 0, vertexSize) }
        DrawShader.unbind()

        vertexSize = 0
    }

    private fun getArcVertices(
        center: Vec2f,
        radius: Float,
        angleRange: Pair<Float, Float>,
        segments: Int
    ): Array<Vec2f> {
        val range =
            max(angleRange.first, angleRange.second) - min(angleRange.first, angleRange.second)
        val seg = calcSegments(segments, radius, range)
        val segAngle = (range / seg.toFloat())

        return Array(seg + 1) {
            val angle = (it * segAngle + angleRange.first).toRadian()
            val unRounded = Vec2f(sin(angle), -cos(angle)).times(radius).plus(center)
            Vec2f(MathUtils.round(unRounded.x, 8), MathUtils.round(unRounded.y, 8))
        }
    }

    private fun calcSegments(segmentsIn: Int, radius: Float, range: Float): Int {
        if (segmentsIn != -0) return segmentsIn
        val segments = radius * 0.5 * PI * (range / 360.0)
        return max(segments.roundToInt(), 16)
    }

    fun prepareGl() {
        GlStateUtils.texture2d(false)
        GlStateUtils.blend(true)
        GlStateUtils.smooth(true)
        GlStateUtils.lineSmooth(true)
        GlStateUtils.cull(false)
    }

    fun releaseGl() {
        GlStateUtils.texture2d(true)
        GlStateUtils.smooth(false)
        GlStateUtils.lineSmooth(false)
        GlStateUtils.cull(true)
    }

    private object DrawShader :
        Shader(
            "/assets/melon/shaders/general/Pos2Color.vsh",
            "/assets/melon/shaders/general/Pos2Color.fsh"
        )

    enum class HalfRoundedDirection {
        TOP,
        LEFT,
        RIGHT,
        BOTTOM,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
