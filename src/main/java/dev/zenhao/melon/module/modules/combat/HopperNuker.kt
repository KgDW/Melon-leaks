package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.block.BlockEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager.getModuleByClass
import dev.zenhao.melon.module.modules.player.PacketMine
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.entity.CrystalUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import net.minecraft.block.BlockHopper
import net.minecraft.block.BlockShulkerBox
import net.minecraft.block.BlockTallGrass
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "HopperNuker", category = Category.COMBAT)
class HopperNuker : Module() {
    private var range = isetting("Range", 4, 1, 6)
    private var swing = bsetting("Swing", false)
    private var shulker = bsetting("Shulker", false)
    private var hooper = bsetting("Hopper", false)
    private var grass = bsetting("Grass", false)
    private var pos: BlockPos? = null
    private var ownHopper = CopyOnWriteArrayList<BlockPos>()
    private var canMine = true

    init {
        safeEventListener<PacketEvents.Send> {
            if (it.packet is CPacketPlayerTryUseItemOnBlock) {
                if (!ownHopper.contains(it.packet.pos)) {
                    if (mc.world.getBlockState(it.packet.pos).block.equals(Blocks.HOPPER) || mc.world.getBlockState(it.packet.pos).block is BlockShulkerBox) {
                        ownHopper.add(it.packet.pos)
                    }
                }
            }
        }

        safeEventListener<PlayerMotionEvent> {
            if (getModuleByClass(PacketMine::class.java).isDisabled) {
                disable()
                return@safeEventListener
            }
            pos = CrystalUtil.getSphere(
                EntityUtil.getPlayerPos(mc.player),
                range.value.toDouble(),
                range.value.toDouble(),
                false,
                true,
                0
            ).stream()
                .filter {
                    (hooper.value && mc.world.getBlockState(it!!).block is BlockHopper)
                            || (shulker.value && mc.world.getBlockState(it).block is BlockShulkerBox)
                            || (grass.value && mc.world.getBlockState(it).block is BlockTallGrass)
                }
                .max(Comparator.comparing { mc.player.getDistanceSq(it!!) })
                .orElse(null)
            if (pos != null) {
                if (mc.player.getDistance(
                        pos!!.getX().toDouble(),
                        pos!!.getY().toDouble(),
                        pos!!.getZ().toDouble()
                    ) > range.value
                ) {
                    return@safeEventListener
                }
                if (PacketMine.currentPos != null) {
                    if (PacketMine.currentPos!!.getZ() == pos!!.getZ() && PacketMine.currentPos!!.getX() == pos!!.getX() && PacketMine.currentPos!!.getY() == pos!!.getY()) {
                        return@safeEventListener
                    }
                }
                ownHopper.forEach {
                    if (!BlockUtil.canBreak(it, false)) {
                        ownHopper.remove(it)
                    }
                    if (it.equals(pos)) {
                        canMine = false
                        return@safeEventListener
                    } else {
                        canMine = true
                    }
                }
                if (BlockUtil.canBreak(pos, false) && canMine) {
                    if (swing.value && mc.connection != null) {
                        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                    }
                    //mc.playerController.onPlayerDamageBlock(pos, BlockUtil.getRayTraceFacing(pos));
                    BlockEvent(pos!!, BlockUtil.getRayTraceFacing(pos)).post()
                }
            }
        }
    }
}