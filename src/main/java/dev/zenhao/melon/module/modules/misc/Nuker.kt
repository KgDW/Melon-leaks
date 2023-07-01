package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.event.events.block.BlockEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.entity.CrystalUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.inventory.InventoryUtil
import melon.system.event.safeEventListener
import net.minecraft.block.Block
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Module.Info(name = "Nuker", category = Category.MISC)
class Nuker : Module() {
    private val timerUtils = TimerUtils()
    private val autoSwitch: Setting<Boolean> = bsetting("AutoSwitch", true)
    var rotate: Setting<Boolean> = bsetting("Rotate", true)
    var distance = isetting("Range", 6, 1, 6)
    var blockPerTick: Setting<Int> = isetting("BlocksPerTick", 50, 1, 100)
    var delay: Setting<Int> = isetting("Delay", 50, 1, 100)
    var mode = msetting("Mode", Mode.NUKE)
    var antiRegear: Setting<Boolean> = bsetting("AntiRegear", false)
    var hopperNuker: Setting<Boolean> = bsetting("HopperNuker", false)
    var bedFuker = bsetting("BedFucker", false)
    var picslot = -1
    private var oldSlot = -1
    private var selected: Block? = null
    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        selected = null
        picslot = InventoryUtil.findHotbarItem(Items.DIAMOND_PICKAXE)
        oldSlot = mc.player.inventory.currentItem
    }

    init {
        safeEventListener<BlockEvent> {
            val block: Block? = null
            if ((mode.value == Mode.SELECTION || mode.value == Mode.NUKE) && block !== selected) {
                selected = block
            }
        }

        safeEventListener<PlayerMotionEvent> {
            picslot = InventoryUtil.findHotbarItem(Items.DIAMOND_PICKAXE)
            oldSlot = mc.player.inventory.currentItem
            var i = 0
            var pos: BlockPos? = null
            when (mode.value) {
                Mode.SELECTION, Mode.NUKE -> {
                    pos = closestBlockSelection
                }

                Mode.ALL -> {
                    pos = closestBlockAll
                }
            }
            if (pos != null) {
                if (mode.value == Mode.SELECTION || mode.value == Mode.ALL) {
                    if (rotate.value) {
                        it.setRotation(
                            BlockInteractionHelper.getLegitRotations(Vec3d(pos))[0],
                            BlockInteractionHelper.getLegitRotations(
                                Vec3d(pos)
                            )[1]
                        )
                    }
                    if (canBreak(pos)) {
                        mc.player.connection.sendPacket(
                            CPacketPlayerDigging(
                                CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                                pos, BlockUtil.getFacing(pos)
                            )
                        )
                        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.OFF_HAND))
                    }
                } else {
                    while (i < blockPerTick.value) {
                        pos = closestBlockSelection
                        if (pos == null) continue
                        if (rotate.value) {
                            it.setRotation(
                                BlockInteractionHelper.getLegitRotations(Vec3d(pos))[0],
                                BlockInteractionHelper.getLegitRotations(
                                    Vec3d(pos)
                                )[1]
                            )
                        }
                        if (!timerUtils.passedMs(delay.value.toLong())) continue
                        mc.player.connection.sendPacket(
                            CPacketPlayerDigging(
                                CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                                pos, BlockUtil.getFacing(pos)
                            )
                        )
                        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.OFF_HAND))
                        timerUtils.reset()
                        ++i
                    }
                }
            }
            if (antiRegear.value) {
                breakBlocks(it, BlockUtil.shulkerList)
            }
            if (hopperNuker.value) {
                val blocklist = ArrayList<Block?>()
                blocklist.add(Blocks.HOPPER)
                breakBlocks(it, blocklist)
            }
            if (bedFuker.value) {
                val blocklist = ArrayList<Block?>()
                blocklist.add(Blocks.BED)
                breakBlocks(it, blocklist)
            }
        }
    }

    fun breakBlocks(event: PlayerMotionEvent, blocks: List<Block?>) {
        picslot = InventoryUtil.findHotbarItem(Items.DIAMOND_PICKAXE)
        oldSlot = mc.player.inventory.currentItem
        val pos = CrystalUtil.getSphere(
            EntityUtil.getPlayerPos(mc.player),
            distance.value.toDouble(),
            distance.value.toDouble(),
            false,
            true,
            0
        ).stream()
            .filter { e: BlockPos? -> !blocks.contains(mc.world.getBlockState(e!!).block) }
            .min(Comparator.comparing { e: BlockPos? -> mc.player.getDistanceSq(e!!) })
            .orElse(null)
        if (pos != null) {
            if (mc.player.getDistance(
                    pos.getX().toDouble(),
                    pos.getY().toDouble(),
                    pos.getZ().toDouble()
                ) > distance.value
            ) {
                return
            }
            if (rotate.value) {
                event.setRotation(
                    BlockInteractionHelper.getLegitRotations(Vec3d(pos))[0], BlockInteractionHelper.getLegitRotations(
                        Vec3d(pos)
                    )[1]
                )
            }
            if (canBreak(pos)) {
                mc.player.connection.sendPacket(
                    CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                        pos, BlockUtil.getFacing(pos)
                    )
                )
                if (autoSwitch.value) {
                    InventoryUtil.switchToHotbarSlot(picslot)
                }
                mc.player.connection.sendPacket(
                    CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        pos, BlockUtil.getFacing(pos)
                    )
                )
                mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                if (autoSwitch.value) {
                    InventoryUtil.switchToHotbarSlot(oldSlot)
                }
            }
        }
    }

    fun canBreak(pos: BlockPos?): Boolean {
        val blockState = mc.world.getBlockState(pos)
        val block = blockState.block
        return block.getBlockHardness(blockState, mc.world, pos) != -1.0f
    }

    private val closestBlockAll: BlockPos?
        private get() {
            var maxDist = distance.value.toFloat()
            var ret: BlockPos? = null
            run {
                var x = maxDist
                while (x >= -maxDist) {
                    run {
                        var y = maxDist
                        while (y >= -maxDist) {
                            var z = maxDist
                            while (z >= -maxDist) {
                                val pos = BlockPos(
                                    mc.player.posX + x.toDouble(),
                                    mc.player.posY + y.toDouble(),
                                    mc.player.posZ + z.toDouble()
                                )
                                val dist = mc.player.getDistance(
                                    pos.getX().toDouble(),
                                    pos.getY().toDouble(),
                                    pos.getZ().toDouble()
                                )
                                if (dist > maxDist.toDouble() || mc.world.getBlockState(pos).block === Blocks.AIR || mc.world.getBlockState(
                                        pos
                                    ).block is BlockLiquid || !this.canBreak(pos) || pos.getY()
                                        .toDouble() < mc.player.posY
                                ) {
                                    z -= 1.0f
                                    continue
                                }
                                maxDist = dist.toFloat()
                                ret = pos
                                z -= 1.0f
                            }
                            y -= 1.0f
                        }
                    }
                    x -= 1.0f
                }
            }
            return ret
        }
    private val closestBlockSelection: BlockPos?
        private get() {
            var maxDist = distance.value.toFloat()
            var ret: BlockPos? = null
            run {
                var x = maxDist
                while (x >= -maxDist) {
                    run {
                        var y = maxDist
                        while (y >= -maxDist) {
                            var z = maxDist
                            while (z >= -maxDist) {
                                val pos = BlockPos(
                                    mc.player.posX + x.toDouble(),
                                    mc.player.posY + y.toDouble(),
                                    mc.player.posZ + z.toDouble()
                                )
                                val dist = mc.player.getDistance(
                                    pos.getX().toDouble(),
                                    pos.getY().toDouble(),
                                    pos.getZ().toDouble()
                                )
                                if (dist > maxDist.toDouble() || mc.world.getBlockState(pos).block === Blocks.AIR || mc.world.getBlockState(
                                        pos
                                    ).block is BlockLiquid || mc.world.getBlockState(pos).block !== this.selected || !this.canBreak(
                                        pos
                                    ) || pos.getY().toDouble() < mc.player.posY
                                ) {
                                    z -= 1.0f
                                    continue
                                }
                                maxDist = dist.toFloat()
                                ret = pos
                                z -= 1.0f
                            }
                            y -= 1.0f
                        }
                    }
                    x -= 1.0f
                }
            }
            return ret
        }

    enum class Mode {
        SELECTION, ALL, NUKE
    }
}