package dev.zenhao.melon.module.modules.chat;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.chat.ChatUtil;
import dev.zenhao.melon.utils.entity.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Module.Info(name = "AutoGG", category = Category.CHAT, description = "Announce killed Players")
public class AutoGG extends Module {
    public static AutoGG INSTANCE = new AutoGG();
    private final Setting<String> text = ssetting("Text", "Hello Nigga ");
    private final Setting<Integer> timeoutTicks = isetting("TimeOutTicks", 20, 1, 50);
    private ConcurrentHashMap<String, Integer> targetedPlayers = null;

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (targetedPlayers == null) {
            targetedPlayers = new ConcurrentHashMap<>();
        }
        EntityLivingBase entity = event.getEntityLiving();
        if (entity == null) {
            return;
        }
        if (!EntityUtil.isPlayer(entity)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (player.getHealth() > 0) {
            return;
        }
        String name = player.getName();
        if (shouldAnnounce(name)) {
            doAnnounce(name);
        }
    }

    @SubscribeEvent
    public void sendListener(PacketEvent.Send event) {

        if (mc.player == null) {
            return;
        }

        if (targetedPlayers == null) {
            targetedPlayers = new ConcurrentHashMap<>();
        }
        if (!(event.getPacket() instanceof CPacketUseEntity)) {
            return;
        }
        CPacketUseEntity cPacketUseEntity = (CPacketUseEntity) event.getPacket();
        if (!(cPacketUseEntity.getAction().equals(CPacketUseEntity.Action.ATTACK))) {
            return;
        }
        Entity targetEntity = cPacketUseEntity.getEntityFromWorld(mc.world);
        if (!EntityUtil.isPlayer(targetEntity)) {
            return;
        }
        addTargetedPlayer(targetEntity.getName());
    }

    @Override
    public void onEnable() {
        targetedPlayers = new ConcurrentHashMap<>();
    }

    @Override
    public void onDisable() {
        targetedPlayers = null;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (targetedPlayers == null) {
            targetedPlayers = new ConcurrentHashMap<>();
        }
        for (Entity entity : new ArrayList<>(mc.world.getLoadedEntityList())) {
            // skip non player entities
            if (!EntityUtil.isPlayer(entity)) {
                continue;
            }
            EntityPlayer player = (EntityPlayer) entity;
            // skip if player is alive
            if (player.getHealth() > 0) {
                continue;
            }
            String name = player.getName();
            if (shouldAnnounce(name)) {
                doAnnounce(name);
                break;
            }
        }
        targetedPlayers.forEach((name, timeout) -> {
            if (timeout <= 0) {
                targetedPlayers.remove(name);
            } else {
                targetedPlayers.put(name, timeout - 1);
            }
        });
    }

    private boolean shouldAnnounce(String name) {
        return targetedPlayers.containsKey(name);
    }

    private void doAnnounce(String name) {
        String message = "";
        if (text.getValue().contains("!Player")) {
            message = text.getValue().replace("!Player", name);
        } else {
            message = text.getValue() + name + "!";
        }
        String messageSanitized = message.replaceAll(ChatUtil.SECTIONSIGN, "");
        if (messageSanitized.length() > 255) {
            messageSanitized = messageSanitized.substring(0, 255);
        }
        mc.player.connection.sendPacket(new CPacketChatMessage(messageSanitized));
        targetedPlayers.remove(name);
    }

    public void addTargetedPlayer(String name) {
        if (Objects.equals(name, mc.player.getName())) {
            return;
        }
        if (targetedPlayers == null) {
            targetedPlayers = new ConcurrentHashMap<>();
        }
        targetedPlayers.put(name, timeoutTicks.getValue());
    }
}