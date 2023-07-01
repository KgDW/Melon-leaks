package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.IModule
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import melon.events.TickEvent
import melon.system.event.safeEventListener
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.CPacketHeldItemChange

@Module.Info(name = "PacketEat", category = Category.MISC, description = "PacketEat")
class PacketEat : Module() {
    var fastEat = bsetting("FastEat", false)
    var timerDelay = TimerUtils()

    init {
        safeEventListener<TickEvent.Pre> {
            if (fastEat.value && player.isHandActive) {
                val usingItem = player.getActiveItemStack().getItem()
                if (usingItem is ItemFood || usingItem is ItemBucketMilk
                    || usingItem is ItemPotion
                ) {
                    if (player.itemInUseMaxCount >= 1) {
                        connection.sendPacket(CPacketHeldItemChange(player.inventory.currentItem))
                    }
                }
                if (!player.isHandActive) {
                    timerDelay.reset()
                }
            }
        }
    }
}