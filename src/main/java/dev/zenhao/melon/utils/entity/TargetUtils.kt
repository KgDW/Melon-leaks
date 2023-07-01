package dev.zenhao.melon.utils.entity

import net.minecraft.client.Minecraft
import net.minecraft.util.math.Vec3d

object TargetUtils {
    fun canAttack(vec: Vec3d, pos: Vec3d): Boolean {
        val flag = Minecraft.getMinecraft().world.rayTraceBlocks(vec, pos, false, true, false) == null
        var d0 = 36.0
        if (!flag) {
            d0 = 9.0
        }
        return vec.squareDistanceTo(pos) < d0
    }
}