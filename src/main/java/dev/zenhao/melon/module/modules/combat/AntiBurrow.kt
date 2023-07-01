package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.block.BlockEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager.getModuleByClass
import dev.zenhao.melon.module.modules.player.PacketMine
import dev.zenhao.melon.utils.block.BlockUtil
import melon.utils.player.getTarget
import net.minecraft.client.gui.GuiHopper
import net.minecraft.init.Blocks
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

@Module.Info(name = "AntiBurrow", category = Category.COMBAT)
class AntiBurrow : Module() {
    private var range = isetting("Range", 4, 0, 6)
    private var swing = bsetting("Swing", false)
    private var disable = bsetting("Toggle", false)
    private var mineWeb = bsetting("MineWeb", false)

    override fun onUpdate() {
        if (fullNullCheck()) {
            return
        }
        try {
            if (mc.currentScreen is GuiHopper) {
                return
            }
            if (getModuleByClass(CevBreaker::class.java).isEnabled) return
            val player = getTarget(range.value)
            if (disable.value) {
                disable()
            }
            if (player == null) {
                return
            }
            val pos = BlockPos(player.posX, player.posY + 0.5, player.posZ)
            if (getModuleByClass(PacketMine::class.java).isDisabled) {
                disable()
                return
            }
            if (PacketMine.currentPos != null) {
                if (PacketMine.currentPos!!.getZ() == pos.getZ() && PacketMine.currentPos!!.getX() == pos.getX() && PacketMine.currentPos!!.getY() == pos.getY()) {
                    return
                }
            }
            if (mc.world.getBlockState(pos).block == Blocks.WEB && !mineWeb.value) {
                return
            }
            if (BlockUtil.canBreak(pos, false)) {
                if (swing.value) {
                    mc.player.swingArm(EnumHand.MAIN_HAND)
                }
                BlockEvent(pos, BlockUtil.getRayTraceFacing(pos)).post()
                //mc.playerController.clickBlock(pos, BlockUtil.getRayTraceFacing(pos));
                //PacketMine.currentPos = pos;
                //mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, BlockUtil.getRayTraceFacing(pos)));
            }
        } catch (ignored: Exception) {
        }
    }
}