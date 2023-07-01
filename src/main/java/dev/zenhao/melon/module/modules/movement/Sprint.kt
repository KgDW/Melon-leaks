package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.BooleanSetting
import dev.zenhao.melon.utils.entity.EntityUtil

/**
 * Created by 086 on 23/08/2017.
 * Updated by S-B99 on 06/03/20
 */
@Module.Info(name = "Sprint", description = "Automatically makes the player sprint", category = Category.MOVEMENT)
class Sprint : Module() {
    var Legit: BooleanSetting = bsetting("Legit", false)
    override fun onUpdate() {
        if (fullNullCheck() || mc.player.isElytraFlying || mc.player.capabilities.isFlying) {
            return
        }
        if (Legit.value) {
            try {
                mc.player.isSprinting = !mc.player.collidedHorizontally && mc.player.moveForward > 0
            } catch (ignored: Exception) {
            }
        } else {
            if (EntityUtil.isMoving()) {
                mc.player.isSprinting = true
            }
        }
    }
}