package dev.zenhao.melon.event.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class Wrapper
{
    final static Minecraft mc = Minecraft.getMinecraft();
    public static Minecraft GetMC()
    {
        return mc;
    }

    public static EntityPlayerSP GetPlayer()
    {
        return mc.player;
    }
}
