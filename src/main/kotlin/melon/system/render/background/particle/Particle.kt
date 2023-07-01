package melon.system.render.background.particle

import melon.system.render.background.AParticle
import dev.zenhao.melon.utils.BackgroundEffect.FakeMeteor.Meteor
import dev.zenhao.melon.utils.vector.Vec2f
import org.lwjgl.opengl.Display
import java.awt.Color
import kotlin.random.Random

class Particle(pos: Vec2f, size: Float, velocity: Vec2f, speed: Long, color: Color) : AParticle(pos, size, velocity, speed, color) {
    var alpha = color.alpha.toFloat()

    override fun render() {

    }

    override fun update() {
        pos.x += velocity.x * 7 * speed
        pos.y += velocity.y * 7 * speed

        if (alpha < 255F) this.alpha += 0.05f * 10

        if (pos.x > Display.getWidth()) pos.y = 0f
        if (pos.x < 0) pos.x = Display.getWidth().toFloat()

        if (pos.y > Display.getHeight()) pos.y = 0f
        if (pos.y < 0) pos.y = Display.getHeight().toFloat()
    }

    override fun refresh() {
        this.alpha = 0F
    }

    override fun generate(): AParticle {
        val pos = Vec2f(Random.nextInt(Display.getWidth()).toFloat(), Random.nextInt(Display.getHeight()).toFloat())
        val size = (Math.random() * 2.0f).toFloat() + 1.0f
        val velocity = Vec2f((Math.random() * 2.0f - 1.0f).toFloat(), (Math.random() * 2.0f - 1.0f).toFloat())
        return Particle(pos, size, velocity, 20L, Color.WHITE)
    }

    companion object {
        @JvmField
        val GET: Particle

        init {
            val pos = Vec2f(Random.nextInt(Display.getWidth()).toFloat(), Random.nextInt(Display.getHeight()).toFloat())
            val size = (Math.random() * 2.0f).toFloat() + 1.0f
            val velocity = Vec2f((Math.random() * 2.0f - 1.0f).toFloat(), (Math.random() * 2.0f - 1.0f).toFloat())
            GET = Particle(pos, size, velocity, 20L, Color.WHITE)
        }
    }
}