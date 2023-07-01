package melon.system.render.shader

import melon.events.TickEvent
import melon.system.event.AlwaysListening
import melon.system.event.listener
import melon.system.render.graphic.RenderUtils3D
import melon.system.render.graphic.shaders.GLSLSandbox
import org.lwjgl.input.Mouse

object ParticleShader : GLSLSandbox("/assets/melon/shaders/gui/Particle.fsh"), AlwaysListening {
    private val initTime = System.currentTimeMillis()
    private var prevMouseX = 0.0f
    private var prevMouseY = 0.0f
    private var mouseX = 0.0f
    private var mouseY = 0.0f

    init {
        listener<TickEvent.Post>(true) {
            prevMouseX = mouseX
            prevMouseY = mouseY

            mouseX = Mouse.getX() - 1.0f
            mouseY = mc.displayHeight - Mouse.getY() - 1.0f
        }
    }

    fun render() {
        val deltaTicks = RenderUtils3D.partialTicks
        val width = mc.displayWidth.toFloat()
        val height = mc.displayHeight.toFloat()
        val mouseX = prevMouseX + (mouseX - prevMouseX) * deltaTicks
        val mouseY = prevMouseY + (mouseY - prevMouseY) * deltaTicks

        render(width, height, mouseX, mouseY, initTime)
    }
}
