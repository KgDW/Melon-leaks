package dev.zenhao.melon.module.modules.misc.nocom

import dev.zenhao.melon.manager.FontManager
import dev.zenhao.melon.module.modules.misc.nocom.NoCom.Cout
import dev.zenhao.melon.module.modules.misc.nocom.NoCom.couti
import dev.zenhao.melon.module.modules.misc.nocom.NoCom.incCouti
import dev.zenhao.melon.module.modules.misc.nocom.NoCom.rerun
import dev.zenhao.melon.module.modules.misc.nocom.NoCom.scale
import dev.zenhao.melon.utils.KeyboardUtilsJava
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.threads.runAsyncThread
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

object GuiScanner : GuiScreen() {
    var consoleout = CopyOnWriteArrayList<Cout>()
    var multiply = 1
    var radarx = 0
    var radary = 0
    var radarx1 = 0
    var radary1 = 0
    var centerx = 0
    var centery = 0
    var consolex = 0
    var consoley = 0
    var consolex1 = 0
    var consoley1 = 0
    var hovery = 0
    var hoverx = 0
    var searchx = 0
    var searchy = 0
    var wheely = 0
    private var typeDir = "1"

    override fun keyTyped(c: Char, key: Int) {
        when (c) {
            '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                typeDir = c.toString()
                multiply = typeDir.toInt()
            }

            'r' -> {
                typeDir = "1"
                multiply = 1
            }

            else -> {
                if ((NoCom.getBind() == 0 && !KeyboardUtilsJava.isCtrlDown()) || key == NoCom.getBind()) {
                    NoCom.disable()
                    mc.displayGuiScreen(null)
                }
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    fun getscale(): Float {
        if (scale.value == 1) {
            return 500f
        }
        if (scale.value == 2) {
            return 250f
        }
        if (scale.value == 3) {
            return 125f
        }
        return if (scale.value == 4) {
            75f
        } else 705f
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        checkMouseWheel(mouseX, mouseY)
        radarx = sr.scaledWidth / 8
        radarx1 = sr.scaledWidth * 5 / 8
        radary = sr.scaledHeight / 2 - (radarx1 - radarx) / 2
        radary1 = sr.scaledHeight / 2 + (radarx1 - radarx) / 2
        centerx = (radarx + radarx1) / 2
        centery = (radary + radary1) / 2
        consolex = (sr.scaledWidth * 5.5f / 8f).toInt()
        consolex1 = sr.scaledWidth - 50
        consoley = radary
        consoley1 = radary1 - 50

        GuiRenderHelper.drawOutlineRect(
            consolex.toFloat(),
            consoley.toFloat(),
            (consolex1 - consolex).toFloat(),
            (consoley1 - consoley).toFloat(),
            4f,
            Color(-0x32575758, true).rgb
        )

        drawRect2(
            consolex.toDouble(),
            consoley.toDouble(),
            consolex1.toDouble(),
            consoley1.toDouble(),
            Color(-0x8f3f3f4, true).rgb
        )
        GuiRenderHelper.drawOutlineRect(
            consolex.toFloat(),
            (consoley1 + 3).toFloat(),
            (consolex1 - consolex).toFloat(),
            15f,
            4f,
            Color(-0x32575758, true).rgb
        )
        drawRect2(
            consolex.toDouble(),
            (consoley1 + 3).toDouble(),
            consolex1.toDouble(),
            (consoley1 + 17).toDouble(),
            Color(-0x8f3f3f4, true).rgb
        )
        FontManager.fonts!!.drawString(
            "cursor pos: " + hoverx * (64 * multiply) + "x" + "  " + hovery * (64 * multiply) + "z (Multiply: " + multiply * 64 + ")",
            (consolex + 4).toFloat(),
            (consoley1 + 6).toFloat(),
            -1
        )
        GuiRenderHelper.drawOutlineRect(
            consolex.toFloat(),
            (consoley1 + 20).toFloat(),
            (consolex1 - consolex).toFloat(),
            15f,
            4f,
            Color(-0x32575758, true).rgb
        )
        if (!track) {
            drawRect2(
                consolex.toDouble(),
                (consoley1 + 20).toDouble(),
                consolex1.toDouble(),
                (consoley1 + 35).toDouble(),
                Color(-0x8f3f3f4, true).rgb
            )
            FontManager.fonts!!.drawString("tracker off", (consolex + 4).toFloat(), (consoley1 + 26).toFloat(), -1)
        } else {
            drawRect2(
                consolex.toDouble(),
                (consoley1 + 20).toDouble(),
                consolex1.toDouble(),
                (consoley1 + 35).toDouble(),
                Color(-0x8a1a1a2, true).rgb
            )
            FontManager.fonts!!.drawString("tracker on", (consolex + 4).toFloat(), (consoley1 + 26).toFloat(), -1)
        }
        GuiRenderHelper.drawOutlineRect(
            radarx.toFloat(),
            radary.toFloat(),
            (radarx1 - radarx).toFloat(),
            (radary1 - radary).toFloat(),
            4f,
            Color(-0x32575758, true).rgb
        )
        drawRect2(
            radarx.toDouble(),
            radary.toDouble(),
            radarx1.toDouble(),
            radary1.toDouble(),
            Color(-0x1feaeaeb, true).rgb
        )
        try {
            for (point in NoCom.dots) {
                if (point.type === NoCom.DotType.Searched) {
                    if (NoCom.renderNon.value) {
                        drawRect2(
                            (point.posX / 4f + centerx).toDouble(),
                            (point.posY / 4f + centery).toDouble(),
                            (point.posX / 4f + (radarx1 - radarx) / getscale() + centerx).toDouble(),
                            (point.posY / 4f + (radary1 - radary) / getscale() + centery).toDouble(),
                            Color(-0x18575758, true).rgb
                        )
                    }
                } else {
                    drawRect2(
                        (point.posX / 4f + centerx).toDouble(),
                        (point.posY / 4f + centery).toDouble(),
                        (point.posX / 4f + (radarx1 - radarx) / getscale() + centerx).toDouble(),
                        (point.posY / 4f + (radary1 - radary) / getscale() + centery).toDouble(),
                        Color(0x3CE708).rgb
                    )
                }
            }
        } catch (_: Exception) {
        }
        drawRect2(
            (centerx - 1f).toDouble(),
            (centery - 1f).toDouble(),
            (centerx + 1f).toDouble(),
            (centery + 1f).toDouble(),
            Color(0xFF0303).rgb
        )
        drawRect2(
            mc.player.posX / 16 / 4f + centerx,
            mc.player.posZ / 16 / 4f + centery,
            mc.player.posX / 16 / 4f + (radarx1 - radarx) / getscale() + centerx,
            mc.player.posZ / 16 / 4f + (radary1 - radary) / getscale() + centery,
            Color(0x0012FF).rgb
        )
        if (mouseX in (radarx + 1) until radarx1 && mouseY > radary && mouseY < radary1) {
            hoverx = mouseX - centerx
            hovery = mouseY - centery
        }
        glScissor(consolex.toFloat(), consoley.toFloat(), consolex1.toFloat(), (consoley1 - 10).toFloat(), sr)
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        try {
            for (out in consoleout) {
                FontManager.fonts!!.drawString(
                    out.string,
                    (consolex + 4).toFloat(), (consoley + 6 + out.posY * 11 + wheely).toFloat(), -1
                )
            }
        } catch (ignored: Exception) {
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        FontManager.fonts!!.drawString("X+", (radarx1 + 5).toFloat(), centery.toFloat(), -1)
        FontManager.fonts!!.drawString("X-", (radarx - 15).toFloat(), centery.toFloat(), -1)
        FontManager.fonts!!.drawString("Y+", centerx.toFloat(), (radary1 + 5).toFloat(), -1)
        FontManager.fonts!!.drawString("Y-", centerx.toFloat(), (radary - 8).toFloat(), -1)
    }

    public override fun mouseClicked(mouseX: Int, mouseY: Int, clickedButton: Int) {
        if (mouseX in (radarx + 1) until radarx1 && mouseY > radary && mouseY < radary1) {
            consoleout.clear()
            busy = true
            searchx = mouseX - centerx
            searchy = mouseY - centery
            ChatUtil.sendMessage((searchx * (64 * multiply)).toString() + " " + searchy * (64 * multiply))
            rerun(searchx * (64 * multiply), searchy * (64 * multiply))
            runAsyncThread {
                consoleout.add(
                    Cout(
                        couti,
                        "Selected pos " + searchx * (64 * multiply) + "x " + searchy * (64 * multiply) + "z "
                    )
                )
            }
            incCouti()
        }
        if (mouseX in (consolex + 1) until consolex1 && mouseY > consoley1 + 20 && mouseY < consoley1 + 36) {
            track = !track
        }
    }

    private fun checkMouseWheel(mouseX: Int, mouseY: Int) {
        val dWheel = Mouse.getDWheel()
        if (dWheel < 0) {
            wheely -= 20
        } else if (dWheel > 0) {
            wheely += 20
        }
    }

    var neartrack = false
    var track = false
    var busy = false

    private fun drawRect2(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var left0 = left
        var top0 = top
        var right0 = right
        var bottom0 = bottom
        GlStateManager.pushMatrix()
        if (left0 < right) {
            val i = left0
            left0 = right
            right0 = i
        }
        if (top < bottom) {
            val j = top
            top0 = bottom
            bottom0 = j
        }
        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        )
        GlStateManager.color(f, f1, f2, f3)
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION)
        bufferbuilder.pos(left0, bottom0, 0.0).endVertex()
        bufferbuilder.pos(right0, bottom0, 0.0).endVertex()
        bufferbuilder.pos(right0, top0, 0.0).endVertex()
        bufferbuilder.pos(left0, top0, 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    private fun glScissor(x: Float, y: Float, x1: Float, y1: Float, sr: ScaledResolution) {
        GL11.glScissor(
            (x * sr.scaleFactor.toFloat()).toInt(),
            (mc.displayHeight.toFloat() - y1 * sr.scaleFactor.toFloat()).toInt(),
            ((x1 - x) * sr.scaleFactor.toFloat()).toInt(),
            ((y1 - y) * sr.scaleFactor.toFloat()).toInt()
        )
    }
}