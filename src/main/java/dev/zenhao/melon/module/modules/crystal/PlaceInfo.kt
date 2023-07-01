package dev.zenhao.melon.module.modules.crystal

import dev.zenhao.melon.module.modules.crystal.MelonAura2.getPlaceSide
import melon.system.event.SafeClientEvent
import melon.utils.math.vector.Vec3f
import melon.utils.world.getHitVecOffset
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHandSide
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

open class PlaceInfo(
    open val target: EntityLivingBase,
    open val blockPos: BlockPos,
    open val selfDamage: Float,
    open val targetDamage: Double,
    open val side: EnumFacing,
    open val hitVecOffset: Vec3f,
    open val hitVec: Vec3d,
    open val dropsItem: Boolean = false
) {
    class Mutable(
        target: EntityLivingBase
    ) : PlaceInfo(
        target,
        BlockPos.ORIGIN,
        Float.MAX_VALUE,
        MelonAura2.forcePlaceDmg.value,
        EnumFacing.UP,
        Vec3f.ZERO,
        Vec3d.ZERO,
        false
    ) {
        override var target = target; private set
        override var blockPos = super.blockPos; private set
        override var selfDamage = super.selfDamage; private set
        override var targetDamage = super.targetDamage; private set
        override var side = super.side; private set
        override var hitVecOffset = super.hitVecOffset; private set
        override var hitVec = super.hitVec; private set
        override var dropsItem = super.dropsItem; private set

        fun update(
            target: EntityLivingBase? = null,
            blockPos: BlockPos,
            selfDamage: Double,
            targetDamage: Double,
            dropsItem: Boolean = false
        ) {
            target?.let {
                this.target = it
            }
            this.blockPos = blockPos
            this.selfDamage = selfDamage.toFloat()
            this.targetDamage = targetDamage
            this.dropsItem = dropsItem
        }

        fun calcPlacement(event: SafeClientEvent) {
            event {
                side = getPlaceSide(blockPos)
                hitVecOffset = getHitVecOffset(side)
                hitVec = Vec3d((blockPos.x + hitVecOffset.x).toDouble(),
                    (blockPos.y + hitVecOffset.y).toDouble(), (blockPos.z + hitVecOffset.z).toDouble()
                )
            }
        }

        fun clear(player: EntityPlayerSP) {
            update(player, BlockPos.ORIGIN, Double.MAX_VALUE, MelonAura2.forcePlaceDmg.value)
        }

        fun takeValid(): Mutable? {
            return this.takeIf {
                target != Minecraft.getMinecraft().player
                        && selfDamage != Float.MAX_VALUE
                        && targetDamage != MelonAura2.forcePlaceDmg.value
            }
        }
    }

    companion object {
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        @JvmField
        val INVALID = PlaceInfo(object : EntityLivingBase(null) {
            override fun getArmorInventoryList(): MutableIterable<ItemStack> {
                return ArrayList()
            }

            override fun setItemStackToSlot(slotIn: EntityEquipmentSlot, stack: ItemStack) {

            }

            override fun getItemStackFromSlot(slotIn: EntityEquipmentSlot): ItemStack {
                return ItemStack.EMPTY
            }

            override fun getPrimaryHand(): EnumHandSide {
                return EnumHandSide.RIGHT
            }
        }, BlockPos.ORIGIN, Float.NaN, Double.NaN, EnumFacing.UP, Vec3f.ZERO, Vec3d.ZERO)
    }
}