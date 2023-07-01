package melon.utils.extension

import dev.zenhao.melon.manager.CrystalManager
import melon.system.event.SafeClientEvent
import melon.utils.block.BlockUtil
import melon.utils.block.BlockUtil.getNeighbor
import melon.utils.math.vector.toBlockPos
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

fun fastPosDirection(
    pos: BlockPos,
    facing: EnumFacing = EnumFacing.UP,
    hand: EnumHand = EnumHand.MAIN_HAND,
    offsetX: Float = 0.5f,
    offsetY: Float = 1f,
    offsetZ: Float = 0.5f,
): CPacketPlayerTryUseItemOnBlock {
    return CPacketPlayerTryUseItemOnBlock(pos, facing, hand, offsetX, offsetY, offsetZ)
}

fun fastPosDirectionDown(
    pos: BlockPos,
    hand: EnumHand = EnumHand.MAIN_HAND,
    offsetX: Float = 0.5f,
    offsetY: Float = 1f,
    offsetZ: Float = 0.5f,
): CPacketPlayerTryUseItemOnBlock {
    return CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.DOWN, hand, offsetX, offsetY, offsetZ)
}

fun SafeClientEvent.fastPos(
    pos: BlockPos,
    strictDirection: Boolean = false,
    face: EnumFacing = EnumFacing.UP,
    hand: EnumHand = EnumHand.MAIN_HAND,
    offsetX: Float = 0.5f,
    offsetY: Float = 1f,
    offsetZ: Float = 0.5f,
): CPacketPlayerTryUseItemOnBlock {
    val placePos = getNeighbor(pos, strictDirection) ?: BlockUtil.EasyBlock(pos, face)
    return CPacketPlayerTryUseItemOnBlock(placePos.blockPos, placePos.face, hand, offsetX, offsetY, offsetZ)
}

fun SafeClientEvent.fastPos(
    vec: Vec3d,
    strictDirection: Boolean = false,
    face: EnumFacing = EnumFacing.UP,
    hand: EnumHand = EnumHand.MAIN_HAND,
    offsetX: Float = 0.5f,
    offsetY: Float = 1f,
    offsetZ: Float = 0.5f,
): CPacketPlayerTryUseItemOnBlock {
    val placePos = getNeighbor(vec.toBlockPos(), strictDirection) ?: BlockUtil.EasyBlock(vec.toBlockPos(), face)
    return CPacketPlayerTryUseItemOnBlock(placePos.blockPos, placePos.face, hand, offsetX, offsetY, offsetZ)
}

fun SafeClientEvent.position(yOffset: Int = 0, ground: Boolean = false): CPacketPlayer.Position {
    return CPacketPlayer.Position(
        player.posX,
        player.posY + yOffset,
        player.posZ,
        ground
    )
}

fun SafeClientEvent.position(yOffset: Double = 0.0): CPacketPlayer.Position {
    return CPacketPlayer.Position(
        player.posX,
        player.posY + yOffset,
        player.posZ,
        false
    )
}

fun SafeClientEvent.positionRotation(yOffset: Double = 0.0): CPacketPlayer.PositionRotation {
    return CPacketPlayer.PositionRotation(
        player.posX,
        player.posY + yOffset,
        player.posZ,
        CrystalManager.rotation.x,
        90f,
        false
    )
}

fun positionBypass(
    vec: BlockPos,
    ground: Boolean = false
): CPacketPlayer.Position {
    return CPacketPlayer.Position(
        vec.getX().toDouble(),
        vec.getY().toDouble(),
        vec.getZ().toDouble(),
        ground
    )
}

fun positionRotationBypass(
    vec: BlockPos,
    ground: Boolean = false
): CPacketPlayer.PositionRotation {
    return CPacketPlayer.PositionRotation(
        vec.getX().toDouble(),
        vec.getY().toDouble(),
        vec.getZ().toDouble(),
        CrystalManager.rotation.x,
        90f,
        ground
    )
}

fun SafeClientEvent.packetClick(slot: Int, clickType: ClickType = ClickType.PICKUP): CPacketClickWindow {
    return CPacketClickWindow(
        player.inventoryContainer.windowId,
        slot,
        0,
        clickType,
        player.inventory.getStackInSlot(slot),
        player.openContainer.getNextTransactionID(player.inventory)
    )
}