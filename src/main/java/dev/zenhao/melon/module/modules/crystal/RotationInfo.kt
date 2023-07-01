package dev.zenhao.melon.module.modules.crystal

import dev.zenhao.melon.utils.vector.Vec2f

class RotationInfo(var rotation: Vec2f) {
    fun update(rotation: Vec2f) {
        this.rotation = rotation
    }

    fun reset() {
        this.rotation = Vec2f.ZERO
    }
}