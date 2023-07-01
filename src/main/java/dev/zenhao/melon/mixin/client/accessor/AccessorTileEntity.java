package dev.zenhao.melon.mixin.client.accessor;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TileEntity.class)
public interface AccessorTileEntity {
    @Accessor("pos")
    BlockPos getPos();
}
