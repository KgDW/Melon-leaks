package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.block.AddCollisionBoxEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.DoubleSetting
import dev.zenhao.melon.utils.animations.fastFloor
import melon.events.PlayerMoveEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.block.getBlock
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

@Module.Info(name = "AntiWeb", category = Category.COMBAT)
class AntiWeb : Module() {
    private var speedMultiplier: DoubleSetting = dsetting("SpeedMultiplier", 0.8, 0.1, 1.0)

    init {
        safeEventListener<AddCollisionBoxEvent> {
            if (it.entity == player && it.block == Blocks.WEB) {
                it.collidingBoxes.add(
                    AxisAlignedBB(
                        it.pos.x.toDouble(), it.pos.y.toDouble(), it.pos.z.toDouble(),
                        it.pos.x + 1.0, it.pos.y + 1.0, it.pos.z + 1.0
                    )
                )
            }
        }

        safeEventListener<PlayerMoveEvent>(-2000) {
            if (!player.isFlying && player.onGround && player.motionY <= 0.0 && player.motionY >= -0.08 && !player.isInWeb && isAboveWeb()) {
                it.x = player.motionX * speedMultiplier.value
                it.z = player.motionZ * speedMultiplier.value
            }
        }
    }

    val EntityPlayer.isFlying: Boolean
        get() = this.isElytraFlying || this.capabilities.isFlying

    private fun SafeClientEvent.isAboveWeb(): Boolean {
        val box = player.entityBoundingBox
        val pos = BlockPos.PooledMutableBlockPos.retain()
        val y = (player.posY - 0.08).fastFloor()

        for (x in box.minX.fastFloor()..box.maxX.fastFloor()) {
            for (z in box.minZ.fastFloor()..box.maxZ.fastFloor()) {
                if (world.getBlock(pos.setPos(x, y, z)) != Blocks.WEB) {
                    pos.release()
                    return false
                }
            }
        }

        pos.release()
        return true
    }
}