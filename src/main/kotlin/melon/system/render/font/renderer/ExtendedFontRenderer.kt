package melon.system.render.font.renderer

import melon.system.render.font.Style
import java.awt.Font

open class ExtendedFontRenderer(font: Font, size: Float, textureSize: Int) :
    AbstractFontRenderer(font, size, textureSize) {
    val boldGlyph = loadFont(font, size, Style.BOLD)
    val italicGlyph = loadFont(font, size, Style.ITALIC)

    override val renderContext: ExtendedFontRenderContext
        get() = ExtendedFontRenderContext(this)

    override fun destroy() {
        super.destroy()
        boldGlyph.destroy()
        italicGlyph.destroy()
    }
}
