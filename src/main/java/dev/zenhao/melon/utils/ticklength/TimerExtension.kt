package dev.zenhao.melon.utils.ticklength

import melon.utils.Wrapper
import net.minecraft.util.Timer

fun Timer.setTimer(timer: Float) {
    tickLength = 50f / timer
}