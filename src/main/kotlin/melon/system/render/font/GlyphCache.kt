package melon.system.render.font

import dev.zenhao.melon.Melon
import melon.system.render.font.glyph.CharInfo
import dev.zenhao.melon.utils.java.*
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import javax.imageio.ImageIO

object GlyphCache {
    private val directory = Melon.Companion.CachePath.GLYPHS

    fun delete(font: Font) {
        runCatching {
            val regularFont = font.deriveFont(Font.PLAIN)
            (directory resolve  "${regularFont.hashCode()}").toFile().delete()
        }
    }

    fun put(font: Font, chunk: Int, image: BufferedImage, charInfoArray: Array<CharInfo>) {
        val regularFont = font.deriveFont(Font.PLAIN)

        val dirPath = directory resolve "${regularFont.hashCode()}/"
        val dir = dirPath.toFile()
        dir.mkdirsIfNotExists()

        val imageFile = (dirPath resolve "${font.style}-${chunk}.png").toFile()
        val infoFile = (dirPath resolve "${font.style}-${chunk}.info").toFile()

        runCatching {
                imageFile.createFileIfNotExists()

                ImageIO.write(image, "png", imageFile)
                saveInfo(infoFile, charInfoArray)
            }
            .onFailure { e ->
                Melon.logger.info("Failed saving glyph cache", e)
                runCatching {
                        imageFile.delete()
                        infoFile.delete()
                    }
                    .onFailure { Melon.logger.error("Error delete failed saving glyph cache", it) }
            }
    }

    fun get(font: Font, chunk: Int): Pair<BufferedImage, Array<CharInfo>>? {
        return runCatching {
                val regularFont = font.deriveFont(Font.PLAIN)
                val path = "$directory/${regularFont.hashCode()}/${font.style}-${chunk}"
                val imageFile = File("$path.png")
                val infoFile = File("$path.info")
                if (!imageFile.exists() || !infoFile.exists()) return null

                val image = ImageIO.read(imageFile)
                val info = loadInfo(infoFile)

                image to info
            }
            .onFailure { Melon.logger.error("Error reading glyph cache", it) }
            .getOrNull()
    }

    fun loadInfo(file: File): Array<CharInfo> {
        return DataInputStream(file.inputStream().buffered(2048)).use { stream ->
            Array(512) {
                CharInfo(
                    stream.readFloat(),
                    stream.readFloat(),
                    stream.readFloat(),
                    stream.readShort(),
                    stream.readShort(),
                    stream.readShort(),
                    stream.readShort()
                )
            }
        }
    }

    fun saveInfo(file: File, charInfoArray: Array<CharInfo>) {
        DataOutputStream(file.outputStream().buffered(2048)).use { stream ->
            for (charInfo in charInfoArray) {
                stream.writeFloat(charInfo.width)
                stream.writeFloat(charInfo.height)
                stream.writeFloat(charInfo.renderWidth)
                stream.writeShort(charInfo.uv[0].toInt())
                stream.writeShort(charInfo.uv[1].toInt())
                stream.writeShort(charInfo.uv[2].toInt())
                stream.writeShort(charInfo.uv[3].toInt())
            }
        }
    }
}