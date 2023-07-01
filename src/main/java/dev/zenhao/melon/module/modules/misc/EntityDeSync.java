package dev.zenhao.melon.module.modules.misc;

import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.event.events.entity.EventPlayerUpdate;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.utils.chat.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Info(name = "EntityDeSync", category = Category.MISC)
public class EntityDeSync
        extends Module {
    static EntityDeSync INSTANCE;
    private Entity Riding = null;

    @Override
    public void onInit() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            this.Riding = null;
            this.toggle();
            return;
        }
        if (!mc.player.isRiding()) {
            ChatUtil.NoSpam.sendWarnMessage("You are not riding an entity.");
            this.Riding = null;
            this.toggle();
            return;
        }
        ChatUtil.NoSpam.sendMessage("Vanished");
        this.Riding = mc.player.getRidingEntity();
        mc.player.dismountRidingEntity();
        mc.world.removeEntity(this.Riding);
    }

    @Override
    public void onDisable() {
        if (this.Riding != null) {
            this.Riding.isDead = false;
            if (!mc.player.isRiding()) {
                mc.world.spawnEntity(this.Riding);
                mc.player.startRiding(this.Riding, true);
            }
            this.Riding = null;
            ChatUtil.NoSpam.sendMessage("Remounted.");
        }
    }

    @SubscribeEvent
    public void OnPlayerUpdate(EventPlayerUpdate event) {
        if (this.Riding == null) {
            return;
        }
        if (mc.player.isRiding()) {
            return;
        }
        mc.player.onGround = true;
        this.Riding.setPosition(mc.player.posX, mc.player.posY, mc.player.posZ);
        mc.player.connection.sendPacket(new CPacketVehicleMove(this.Riding));
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSetPassengers) {
            if (this.Riding == null) {
                return;
            }
            SPacketSetPassengers l_Packet = (SPacketSetPassengers) event.getPacket();
            Entity en = mc.world.getEntityByID(l_Packet.getEntityId());
            if (en == this.Riding) {
                for (int i : l_Packet.getPassengerIds()) {
                    Entity ent = mc.world.getEntityByID(i);
                    if (ent != mc.player) continue;
                    return;
                }
                ChatUtil.NoSpam.sendMessage("You dismounted");
                this.toggle();
            }
        } else if (event.getPacket() instanceof SPacketDestroyEntities) {
            SPacketDestroyEntities l_Packet = (SPacketDestroyEntities) event.getPacket();
            for (int l_EntityId : l_Packet.getEntityIDs()) {
                if (l_EntityId != this.Riding.getEntityId()) continue;
                ChatUtil.NoSpam.sendErrorMessage("Entity is now null!");
                return;
            }
        }
    }

    @SubscribeEvent
    public void OnWorldEvent(EntityJoinWorldEvent event) {
        if (event.getEntity() == mc.player) {
            ChatUtil.sendMessage("Player " + event.getEntity().getName() + " joined the world!");
        }
    }
}

