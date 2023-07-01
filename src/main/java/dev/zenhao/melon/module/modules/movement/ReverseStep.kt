package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager.getModuleByClass
import dev.zenhao.melon.module.modules.player.Freecam
import dev.zenhao.melon.setting.Setting

@Module.Info(name = "ReverseStep", category = Category.MOVEMENT, description = "null")
class ReverseStep : Module() {
    private var strictToggle = bsetting("StrictToggle", false)
    private var fallSpeed = bsetting("UseFallSpeed", true)
    private val stepHeight = dsetting("Height", 3.0, 0.5, 3.0).bn(fallSpeed)
    private var fallingSpeed = isetting("FallSpeed", 3, 1, 10)
    override fun onUpdate() {
        if (fullNullCheck()
            || mc.player.isInWater
            || mc.player.isInLava
            || mc.player.isOnLadder
            || mc.gameSettings.keyBindJump.isKeyDown
            || getModuleByClass(Freecam::class.java).isEnabled
            || strictToggle.value
        ) {
            return
        }
        if (mc.player != null && mc.player.onGround && !mc.player.isInWater && !mc.player.isOnLadder) {
            if (fallSpeed.value) {
                mc.player.motionY -= fallingSpeed.value.toDouble()
            } else {
                run {
                    var y = 0.0
                    while (y < this.stepHeight.value + 0.5) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, -y, 0.0))
                                .isEmpty()
                        ) {
                            mc.player.motionY = -15.0
                            break
                        }
                        y += 0.01
                    }
                }
            }
        }
    }
}