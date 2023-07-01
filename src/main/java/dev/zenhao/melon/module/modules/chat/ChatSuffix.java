package dev.zenhao.melon.module.modules.chat;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Info(name = "ChatSuffix", category = Category.CHAT, description = "Custom Chat Suffix")
public class ChatSuffix extends Module {

    public static String CHAT_SUFFIX = " \u1D39\u1D49\u02E1\u00BA\u207F";
    public Setting<Boolean> commands = bsetting("Command", false);

    @SubscribeEvent
    public void NMSL(PacketEvent.Send event) {
        if (event.getStage() == 0) {
            if (event.getPacket() instanceof CPacketChatMessage) {
                String s = ((CPacketChatMessage) event.getPacket()).getMessage();
                if (s.startsWith("/") && !commands.getValue()) return;
                s += CHAT_SUFFIX;
                if (s.length() >= 256) s = s.substring(0, 256);
                ((CPacketChatMessage) event.getPacket()).message = s;
            }
        }
    }

}
