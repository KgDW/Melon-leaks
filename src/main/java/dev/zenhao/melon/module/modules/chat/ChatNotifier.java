package dev.zenhao.melon.module.modules.chat;

import com.mojang.realmsclient.gui.ChatFormatting;
import dev.zenhao.melon.manager.FriendManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.BooleanSetting;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

@Module.Info(name = "ChatNotifier", category = Category.CHAT)
public class ChatNotifier extends Module {
    public BooleanSetting friend = bsetting("Friend", true);
    public BooleanSetting playSound = bsetting("PlaySound", true);

    @SubscribeEvent
    public void onReceiveChat(ClientChatReceivedEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (friend.getValue()) {
            if (FriendManager.getFriendStringList() != null) {
                FriendManager.getFriendStringList().forEach(p -> {
                    if (p != null && event.getMessage().getUnformattedText().contains(p)) {
                        event.setMessage(new TextComponentString(event.getMessage().getUnformattedText().replace(p, ChatFormatting.AQUA + "" + ChatFormatting.BOLD + p + ChatFormatting.WHITE)));
                        if (playSound.getValue()) {
                            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.1f, 0.5f);
                        }
                    }
                });
            }
        }
        if (event.getMessage().getUnformattedText().contains(mc.player.getName())) {
            event.setMessage(new TextComponentString(event.getMessage().getUnformattedText().replace(mc.player.getName(), ChatFormatting.RED + "" + ChatFormatting.BOLD + mc.player.getName() + ChatFormatting.WHITE)));
            if (!event.getMessage().getUnformattedText().contains("<" + mc.player.getName() + ">")) {
                if (playSound.getValue()) {
                    mc.player.playSound(SoundEvents.BLOCK_NOTE_BASS, 0.1f, 0.5f);
                }
            }
        }
    }
}
