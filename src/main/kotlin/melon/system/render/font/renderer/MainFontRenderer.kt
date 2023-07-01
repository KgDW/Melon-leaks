package melon.system.render.font.renderer

import dev.zenhao.melon.Melon
import dev.zenhao.melon.module.modules.client.NewCustomFont
import melon.system.render.graphic.GlStateUtils
import melon.system.util.color.ColorRGB
import melon.system.util.color.ColorUtils
import java.awt.Font

object MainFontRenderer : IFontRenderer {
    var delegate: ExtendedFontRenderer
    private val defaultFont: Font

    init {
        this.javaClass.getResourceAsStream("/assets/fonts/LexendDeca-Regular.ttf").use {
            defaultFont = Font.createFont(Font.TRUETYPE_FONT, it)
        }

        delegate = loadFont()
    }

    fun reloadFonts() {
        delegate.destroy()
        delegate = loadFont()
    }

    private fun loadFont(): ExtendedFontRenderer {
        val font =
            try {
                defaultFont
            } catch (e: Exception) {
                Melon.logger.warn("Failed loading main font. Using Sans Serif font.", e)
                AbstractFontRenderer.getSansSerifFont()
            }

        return DelegateFontRenderer(font)
    }

    fun drawStringJava(
        string: String,
        posX: Float,
        posY: Float,
        color: Int,
        scale: Float,
        drawShadow: Boolean
    ) {
        var adjustedColor = color
        if (adjustedColor and -67108864 == 0) adjustedColor = color or -16777216

        GlStateUtils.alpha(false)
        drawString(
            string,
            posX,
            posY - 1.0f,
            ColorRGB(ColorUtils.argbToRgba(adjustedColor)),
            scale,
            drawShadow
        )
        GlStateUtils.alpha(true)
        GlStateUtils.useProgramForce(0)
    }

    override fun drawString(
        charSequence: CharSequence,
        posX: Float,
        posY: Float,
        color: ColorRGB,
        scale: Float,
        drawShadow: Boolean
    ) {
        delegate.drawString(charSequence, posX, posY, color, scale, drawShadow)
    }

    override fun getWidth(text: CharSequence, scale: Float): Float {
        return delegate.getWidth(text, scale)
    }

    override fun getWidth(char: Char, scale: Float): Float {
        return delegate.getWidth(char, scale)
    }

    override fun getHeight(scale: Float): Float {
        return delegate.run { regularGlyph.fontHeight * NewCustomFont.lineSpace * scale }
    }

    private class DelegateFontRenderer(font: Font) : ExtendedFontRenderer(font, 64.0f, 2048) {
        override val sizeMultiplier: Float
            get() = NewCustomFont.size

        override val baselineOffset: Float
            get() = NewCustomFont.baselineOffset

        override val charGap: Float
            get() = NewCustomFont.charGap

        override val lineSpace: Float
            get() = NewCustomFont.lineSpace

        override val lodBias: Float
            get() = NewCustomFont.lodBias

        override val shadowDist: Float
            get() = 5.0f
    }
}