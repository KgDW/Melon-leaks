package dev.zenhao.melon.utils.player

import net.minecraft.client.Minecraft

object MovementUtil {
    var mc = Minecraft.getMinecraft()
    @JvmStatic
    fun motionJump() {
        if (!mc.player.collidedVertically) {
            if (mc.player.motionY == -0.07190068807140403) {
                mc.player.motionY *= 0.3499999940395355
            } else if (mc.player.motionY == -0.10306193759436909) {
                mc.player.motionY *= 0.550000011920929
            } else if (mc.player.motionY == -0.13395038817442878) {
                mc.player.motionY *= 0.6700000166893005
            } else if (mc.player.motionY == -0.16635183030382) {
                mc.player.motionY *= 0.6899999976158142
            } else if (mc.player.motionY == -0.19088711097794803) {
                mc.player.motionY *= 0.7099999785423279
            } else if (mc.player.motionY == -0.21121925191528862) {
                mc.player.motionY *= 0.20000000298023224
            } else if (mc.player.motionY == -0.11979897632390576) {
                mc.player.motionY *= 0.9300000071525574
            } else if (mc.player.motionY == -0.18758479151225355) {
                mc.player.motionY *= 0.7200000286102295
            } else if (mc.player.motionY == -0.21075983825251726) {
                mc.player.motionY *= 0.7599999904632568
            }
            if (mc.player.motionY < -0.2 && mc.player.motionY > -0.24) {
                mc.player.motionY *= 0.7
            }
            if (mc.player.motionY < -0.25 && mc.player.motionY > -0.32) {
                mc.player.motionY *= 0.8
            }
            if (mc.player.motionY < -0.35 && mc.player.motionY > -0.8) {
                mc.player.motionY *= 0.98
            }
            if (mc.player.motionY < -0.8 && mc.player.motionY > -1.6) {
                mc.player.motionY *= 0.99
            }
        }
    }
}