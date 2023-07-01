package dev.zenhao.melon.event.events.world

import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class WorldAddEntityEvent(val entity: Entity) : Event()