package melon.system.render.graphic.buffer

import dev.zenhao.melon.utils.TickTimer
import dev.zenhao.melon.utils.TimeUnit
import dev.zenhao.melon.utils.extension.ceilToInt
import melon.events.TickEvent
import melon.system.event.AlwaysListening
import melon.system.event.listener
import net.minecraft.client.renderer.GLAllocation
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL20.glVertexAttribPointer
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import java.nio.Buffer
import java.nio.ByteBuffer
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

enum class DynamicVAO(private val vertexSize: Int, private val buildVAO: () -> Unit) {
    POS2_COLOR(
        12,
        {
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 12, 0L)
            glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, 12, 8L)

            glEnableVertexAttribArray(0)
            glEnableVertexAttribArray(1)
        }
    ),
    POS3_COLOR(
        16,
        {
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 16, 0L)
            glVertexAttribPointer(1, 4, GL_UNSIGNED_BYTE, true, 16, 12L)

            glEnableVertexAttribArray(0)
            glEnableVertexAttribArray(1)
        }
    );

    val vaoID = glGenVertexArrays()
    val vboID = glGenBuffers()

    private var bufferSize = 0

    init {
        allocateBuffer(64)
    }

    inline fun useVbo(block: DynamicVAO.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        block.invoke(this)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    inline fun useVao(block: DynamicVAO.() -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        glBindVertexArray(vaoID)
        block.invoke(this)
        glBindVertexArray(0)
    }

    fun upload(size: Int) {
        (buffer as Buffer).flip()
        ensureCapacity(size * vertexSize)

        useVbo { glBufferSubData(GL_ARRAY_BUFFER, 0, buffer) }

        (buffer as Buffer).clear()
    }

    private fun ensureCapacity(size: Int) {
        if (size > bufferSize) {
            allocateBuffer((size / 64.0).ceilToInt() * 64)
        }
    }

    private fun allocateBuffer(size: Int) {
        if (size == bufferSize) return

        useVao {
            useVbo {
                glBufferData(GL_ARRAY_BUFFER, size.toLong(), GL_DYNAMIC_DRAW)
                buildVAO.invoke()
            }
        }
        bufferSize = size
    }

    companion object : AlwaysListening {
        val buffer: ByteBuffer = GLAllocation.createDirectByteBuffer(0x800000)
        private val timer = TickTimer(TimeUnit.SECONDS)

        init {
            listener<TickEvent.Post> {
                if (timer.tick(5)) {
                    values().forEach { it.allocateBuffer(64) }
                }
            }
        }
    }
}