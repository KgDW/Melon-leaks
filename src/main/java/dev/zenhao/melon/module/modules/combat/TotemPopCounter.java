package dev.zenhao.melon.module.modules.combat;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.gui.Notification;
import dev.zenhao.melon.manager.FriendManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.ModeSetting;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.chat.ChatUtil;
import dev.zenhao.melon.utils.chat.ColourTextFormatting;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

import static dev.zenhao.melon.utils.chat.ColourTextFormatting.toTextMap;

@Module.Info(name = "TotemPopCounter", description = "Counts how many times players pop", category = Category.COMBAT)
public class TotemPopCounter extends Module {
    private final Setting<?> announceSetting = msetting("Announce", Announce.CLIENT);
    private final ModeSetting<?> mode = msetting("Mode", Mode.Notification);
    private final Setting<Boolean> countFriends = bsetting("CountFriends", true);
    private final Setting<Boolean> countSelf = bsetting("CountSelf", true);
    private final Setting<?> colourCode = msetting("ColorName", ColourTextFormatting.ColourCode.AQUA);
    private final Setting<?> colourCode1 = msetting("ColorNumber", ColourTextFormatting.ColourCode.AQUA);
    private HashMap<String, Integer> playerList = new HashMap<>();
    private boolean isDead = false;

    @SubscribeEvent
    public void useTotem(NMSL event) {
        if (fullNullCheck()) {
            return;
        }
        if (playerList == null) {
            playerList = new HashMap<>();
        }
        if (playerList.get(event.getEntity().getName()) == null) {
            playerList.put(event.getEntity().getName(), 1);
            sendMessage(formatName(event.getEntity().getName()) + " popped " + formatNumber(1) + " totem" + "!");
        } else if (!(playerList.get(event.getEntity().getName()) == null)) {
            int popCounter = playerList.get(event.getEntity().getName());
            popCounter += 1;
            playerList.put(event.getEntity().getName(), popCounter);
            sendMessage(formatName(event.getEntity().getName()) + " popped " + formatNumber(popCounter) + " totems" + "!");
        }
    }

    @SubscribeEvent
    public void popListener(PacketEvent.Receive event) {
        if (mc.player == null) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 35) {
                Entity entity = packet.getEntity(mc.world);
                if (friendCheck(entity.getName()) || selfCheck(entity.getName())) {
                    MinecraftForge.EVENT_BUS.post(new NMSL(entity));
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (!isDead && 0 >= mc.player.getHealth()) {
            sendMessage(formatName(mc.player.getName()) + " died and " + grammar(mc.player.getName()) + " pop list was reset!");
            isDead = true;
            playerList.clear();
            return;
        }
        if (isDead && 0 < mc.player.getHealth()) {
            isDead = false;
        }

        mc.world.playerEntities.forEach(player -> {
            if (0 >= player.getHealth() && friendCheck(player.getName()) && selfCheck(player.getName()) && playerList.containsKey(player.getName())) {
                sendMessage(formatName(player.getName()) + " died after popping " + formatNumber(playerList.get(player.getName())) + " totems" + "!");
                playerList.remove(player.getName(), playerList.get(player.getName()));
            }

        });
    }

    private boolean friendCheck(String name) {
        if (isDead) {
            return false;
        }
        if (FriendManager.isFriend(name)) {
            return countFriends.getValue();
        }
        return true;
    }

    private boolean selfCheck(String name) {
        if (isDead) {
            return false;
        }
        if (countSelf.getValue() && name.equalsIgnoreCase(mc.player.getName())) {
            return true;
        } else return countSelf.getValue() || !name.equalsIgnoreCase(mc.player.getName());
    }

    private boolean isSelf(String name) {
        return name.equalsIgnoreCase(mc.player.getName());
    }

    private String formatName(String name) {
        String extraText = "";
        if (FriendManager.isFriend(name) && !isPublic()) {
            extraText = "Your friend, ";
        } else if (FriendManager.isFriend(name) && isPublic()) {
            extraText = "My friend, ";
        }
        if (isSelf(name)) {
            extraText = "";
            name = "I";
        }

        if (announceSetting.getValue().equals(Announce.EVERYONE)) {
            return extraText + name;
        }
        return extraText + setToText((ColourTextFormatting.ColourCode) colourCode.getValue()) + name + TextFormatting.RESET;
    }

    private String grammar(String name) {
        if (isSelf(name)) {
            return "my";
        } else {
            return "their";
        }
    }

    private boolean isPublic() {
        return announceSetting.getValue().equals(Announce.EVERYONE);
    }

    private String formatNumber(int message) {
        if (announceSetting.getValue().equals(Announce.EVERYONE)) {
            return "" + message;
        }
        return setToText((ColourTextFormatting.ColourCode) colourCode1.getValue()) + "" + message + TextFormatting.RESET;
    }

    private void sendMessage(String message) {
        switch ((Announce) announceSetting.getValue()) {
            case CLIENT:
                switch ((Mode) mode.getValue()) {
                    case Chat: {
                        ChatUtil.sendMessage(message);
                        break;
                    }
                    case Notification: {
                        ChatUtil.sendClientMessage(message, Notification.Type.WARNING);
                        break;
                    }
                    case Duo: {
                        ChatUtil.sendMessage(message);
                        ChatUtil.sendClientMessage(message, Notification.Type.WARNING);
                        break;
                    }
                }
                return;
            case EVERYONE:
                ChatUtil.sendServerMessage(message);
                return;
            default:
        }
        ChatUtil.sendClientMessage(message, Notification.Type.INFO);
    }

    private TextFormatting setToText(ColourTextFormatting.ColourCode colourCode) {
        return toTextMap.get(colourCode);
    }

    private enum Announce {CLIENT, EVERYONE}

    private enum Mode {Chat, Notification , Duo}

    public static class NMSL extends Event {
        public Entity entity;

        public NMSL(Entity entity) {
            super();
            this.entity = entity;
        }

        public Entity getEntity() {
            return entity;
        }
    }
}