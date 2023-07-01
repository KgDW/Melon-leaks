package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.manager.*
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.animations.Easing
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.block.BreakingUtil.Companion.hotbarSlots
import dev.zenhao.melon.utils.inventory.HotbarSlot
import dev.zenhao.melon.utils.math.RotationUtils.getRotationTo
import dev.zenhao.melon.utils.runIf
import dev.zenhao.melon.utils.vector.VectorUtils.toVec3d
import dev.zenhao.melon.utils.vector.distance
import it.unimi.dsi.fastutil.longs.Long2LongMaps
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2LongMaps
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import melon.events.Render3DEvent
import melon.events.RunGameLoopEvent
import melon.events.WorldEvent
import melon.system.event.SafeClientEvent
import melon.system.event.listener
import melon.system.event.safeConcurrentListener
import melon.system.util.color.ColorRGB
import melon.utils.TickTimer
import melon.utils.block.isReplaceable
import melon.utils.concurrent.threads.onMainThread
import melon.utils.concurrent.threads.runSynchronized
import melon.utils.entity.EntityUtils.eyePosition
import melon.utils.entity.EntityUtils.isFriend
import melon.utils.entity.EntityUtils.spoofSneak
import melon.utils.extension.fastPosDirection
import melon.utils.graphics.ESPRenderer
import melon.utils.hole.HoleType
import melon.utils.inventory.slot.firstBlock
import melon.utils.math.isInSight
import melon.utils.math.vector.distanceSqTo
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.*

@Module.Info(name = "HoleFiller", description = "Auto Hole Filling", category = Category.COMBAT)
class HoleFiller : Module() {
    private var bedrockHole = bsetting("BedrockHole", true)
    private var obbyHole = bsetting("ObbyHole", true)
    private var twoBlocksHole = bsetting("2BlocksHole", true)
    private var fourBlocksHole = bsetting("4BlocksHole", true)
    private var predictTicks = isetting("PredictTicks", 8, 0, 50)
    private var detectRange = fsetting("DetectRange", 5.0f, 0.0f, 16.0f)
    private var hRange = fsetting("HRange", 0.5f, 0.0f, 4.0f)
    private var vRange = fsetting("VRange", 3.0f, 0.0f, 8.0f)
    private var distanceBalance = fsetting("DistanceBalance", 1.0f, -5.0f, 5.0f)
    private var fillDelay = isetting("FillDelay", 50, 0, 1000)
    private var fillTimeout = isetting("FillTimeout", 100, 0, 1000)
    private var fillRange = fsetting("FillRange", 5.0f, 1.0f, 6.0f)
    private var rotation = bsetting("Rotation", true)
    private var webFill = bsetting("WebFill", false)
    private var targetColor = csetting("TargetColor", Color(32, 255, 32))
    private var otherColor = csetting("OtherColor", Color(255, 222, 32))
    private var filledColor = csetting("FilledColor", Color(255, 32, 32))

    private val placeMap = Long2LongMaps.synchronize(Long2LongOpenHashMap())
    private val updateTimer = TickTimer()
    private val placeTimer = TickTimer()

    private var holeInfos = emptyList<IntermediateHoleInfo>()
    private var nextHole: BlockPos? = null
    private val renderBlockMap = Object2LongMaps.synchronize(Object2LongOpenHashMap<BlockPos>())
    private val renderer = ESPRenderer().apply { aFilled = 33; aOutline = 233 }

    override fun onDisable() {
        holeInfos = emptyList()
        nextHole = null
        renderBlockMap.clear()
        renderer.replaceAll(Collections.emptyList())
    }

