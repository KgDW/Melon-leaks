package melon.system.render.background

import dev.zenhao.melon.utils.vector.Vec2f
import java.awt.Color
class ParticleSystem(private val particle: AParticle, amount: Int) {

    val particles = ArrayList<AParticle>()

    init {
        generateParticles(amount)
    }



    fun updateAll() {
        particles.forEach {
            it.update()
        }
    }

    fun renderAll() {
        particles.forEach {
            it.render()
        }
    }

    fun generateParticles(amount: Int) {
        for (i in 0 until amount) {
            particles.add(particle.generate())
        }
    }
}


abstract class AParticle {
    var pos: Vec2f
    var size: Float
    var speed: Long
    var color: Color
    var velocity: Vec2f

    constructor() {
        val generate = this.generate()
        this.pos = generate.pos
        this.size = generate.size
        this.speed = generate.speed
        this.color = generate.color
        this.velocity = generate.velocity
    }

    constructor(pos: Vec2f, size: Float, velocity: Vec2f, speed: Long, color: Color) {
        this.pos = pos
        this.size = size
        this.speed = speed
        this.color = color
        this.velocity = velocity
    }

    abstract fun render()

    abstract fun update()

    abstract fun refresh()

    abstract fun generate(): AParticle
}