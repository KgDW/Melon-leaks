package dev.zenhao.melon.mixin.client;

import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.movement.NoSlowDown;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by 086 on 16/12/2017.
 */
@Mixin(BlockSoulSand.class)
public class MixinBlockSoulSand {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo info) {
        if (ModuleManager.getModuleByClass(NoSlowDown.class).isEnabled() && NoSlowDown.INSTANCE.getSoulSand().getValue()) {
            info.cancel();
        }
    }

}
