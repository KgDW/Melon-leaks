package dev.zenhao.melon.mixin.client;

import com.google.common.base.Predicate;
import dev.zenhao.melon.event.events.entity.PushEvent;
import dev.zenhao.melon.event.events.render.RenderLightEvent;
import dev.zenhao.melon.event.events.world.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = {World.class})
public abstract class MixinWorld {

    @Inject(method = "notifyBlockUpdate", at = @At("HEAD"), cancellable = true)
    public void notifyBlockUpdate(BlockPos p_184138_1_, IBlockState p_184138_2_, IBlockState p_184138_3_, int p_184138_4_, CallbackInfo ci) {
        WorldBlockEvent event = new WorldBlockEvent(p_184138_1_, p_184138_2_, p_184138_3_);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onEntityAdded", at = @At("HEAD"))
    public void onEntityAdded(Entity p_72923_1_, CallbackInfo ci) {
        WorldAddEntityEvent event = new WorldAddEntityEvent(p_72923_1_);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = "onEntityRemoved", at = @At("HEAD"))
    public void onEntityRemoved(Entity p_72923_1_, CallbackInfo ci) {
        WorldRemoveEntityEvent event = new WorldRemoveEntityEvent(p_72923_1_);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = "markBlockRangeForRenderUpdate(IIIIII)V", at = @At("HEAD"))
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2, CallbackInfo ci) {
        WorldRenderUpdateEvent event = new WorldRenderUpdateEvent(x1, y1, z1, x2, y2, z2);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void checkLightForHead(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        RenderLightEvent event = new RenderLightEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled() && lightType == EnumSkyBlock.SKY) {
            ci.setReturnValue(false);
        }
    }

    @Redirect(method = {"getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getEntitiesOfTypeWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"))
    public <T extends Entity> void getEntitiesOfTypeWithinAABBHook(Chunk chunk, Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
        try {
            chunk.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);
        } catch (Exception ignored) {
        }
    }

    //水流推动
    @Redirect(method = {"handleMaterialAcceleration"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isPushedByWater()Z"))
    public boolean isPushedbyWaterHook(Entity entity) {
        PushEvent event = new PushEvent(2, entity);
        MinecraftForge.EVENT_BUS.post(event);
        return entity.isPushedByWater() && !event.isCanceled();
    }
}