    init {
        listener<WorldEvent.ClientBlockUpdate> {
            if (!it.newState.isReplaceable) {
                placeMap.remove(it.pos.toLong())
                if (it.pos == nextHole) nextHole = null
                renderBlockMap.runSynchronized {
                    replace(it.pos, System.currentTimeMillis())
                }
            }
        }

        listener<Render3DEvent> {
            val list = ArrayList<ESPRenderer.Info>()
            renderBlockMap.runSynchronized {
                object2LongEntrySet().mapTo(list) {
                    val color = when {
                        it.key == nextHole -> targetColor.value
                        it.longValue == -1L -> otherColor.value
                        else -> filledColor.value
                    }
                    val c = ColorRGB(color)
                    if (it.longValue == -1L) {
                        ESPRenderer.Info(it.key, c)
                    } else {
                        val progress = Easing.IN_CUBIC.dec(Easing.toDelta(it.longValue, 1000L))
                        val size = progress * 0.5
                        val n = 0.5 - size
                        val p = 0.5 + size
                        val box = AxisAlignedBB(
                            it.key.x + n, it.key.y + n, it.key.z + n,
                            it.key.x + p, it.key.y + p, it.key.z + p,
                        )
                        ESPRenderer.Info(box, c.alpha((255.0f * progress).toInt()))
                    }
                }
            }
            renderer.replaceAll(list)
            renderer.render(false)
        }

        safeConcurrentListener<RunGameLoopEvent.Tick> {
            val slot =
                if (webFill.value && player.hotbarSlots.firstBlock(Blocks.WEB) != null) player.hotbarSlots.firstBlock(
                    Blocks.WEB
                ) else player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)
            val place = placeTimer.tick(fillDelay.value) && slot != null

            if (place || updateTimer.tickAndReset(25L)) {
                val newHoleInfo = getHoleInfos()
                holeInfos = newHoleInfo

                val current = System.currentTimeMillis()
                placeMap.runSynchronized {
                    values.removeIf { it <= current }
                    nextHole?.let {
                        if (!containsKey(it.toLong())) nextHole = null
                    }
                }

                if (place) {
                    getPos(newHoleInfo, rotation.value)?.let {
                        nextHole = it
                        placeBlock(slot!!, it)
                    }
                } else {
                    updatePosRender(newHoleInfo)
                }
            }
        }
    }

    private fun SafeClientEvent.updatePosRender(holeInfos: List<IntermediateHoleInfo>) {
        val sqRange = detectRange.value.sq
        val set = LongOpenHashSet()

        for (entity in EntityManager.players) {
            if (entity == player) continue
            if (!entity.isEntityAlive) continue
            if (entity == player) continue
            if (entity.isFriend) continue
            if (player.getDistanceSq(entity) > sqRange) continue

            val current = entity.positionVector
            val predict = entity.calcPredict(current)

            for (holeInfo in holeInfos) {
                if (entity.posY <= holeInfo.blockPos.y + 0.5) continue
                if (holeInfo.toward && holeInfo.playerDist - entity.horizontalDist(holeInfo.center) < distanceBalance.value) continue

                if (holeInfo.detectBox.contains(current)
                    || !holeInfo.toward
                    && (holeInfo.detectBox.contains(predict) || holeInfo.detectBox.calculateIntercept(
                        current,
                        predict
                    ) != null)
                ) {
                    set.add(holeInfo.blockPos.toLong())
                    renderBlockMap.putIfAbsent(holeInfo.blockPos, -1L)
                }
            }
        }

        renderBlockMap.runSynchronized {
            object2LongEntrySet().removeIf {
                it.longValue == -1L && !placeMap.containsKey(it.key.toLong()) && !set.contains(it.key.toLong())
            }
        }
    }

    private fun SafeClientEvent.getPos(holeInfos: List<IntermediateHoleInfo>, checkRotation: Boolean): BlockPos? {
        val sqRange = detectRange.value.sq

        val placeable = Object2FloatOpenHashMap<BlockPos>()

        for (entity in EntityManager.players) {
            if (entity == player) continue
            if (!entity.isEntityAlive) continue
            if (entity == player) continue
            if (FriendManager.isFriend(entity)) continue
            if (player.getDistanceSq(entity) > sqRange) continue

            val current = entity.positionVector
            val predict = entity.calcPredict(current)

            for (holeInfo in holeInfos) {
                if (entity.posY <= holeInfo.blockPos.y + 0.5) continue
                val dist = entity.horizontalDist(holeInfo.center)
                if (holeInfo.toward && holeInfo.playerDist - dist < distanceBalance.value) continue

                if (holeInfo.detectBox.contains(current)
                    || !holeInfo.toward
                    && (holeInfo.detectBox.contains(predict) || holeInfo.detectBox.calculateIntercept(
                        current,
                        predict
                    ) != null)
                ) {

                    placeable[holeInfo.blockPos] = dist.toFloat()
                    renderBlockMap.putIfAbsent(holeInfo.blockPos, -1L)
                }
            }
        }

        val eyePos = CrystalManager.position.add(0.0, player.getEyeHeight().toDouble(), 0.0)

        val targetPos = placeable.object2FloatEntrySet().asSequence()
            .runIf(checkRotation) {
                filter {
                    AxisAlignedBB(
                        it.key.x.toDouble(), it.key.y - 1.0, it.key.z.toDouble(),
                        it.key.x + 1.0, it.key.y.toDouble(), it.key.z + 1.0,
                    ).isInSight(eyePos, CrystalManager.rotation) != null
                }
            }
            .minByOrNull { it.floatValue }
            ?.key

        renderBlockMap.runSynchronized {
            object2LongEntrySet().removeIf {
                it.longValue == -1L && !placeMap.containsKey(it.key.toLong()) && !placeable.containsKey(it.key)
            }
        }

        return targetPos
    }

    private fun SafeClientEvent.getRotationPos(holeInfos: List<IntermediateHoleInfo>): BlockPos? {
        val sqRange = detectRange.value.sq

        var minDist = Double.MAX_VALUE
        var minDistPos: BlockPos? = null

        for (entity in EntityManager.players) {
            if (entity == player) continue
            if (!entity.isEntityAlive) continue
            if (entity == player) continue
            if (FriendManager.isFriend(entity)) continue
            if (player.getDistanceSq(entity) > sqRange) continue

            val current = entity.positionVector
            val predict = entity.calcPredict(current)

            for (holeInfo in holeInfos) {
                if (entity.posY <= holeInfo.blockPos.y + 0.5) continue

                val dist = entity.horizontalDist(holeInfo.center)
                if (dist >= minDist) continue
                if (holeInfo.toward && holeInfo.playerDist - dist < distanceBalance.value) continue

                if (holeInfo.detectBox.contains(current)
                    || !holeInfo.toward
                    && (holeInfo.detectBox.contains(predict) || holeInfo.detectBox.calculateIntercept(
                        current,
                        predict
                    ) != null)
                ) {

                    minDistPos = holeInfo.blockPos
                    minDist = dist
                }
            }
        }

        return minDistPos
    }

    private fun SafeClientEvent.getHoleInfos(): List<IntermediateHoleInfo> {
        val eyePos = player.eyePosition
        val rangeSq = fillRange.value.sq
        val entities = EntityManager.entity.filter {
            it.preventEntitySpawning && it.isEntityAlive
        }

        return HoleManager.holeInfos.asSequence()
            .filterNot {
                it.isFullyTrapped
            }
            .filter {
                when (it.type) {
                    HoleType.BEDROCK -> bedrockHole.value
                    HoleType.OBBY -> obbyHole.value
                    HoleType.TWO -> twoBlocksHole.value
                    HoleType.FOUR -> fourBlocksHole.value
                    else -> false
                }
            }
            .filter { holeInfo ->
                holeInfo.holePos.any {
                    eyePos.distanceSqTo(it) <= rangeSq
                }
            }
            .filter { holeInfo ->
                entities.none {
                    it.entityBoundingBox.intersects(holeInfo.boundingBox)
                }
            }
            .mapNotNull { holeInfo ->
                holeInfo.holePos.asSequence()
                    .filter { !placeMap.containsKey(it.toLong()) }
                    .minByOrNull { eyePos.distanceSqTo(it) }
                    ?.let {
                        val box = AxisAlignedBB(
                            holeInfo.boundingBox.minX - hRange.value,
                            holeInfo.boundingBox.minY,
                            holeInfo.boundingBox.minZ - hRange.value,
                            holeInfo.boundingBox.maxX + hRange.value,
                            holeInfo.boundingBox.maxY + vRange.value,
                            holeInfo.boundingBox.maxZ + hRange.value
                        )

                        if (player.entityBoundingBox.intersects(holeInfo.boundingBox)) {
                            null
                        } else {
                            val dist = player.horizontalDist(holeInfo.center)
                            val prevDist =
                                distance(player.lastTickPosX, player.lastTickPosZ, holeInfo.center.x, holeInfo.center.z)
                            IntermediateHoleInfo(
                                holeInfo.center,
                                it,
                                box,
                                dist,
                                dist - prevDist < -0.15
                            )
                        }
                    }
            }
            .toList()
    }

    private fun Entity.horizontalDist(vec3d: Vec3d): Double {
        return distance(this.posX, this.posZ, vec3d.x, vec3d.z)
    }

    private fun Entity.calcPredict(current: Vec3d): Vec3d {
        return if (predictTicks.value == 0) {
            current
        } else {
            Vec3d(
                this.posX + (this.posX - this.lastTickPosX) * predictTicks.value,
                this.posY + (this.posY - this.lastTickPosY) * predictTicks.value,
                this.posZ + (this.posZ - this.lastTickPosZ) * predictTicks.value
            )
        }
    }

    private fun SafeClientEvent.placeBlock(slot: HotbarSlot, pos: BlockPos) {
        val target = pos.down()

        onMainThread {
            if (rotation.value) {
                (nextHole ?: getRotationPos(holeInfos))?.let {
                    RotationManager.addRotationsNew(it)
                }
            }
            player.spoofSneak {
                spoofHotbar(slot) {
                    connection.sendPacket(fastPosDirection(target))
                }
            }

            connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }

        placeMap[pos.toLong()] = System.currentTimeMillis() + fillTimeout.value
        placeTimer.reset()
    }

    private class IntermediateHoleInfo(
        val center: Vec3d,
        val blockPos: BlockPos,
        val detectBox: AxisAlignedBB,
        val playerDist: Double,
        val toward: Boolean
    )
}