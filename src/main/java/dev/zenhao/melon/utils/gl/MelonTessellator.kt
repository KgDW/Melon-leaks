package dev.zenhao.melon.utils.gl

import dev.zenhao.melon.utils.Wrapper
import dev.zenhao.melon.utils.animations.sq
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_SMOOTH
import java.awt.Color
import java.lang.Math.toRadians
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object MelonTessellator : Tessellator(2097152) {
    var mc = Minecraft.getMinecraft()
    var camera = Frustum()
    fun prepare(mode: Int) {
        prepareGL()
        begin(mode)
    }

    @JvmStatic
    fun glRestore() {
        GlStateManager.enableCull()
        GlStateManager.enableAlpha()
        GlStateManager.shadeModel(GL11.GL_FLAT)
    }

    fun color(color: Int) {
        val f = (color shr 24 and 255).toFloat() / 255.0f
        val f1 = (color shr 16 and 255).toFloat() / 255.0f
        val f2 = (color shr 8 and 255).toFloat() / 255.0f
        val f3 = (color and 255).toFloat() / 255.0f
        GL11.glColor4f(f1, f2, f3, f)
    }

    fun drawCircle(
        entity: Entity,
        rad: Double,
        height: Float,
        lineWidth: Float,
        color: Color
    ) {
        prepare(GL11.GL_QUADS)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glDepthMask(false)
        GL11.glLineWidth(lineWidth)
        GL11.glBegin(GL11.GL_LINE_STRIP)
        val x =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.renderPartialTicks - mc.getRenderManager().viewerPosX
        val y =
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.renderPartialTicks - mc.getRenderManager().viewerPosY
        val z =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.renderPartialTicks - mc.getRenderManager().viewerPosZ
        val pix2 = Math.PI * 2.0
        GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
        for (i in 0..180) {
            GL11.glVertex3d(x + rad * cos(i * pix2 / 45), y + height, z + rad * sin(i * pix2 / 45))
        }
        GL11.glEnd()
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        release()
    }

    @JvmStatic
    fun drawBlockOutline(bb: AxisAlignedBB, color: Color, alphaVal: Float, linewidth: Float) {
        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(linewidth)
        val tessellator = getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alphaVal).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawPlane(x: Double, y: Double, z: Double, bb: AxisAlignedBB, width: Float, color: Int) {
        GL11.glPushMatrix()
        GL11.glTranslated(x, y, z)
        drawPlane(bb, width, color)
        GL11.glPopMatrix()
    }

    fun drawPlane(axisalignedbb: AxisAlignedBB, width: Float, color: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.glLineWidth(width)
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE
        )
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        drawPlane(axisalignedbb, color)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawPlane(boundingBox: AxisAlignedBB, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        val minX = boundingBox.minX
        val minY = boundingBox.minY
        val minZ = boundingBox.minZ
        val maxX = boundingBox.maxX
        val maxZ = boundingBox.maxZ
        val tessellator = getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(minX, minY, maxZ).color(red, green, blue, 0f).endVertex()
        bufferbuilder.pos(maxZ, minY, minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
    }

    @JvmStatic
    fun drawRect(x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        GL11.glPushMatrix()
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glPushMatrix()
        color(color)
        GL11.glBegin(7)
        GL11.glVertex2d(x2.toDouble(), y1.toDouble())
        GL11.glVertex2d(x1.toDouble(), y1.toDouble())
        GL11.glVertex2d(x1.toDouble(), y2.toDouble())
        GL11.glVertex2d(x2.toDouble(), y2.toDouble())
        GL11.glEnd()
        GL11.glPopMatrix()
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glPopMatrix()
        Gui.drawRect(0, 0, 0, 0, 0)
    }

    @JvmStatic
    fun drawSolidBlockESP(x: Double, y: Double, z: Double, red: Float, green: Float, blue: Float, alpha: Float) {
        prepare(7)
        glColor(red, green, blue, alpha)
        //drawBBBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0), new Color(red, green, blue), (int) alpha, 1.5f, true);
        drawBoundingBox(AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0))
        release()
    }

    @JvmStatic
    fun rPos(): DoubleArray {
        return try {
            doubleArrayOf(
                Minecraft.getMinecraft().getRenderManager().renderPosX,
                Minecraft.getMinecraft().getRenderManager().renderPosY,
                Minecraft.getMinecraft().getRenderManager().renderPosZ
            )
        } catch (e: Exception) {
            doubleArrayOf(
                0.0,
                0.0,
                0.0
            )
        }
    }

    @JvmStatic
    fun prepareGL() {
        GL11.glBlendFunc(770, 771)
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.glLineWidth(1.5f)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GlStateManager.enableAlpha()
        GlStateManager.color(1.0f, 1.0f, 1.0f)
    }

    fun begin(mode: Int) {
        buffer.begin(mode, DefaultVertexFormats.POSITION_COLOR)
    }

    @JvmStatic
    fun release() {
        render()
        releaseGL()
    }

    fun render() {
        draw()
    }

    @JvmStatic
    fun releaseGL() {
        GlStateManager.enableCull()
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.enableDepth()
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    fun drawBoundingBox(bb: AxisAlignedBB, width: Float, red: Float, green: Float, blue: Float, alpha: Float) {
        GL11.glLineWidth(width)
        glColor(red, green, blue, alpha)
        drawBoundingBox(bb)
    }

    fun drawBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = getInstance()
        val vertexBuffer = tessellator.buffer
        vertexBuffer.begin(3, DefaultVertexFormats.POSITION)
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
        vertexBuffer.begin(3, DefaultVertexFormats.POSITION)
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        tessellator.draw()
        vertexBuffer.begin(1, DefaultVertexFormats.POSITION)
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexBuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        vertexBuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        vertexBuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        tessellator.draw()
    }

    fun glBillboard(x: Float, y: Float, z: Float) {
        val scale = 0.02666667f
        GlStateManager.translate(
            x.toDouble() - Minecraft.getMinecraft().getRenderManager().renderPosX,
            y.toDouble() - Minecraft.getMinecraft().getRenderManager().renderPosY,
            z.toDouble() - Minecraft.getMinecraft().getRenderManager().renderPosZ
        )
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(-Minecraft.getMinecraft().player.rotationYaw, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(
            Minecraft.getMinecraft().player.rotationPitch,
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) -1.0f else 1.0f,
            0.0f,
            0.0f
        )
        GlStateManager.scale(-scale, -scale, scale)
    }

    fun glBillboardDistanceScaled(x: Float, y: Float, z: Float, player: EntityPlayer, scale: Float) {
        glBillboard(x, y, z)
        val distance = player.getDistance(x.toDouble(), y.toDouble(), z.toDouble()).toInt()
        var scaleDistance = distance.toFloat() / 2.0f / (2.0f + (2.0f - scale))
        if (scaleDistance < 1.0f) {
            scaleDistance = 1.0f
        }
        GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance)
    }

    fun drawBBBox(BB: AxisAlignedBB, colour: Color, alpha: Int, lineWidth: Float, outline: Boolean) {
        val bb = AxisAlignedBB(
            BB.minX - mc.getRenderManager().viewerPosX,
            BB.minY - mc.getRenderManager().viewerPosY,
            BB.minZ - mc.getRenderManager().viewerPosZ,
            BB.maxX - mc.getRenderManager().viewerPosX,
            BB.maxY - mc.getRenderManager().viewerPosY,
            BB.maxZ - mc.getRenderManager().viewerPosZ
        )
        camera.setPosition(
            Objects.requireNonNull(mc.getRenderViewEntity())!!.posX,
            mc.getRenderViewEntity()!!.posY,
            mc.getRenderViewEntity()!!.posZ
        )
        if (camera.isBoundingBoxInFrustum(
                AxisAlignedBB(
                    bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY,
                    bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX,
                    bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ
                )
            )
        ) {
            prepare(GL11.GL_QUADS)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glShadeModel(GL_SMOOTH)
            if (outline) {
                drawBoundingBox(
                    bb,
                    lineWidth,
                    colour.red.toFloat(),
                    colour.green.toFloat(),
                    colour.blue.toFloat(),
                    255f
                )
            }
            RenderGlobal.renderFilledBox(
                bb,
                colour.red / 255.0f,
                colour.green / 255.0f,
                colour.blue / 255.0f,
                alpha / 255.0f
            )
            //glColor(colour.red, colour.green, colour.blue, alpha)
            //drawBox(bb)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            release()
        }
    }

    fun boxESP(blockPos: BlockPos, color: Color, alpha: Int, lineWidth: Float, progress: Float, mode: Int) {
        val axisAlignedBB = AxisAlignedBB(
            blockPos.getX().toDouble() - mc.getRenderManager().viewerPosX,
            blockPos.getY().toDouble() - mc.getRenderManager().viewerPosY,
            blockPos.getZ().toDouble() - mc.getRenderManager().viewerPosZ,
            (blockPos.getX() + 1).toDouble() - mc.getRenderManager().viewerPosX,
            (blockPos.getY() + 1).toDouble() - mc.getRenderManager().viewerPosY,
            (blockPos.getZ() + 1).toDouble() - mc.getRenderManager().viewerPosZ
        )
        boxESP(axisAlignedBB, color, alpha, lineWidth, progress, mode)
    }

    fun boxESP(axisAlignedBB: AxisAlignedBB, color: Color, alpha: Int, lineWidth: Float, progress: Float, mode: Int) {
        camera.setPosition(
            Objects.requireNonNull(mc.getRenderViewEntity())!!.posX,
            mc.getRenderViewEntity()!!.posY,
            mc.getRenderViewEntity()!!.posZ
        )
        if (camera.isBoundingBoxInFrustum(
                AxisAlignedBB(
                    axisAlignedBB.minX + mc.getRenderManager().viewerPosX,
                    axisAlignedBB.minY + mc.getRenderManager().viewerPosY,
                    axisAlignedBB.minZ + mc.getRenderManager().viewerPosZ,
                    axisAlignedBB.maxX + mc.getRenderManager().viewerPosX,
                    axisAlignedBB.maxY + mc.getRenderManager().viewerPosY,
                    axisAlignedBB.maxZ + mc.getRenderManager().viewerPosZ
                )
            )
        ) {
            var d: Double
            var d2: Double
            var d3: Double
            var d4: Double
            var d5: Double
            var d6: Double
            val d8: Double = if (progress == 0f) {
                mc.playerController.curBlockDamageMP.toDouble()
            } else {
                progress.toDouble()
            }
            //double d8 = mc.playerController.curBlockDamageMP;
            d6 = axisAlignedBB.minX + 1 - square(d8)
            d5 = axisAlignedBB.minY + 1 - square(d8)
            d4 = axisAlignedBB.minZ + 1 - square(d8)
            d3 = axisAlignedBB.maxX - 1 + square(d8)
            d2 = axisAlignedBB.maxY - 1 + square(d8)
            d = axisAlignedBB.maxZ - 1 + square(d8)
            when (mode) {
                1 -> {
                    d6 = axisAlignedBB.minX + 1
                    d5 = axisAlignedBB.minY + 1 - square(d8)
                    d4 = axisAlignedBB.minZ + 1
                    d3 = axisAlignedBB.maxX - 1
                    d2 = axisAlignedBB.maxY - 1 + square(d8)
                    d = axisAlignedBB.maxZ - 1
                }

                2 -> {
                    d6 = axisAlignedBB.minX + 1
                    d5 = axisAlignedBB.minY + 1
                    d4 = axisAlignedBB.minZ + 1
                    d3 = axisAlignedBB.maxX - 1
                    d2 = axisAlignedBB.maxY - 1 + square(d8)
                    d = axisAlignedBB.maxZ - 1
                }

                3 -> {
                    d6 = axisAlignedBB.minX + 1
                    d5 = axisAlignedBB.minY + 1 - square(d8)
                    d4 = axisAlignedBB.minZ + 1
                    d3 = axisAlignedBB.maxX - 1
                    d2 = axisAlignedBB.maxY - 1
                    d = axisAlignedBB.maxZ - 1
                }

                4 -> {
                    d6 = axisAlignedBB.minX + 1 - square(sin(PI * d8 * d8))
                    d5 = axisAlignedBB.minY + 1 - square(d8)
                    d4 = axisAlignedBB.minZ + 1 - square(cos(PI * d8 * d8))
                    d3 = axisAlignedBB.maxX - 1 + square(sin(PI * -d8 * -d8))
                    d2 = axisAlignedBB.maxY - 1 + square(d8)
                    d = axisAlignedBB.maxZ - 1 + square(cos(PI * -d8 * -d8))
                }

                5 -> {
                    d6 = axisAlignedBB.minX + 1 - square(cos(4f / 3f * PI * d8 * d8))
                    d5 = axisAlignedBB.minY + 1 - square(d8)
                    d4 = axisAlignedBB.minZ + 1 - square(sin(4f / 3f * PI * d8 * d8))
                    d3 = axisAlignedBB.maxX - 1 + square(sin(4f / 3f * PI * -d8 * -d8))
                    d2 = axisAlignedBB.maxY - 1 + square(d8)
                    d = axisAlignedBB.maxZ - 1 + square(cos(4f / 3f * PI * -d8 * -d8))
                }

                6 -> {
                    d6 = axisAlignedBB.minX + 1 - square(1f / 2f * d8 * 0.5)
                    d5 = axisAlignedBB.minY + 1 - square(sin(PI * d8 * d8))
                    d4 = axisAlignedBB.minZ + 1 - square(1f / 2f * d8 * 0.5)
                    d3 = axisAlignedBB.maxX - 1 + square(1f / 2f * d8 * 0.5)
                    d2 = axisAlignedBB.maxY - 1 + square(cos(PI * d8 * d8))
                    d = axisAlignedBB.maxZ - 1 + square(1f / 2f * d8 * 0.5)
                }

                7 -> {
                    val sin = sin(toRadians(d8 * 360f))
                    val cos = cos(toRadians(d8 * 180f))
                    d6 = axisAlignedBB.minX + 1 - square(sin)
                    d5 = axisAlignedBB.minY + 1
                    d4 = axisAlignedBB.minZ + square(MathHelper.clamp(1 * sin, 0.0, 1.0))
                    d3 = axisAlignedBB.maxX - 1 + square(cos)
                    d2 = axisAlignedBB.maxY - 1
                    d = axisAlignedBB.maxZ - square(MathHelper.clamp(1 * cos, 0.0, 1.0))
                }
            }
            val newBB = AxisAlignedBB(d6, d5, d4, d3, d2, d)
            prepare(7)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_POINT_SMOOTH)
            GlStateManager.shadeModel(GL_SMOOTH)
            drawBoundingBox(newBB, lineWidth, color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 255f)
            RenderGlobal.renderFilledBox(
                newBB,
                color.red / 255.0f,
                color.green / 255.0f,
                color.blue / 255.0f,
                alpha / 255.0f
            )
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_POINT_SMOOTH)
            release()
        }
    }

    fun square(`val`: Double): Double {
        return `val`.sq / 2
    }

    fun glColor(red2: Float, green2: Float, blue2: Float, alpha: Float) {
        GL11.glColor4f(red2 / 255.0f, green2 / 255.0f, blue2 / 255.0f, alpha / 255.0f)
    }

    fun drawFullBox(pos: BlockPos, width: Float, red: Int, green: Int, blue: Int, alpha: Int) {
        drawBoundingFullBox(getBoundingFromPos(pos), red, green, blue, alpha)
        drawBoundingBox(getBoundingFromPos(pos), width, red.toFloat(), green.toFloat(), blue.toFloat(), 255f)
    }

    fun drawBoundingFullBox(bb: AxisAlignedBB, red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(
            red.toFloat() / 255.0f,
            green.toFloat() / 255.0f,
            blue.toFloat() / 255.0f,
            alpha.toFloat() / 255.0f
        )
        drawFilledBox(bb)
    }

    fun getBoundingFromPos(render: BlockPos): AxisAlignedBB {
        val iBlockState = Wrapper.mc.world.getBlockState(render)
        val interp = interpolateEntity(Wrapper.mc.player, Wrapper.mc.renderPartialTicks)
        return iBlockState.getSelectedBoundingBox(Wrapper.mc.world, render).expand(0.002, 0.002, 0.002)
            .offset(-interp.x, -interp.y, -interp.z)
    }

    fun getBoundingFromPos(renders: Vec3d): AxisAlignedBB {
        val render = BlockPos(renders)
        val iBlockState = Wrapper.mc.world.getBlockState(render)
        val interp = interpolateEntity(Wrapper.mc.player, Wrapper.mc.renderPartialTicks)
        return iBlockState.getSelectedBoundingBox(Wrapper.mc.world, render).expand(0.002, 0.002, 0.002)
            .offset(-interp.x, -interp.y, -interp.z)
    }

    fun interpolateEntity(entity: Entity, time: Float): Vec3d {
        return Vec3d(
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time.toDouble(),
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time.toDouble(),
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time.toDouble()
        )
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = getInstance()
        val vertexbuffer = tessellator.buffer
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        vertexbuffer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }
}