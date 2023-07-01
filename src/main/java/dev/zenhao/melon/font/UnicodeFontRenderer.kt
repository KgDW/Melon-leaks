package dev.zenhao.melon.font

import melon.system.render.font.renderer.MainFontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.*
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import java.util.*

class UnicodeFontRenderer(
    private val font: Font,
    size: Int,
    private val antiAlias: Boolean,
    private val fractionalMetrics: Boolean,
    private val imgSize: Int,
    private val chunkSize: Int,
    private val linearMag: Boolean,
    scaleFactor: Float
) {
    private val defaultFont: Font
    private val charDataArray: Array<CharData?> = arrayOfNulls(65536)
    private val scaledOffset: Float
    private val textures: Array<MipmapTexture?>
    private val badChunks: IntArray
    private var height = 0

    init {
        this.javaClass.getResourceAsStream("/assets/fonts/LexendDeca-Regular.ttf").use {
            defaultFont = Font.createFont(Font.TRUETYPE_FONT, it)
        }
    }

    var scaleFactor: Float
        private set

    fun setScale(scale: Float): UnicodeFontRenderer {
        scaleFactor = scale
        return this
    }

    private fun initChunk(chunk: Int): MipmapTexture {
        val img = BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB)
        val graphics = img.graphics as Graphics2D
        graphics.font = font
        graphics.color = Color(255, 255, 255, 0)
        graphics.fillRect(0, 0, imgSize, imgSize)
        graphics.color = Color.WHITE
        graphics.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS,
            if (fractionalMetrics) RenderingHints.VALUE_FRACTIONALMETRICS_ON else RenderingHints.VALUE_FRACTIONALMETRICS_OFF
        )
        graphics.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            if (antiAlias) RenderingHints.VALUE_TEXT_ANTIALIAS_ON else RenderingHints.VALUE_TEXT_ANTIALIAS_OFF
        )
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            if (antiAlias) RenderingHints.VALUE_ANTIALIAS_ON else RenderingHints.VALUE_ANTIALIAS_OFF
        )
        val metrics = graphics.fontMetrics
        var charHeight = 0
        var posX = 0
        var posY = 1
        for (index in 0 until chunkSize) {
            val dimension = metrics.getStringBounds((chunk * chunkSize + index).toChar().toString(), graphics)
            val charData = CharData(dimension.bounds.width, dimension.bounds.height)
            val imgWidth = charData.width + scaledOffset * 2
            if (charData.height > charHeight) {
                charHeight = charData.height
                if (charHeight > height) height = charHeight // Set the max height as Font height
            }
            if (posX + imgWidth > imgSize) {
                posX = 0
                posY += charHeight
                charHeight = 0
            }
            charData.u = (posX + scaledOffset) / imgSize.toFloat()
            charData.v = posY / imgSize.toFloat()
            charData.u1 = (posX + scaledOffset + charData.width) / imgSize.toFloat()
            charData.v1 = (posY + charData.height) / imgSize.toFloat()
            charDataArray[chunk * chunkSize + index] = charData
            graphics.drawString(
                (chunk * chunkSize + index).toChar().toString(),
                posX + scaledOffset,
                (posY + metrics.ascent).toFloat()
            )
            posX += imgWidth.toInt()
        }
        val texture = MipmapTexture(img, GL11.GL_RGBA)
        texture.bindTexture()
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0f)
        if (!linearMag) GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
        texture.unbindTexture()
        textures[chunk] = texture
        return texture
    }

    fun getHeight(): Float {
        return height * scaleFactor
    }

    fun getHeight(scale: Float): Float {
        return height * scale * scaleFactor
    }

    fun getWidth(text: String): Float {
        return getWidth0(text) * scaleFactor
    }

    fun getWidth(text: String, scale: Float): Float {
        return getWidth0(text) * scale * scaleFactor
    }

    private val colorCode =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'r')

    init {
        scaledOffset = 4 * size / 25f
        textures = arrayOfNulls(65536 / chunkSize)
        badChunks = IntArray(65536 / chunkSize)
        for (index in 0 until 65536 / chunkSize) {
            badChunks[index] = 0
        }
        this.scaleFactor = scaleFactor
        for (chunk in 0 until 256 / chunkSize) {
            initChunk(chunk)
        }
    }

    private fun getWidth0(text: String): Int {
        var sum = 0
        var shouldSkip = false
        for (index in 0 until text.length) {
            if (shouldSkip) {
                shouldSkip = false
                continue
            }
            val c = text[index]
            val chunk = c.code / chunkSize
            if (badChunks[chunk] == 1) continue
            if (textures.size <= chunk) continue
            if (textures[chunk] == null) {
                var newTexture: MipmapTexture
                try {
                    newTexture = initChunk(chunk)
                    textures[chunk] = newTexture
                } catch (ignored: Exception) {
                    badChunks[chunk] = 1
                    continue
                }
            }
            var delta = 0
            val data = charDataArray[c.code]
            if (data != null) delta = data.width
            if (c == '§' || c == '&') {
                if (index + 1 < text.length) {
                    val next = text[index + 1]
                    for (c1 in colorCode) {
                        if (next == c1) {
                            shouldSkip = true
                            break
                        }
                    }
                }
            } else sum += delta
        }
        return sum
    }

    fun drawString(text: String, x: Float, y: Float) {
        drawString0(text, x, y, Color.WHITE, 1f, false)
    }

    fun drawString(text: String, x: Float, y: Float, color: Int) {
        drawString0(text, x, y, Color(color), 1f, false)
    }

    fun drawString(text: String, x: Float, y: Float, color: Color) {
        drawString0(text, x, y, color, 1f, false)
    }

    fun drawString(text: String, x: Float, y: Float, color: Color, scale: Float) {
        drawString0(text, x, y, color, scale, false)
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float) {
        drawString0(text, x + 1f, y + 1f, Color.WHITE, 1f, true)
        drawString0(text, x, y, Color.WHITE, 1f, false)
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Color) {
        drawString0(text, x + 1f, y + 1f, color, 1f, true)
        drawString0(text, x, y, color, 1f, false)
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, color: Color, scale: Float) {
        drawString0(text, x + 1f, y + 1f, color, scale, true)
        drawString0(text, x, y, color, scale, false)
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, shadowDepth: Float) {
        drawString0(text, x + shadowDepth, y + shadowDepth, Color.WHITE, 1f, true)
        drawString0(text, x, y, Color.WHITE, 1f, false)
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, shadowDepth: Float, color: Color) {
        drawString0(text, x + shadowDepth, y + shadowDepth, color, 1f, true)
        drawString0(text, x, y, color, 1f, false)
    }

    fun drawStringWithShadow(text: String, x: Float, y: Float, shadowDepth: Float, color: Color, scale: Float) {
        drawString0(text, x + shadowDepth, y + shadowDepth, color, scale, true)
        drawString0(text, x, y, color, scale, false)
    }

    private fun drawString0(text: String, x: Float, y: Float, color0: Color, scale0: Float, shadow: Boolean) {
        val shadowColor = Color(0, 0, 0, 128)
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        var startX = x
        var startY = y

        // Color
        val alpha = color0.alpha
        var currentColor = color0

        // Scale
        val scale = scale0 * scaleFactor
        if (scale != 1f) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPushMatrix()
            GL11.glTranslatef(x, y, 0f)
            GL11.glScalef(scale, scale, 1f)
            startX = 0f
            startY = 0f
        }
        var chunk = -1
        var shouldSkip = false
        for (index in 0 until text.length) {
            if (shouldSkip) {
                shouldSkip = false
                continue
            }
            val c = text[index]
            if (c == '\n') {
                startY += height.toFloat()
                startX = x
                continue
            }
            if (c == '§' || c == '&') {
                if (index + 1 < text.length) {
                    val next = text[index + 1]
                    //Color
                    val newColor = getColor(next, color0)
                    if (newColor != null) {
                        currentColor = Color(newColor.red, newColor.green, newColor.blue, alpha)
                        shouldSkip = true
                        continue
                    }
                }
            }
            val currentChunk = c.code / chunkSize
            if (currentChunk != chunk) {
                chunk = currentChunk
                val texture = textures[chunk]
                if (texture == null) {
                    // If this is a bad chunk then we skip it
                    if (badChunks[chunk] == 1) continue
                    var newTexture: MipmapTexture? = null
                    try {
                        newTexture = initChunk(chunk)
                    } catch (ignore: Exception) {
                        badChunks[chunk] = 1
                    }
                    if (newTexture == null) {
                        continue
                    } else {
                        textures[chunk] = newTexture
                        newTexture.bindTexture()
                    }
                } else texture.bindTexture()
            }
            var data: CharData?
            data =
                if (c.code >= charDataArray.size || charDataArray[c.code] == null) continue else charDataArray[c.code]
            val renderColor = if (shadow) shadowColor else currentColor
            val endX = startX + data!!.width
            val endY = startY + data.height
            VertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

            //RT
            VertexBuffer.tex2D(endX, startY, data.u1, data.v, renderColor)
            //LT
            VertexBuffer.tex2D(startX, startY, data.u, data.v, renderColor)
            //LB
            VertexBuffer.tex2D(startX, endY, data.u, data.v1, renderColor)
            //RB
            VertexBuffer.tex2D(endX, endY, data.u1, data.v1, renderColor)
            VertexBuffer.end()
            startX = endX
        }
        GlStateManager.bindTexture(0)
        if (scale != 1f) {
            GL11.glMatrixMode(GL11.GL_MODELVIEW)
            GL11.glPopMatrix()
        }
        GlStateManager.disableTexture2D()
    }

    private fun getColor(colorCode: Char, prev: Color): Color? {
        return when (colorCode) {
            '0' -> Color(0, 0, 0)
            '1' -> Color(0, 0, 170)
            '2' -> Color(0, 170, 0)
            '3' -> Color(0, 170, 170)
            '4' -> Color(170, 0, 0)
            '5' -> Color(170, 0, 170)
            '6' -> Color(255, 170, 0)
            '7' -> Color(170, 170, 170)
            '8' -> Color(85, 85, 85)
            '9' -> Color(85, 85, 255)
            'a' -> Color(85, 255, 85)
            'b' -> Color(85, 255, 255)
            'c' -> Color(255, 85, 85)
            'd' -> Color(255, 85, 255)
            'e' -> Color(255, 255, 85)
            'f' -> Color(255, 255, 255)
            'r' -> prev
            else -> null
        }
    }

    class CharData(val width: Int, val height: Int) {
        var u = 0f
        var v = 0f
        var u1 = 0f
        var v1 = 0f
    }

    companion object {
        @JvmOverloads
        fun create(
            path: String?,
            size: Float,
            imgSize: Int = 512,
            chunkSize: Int = 64,
            scaleFactor: Float = 1f
        ): UnicodeFontRenderer {
            val font = UnicodeFontRenderer::class.java.getResourceAsStream("/assets/fonts/LexendDeca-Regular.ttf").use {
                Font.createFont(Font.TRUETYPE_FONT, it)
            }
            return UnicodeFontRenderer(
                font.deriveFont(size).deriveFont(Font.PLAIN),
                size.toInt(),
                true,
                false,
                imgSize,
                chunkSize,
                true,
                scaleFactor
            )
        }
    }
}