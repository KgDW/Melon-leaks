package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.utils.block.BlockUtil.getNeighbor
import melon.utils.concurrent.threads.runSafe
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Module.Info(name = "AirPlace", category = Category.MISC)
object AirPlace : Module() {

    @SubscribeEvent
    fun onUseItem(event: PlayerInteractEvent.RightClickItem) {
        if (fullNullCheck()) {
            return
        }
        runSafe {
            val hitResult = player.rayTrace(4.0, 0F)

            if (player.heldItemMainhand.getItem() !is ItemBlock || hitResult == null) return
            player.connection.sendPacket(
                CPacketPlayerTryUseItemOnBlock(
                    hitResult.blockPos,
                    EnumFacing.DOWN,
                    EnumHand.MAIN_HAND,
                    0.5f,
                    1.0f,
                    0.5f
                )
            )
        }
    }

}