package dev.zenhao.melon.mixin.client.render;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.zenhao.melon.event.events.block.BlockBreakEvent;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.render.BreakESP;
import melon.events.render.RenderEntityEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by 086 on 11/04/2018.
 */
@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Final
    @Shadow
    public final Map<Integer, DestroyBlockProgress> damagedBlocks = Maps.newHashMap();
    @Final
    @Shadow
    public final Set<BlockPos> setLightUpdates = Sets.newHashSet();
    @Final
    @Shadow
    public
    Minecraft mc;
    @Shadow
    public int cloudTickCounter;
    @Shadow
    public ChunkRenderDispatcher renderDispatcher;

    @Shadow
    public final Set<RenderChunk> chunksToUpdate = Sets.newLinkedHashSet();

    @Shadow
    public void markBlocksForUpdate(int p_184385_1_, int p_184385_2_, int p_184385_3_, int p_184385_4_, int p_184385_5_, int p_184385_6_, boolean p_184385_7_) {
    }

    @Shadow
    public void cleanupDamagedBlocks(Iterator<DestroyBlockProgress> p_174965_1_) {
    }

    @Inject(method = "renderEntities", at = @At("HEAD"))
    public void renderEntitiesHead(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(true);
    }

    @Inject(method = "renderEntities", at = @At("RETURN"))
    public void renderEntitiesReturn(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        RenderEntityEvent.setRenderingEntities(false);
    }

    @Inject(method = "drawBlockDamageTexture", at = @At("HEAD"), cancellable = true)
    public void drawBlockDamageTexture(Tessellator tessellatorIn, BufferBuilder bufferBuilderIn, Entity entityIn, float partialTicks, CallbackInfo callbackInfo) {
        if (ModuleManager.getModuleByClass(BreakESP.class).isEnabled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "sendBlockBreakProgress", at = @At("HEAD"))
    public void onSendingBlockBreakProgressPre(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        new BlockBreakEvent(breakerId, pos, progress).post();
    }

    /**
     * @author zenhao
     * @reason Fuck
     */
    @Overwrite
    public void updateClouds() {
        synchronized (damagedBlocks) {
            synchronized (setLightUpdates) {
                synchronized (chunksToUpdate) {
                    ++this.cloudTickCounter;
                    if (this.cloudTickCounter % 20 == 0) {
                        this.cleanupDamagedBlocks(this.damagedBlocks.values().iterator());
                    }

                    if (!this.setLightUpdates.isEmpty() && !this.renderDispatcher.hasNoFreeRenderBuilders() && this.chunksToUpdate.isEmpty()) {
                        Iterator<BlockPos> iterator = this.setLightUpdates.iterator();

                        while (iterator.hasNext()) {
                            BlockPos blockpos = iterator.next();
                            iterator.remove();
                            int k1 = blockpos.getX();
                            int l1 = blockpos.getY();
                            int i2 = blockpos.getZ();
                            this.markBlocksForUpdate(k1 - 1, l1 - 1, i2 - 1, k1 + 1, l1 + 1, i2 + 1, false);
                        }
                    }
                }
            }
        }
    }
}
