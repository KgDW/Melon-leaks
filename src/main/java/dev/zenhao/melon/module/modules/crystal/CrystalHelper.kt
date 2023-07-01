package dev.zenhao.melon.module.modules.crystal

import dev.zenhao.melon.manager.CombatManager
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.calcDamage
import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.utils.animations.fastFloor
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.inventory.HotbarSlot
import melon.system.event.AlwaysListening
import melon.system.event.SafeClientEvent
import melon.utils.combat.CrystalUtils
import melon.utils.inventory.slot.firstItem
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.world.rayTraceVisible
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

object CrystalHelper : AlwaysListening {
    val mc: Minecraft = Minecraft.getMinecraft()
    val EntityEnderCrystal.blockPos: BlockPos
        get() = BlockPos(this.posX.fastFloor(), this.posY.fastFloor() - 1, this.posZ.fastFloor())

    @JvmStatic
    val EntityLivingBase.scaledHealth: Float
        get() = this.health + this.absorptionAmount * (this.health / this.maxHealth)

    @JvmStatic
    val EntityLivingBase.totalHealth: Float
        get() = this.health + this.absorptionAmount

    fun EntityPlayerSP.getCrystalSlot(): HotbarSlot? {
        return this.hotbarSlots.firstItem(Items.END_CRYSTAL)
    }

    fun EntityPlayerSP.getItemSlot(item: Item): HotbarSlot? {
        return this.hotbarSlots.firstItem(item)
    }

    fun calcCollidingCrystalDamageOld(
        crystals: List<Pair<EntityEnderCrystal, CrystalDamage>>,
        placeBox: AxisAlignedBB
    ): Float {
        var max = 0.0f

        for ((crystal, crystalDamage) in crystals) {
            if (!placeBox.intersects(crystal.entityBoundingBox)) continue
            if (crystalDamage.selfDamage > max) {
                max = crystalDamage.selfDamage
            }
        }

        return max
    }

    fun SafeClientEvent.calcCollidingCrystalDamage(
        placeBox: AxisAlignedBB
    ): Float {
        var max = 0.0f
        if (world.loadedEntityList.isNotEmpty()) {
            for (c in world.loadedEntityList) {
                if (c == null) continue
                if (c !is EntityEnderCrystal) continue
                if (player.getDistance(c) > 6) continue
                val mutableBlockPos = BlockPos.MutableBlockPos()
                if (!placeBox.intersects(c.entityBoundingBox)) continue
                val context = CombatManager.contextSelf ?: return 0f
                val crystalX = c.posX
                val crystalY = c.posY
                val crystalZ = c.posZ
                val selfDamage = max(
                    context.calcDamage(crystalX, crystalY, crystalZ, false, mutableBlockPos),
                    context.calcDamage(crystalX, crystalY, crystalZ, true, mutableBlockPos)
                ).toDouble()

                if (selfDamage > max) {
                    max = selfDamage.toFloat()
                }
            }
        }
        return max
    }

    @JvmStatic
    fun SafeClientEvent.checkBreakRange(
        entity: EntityEnderCrystal,
        breakRange: Float,
        wallRange: Float,
        mutableBlockPos: BlockPos.MutableBlockPos
    ): Boolean {
        return checkBreakRange(
            entity.posX,
            entity.posY,
            entity.posZ,
            breakRange,
            wallRange,
            mutableBlockPos
        )
    }

    @JvmStatic
    fun SafeClientEvent.checkBreakRange(
        x: Double,
        y: Double,
        z: Double,
        breakRange: Float,
        wallRange: Float,
        mutableBlockPos: BlockPos.MutableBlockPos
    ): Boolean {
        return player.getDistanceSq(x, y, z) <= breakRange.sq
                && (player.getDistanceSq(x, y, z) <= wallRange.sq
                || world.rayTraceVisible(player.posX, player.posY + player.eyeHeight, player.posZ, x, y + 1.7, z, 20, mutableBlockPos))
    }

    @JvmStatic
    fun SafeClientEvent.isValidPos(
        newPlacement: Boolean,
        breakRange: Float,
        wallRange: Float,
        pos: BlockPos,
        entity: EntityEnderCrystal,
        mutableBlockPos: BlockPos.MutableBlockPos
    ): Boolean {
        if (!isPlaceable(pos, newPlacement, mutableBlockPos)) {
            return false
        }

        val minX = pos.x + 0.001
        val minY = pos.y + 1.0
        val minZ = pos.z + 0.001
        val maxX = pos.x + 0.999
        val maxY = pos.y + 3.0
        val maxZ = pos.z + 0.999

        if (entity.isEntityAlive && entity.entityBoundingBox.intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (!checkBreakRange(entity, breakRange, wallRange, mutableBlockPos)) return false
        }

        return true
    }

