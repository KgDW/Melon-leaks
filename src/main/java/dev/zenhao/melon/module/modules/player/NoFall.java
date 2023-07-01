package dev.zenhao.melon.module.modules.player;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.movement.ElytraPlus;
import dev.zenhao.melon.setting.ModeSetting;
import dev.zenhao.melon.utils.block.BlockUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by 086 on 19/11/2017.
 * Updated by S-B99 on 05/03/20
 */
@Module.Info(category = Category.PLAYER, description = "Prevents fall damage", name = "NoFall")
public class NoFall extends Module {

    public ModeSetting<?> fallMode = msetting("Mode", FallMode.PACKET);
    double fall;

    @SubscribeEvent
    public void send(PacketEvent.Send event) {
        if (fullNullCheck()) {
            return;
        }
        if (fallMode.getValue().equals(FallMode.GROUND)) {
            if (event.getPacket() instanceof CPacketPlayer) {
                ((CPacketPlayer) event.getPacket()).onGround = true;
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() || mc.player.isElytraFlying() || ModuleManager.getModuleByClass(ElytraPlus.class).isEnabled()) {
            return;
        }
        if (fallMode.getValue().equals(FallMode.PACKET)) {
            if (!BlockUtil.isReallyOnGround()) {
                if (mc.player.motionY < -0.08) {
                    fall -= mc.player.motionY;
                }
                if (fall > 3) {
                    mc.player.connection.sendPacket(new CPacketPlayer(false));
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    mc.player.connection.sendPacket(new CPacketPlayer(true));
                    fall = 0;
                }
            } else fall = 0;
        }
    }

    public enum FallMode {
        GROUND, PACKET
    }
}
