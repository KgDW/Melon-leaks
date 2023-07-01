// 
// Decompiled by Procyon v0.5.36
// 

package dev.zenhao.melon.module.modules.misc;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Info(name = "XCarry", category = Category.MISC)
public class XCarry extends Module {
    private static XCarry INSTANCE = new XCarry();
    private final Setting<Boolean> ForceCancel = bsetting("ForceCancel", false);

    public XCarry() {
        this.setInstance();
    }

    public static XCarry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new XCarry();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        if (mc.world != null) {
            mc.player.connection.sendPacket(new CPacketCloseWindow(mc.player.inventoryContainer.windowId));
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketCloseWindow) {
            final CPacketCloseWindow packet = (CPacketCloseWindow) event.getPacket();
            if (packet.windowId == mc.player.inventoryContainer.windowId || ForceCancel.getValue()) {
                event.setCanceled(true);
            }
        }
    }
}