    @JvmStatic
    fun SafeClientEvent.isPlaceable(pos: BlockPos, newPlacement: Boolean, mutableBlockPos: BlockPos.MutableBlockPos): Boolean {
        if (!canPlaceCrystalOn(pos)) {
            return false
        }
        val posUp = mutableBlockPos.setAndAdd(pos, 0, 1, 0)
        return if (newPlacement) {
            world.isAirBlock(posUp)
        } else {
            isValidMaterial(world.getBlockState(posUp)) && isValidMaterial(
                world.getBlockState(
                    posUp.add(
                        0,
                        1,
                        0
                    )
                )
            )
        }
    }

    @JvmStatic
    fun BlockPos.MutableBlockPos.setAndAdd(set: BlockPos, add: BlockPos): BlockPos.MutableBlockPos {
        return this.setPos(set.x + add.x, set.y + add.y, set.z + add.z)
    }

    @JvmStatic
    fun BlockPos.MutableBlockPos.setAndAdd(set: BlockPos, x: Int, y: Int, z: Int): BlockPos.MutableBlockPos {
        return this.setPos(set.x + x, set.y + y, set.z + z)
    }

    @JvmStatic
    private val mutableBlockPos = ThreadLocal.withInitial {
        BlockPos.MutableBlockPos()
    }

    @JvmStatic
            /** Checks colliding with blocks and given entity */
    fun SafeClientEvent.canPlaceCrystalNew(pos: BlockPos, entity: EntityLivingBase? = null): Boolean {
        return canPlaceCrystalOn(pos)
                && (entity == null || !getCrystalPlacingBB(pos).intersects(entity.entityBoundingBox))
                && hasValidSpaceForCrystal(pos)
    }

    @JvmStatic
            /** Checks if the block is valid for placing crystal */
    fun SafeClientEvent.canPlaceCrystalOn(pos: BlockPos): Boolean {
        val block = world.getBlockState(pos).block
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN
    }

    @JvmStatic
    fun SafeClientEvent.hasValidSpaceForCrystal(pos: BlockPos): Boolean {
        val mutableBlockPos = mutableBlockPos.get()
        return isValidMaterial(world.getBlockState(mutableBlockPos.setAndAdd(pos, 0, 1, 0)))
                && isValidMaterial(world.getBlockState(mutableBlockPos.add(0, 1, 0)))
    }

    @JvmStatic
    fun SafeClientEvent.isValidMaterial(blockState: IBlockState): Boolean {
        return !blockState.material.isLiquid && blockState.material.isReplaceable
    }

    @JvmStatic
    fun SafeClientEvent.isReplaceable(block: Block): Boolean {
        return block === Blocks.FIRE || block === Blocks.DOUBLE_PLANT || block === Blocks.VINE
    }

    @JvmStatic
    fun SafeClientEvent.getVecDistance(a: BlockPos, posX: Double, posY: Double, posZ: Double): Double {
        val x1 = a.getX() - posX
        val y1 = a.getY() - posY
        val z1 = a.getZ() - posZ
        return sqrt(x1 * x1 + y1 * y1 + z1 * z1)
    }

    @JvmStatic
    fun SafeClientEvent.getVecDistance(pos: BlockPos, entity: Entity): Double {
        return getVecDistance(pos, entity.posX, entity.posY, entity.posZ)
    }

    inline val Entity.realSpeed get() = hypot(posX - prevPosX, posZ - prevPosZ)

    @JvmStatic
    fun SafeClientEvent.shouldForcePlace(entity: EntityLivingBase, forcePlaceHealth: Float): Boolean {
        return (entity.health + entity.absorptionAmount) <= forcePlaceHealth
    }

    @JvmStatic
    fun SafeClientEvent.normalizeAngle(angleIn: Double): Double {
        var angle = angleIn
        angle %= 360.0
        if (angle >= 180.0) {
            angle -= 360.0
        }
        if (angle < -180.0) {
            angle += 360.0
        }
        return angle
    }

    @JvmStatic
    fun normalizeAngle(angleIn: Float): Float {
        var angle = angleIn
        angle %= 360f
        if (angle >= 180f) {
            angle -= 360f
        }
        if (angle < -180f) {
            angle += 360f
        }
        return angle
    }

    fun placeBoxIntersectsCrystalBox(placePos: BlockPos, crystal: EntityEnderCrystal): Boolean {
        return placeBoxIntersectsCrystalBox(placePos, crystal.posX, crystal.posY, crystal.posZ)
    }

