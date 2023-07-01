package dev.zenhao.melon.module.modules.extra

import dev.zenhao.melon.manager.CrystalManager
import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.manager.HoleManager
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.realSpeed
import dev.zenhao.melon.utils.block.BreakingUtil.Companion.hotbarSlots
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.inventory.HotbarSlot
import dev.zenhao.melon.utils.math.RotationUtils.getRotationTo
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSets
import melon.events.*
import melon.system.event.SafeClientEvent
import melon.system.event.listener
import melon.system.event.safeEventListener
import melon.system.util.collections.EnumMap
import melon.utils.TickTimer
import melon.utils.TimeUnit
import melon.utils.block.isReplaceable
import melon.utils.combat.CrystalUtils
import melon.utils.concurrent.threads.onMainThreadSafe
import melon.utils.concurrent.threads.runSynchronized
import melon.utils.extension.synchronized
import melon.utils.hole.HoleType
import melon.utils.hole.SurroundUtils.betterPosition
import melon.utils.inventory.slot.firstBlock
import melon.utils.math.isInSight
import melon.utils.world.*
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

@Module.Info(name = "Surround", category = Category.XDDD, description = "Continually places obsidian around your feet")
class Surround : Module() {
    private var placeDelay = isetting("PlaceDelay", 50, 0, 1000)
    private var multiPlace = isetting("MultiPlace", 2, 1, 5)
    private var groundCheck = bsetting("GroundCheck", true)
    private var strictDirection = bsetting("StrictDirection", false)
    private var autoCenter = bsetting("AutoCenter", true)
    private var rotation = bsetting("Rotation", false)
    private var enableInHole = bsetting("EnableInHole", false)
    private var inHoleTimeout = isetting("InHoleTimeOut", 50, 1, 100).b(enableInHole)
    private val placing = EnumMap<SurroundOffset, List<PlaceInfo>>().synchronized()
    private val placingSet = LongOpenHashSet()
    private val pendingPlacing = Long2LongMaps.synchronize(Long2LongOpenHashMap()).apply { defaultReturnValue(-1L) }
    private val placed = LongSets.synchronize(LongOpenHashSet())
    private val toggleTimer = TickTimer(TimeUnit.TICKS)
    private var placeTimer = TickTimer()
    private var holePos: BlockPos? = null
    private var enableTicks = 0

    override fun onDisable() {
        placeTimer.reset(-114514L)
        toggleTimer.reset()

        placing.clear()
        placingSet.clear()
        pendingPlacing.clear()
        placed.clear()

        holePos = null
        enableTicks = 0
    }

    override fun onEnable() {
        if (autoCenter.value) {
            EntityUtil.autoCenter()
        }
    }

    init {
        safeEventListener<CrystalSetDeadEvent> { event ->
            if (event.crystals.none { it.getDistanceSq(player) < 6.0 }) return@safeEventListener
            var placeCount = 0

            placing.runSynchronized {
                val iterator = values.iterator()
                while (iterator.hasNext()) {
                    val list = iterator.next()
                    var allPlaced = true

                    loop@ for (placeInfo in list) {
                        if (event.crystals.none {
                                CrystalUtils.placeBoxIntersectsCrystalBox(
                                    placeInfo.placedPos,
                                    it
                                )
                            }) continue

                        val long = placeInfo.placedPos.toLong()
                        if (placed.contains(long)) continue
                        allPlaced = false

                        if (System.currentTimeMillis() <= pendingPlacing[long]) continue
                        if (!checkRotation(placeInfo)) continue

                        placeBlock(placeInfo)
                        placeCount++
                        if (placeCount >= multiPlace.value) return@safeEventListener
                    }

                    if (allPlaced) iterator.remove()
                }
            }
        }

        safeEventListener<WorldEvent.ServerBlockUpdate> { event ->
            val pos = event.pos
            if (!event.newState.isReplaceable) {
                val long = pos.toLong()
                if (placingSet.contains(long)) {
                    pendingPlacing.remove(long)
                    placed.add(long)
                }
            } else {
                val relative = pos.subtract(player.betterPosition)
                if (SurroundOffset.values().any { it.offset == relative } && checkColliding(pos)) {
                    getNeighbor(pos)?.let { placeInfo ->
                        if (checkRotation(placeInfo)) {
                            placingSet.add(placeInfo.placedPos.toLong())
                            placeBlock(placeInfo)
                        }
                    }
                }
            }
        }

        safeEventListener<TickEvent.Pre> {
            enableTicks++
        }

        listener<StepEvent> {
            placing.clear()
            placingSet.clear()
            pendingPlacing.clear()
            placed.clear()
            holePos = null
        }

        safeEventListener<RunGameLoopEvent.Tick>(true) {
            if (groundCheck.value) {
                if (!player.onGround) {
                    if (isEnabled) disable()
                    return@safeEventListener
                }
            }

            var playerPos = player.betterPosition
            val isInHole =
                player.onGround && player.realSpeed < 0.1 && HoleManager.getHoleInfo(playerPos).type == HoleType.OBBY

            if (isDisabled) {
                enableInHoleCheck(isInHole)
                return@safeEventListener
            }

            if (world.getBlockState(playerPos.down()).getCollisionBoundingBox(world, playerPos) == null) {
                playerPos = world.getGroundPos(player).up()
            }

            if (isInHole || holePos == null) {
                holePos = playerPos
            }

            updatePlacingMap(playerPos)

            if (placing.isNotEmpty() && placeTimer.tickAndReset(placeDelay.value)) {
                runPlacing()
            }
        }
    }

    private fun enableInHoleCheck(isInHole: Boolean) {
        if (enableInHole.value && isInHole) {
            if (toggleTimer.tickAndReset(inHoleTimeout.value)) {
                enable()
            }
        } else {
            toggleTimer.reset()
        }
    }

