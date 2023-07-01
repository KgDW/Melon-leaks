package melon.utils.block

import melon.system.event.SafeClientEvent
import melon.utils.world.getMiningSide
import melon.utils.world.getVisibleSides
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

object BlockUtil {
    fun SafeClientEvent.getStrictFacing(pos: BlockPos): EnumFacing {
        return getMiningSide(pos) ?: EnumFacing.UP
    }

    fun SafeClientEvent.getNeighbor(pos: BlockPos, strictDirection: Boolean): EasyBlock? {
        for (side in EnumFacing.values()) {
            val offsetPos = pos.offset(side)
            val oppositeSide = side.getOpposite()

            if (strictDirection && !getVisibleSides(offsetPos, true).contains(oppositeSide)) continue
            if (world.getBlockState(offsetPos).isReplaceable) continue

            return EasyBlock(offsetPos, oppositeSide ?: EnumFacing.UP)
        }

        return null
    }

    class EasyBlock(var blockPos: BlockPos, var face: EnumFacing)
}