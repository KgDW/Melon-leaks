package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import melon.system.event.safeEventListener

@Module.Info(name = "AntiAim", category = Category.MISC)
class AntiAim : Module() {
    var yawMode = msetting("YawMode", YawMode.Spin)
    var pitchMode = msetting("PitchMode", PitchMode.Down)
    var spinSpeed: Setting<Float> = fsetting("SpinSpeed", 10f, 1f, 100f)
    var CustomPitch: Setting<Int> = isetting("CustomPitch", 90, 0, 360).m(pitchMode, PitchMode.Custom)
    private var rotation = 0f
    private var rotation2 = 0f
    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        rotation = mc.player.rotationYaw
        rotation2 = mc.player.rotationPitch
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        rotation = mc.player.rotationYaw
        rotation2 = mc.player.rotationPitch
    }


    init {
        safeEventListener<PlayerMotionEvent> {
            val derpRotations =
                floatArrayOf(
                    rotation + (Math.random() * 360.0 - 180.0).toFloat(),
                    (Math.random() * 180.0 - 90.0).toFloat()
                )
            when (yawMode.value) {
                YawMode.Off -> {}
                YawMode.Spin -> {
                    rotation += spinSpeed.value
                    if (rotation > 360.0f) {
                        rotation = 0.0f
                    }
                }

                YawMode.Jitter -> {
                    rotation = (if (mc.player.ticksExisted % 2 == 0) 90 else -90).toFloat()
                }

                YawMode.Bruh -> {
                    rotation = derpRotations[0]
                }

                else -> {}
            }
            when (pitchMode.value) {
                PitchMode.Off -> {}
                PitchMode.Up -> {
                    rotation2 = -90.0f
                }

                PitchMode.Down -> {
                    rotation2 = 90.0f
                }

                PitchMode.Jitter -> {
                    rotation2 += 30.0f
                    if (rotation2 > 90.0f) {
                        rotation2 = -90.0f
                        return@safeEventListener
                    }
                    if (rotation2 >= -90.0f) {
                        rotation2 = 90.0f
                    }
                }

                PitchMode.Headless -> {
                    rotation2 = 180.0f
                }

                PitchMode.Bruh -> {
                    rotation2 = derpRotations[1]
                }

                PitchMode.Custom -> {
                    rotation2 = CustomPitch.value.toFloat()
                }

                else -> {}
            }
            it.setRotation(rotation, rotation2)
        }
    }

    private enum class YawMode {
        Off, Spin, Jitter, Bruh
    }

    private enum class PitchMode {
        Off, Up, Down, Jitter, Headless, Bruh, Custom
    }
}