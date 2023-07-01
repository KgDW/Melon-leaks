package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import melon.utils.inventory.slot.currentHotbarSlot
import melon.utils.inventory.slot.firstBlock
import melon.utils.inventory.slot.hotbarSlots
import net.minecraft.entity.passive.AbstractChestHorse
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketUseEntity

@Module.Info(name = "MountBypass", category = Category.MISC)
object MountBypass : Module() {
    init {
        safeEventListener<PacketEvents.Send> { event ->
            val slot = player.hotbarSlots.firstBlock(Blocks.CHEST)?: return@safeEventListener
            if (event.packet is CPacketUseEntity
                && event.packet.getAction() == CPacketUseEntity.Action.INTERACT_AT
                && event.packet.getEntityFromWorld(world) is AbstractChestHorse
                && player.currentHotbarSlot == slot
            ) {
                event.cancel()
            }
        }
    }
}