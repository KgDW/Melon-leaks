package melon.utils.player

import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.manager.RotationManager.addRotationsNew
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.entity.EntityUtil.isntValid
import melon.system.event.SafeClientEvent
import net.minecraft.client.Minecraft
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.util.concurrent.CopyOnWriteArrayList

val mc: Minecraft = Minecraft.getMinecraft()
fun getTarget(range: Int): EntityPlayer? {
    return CopyOnWriteArrayList(mc.world.playerEntities).stream()
        .filter {
            !isntValid(
                it!!,
                range.toDouble()
            )
        }
        .min(Comparator.comparing {
            mc.player.getDistanceSq(
                it!!
            )
        })
        .orElse(null)
}

fun getTargetMax(range: Int): EntityPlayer? {
    return CopyOnWriteArrayList(mc.world.playerEntities).stream()
        .filter {
            !isntValid(
                it!!,
                range.toDouble()
            )
        }
        .filter { mc.player.getDistance(it!!) <= range }
        .max(Comparator.comparing {
            mc.player.getDistance(
                it!!
            )
        })
        .orElse(null)
}

fun SafeClientEvent.breakCrystal(pos: BlockPos? = null, rotate: Boolean = false) {
    for (entity in EntityManager.entity) {
        if (entity !is EntityEnderCrystal) continue
        if (!entity.preventEntitySpawning) continue
        if (!entity.isEntityAlive) continue
        if (pos != null && !entity.entityBoundingBox.intersects(AxisAlignedBB(pos))) continue
        if (rotate && player.getDistanceSq(entity) > 4.5.sq) continue
        if (rotate) addRotationsNew(entity.position)
        connection.sendPacket(CPacketUseEntity(entity))
        connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
    }
}