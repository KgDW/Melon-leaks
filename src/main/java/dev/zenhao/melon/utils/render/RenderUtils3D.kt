package dev.zenhao.melon.utils.render

import dev.zenhao.melon.module.modules.render.Nametags
import dev.zenhao.melon.utils.color.GSColor
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.gl.MelonTessellator.prepare
import dev.zenhao.melon.utils.gl.MelonTessellator.release
import net.minecraft.block.material.Material
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

object RenderUtils3D {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private var camera: ICamera = Frustum()
    fun drawBorderedRect(
        x: Double,
        y: Double,
        x1: Double,
        y1: Double,
        lineWidth: Float,
        inside: GSColor,
        border: GSColor
    ) {
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        inside.glColor()
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        bufferbuilder.pos(x, y1, 0.0).endVertex()
        bufferbuilder.pos(x1, y1, 0.0).endVertex()
        bufferbuilder.pos(x1, y, 0.0).endVertex()
        bufferbuilder.pos(x, y, 0.0).endVertex()
        tessellator.draw()
        border.glColor()
        GlStateManager.glLineWidth(lineWidth)
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        bufferbuilder.pos(x, y, 0.0).endVertex()
        bufferbuilder.pos(x, y1, 0.0).endVertex()
        bufferbuilder.pos(x1, y1, 0.0).endVertex()
        bufferbuilder.pos(x1, y, 0.0).endVertex()
        bufferbuilder.pos(x, y, 0.0).endVertex()
        tessellator.draw()
    }

    fun drawBoxESP(
        pos: BlockPos,
        color: Color,
        secondColor: Color,
        lineWidth: Float,
        outline: Boolean,
        box: Boolean,
        air: Boolean,
        height: Double,
        gradientBox: Boolean,
        gradientOutline: Boolean,
        invertGradientBox: Boolean,
        invertGradientOutline: Boolean,
        gradientAlpha: Int
    ) {
        if (box) {
            drawBox(
                pos,
                Color(color.red, color.green, color.blue, color.alpha),
                height,
                gradientBox,
                invertGradientBox,
                gradientAlpha
            )
        }
        if (outline) {
            drawBlockOutline2(pos, secondColor, lineWidth, air, height, gradientOutline, invertGradientOutline)
        }
    }

    fun drawBlockOutline2(
        pos: BlockPos,
        color: Color,
        linewidth: Float,
        air: Boolean,
        height: Double,
        gradient: Boolean,
        invert: Boolean
    ) {
        if (gradient) {
            val endColor = Color(color.red, color.green, color.blue, color.alpha)
            drawGradientBlockOutline(
                pos,
                if (invert) endColor else color,
                if (invert) color else endColor,
                linewidth,
                height
            )
            return
        }
        val iblockstate = mc.world.getBlockState(pos)
        if ((air || iblockstate.material !== Material.AIR) && mc.world.worldBorder.contains(pos)) {
            val blockAxis = AxisAlignedBB(
                pos.getX().toDouble() - mc.getRenderManager().viewerPosX,
                pos.getY().toDouble() - mc.getRenderManager().viewerPosY,
                pos.getZ().toDouble() - mc.getRenderManager().viewerPosZ,
                (pos.getX() + 1).toDouble() - mc.getRenderManager().viewerPosX,
                (pos.getY() + 1).toDouble() - mc.getRenderManager().viewerPosY + height,
                (pos.getZ() + 1).toDouble() - mc.getRenderManager().viewerPosZ
            )
            drawBlockOutline(blockAxis.grow(0.002), color, linewidth)
        }
    }

