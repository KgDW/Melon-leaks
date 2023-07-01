package dev.zenhao.melon.event.events.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class WorldBlockEvent extends Event {
    private final BlockPos pos;
    private final IBlockState oldState;
    private final IBlockState newState;

    public WorldBlockEvent(BlockPos pos, IBlockState oldState, IBlockState newState) {
        this.pos = pos;
        this.oldState = oldState;
        this.newState = newState;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public IBlockState getOldState() {
        return this.oldState;
    }

    public IBlockState getNewState() {
        return this.newState;
    }
}
