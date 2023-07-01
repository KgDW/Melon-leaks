package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import melon.events.PlayerMoveEvent
import melon.system.event.safeEventListener

@Module.Info(name = "FastSwim", category = Category.MOVEMENT)
class FastSwim : Module() {
    var waterHorizontal: Setting<Double> = dsetting("WaterHorizontal", 3.0, 1.0, 20.0)
    var waterVertical: Setting<Double> = dsetting("WaterVertical", 3.0, 1.0, 20.0)
    var lavaHorizontal: Setting<Double> = dsetting("LavaHorizontal", 3.0, 1.0, 20.0)
    var lavaVertical: Setting<Double> = dsetting("LavaVertical", 3.0, 1.0, 20.0)

    init {
        safeEventListener<PlayerMoveEvent> { event ->
            if (player.isInLava && !player.onGround) {
                event.x = event.x * lavaHorizontal.value
                event.z = event.z * lavaHorizontal.value
                event.y = event.y * lavaVertical.value
            } else if (player.isInWater && !player.onGround) {
                event.x = event.x * waterHorizontal.value
                event.z = event.z * waterHorizontal.value
                event.y = event.y * waterVertical.value
            }
        }
    }
}