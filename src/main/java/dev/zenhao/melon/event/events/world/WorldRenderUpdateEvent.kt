package dev.zenhao.melon.event.events.world

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class WorldRenderUpdateEvent(
    val x1: Int,
    val y1: Int,
    val z1: Int,
    val x2: Int,
    val y2: Int,
    val z2: Int
) : Event()