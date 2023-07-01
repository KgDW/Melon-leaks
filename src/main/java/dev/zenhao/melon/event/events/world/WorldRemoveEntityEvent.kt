package dev.zenhao.melon.event.events.world

import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class WorldRemoveEntityEvent(val entity: Entity) : Event()