    fun placeBoxIntersectsCrystalBox(placePos: BlockPos, crystalX: Double, crystalY: Double, crystalZ: Double): Boolean {
        return (crystalY.fastFloor() - placePos.y).withIn(0, 1)
                && (crystalX.fastFloor() - placePos.x).withIn(-1, 1)
                && (crystalZ.fastFloor() - placePos.z).withIn(-1, 1)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.withIn(a: Int, b: Int): Boolean {
        return this in a..b
    }

    @JvmStatic
    fun placeBoxIntersectsCrystalBox(placePos: BlockPos, crystalPos: BlockPos): Boolean {
        return crystalPos.y - placePos.y in 0..2
                && abs(crystalPos.x - placePos.x) < 2
                && abs(crystalPos.z - placePos.z) < 2
    }

    @JvmStatic
    fun placeBoxIntersectsCrystalBox(placePos: Vec3d, crystalPos: BlockPos): Boolean {
        return crystalPos.y - placePos.y in 0.0..2.0
                && abs(crystalPos.x - placePos.x) < 2.0
                && abs(crystalPos.z - placePos.z) < 2.0
    }

    @JvmStatic
    fun placeBoxIntersectsCrystalBox(
        placeX: Double,
        placeY: Double,
        placeZ: Double,
        crystalPos: BlockPos
    ): Boolean {
        return crystalPos.y - placeY in 0.0..2.0
                && abs(crystalPos.x - placeX) < 2.0
                && abs(crystalPos.z - placeZ) < 2.0
    }

    @JvmStatic
    fun placeBoxIntersectsCrystalBox(
        placeX: Double,
        placeY: Double,
        placeZ: Double,
        crystalX: Double,
        crystalY: Double,
        crystalZ: Double
    ): Boolean {
        return crystalY - placeY in 0.0..2.0
                && abs(crystalX - placeX) < 2.0
                && abs(crystalZ - placeZ) < 2.0
    }

    @JvmStatic
    fun SafeClientEvent.canPlaceCollide(pos: BlockPos): Boolean {
        val placingBB = getCrystalPlacingBB(pos.up())
        return world.let { world ->
            world.getEntitiesWithinAABBExcludingEntity(null, placingBB).all {
                it.isDead || it is EntityLivingBase && it.health <= 0.0f
            }
        } ?: false
    }

    @JvmStatic
    fun SafeClientEvent.getCrystalPlacingBB(pos: BlockPos): AxisAlignedBB {
        return getCrystalPlacingBB(pos.x, pos.y, pos.z)
    }

    @JvmStatic
    fun getCrystalPlacingBB(x: Int, y: Int, z: Int): AxisAlignedBB {
        return AxisAlignedBB(
            x + 0.001, y + 1.0, z + 0.001,
            x + 0.999, y + 3.0, z + 0.999
        )
    }

    @JvmStatic
    fun SafeClientEvent.getCrystalPlacingBB(pos: Vec3d): AxisAlignedBB {
        return getCrystalPlacingBB(pos.x, pos.y, pos.z)
    }

    @JvmStatic
    fun SafeClientEvent.getCrystalPlacingBB(x: Double, y: Double, z: Double): AxisAlignedBB {
        return AxisAlignedBB(
            x - 0.499, y, z - 0.499,
            x + 0.499, y + 2.0, z + 0.499
        )
    }

    @JvmStatic
    fun getCrystalBB(pos: BlockPos): AxisAlignedBB {
        return getCrystalBB(pos.x, pos.y, pos.z)
    }

    @JvmStatic
    fun getCrystalBB(x: Int, y: Int, z: Int): AxisAlignedBB {
        return AxisAlignedBB(
            x - 0.5, y + 1.0, z - 0.5,
            x + 1.5, y + 3.0, z + 1.5
        )
    }

    fun checkPlaceCollision(placeInfo: BlockPos): Boolean {
        return EntityManager.entity.asSequence()
            .filterIsInstance<EntityEnderCrystal>()
            .filter { it.isEntityAlive }
            .filter { CrystalUtils.crystalPlaceBoxIntersectsCrystalBox(placeInfo, it) }
            .none()
    }

    fun getPredictedTarget(target: Entity, ticks: Int): Vec3d {
        val motionX = (target.posX - target.lastTickPosX).coerceIn(-0.6, 0.6)
        val motionY = (target.posY - target.lastTickPosY).coerceIn(-0.5, 0.5)
        val motionZ = (target.posZ - target.lastTickPosZ).coerceIn(-0.6, 0.6)
        val entityBox = target.entityBoundingBox
        var targetBox = entityBox
        for (tick in 0..ticks) {
            targetBox = canMove(targetBox, motionX, motionY, motionZ)
                ?: canMove(targetBox, motionX, 0.0, motionZ)
                        ?: canMove(targetBox, 0.0, motionY, 0.0)
                        ?: break
        }
        val offsetX = targetBox.minX - entityBox.minX
        val offsetY = targetBox.minY - entityBox.minY
        val offsetZ = targetBox.minZ - entityBox.minZ
        return if (ticks > 0) {
            Vec3d(offsetX, offsetY, offsetZ)
        } else {
            target.positionVector
            //Vec3d(motionX, motionY, motionZ)
        }
    }

    fun canMove(box: AxisAlignedBB, x: Double, y: Double, z: Double): AxisAlignedBB? {
        return box.offset(x, y, z).takeIf { !mc.world.collidesWithAnyBlock(it) }
    }
}