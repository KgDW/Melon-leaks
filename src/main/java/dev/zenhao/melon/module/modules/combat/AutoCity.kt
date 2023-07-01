package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.block.BlockEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager.getModuleByClass
import dev.zenhao.melon.module.modules.combat.CevBreaker
import dev.zenhao.melon.module.modules.player.PacketMine
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.inventory.InventoryUtil.getItemHotbar
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

@Module.Info(name = "AutoCity", category = Category.COMBAT)
object AutoCity : Module() {
    var range = isetting("Range", 4, 0, 6)
    var disable = bsetting("Toggle", false)
    var target: EntityPlayer? = null
    override fun onUpdate() {
        if (fullNullCheck()) {
            return
        }
        try {
            if (getModuleByClass(CevBreaker::class.java).isEnabled) return
            if (disable.value) {
                disable()
            }
            if (getItemHotbar(Items.DIAMOND_PICKAXE) == -1) {
                return
            }
            target = getTarget(range.value.toDouble())
            if (target != null) {
                surroundMineBlock(target!!)
            }
        } catch (ignored: Exception) {
        }
    }

    override fun getHudInfo(): String? {
        return if (target == null) null else target!!.name
    }

    fun surroundMineBlock(player: EntityPlayer) {
        val a = player.positionVector
        if (EntityUtil.getSurroundWeakness(a, 1, -1)) {
            surroundMine(a, -1.0, 0.0, 0.0)
            return
        }
        if (EntityUtil.getSurroundWeakness(a, 2, -1)) {
            surroundMine(a, 1.0, 0.0, 0.0)
            return
        }
        if (EntityUtil.getSurroundWeakness(a, 3, -1)) {
            surroundMine(a, 0.0, 0.0, -1.0)
            return
        }
        if (EntityUtil.getSurroundWeakness(a, 4, -1)) {
            surroundMine(a, 0.0, 0.0, 1.0)
            return
        }
        if (EntityUtil.getSurroundWeakness(a, 5, -1)) {
            surroundMine(a, -1.0, 0.0, 0.0)
            return
        }
        if (EntityUtil.getSurroundWeakness(a, 6, -1)) {
            surroundMine(a, 1.0, 0.0, 0.0)
            return
        }
        if (EntityUtil.getSurroundWeakness(a, 7, -1)) {
            surroundMine(a, 0.0, 0.0, -1.0)
            return
        }
        if (!EntityUtil.getSurroundWeakness(a, 8, -1)) return
        surroundMine(a, 0.0, 0.0, 1.0)
    }

    fun surroundMine(pos: Vec3d?, x: Double, y: Double, z: Double) {
        val position = BlockPos(pos).add(x, y, z)
        if (!BlockUtil.canBreak(position, false)) {
            return
        }
        if (getModuleByClass(PacketMine::class.java).isDisabled) {
            return
        }
        if (PacketMine.currentPos != null) {
            if (PacketMine.currentPos == position) {
                return
            }
            if (PacketMine.currentPos == BlockPos(
                    target!!.posX,
                    target!!.posY,
                    target!!.posZ
                ) && mc.world.getBlockState(
                    BlockPos(
                        target!!.posX, target!!.posY, target!!.posZ
                    )
                ).block !== Blocks.AIR
            ) {
                return
            }
        }
        BlockEvent(position, BlockUtil.getRayTraceFacing(position)).post()
        //mc.playerController.clickBlock(position, BlockUtil.getRayTraceFacing(position));
        //PacketMine.currentPos = position;
        //mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, position, BlockUtil.getRayTraceFacing(position)));
    }

    fun getTarget(range: Double): EntityPlayer? {
        for (player in ArrayList(mc.world.playerEntities)) {
            if (EntityUtil.isntValid(player, range) || !EntityUtil.isInHole(player)) continue
            if (mc.player.getDistance(player) > range) continue
            return player
        }
        return null
    }
}