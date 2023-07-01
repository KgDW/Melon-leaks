package dev.zenhao.melon.event.events.tab;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class TabPlayerEvent extends Event {
    private EntityPlayer player;

    public TabPlayerEvent(EntityPlayer player) {
        this.player = player;
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }
}
