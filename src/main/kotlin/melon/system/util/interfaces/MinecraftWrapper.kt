package melon.system.util.interfaces

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient

interface MinecraftWrapper {
    val mc: Minecraft get() = Companion.mc

    val minecraft: Minecraft get() = Companion.mc

    val player: EntityPlayerSP? get() = Companion.mc.player

    val world: WorldClient? get() = Companion.mc.world

    companion object {
        val mc: Minecraft get() = Minecraft.getMinecraft()

        val minecraft: Minecraft get() = mc

        val player: EntityPlayerSP? get() = mc.player

        val world: WorldClient? get() = mc.world
    }
}