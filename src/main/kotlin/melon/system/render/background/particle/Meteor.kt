package melon.system.render.background.particle

import melon.system.render.background.AParticle
import melon.system.render.background.particle.Meteor.MeteorState.*
import dev.zenhao.melon.utils.TickTimer
import dev.zenhao.melon.utils.TimeUnit
import dev.zenhao.melon.utils.animations.AnimationFlag
import dev.zenhao.melon.utils.animations.Easing
import dev.zenhao.melon.utils.render.FadeUtils
import dev.zenhao.melon.utils.render.RenderUtils
import dev.zenhao.melon.utils.vector.Vec2f
import org.lwjgl.opengl.Display
import java.awt.Color
import kotlin.random.Random

class Meteor(pos: Vec2f, size: Float, velocity: Vec2f, speed: Long, color: Color) : AParticle(pos, size, velocity, speed, color) {

    var alpha = color.alpha.toFloat()
        set(value) {
            field = value.coerceAtMost(255F)
        }

    private val refreshTimer = TickTimer(TimeUnit.MILLISECONDS)

    private var state = IN

    private val easing get() = if (state == OUT) Easing.OUT_QUAD else Easing.IN_QUAD

    override fun render() {
        val speedMoves = speed / 5L
        state = if (refreshTimer.tick(speedMoves * 3L)) OUT else if (refreshTimer.tick(speedMoves * 2L)) STAY else IN
        val offset = easing.inc(Easing.toDelta(refreshTimer.time + if (state == OUT) speedMoves * 3L else 0L, speedMoves * 2F), velocity.x)

        val from: Vec2f
        val to: Vec2f
        when (state) {
            IN, STAY -> {
                from = pos
                to = pos + Vec2f(-offset, offset)
            }
            OUT -> {
                from = pos + Vec2f(-velocity.x, velocity.x)
                to = pos + Vec2f(-offset, offset)
            }
        }

        RenderUtils.drawLine(
            from.x.toDouble(),
            from.y.toDouble(),
            to.x.toDouble(),
            to.y.toDouble(),
            size,
            Color(color.red, color.green, color.blue, alpha.toInt())
        )
    }

    override fun refresh() {
        pos = Vec2f(Random.nextInt(Display.getWidth()).toFloat(), Random.nextInt(Display.getHeight()).toFloat())
        velocity = Vec2f(50F + Random.nextInt(300), 0F)
        color = Color(Color.HSBtoRGB(Random.nextInt(360).toFloat(), 0.4f, 1.0f))
        alpha = if (color.alpha == 255) 15F else color.alpha.toFloat()
    }

    override fun update() {
        if (refreshTimer.tickAndReset(speed)) refresh()
        if (alpha < 255F) this.alpha += 0.05f * 10
    }

    override fun generate(): AParticle = Companion.generate()

    companion object {

        @JvmField
        val GET: Meteor = generate()


        @JvmStatic
        fun generate(): Meteor {
            val pos = Vec2f(Random.nextInt(Display.getWidth()).toFloat(), Random.nextInt(Display.getHeight()).toFloat())
            val size = Math.random().toFloat() * 3.0f + 1.0f
            val velocity = Vec2f(50F + Random.nextInt(300), 0F)
            val color = Color(Color.HSBtoRGB(Random.nextInt(360).toFloat(), 0.4f, 1.0f))
            val speed = 3000L + Random.nextInt(1200)
            return Meteor(pos, size, velocity, speed, color)
        }
    }


    private enum class MeteorState {
        IN,
        STAY,
        OUT
    }
}