package dev.zenhao.melon.mixin.client.accessor;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChunkProviderClient.class)
public interface AccessorChunkProviderClient {
    @Accessor(value = "loadedChunks")
    Long2ObjectMap<Chunk> getLoadedChunks();
}