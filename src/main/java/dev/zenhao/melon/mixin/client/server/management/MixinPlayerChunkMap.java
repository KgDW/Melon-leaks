package dev.zenhao.melon.mixin.client.server.management;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;

@Mixin(PlayerChunkMap.class)
public class MixinPlayerChunkMap {
    @Shadow
    @Final
    public final List<PlayerChunkMapEntry> entries = Lists.newArrayList();

    /**
     * @author zenhao
     * @reason fuck
     */
    @Overwrite
    public Iterator<Chunk> getChunkIterator() {
        synchronized (entries) {
            final Iterator<PlayerChunkMapEntry> lvt_1_1_ = this.entries.iterator();
            return new AbstractIterator<Chunk>() {
                protected Chunk computeNext() {
                    while (true) {
                        if (lvt_1_1_.hasNext()) {
                            PlayerChunkMapEntry lvt_1_1_x = lvt_1_1_.next();
                            Chunk lvt_2_1_ = lvt_1_1_x.getChunk();
                            if (lvt_2_1_ == null) {
                                continue;
                            }

                            if (!lvt_2_1_.isLightPopulated() && lvt_2_1_.isTerrainPopulated()) {
                                return lvt_2_1_;
                            }

                            if (!lvt_2_1_.wasTicked()) {
                                return lvt_2_1_;
                            }

                            if (!lvt_1_1_x.hasPlayerMatchingInRange(128.0, PlayerChunkMap.NOT_SPECTATOR)) {
                                continue;
                            }

                            return lvt_2_1_;
                        }

                        return this.endOfData();
                    }
                }
            };
        }
    }
}