    @JvmStatic
    fun drawEasyNameTags(entity: EntityPlayer, x: Double, y: Double, z: Double, color: Color) {
        val dist = mc.player.getDistance(x, y, z)
        var scale: Double
        scale = 0.0018 + 0.003 * dist
        if (dist <= 8.0) scale = 0.0245
        GlStateManager.pushMatrix()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GlStateManager.enablePolygonOffset()
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
        GlStateManager.disableLighting()
        GlStateManager.translate(
            x - mc.getRenderManager().viewerPosX,
            y - mc.getRenderManager().viewerPosY,
            z - mc.getRenderManager().viewerPosZ
        )
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f)
        GlStateManager.rotate(
            mc.getRenderManager().playerViewX,
            (if (mc.gameSettings.thirdPersonView == 2) -1 else 1).toFloat(),
            0f,
            0f
        )
        GlStateManager.scale(-scale, -scale, scale)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GL11.glShadeModel(GL11.GL_SMOOTH)
        mc.fontRenderer.drawStringWithShadow(
            entity.name,
            -mc.fontRenderer.getStringWidth(entity.name) / 2f,
            -mc.fontRenderer.FONT_HEIGHT.toFloat(),
            color.rgb
        )
        mc.fontRenderer.drawStringWithShadow(
            entity.health.toString(),
            -mc.fontRenderer.getStringWidth(entity.name) / 2f + 40f,
            -mc.fontRenderer.FONT_HEIGHT.toFloat(),
            if (entity.health <= 16) Color(255, 0, 0, 100).rgb else Color(0, 255, 0, 100).rgb
        )
        GlStateManager.enableDepth()
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GlStateManager.disablePolygonOffset()
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun drawNametag(x: Double, y: Double, z: Double, text: String, color: Color) {
        val dist = mc.player.getDistance(x, y, z)
        var scale: Double
        val offset = 0.0
        scale = 0.0018 + 0.003 * dist
        if (dist <= 8.0) scale = 0.0245
        GlStateManager.pushMatrix()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        GlStateManager.enablePolygonOffset()
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
        GlStateManager.disableLighting()
        GlStateManager.translate(
            x - mc.getRenderManager().viewerPosX,
            y + offset - mc.getRenderManager().viewerPosY,
            z - mc.getRenderManager().viewerPosZ
        )
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0f, 1f, 0f)
        GlStateManager.rotate(
            mc.getRenderManager().playerViewX,
            (if (mc.gameSettings.thirdPersonView == 2) -1 else 1).toFloat(),
            0f,
            0f
        )
        GlStateManager.scale(-scale, -scale, scale)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GL11.glShadeModel(GL11.GL_SMOOTH)
        var width = 0.0
        val bcolor = GSColor(
            Nametags.INSTANCE.red.value,
            Nametags.INSTANCE.green.value,
            Nametags.INSTANCE.blue.value,
            Nametags.INSTANCE.alpha.value
        )
        val w = (mc.fontRenderer.getStringWidth(text) / 2f).toDouble()
        if (w > width) {
            width = w
        }
        prepare(7)
        //MelonTessellator.drawRect((float) (-width - 1f), -mc.fontRenderer.FONT_HEIGHT, (float) (width + 2f), 2f, bcolor.getRGB());
        drawBorderedRect(
            -width - 1,
            -mc.fontRenderer.FONT_HEIGHT.toDouble(),
            width + 2,
            2.0,
            2f,
            GSColor(0, 4, 0, 100),
            bcolor
        )
        release()
        mc.fontRenderer.drawStringWithShadow(
            text,
            -mc.fontRenderer.getStringWidth(text) / 2f,
            (-mc.fontRenderer.FONT_HEIGHT + 1).toFloat(),
            color.rgb
        )
        GlStateManager.enableDepth()
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GlStateManager.disablePolygonOffset()
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
    }

    fun drawOutlinedBoundingBox(aa: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.buffer
        worldRenderer.begin(3, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(3, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        tessellator.draw()
        worldRenderer.begin(1, DefaultVertexFormats.POSITION)
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawGradientBlockOutline(pos: BlockPos, startColor: Color, endColor: Color, linewidth: Float, height: Double) {
        val iblockstate = mc.world.getBlockState(pos)
        val interp = EntityUtil.interpolateEntity(mc.player, mc.renderPartialTicks)
        drawGradientBlockOutline(
            iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.002).offset(-interp.x, -interp.y, -interp.z)
                .expand(0.0, height, 0.0), startColor, endColor, linewidth
        )
    }

    fun drawGradientBlockOutline(bb: AxisAlignedBB, startColor: Color, endColor: Color, linewidth: Float) {
        val red = startColor.red / 255.0f
        val green = startColor.green / 255.0f
        val blue = startColor.blue / 255.0f
        val alpha = startColor.alpha / 255.0f
        val red1 = endColor.red / 255.0f
        val green1 = endColor.green / 255.0f
        val blue1 = endColor.blue / 255.0f
        val alpha1 = endColor.alpha / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(linewidth)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawBox(pos: BlockPos, color: Color, height: Double, gradient: Boolean, invert: Boolean, alpha: Int) {
        if (gradient) {
            val endColor = Color(color.red, color.green, color.blue, alpha)
            drawOpenGradientBox(pos, if (invert) endColor else color, if (invert) color else endColor, height)
            GL11.glVertex3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
            return
        }
        val bb = AxisAlignedBB(
            pos.getX().toDouble() - mc.getRenderManager().viewerPosX,
            pos.getY().toDouble() - mc.getRenderManager().viewerPosY,
            pos.getZ().toDouble() - mc.getRenderManager().viewerPosZ,
            (pos.getX() + 1).toDouble() - mc.getRenderManager().viewerPosX,
            (pos.getY() + 1).toDouble() - mc.getRenderManager().viewerPosY + height,
            (pos.getZ() + 1).toDouble() - mc.getRenderManager().viewerPosZ
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
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            RenderGlobal.renderFilledBox(
                bb,
                color.red.toFloat() / 255.0f,
                color.green.toFloat() / 255.0f,
                color.blue.toFloat() / 255.0f,
                color.alpha.toFloat() / 255.0f
            )
            GL11.glDisable(2848)
            GlStateManager.depthMask(true)
            GlStateManager.enableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
        GL11.glVertex3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    }

    fun drawOpenGradientBox(pos: BlockPos, startColor: Color, endColor: Color, height: Double) {
        for (face in EnumFacing.values()) {
            if (face == EnumFacing.UP) continue
            drawGradientPlane(pos, face, startColor, endColor, height)
        }
    }

    fun drawGradientPlane(pos: BlockPos, face: EnumFacing, startColor: Color, endColor: Color, height: Double) {
        val tessellator = Tessellator.getInstance()
        val builder = tessellator.buffer
        val iblockstate = mc.world.getBlockState(pos)
        val interp = EntityUtil.interpolateEntity(mc.player, mc.renderPartialTicks)
        val bb = iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.002).offset(-interp.x, -interp.y, -interp.z)
            .expand(0.0, height, 0.0)
        val red = startColor.red.toFloat() / 255.0f
        val green = startColor.green.toFloat() / 255.0f
        val blue = startColor.blue.toFloat() / 255.0f
        val alpha = startColor.alpha.toFloat() / 255.0f
        val red1 = endColor.red.toFloat() / 255.0f
        val green1 = endColor.green.toFloat() / 255.0f
        val blue1 = endColor.blue.toFloat() / 255.0f
        val alpha1 = endColor.alpha.toFloat() / 255.0f
        var x1 = 0.0
        var y1 = 0.0
        var z1 = 0.0
        var x2 = 0.0
        var y2 = 0.0
        var z2 = 0.0
        if (face == EnumFacing.DOWN) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.minY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.UP) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.maxY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.EAST) {
            x1 = bb.maxX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.WEST) {
            x1 = bb.minX
            x2 = bb.minX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.SOUTH) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.maxZ
            z2 = bb.maxZ
        } else if (face == EnumFacing.NORTH) {
            x1 = bb.minX
            x2 = bb.maxX
            y1 = bb.minY
            y2 = bb.maxY
            z1 = bb.minZ
            z2 = bb.minZ
        }
        GlStateManager.pushMatrix()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.depthMask(false)
        builder.begin(5, DefaultVertexFormats.POSITION_COLOR)
        if (face == EnumFacing.EAST || face == EnumFacing.WEST || face == EnumFacing.NORTH || face == EnumFacing.SOUTH) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
        } else if (face == EnumFacing.UP) {
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y1, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x1, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z1).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
            builder.pos(x2, y2, z2).color(red1, green1, blue1, alpha1).endVertex()
        } else if (face == EnumFacing.DOWN) {
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y1, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x1, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z1).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
            builder.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex()
        }
        tessellator.draw()
        GlStateManager.depthMask(true)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    fun drawBox(pos: BlockPos, color: Color) {
        val bb = AxisAlignedBB(
            pos.getX() - mc.getRenderManager().viewerPosX,
            pos.getY() - mc.getRenderManager().viewerPosY,
            pos.getZ() - mc.getRenderManager().viewerPosZ,
            pos.getX() + 1 - mc.getRenderManager().viewerPosX,
            pos.getY() + 1 - mc.getRenderManager().viewerPosY,
            pos.getZ() + 1 - mc.getRenderManager().viewerPosZ
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
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
            GlStateManager.disableTexture2D()
            GlStateManager.depthMask(false)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            RenderGlobal.renderFilledBox(
                bb,
                color.red / 255.0f,
                color.green / 255.0f,
                color.blue / 255.0f,
                color.alpha / 255.0f
            )
            GL11.glDisable(2848)
            GlStateManager.depthMask(true)
            GlStateManager.enableDepth()
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
    }

    fun drawBlockOutline(pos: BlockPos, color: Color, linewidth: Float, air: Boolean) {
        val iblockstate = mc.world.getBlockState(pos)
        if ((air || iblockstate.material !== Material.AIR) && mc.world.worldBorder.contains(pos)) {
            val interp = EntityUtil.interpolateEntity(mc.player, mc.renderPartialTicks)
            drawBlockOutline(
                iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026)
                    .offset(-interp.x, -interp.y, -interp.z), color, linewidth
            )
        }
    }

    fun drawBlockOutline(bb: AxisAlignedBB, color: Color, linewidth: Float) {
        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f
        val alpha = color.alpha / 255.0f
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1)
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)
        GL11.glEnable(2848)
        GL11.glHint(3154, 4354)
        GL11.glLineWidth(linewidth)
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GL11.glDisable(2848)
        GlStateManager.depthMask(true)
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    @JvmStatic
    fun drawRect(x: Float, y: Float, w: Float, h: Float, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos(x.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), h.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(w.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(red, green, blue, alpha).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }

    fun glEnd() {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        GL11.glPopMatrix()
        GL11.glEnable(2929)
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
    }

    fun drawBoxESP(
        pos: BlockPos,
        color: Color,
        secondC: Boolean,
        secondColor: Color,
        lineWidth: Float,
        outline: Boolean,
        box: Boolean,
        boxAlpha: Int,
        air: Boolean
    ) {
        if (box) {
            drawBox(pos, Color(color.red, color.green, color.blue, boxAlpha))
        }
        if (outline) {
            drawBlockOutline(pos, if (secondC) secondColor else color, lineWidth, air)
        }
    }

    fun drawBoundingBox(axisalignedbb: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldrender = tessellator.buffer
        worldrender.begin(3, DefaultVertexFormats.POSITION_COLOR)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ)
        worldrender.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ)
        tessellator.draw()
    }
}