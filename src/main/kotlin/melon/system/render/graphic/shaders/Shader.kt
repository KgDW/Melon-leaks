package melon.system.render.graphic.shaders

import dev.zenhao.melon.Melon
import melon.system.render.graphic.GLObject
import melon.system.render.graphic.GlStateUtils
import melon.system.util.interfaces.MinecraftWrapper
import melon.system.util.io.readText
import org.lwjgl.opengl.GL20.*

open class Shader(vertShaderPath: String, fragShaderPath: String) : GLObject, MinecraftWrapper {
    final override val id: Int

    init {
        val vertexShaderID = createShader(vertShaderPath, GL_VERTEX_SHADER)
        val fragShaderID = createShader(fragShaderPath, GL_FRAGMENT_SHADER)
        val id = glCreateProgram()

        glAttachShader(id, vertexShaderID)
        glAttachShader(id, fragShaderID)

        glLinkProgram(id)
        val linked = glGetProgrami(id, GL_LINK_STATUS)
        if (linked == 0) {
            Melon.logger.error(glGetProgramInfoLog(id, 1024))
            glDeleteProgram(id)
            throw IllegalStateException("Shader failed to link")
        }
        this.id = id

        glDetachShader(id, vertexShaderID)
        glDetachShader(id, fragShaderID)
        glDeleteShader(vertexShaderID)
        glDeleteShader(fragShaderID)
    }

    private fun createShader(path: String, shaderType: Int): Int {
        val srcString = javaClass.getResourceAsStream(path)!!.use { it.readText() }
        val id = glCreateShader(shaderType)

        glShaderSource(id, srcString)
        glCompileShader(id)

        val compiled = glGetShaderi(id, GL_COMPILE_STATUS)
        if (compiled == 0) {
            Melon.logger.error(glGetShaderInfoLog(id, 1024))
            glDeleteShader(id)
            throw IllegalStateException("Failed to compile shader: $path")
        }

        return id
    }

    override fun bind() {
        GlStateUtils.useProgram(id)
    }

    override fun unbind() {
        GlStateUtils.useProgram(0)
    }

    override fun destroy() {
        glDeleteProgram(id)
    }
}
