package melon.system.render.font.glyph

import melon.system.render.font.GlyphTexture
import org.lwjgl.opengl.GL11.GL_TEXTURE_2D
import org.lwjgl.opengl.GL11.glTexParameterf
import org.lwjgl.opengl.GL14.GL_TEXTURE_LOD_BIAS

class GlyphChunk(
    /** ID of this chunk */
    val id: Int,

    /** [MipmapTexture] object */
    val texture: GlyphTexture,

    /** Array for all characters' info in this chunk */
    val charInfoArray: Array<CharInfo>
) {
    private var lodbias = 0.0f

    fun updateLodBias(input: Float) {
        if (input != lodbias) {
            lodbias = input
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_LOD_BIAS, input)
        }
    }

    override fun equals(other: Any?) =
        this === other || other is GlyphChunk && id == other.id && texture == other.texture

    override fun hashCode() = 31 * id + texture.hashCode()
}
