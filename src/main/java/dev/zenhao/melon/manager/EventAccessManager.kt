package dev.zenhao.melon.manager

import dev.zenhao.melon.event.events.player.PlayerMotionEvent

object EventAccessManager {
    var playerMotion: PlayerMotionEvent? = null

    fun getData(): PlayerMotionEvent? {
        if (playerMotion != null) {
            return playerMotion
        }
        return null
    }

    fun setData(e: PlayerMotionEvent) {
        playerMotion = e
    }
}