package dev.zenhao.melon.mixin.client.entity;

import com.google.common.collect.Sets;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;

import java.util.Set;

@Mixin(EntityTracker.class)
public class MixinEntityTracker {
    @Final
    @Shadow
    private static Logger LOGGER = LogManager.getLogger();
    @Final
    @Shadow
    private final IntHashMap<EntityTrackerEntry> trackedEntityHashTable = new IntHashMap<>();
    @Mutable
    @Final
    @Shadow
    private final WorldServer world;
    @Shadow
    private int maxTrackingDistanceThreshold;
    @Final
    @Shadow
    private Set<EntityTrackerEntry> entries = Sets.newHashSet();

    public MixinEntityTracker(WorldServer p_i1516_1_) {
        this.world = p_i1516_1_;
        this.maxTrackingDistanceThreshold = p_i1516_1_.getMinecraftServer().getPlayerList().getEntityViewDistance();
    }

    /**
     * @author zenhao
     * @reason fuck MOJANG
     */
    @Overwrite
    public void track(Entity p_72785_1_, int p_72785_2_, final int p_72785_3_, boolean p_72785_4_) {
        try {
            synchronized (trackedEntityHashTable) {
                if (this.trackedEntityHashTable.containsItem(p_72785_1_.getEntityId())) {
                    throw new IllegalStateException("Entity is already tracked!");
                }
            }

            EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(p_72785_1_, p_72785_2_, this.maxTrackingDistanceThreshold, p_72785_3_, p_72785_4_);
            this.entries.add(entitytrackerentry);
            synchronized (trackedEntityHashTable) {
                this.trackedEntityHashTable.addKey(p_72785_1_.getEntityId(), entitytrackerentry);
            }
            entitytrackerentry.updatePlayerEntities(this.world.playerEntities);
        } catch (Throwable var10) {
            CrashReport crashreport = CrashReport.makeCrashReport(var10, "Adding entity to track");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity To Track");
            crashreportcategory.addCrashSection("Tracking range", p_72785_2_ + " blocks");
            crashreportcategory.addDetail("Update interval", () -> {
                String s = "Once per " + p_72785_3_ + " ticks";
                if (p_72785_3_ == Integer.MAX_VALUE) {
                    s = "Maximum (" + s + ")";
                }

                return s;
            });
            p_72785_1_.addEntityCrashInfo(crashreportcategory);
            this.trackedEntityHashTable.lookup(p_72785_1_.getEntityId()).getTrackedEntity().addEntityCrashInfo(crashreport.makeCategory("Entity That Is Already Tracked"));

            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException var9) {
                LOGGER.error("\"Silently\" catching entity tracking error.", var9);
            }
        }
    }

    /**
     * @author zenhao
     * @reason fuck MOJANG
     */
    @Overwrite
    public void untrack(Entity p_72790_1_) {
        if (p_72790_1_ instanceof EntityPlayerMP) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) p_72790_1_;
            for (EntityTrackerEntry entitytrackerentry : this.entries) {
                entitytrackerentry.removeFromTrackedPlayers(entityplayermp);
            }
        }
        synchronized (trackedEntityHashTable) {
            EntityTrackerEntry entitytrackerentry1 = this.trackedEntityHashTable.removeObject(p_72790_1_.getEntityId());
            if (entitytrackerentry1 != null) {
                this.entries.remove(entitytrackerentry1);
                entitytrackerentry1.sendDestroyEntityPacketToTrackedPlayers();
            }
        }
    }

    /**
     * @author zenhao
     * @reason fuck MOJANG
     */
    @Overwrite
    public void sendToTracking(Entity p_151247_1_, Packet<?> p_151247_2_) {
        synchronized (trackedEntityHashTable) {
            EntityTrackerEntry entitytrackerentry = this.trackedEntityHashTable.lookup(p_151247_1_.getEntityId());
            if (entitytrackerentry != null) {
                entitytrackerentry.sendPacketToTrackedPlayers(p_151247_2_);
            }
        }
    }

    /**
     * @author zenhao
     * @reason fuck MOJANG
     */
    @Overwrite
    public void sendToTrackingAndSelf(Entity p_151248_1_, Packet<?> p_151248_2_) {
        synchronized (trackedEntityHashTable) {
            EntityTrackerEntry entitytrackerentry = this.trackedEntityHashTable.lookup(p_151248_1_.getEntityId());
            if (entitytrackerentry != null) {
                entitytrackerentry.sendToTrackingAndSelf(p_151248_2_);
            }
        }
    }
}
