package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.block.BlockEvent
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.player.PacketMine
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.entity.CrystalUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.inventory.HotbarSlot
import melon.system.event.SafeClientEvent
import melon.utils.block.BlockUtil.getNeighbor
import melon.utils.inventory.slot.firstBlock
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.math.vector.toVec3d
import melon.utils.player.breakCrystal
import melon.utils.player.getTarget
import melon.utils.world.isPlaceable
import melon.utils.world.noCollision
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer.Rotation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

@Module.Info(name = "HoleKicker", category = Category.COMBAT)
object HoleKicker : Module() {
    private val range = isetting("Range", 5, 1, 6)
    private val packetPlace = bsetting("PacketPlace", false)
    private val breakCrystal: Setting<Boolean> = bsetting("BreakCrystal", false)
    private val piston: Setting<Boolean> = bsetting("Piston", true)
    private var facing: EnumFacing? = null
    private var placed = false

    override fun onEnable() {
        placed = false
    }

    init {
        onLoop {
            val target = getTarget(range.value) ?: return@onLoop
            val pos = BlockPos(target).offset(EnumFacing.UP)
            val pistonSlot =
                player.hotbarSlots.firstBlock(Blocks.PISTON) ?: player.hotbarSlots.firstBlock(Blocks.STICKY_PISTON)
                ?: return@onLoop
            val redStoneSlot =
                player.hotbarSlots.firstBlock(Blocks.REDSTONE_BLOCK)
                    ?: player.hotbarSlots.firstBlock(Blocks.REDSTONE_TORCH)
                    ?: return@onLoop
            facing = getFacing(pos) ?: return@onLoop
            when (facing) {
                EnumFacing.NORTH -> connection.sendPacket(Rotation(180.0f, 0f, true))
                EnumFacing.SOUTH -> connection.sendPacket(Rotation(0.0f, 0f, true))
                EnumFacing.WEST -> connection.sendPacket(Rotation(90.0f, 0f, true))
                EnumFacing.EAST -> connection.sendPacket(Rotation(-90.0f, 0f, true))
                else -> {
                    connection.sendPacket(Rotation(player.rotationYaw, 90f, true))
                }
            }
            val placePos = pos.offset(facing!!)
            if (breakCrystal.value) {
                breakCrystal(placePos)
            }
            if (world.isPlaceable(placePos)) {
                placeBlock(placePos, pistonSlot)
            } else {
                ChatUtil.sendMessage("No Suitable Place For Piston!")
                disable()
                return@onLoop
            }
            for (facing in EnumFacing.values()) {
                if (!world.noCollision(pos.offset(facing)) || !world.isPlaceable(pos.offset(facing))) continue
                placeBlock(pos.offset(facing), redStoneSlot)
                if (piston.value) {
                    breakBlocks()
                } else {
                    disable()
                    return@onLoop
                }
                break
            }
        }
    }

    private fun getFacing(position: BlockPos): EnumFacing? {
        for (f in EnumFacing.values()) {
            val pos = BlockPos(position)
            if (pos.offset(f).getY() != position.getY()) continue
            if (!mc.world.isAirBlock(pos.offset(f, -1).offset(EnumFacing.DOWN))) {
                if (mc.world.isAirBlock(pos.offset(f, -1))) {
                    if (mc.world.isAirBlock(pos.offset(f))) {
                        return f
                    }
                }
            }
        }
        return null
    }

    private fun SafeClientEvent.placeBlock(pos: BlockPos, slot: HotbarSlot) {
        val placePos = getNeighbor(pos, false) ?: return
        val sneak = !player.isSneaking
        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))
        spoofHotbar(slot) {
            if (packetPlace.value) {
                connection.sendPacket(
                    CPacketPlayerTryUseItemOnBlock(
                        placePos.blockPos,
                        placePos.face,
                        EnumHand.MAIN_HAND,
                        0.5f,
                        1f,
                        0.5f
                    )
                )
            } else {
                playerController.processRightClickBlock(
                    player, world, placePos.blockPos, placePos.face,
                    placePos.blockPos.toVec3d().add(0.5, 0.5, 0.5).add(
                        placePos.face.getDirectionVec().toVec3d().scale(0.5)
                    ), EnumHand.MAIN_HAND
                )
            }
        }
        connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))

        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
    }

    private fun SafeClientEvent.breakBlocks() {
        val pos = CrystalUtil.getSphere(
            EntityUtil.getPlayerPos(player),
            range.value.toDouble(),
            range.value.toDouble(),
            false,
            true,
            0
        ).stream()
            .filter { world.getBlockState(it).block == Blocks.REDSTONE_BLOCK || world.getBlockState(it).block == Blocks.REDSTONE_TORCH }
            .min(Comparator.comparing { player.getDistanceSq(it) })
            .orElse(null)
        if (pos == null) {
            ChatUtil.sendMessage("BreakPos nulled!")
            disable()
            return
        }
        if (player.getDistanceSq(pos) <= 36) {
            if (BlockUtil.canBreak(pos, false)) {
                if (PacketMine.isEnabled) {
                    BlockEvent(pos, BlockUtil.getRayTraceFacing(pos)).post()
                } else {
                    ChatUtil.sendMessage("PacketMine Needed!")
                    disable()
                    return
                }
            }
        }
    }
}