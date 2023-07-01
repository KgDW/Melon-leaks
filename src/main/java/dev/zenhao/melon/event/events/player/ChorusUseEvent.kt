package dev.zenhao.melon.event.events.player

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
open class ChorusUseEvent(var pos: BlockPos, var player: EntityPlayer, var chat: Boolean) : Event()