    private fun SafeClientEvent.updatePlacingMap(playerPos: BlockPos) {
        pendingPlacing.runSynchronized {
            keys.removeIf {
                if (!world.getBlockState(BlockPos.fromLong(it)).isReplaceable) {
                    placed.add(it)
                    true
                } else {
                    false
                }
            }
        }

        if (placing.isEmpty() && (pendingPlacing.isEmpty() || pendingPlacing.runSynchronized { values.all { System.currentTimeMillis() > it } })) {
            placing.clear()
            placed.clear()
        }

        for (surroundOffset in SurroundOffset.values()) {
            val offsetPos = playerPos.add(surroundOffset.offset)
            if (!world.getBlockState(offsetPos).isReplaceable) continue

            getNeighborSequence(offsetPos, 2, 5.0f, strictDirection.value, false)?.let { list ->
                placing[surroundOffset] = list
                list.forEach {
                    placingSet.add(it.placedPos.toLong())
                }
            }
        }
    }

    private fun SafeClientEvent.runPlacing() {
        var placeCount = 0

        placing.runSynchronized {
            val iterator = placing.values.iterator()
            while (iterator.hasNext()) {
                val list = iterator.next()
                var allPlaced = true
                var breakCrystal = false

                loop@ for (placeInfo in list) {
                    val long = placeInfo.placedPos.toLong()
                    if (placed.contains(long)) continue
                    allPlaced = false

                    if (System.currentTimeMillis() <= pendingPlacing[long]) continue
                    if (!checkRotation(placeInfo)) continue

                    for (entity in EntityManager.entity) {
                        if (breakCrystal && entity is EntityEnderCrystal) continue
                        if (!entity.preventEntitySpawning) continue
                        if (!entity.isEntityAlive) continue
                        if (!entity.entityBoundingBox.intersects(AxisAlignedBB(placeInfo.placedPos))) continue
                        if (entity !is EntityEnderCrystal) continue@loop

                        connection.sendPacket(CPacketUseEntity(entity))
                        connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                        breakCrystal = true
                    }

                    placeBlock(placeInfo)
                    placeCount++
                    if (placeCount >= multiPlace.value) return
                }

                if (allPlaced) iterator.remove()
            }
        }
    }

    private fun SafeClientEvent.getNeighbor(pos: BlockPos): PlaceInfo? {
        for (side in EnumFacing.values()) {
            val offsetPos = pos.offset(side)
            val oppositeSide = side.getOpposite()

            if (strictDirection.value && !getVisibleSides(offsetPos, true).contains(oppositeSide)) continue
            if (world.getBlockState(offsetPos).isReplaceable) continue

            val hitVec = getHitVec(offsetPos, oppositeSide)
            val hitVecOffset = getHitVecOffset(oppositeSide)

            return PlaceInfo(offsetPos, oppositeSide, 0.0, hitVecOffset, hitVec, pos)
        }

        return null
    }

    private fun checkColliding(pos: BlockPos): Boolean {
        val box = AxisAlignedBB(pos)

        return EntityManager.entity.none {
            it.isEntityAlive && it.preventEntitySpawning && it.entityBoundingBox.intersects(box)
        }
    }

    private fun SafeClientEvent.placeBlock(placeInfo: PlaceInfo) {
        val slot = getSlot() ?: run {
            disable()
            return
        }

        val sneak = !player.isSneaking
        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

        if (rotation.value) {
            var eyeHeight = player.getEyeHeight()
            if (!player.isSneaking) eyeHeight -= 0.08f
            RotationManager.addRotations(getRotationTo(Vec3d(player.posX, player.posY + eyeHeight, player.posZ), placeInfo.hitVec))
        }
        spoofHotbar(slot) {
            connection.sendPacket(placeInfo.toPlacePacket(EnumHand.MAIN_HAND))
        }
        connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))

        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))

        onMainThreadSafe {
            val blockState = Blocks.OBSIDIAN.getStateForPlacement(
                world,
                placeInfo.pos,
                placeInfo.side,
                placeInfo.hitVecOffset.x,
                placeInfo.hitVecOffset.y,
                placeInfo.hitVecOffset.z,
                0,
                player,
                EnumHand.MAIN_HAND
            )
            val soundType = blockState.block.getSoundType(blockState, world, placeInfo.pos, player)
            world.playSound(
                player,
                placeInfo.pos,
                soundType.placeSound,
                SoundCategory.BLOCKS,
                (soundType.getVolume() + 1.0f) / 2.0f,
                soundType.getPitch() * 0.8f
            )
        }

        pendingPlacing[placeInfo.placedPos.toLong()] = System.currentTimeMillis() + 50L
    }

    private fun SafeClientEvent.getSlot(): HotbarSlot? {
        val slot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)

        return if (slot == null) {
            ChatUtil.NoSpam.sendMessage("No obsidian in hotbar!")
            null
        } else {
            slot
        }
    }

    private fun SafeClientEvent.checkRotation(placeInfo: PlaceInfo): Boolean {
        var eyeHeight = player.getEyeHeight()
        if (!player.isSneaking) eyeHeight -= 0.08f
        return !rotation.value || AxisAlignedBB(placeInfo.pos).isInSight(
            CrystalManager.position.add(
                0.0,
                eyeHeight.toDouble(),
                0.0
            )
        ) != null
    }

    private enum class SurroundOffset(val offset: BlockPos) {
        DOWN(BlockPos(0, -1, 0)),
        NORTH(BlockPos(0, 0, -1)),
        EAST(BlockPos(1, 0, 0)),
        SOUTH(BlockPos(0, 0, 1)),
        WEST(BlockPos(-1, 0, 0))
    }
}