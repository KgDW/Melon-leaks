package melon.events

import melon.system.event.*
import net.minecraft.client.Minecraft
import net.minecraft.entity.MoverType
import kotlin.math.cos
import kotlin.math.sin

class PlayerMoveEvent(var type: MoverType, var x: Double, var y: Double, var z: Double) : Event,
    ICancellable by Cancellable(), IEventPosting by Companion {
    fun setSpeed(speed: Double) {
        var yaw = Minecraft.getMinecraft().player.rotationYaw
        var forward = Minecraft.getMinecraft().player.movementInput.moveForward.toDouble()
        var strafe = Minecraft.getMinecraft().player.movementInput.moveStrafe.toDouble()
        if (forward == 0.0 && strafe == 0.0) {
            x = 0.0
            z = 0.0
        } else {
            if (forward != 0.0) {
                if (strafe > 0) {
                    yaw += (if (forward > 0) -45 else 45).toFloat()
                } else if (strafe < 0) {
                    yaw += (if (forward > 0) 45 else -45).toFloat()
                }
                strafe = 0.0
                forward = if (forward > 0) {
                    1.0
                } else {
                    -1.0
                }
            }
            val cos = cos(Math.toRadians((yaw + 90).toDouble()))
            val sin = sin(Math.toRadians((yaw + 90).toDouble()))
            x = (forward * speed * cos + strafe * speed * sin)
            z = (forward * speed * sin - strafe * speed * cos)
        }
    }

    companion object : NamedProfilerEventBus("melonPlayerMove")
}