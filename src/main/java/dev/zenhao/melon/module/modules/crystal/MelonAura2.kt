package dev.zenhao.melon.module.modules.crystal

import dev.zenhao.melon.manager.*
import dev.zenhao.melon.manager.HotbarManager.serverSideItem
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.HotbarManager.spoofHotbarBypass
import dev.zenhao.melon.mixin.client.accessor.AccessorEntityItem
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.chat.AutoGG
import dev.zenhao.melon.module.modules.combat.AutoEXP
import dev.zenhao.melon.module.modules.combat.CevBreaker
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.calcDamage
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.isResistant
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.calcCollidingCrystalDamageOld
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.canMove
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.checkBreakRange
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.getCrystalPlacingBB
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.getCrystalSlot
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.isReplaceable
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.scaledHealth
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.totalHealth
import dev.zenhao.melon.module.modules.player.PacketMine
import dev.zenhao.melon.setting.IntegerSetting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.Easing
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.entity.CrystalUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.extension.ceilToInt
import dev.zenhao.melon.utils.extension.synchronized
import dev.zenhao.melon.utils.inventory.HotbarSlot
import dev.zenhao.melon.utils.inventory.InventoryUtil
import dev.zenhao.melon.utils.math.RotationUtils
import dev.zenhao.melon.utils.math.RotationUtils.getRotationTo
import dev.zenhao.melon.utils.threads.runAsyncThread
import dev.zenhao.melon.utils.vector.Vec2f
import it.unimi.dsi.fastutil.ints.Int2LongMaps
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import melon.events.*
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.system.event.safeParallelListener
import melon.system.render.font.renderer.MainFontRenderer
import melon.system.render.graphic.ProjectionUtils
import melon.system.util.color.ColorRGB
import melon.utils.combat.CrystalUtils
import melon.utils.combat.CrystalUtils.crystalPlaceBoxIntersectsCrystalBox
import melon.utils.combat.ExposureSample
import melon.utils.concurrent.threads.runSynchronized
import melon.utils.entity.EntityUtils.isNotDead
import melon.utils.graphics.ESPRenderer
import melon.utils.inventory.slot.filterByStack
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.inventory.slot.swapToSlot
import melon.utils.item.attackDamage
import melon.utils.item.duraPercentage
import melon.utils.math.vector.distanceSq
import melon.utils.math.vector.distanceSqTo
import melon.utils.math.vector.toVec3d
import melon.utils.math.vector.toVec3dCenter
import melon.utils.player.MovementUtils.realSpeed
import melon.utils.player.breakCrystal
import melon.utils.world.getMiningSide
import melon.utils.world.noCollision
import melon.utils.world.rayTraceVisible
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityEgg
import net.minecraft.entity.projectile.EntitySnowball
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemAppleGold
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.*
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.stream.Collectors
import kotlin.math.*

/**
 * Created by zenhao on 18/12/2022.
 * Updated by zenhao on 04/03/2023.
 */
@Module.Info(name = "MelonAura2", category = Category.XDDD)
object MelonAura2 : Module() {
    private var p = msetting("Page", Page.GENERAL)

    //Page GENERAL
    private var switchmode0 = msetting("SwitchMode", Switch.PacketSpoof).m(p, Page.GENERAL)
    private var switchMode = (switchmode0.value as Switch)
    private var antiWeakness = msetting("AntiWeakness", AntiWeaknessMode.Spoof).m(p, Page.GENERAL)
    private var swingMode = msetting("Swing", SwingMode.Off).m(p, Page.GENERAL)
    private val strictDirection = bsetting("StrictDirection", false).m(p, Page.GENERAL)
    private var rotate = bsetting("Rotate", false).m(p, Page.GENERAL)
    private val yawSpeed = fsetting("YawSpeed", 30.0f, 5.0f, 180f, 1f).b(rotate).m(p, Page.GENERAL)
    private var rotateDiff = isetting("RotationDiff", 2, 0, 180).b(rotate).m(p, Page.GENERAL)

    //Page Place
    private var packetPlaceMode = msetting("PacketMode", PacketPlaceMode.Strong).m(p, Page.PLACE)
    private var packetPlace0 = (packetPlaceMode.value as PacketPlaceMode)
    private var newPlace = bsetting("1.13Place", false).m(p, Page.PLACE)
    private var placeSwing = bsetting("PlaceSwing", false).m(p, Page.PLACE)
    private var placeDelay = isetting("PlaceDelay", 45, 1, 1000).m(p, Page.PLACE)
    private var placeRange = dsetting("PlaceRange", 5.5, 1.0, 6.0).m(p, Page.PLACE)
    private var placeMinDmg = dsetting("PlaceMinDmg", 4.0, 0.0, 36.0).m(p, Page.PLACE)
    private var placeMaxSelf = isetting("PlaceMaxSelfDmg", 10, 0, 36).m(p, Page.PLACE)
    private var placeBalance = fsetting("PlaceBalance", -3f, -10f, 10f).m(p, Page.PLACE)
    private val safeRange = dsetting("SafeRange", 1.0, 0.0, 10.0, 0.1).m(p, Page.PLACE)
    private val safeThreshold = dsetting("SafeThreshold", 2.0, 0.0, 10.0, 0.1).m(p, Page.PLACE)

    //Page Break
    private var packetExplode = bsetting("PacketExplode", true).m(p, Page.BREAK)
    private var hitDelay = isetting("HitDelay", 55, 0, 500, 1).m(p, Page.BREAK)
    private var predictHitFactor = isetting("PredictHitFactor", 0, 0, 20).m(p, Page.BREAK)
    private var breakRange = fsetting("BreakRange", 5.5f, 1f, 6f).m(p, Page.BREAK)
    private var breakMinDmg = dsetting("BreakMinDmg", 1.0, 0.0, 36.0).m(p, Page.BREAK)
    private var breakMaxSelf = isetting("BreakMaxSelf", 12, 0, 36).m(p, Page.BREAK)
    private val breakBalance = fsetting("BreakBalance", -7.0f, -10.0f, 10.0f).m(p, Page.BREAK)

    //Page Calculation
    private var maxTargets = isetting("MaxTarget", 3, 1, 8).m(p, Page.CALCULATION)
    private var motionPredict = bsetting("MotionPredict", true).m(p, Page.CALCULATION)
    private var predictTicks = isetting("PredictTicks", 12, 1, 20).b(motionPredict).m(p, Page.CALCULATION)
    private var debug = bsetting("Debug", false).m(p, Page.CALCULATION)
    private var enemyRange = isetting("EnemyRange", 8, 1, 10).m(p, Page.CALCULATION)
    private var noSuicide = fsetting("NoSuicide", 2f, 0f, 20f).m(p, Page.CALCULATION)
    private var wallRange = fsetting("WallRange", 3f, 0f, 8f).m(p, Page.CALCULATION)
    var ownPredictTicks: IntegerSetting = isetting("OwnPredictTicks", 2, 0, 20).m(p, Page.CALCULATION)

    //Page Force
    private var slowFP = bsetting("SlowFacePlace", true).m(p, Page.FORCE)
    private var fpDelay = isetting("FacePlaceDelay", 350, 1, 750).b(slowFP).m(p, Page.FORCE)
    private val forcePlaceBalance = fsetting("ForcePlaceBalance", -1.5f, -10.0f, 10.0f).m(p, Page.FORCE)
    private var forceHealth = isetting("ForceHealth", 6, 0, 20).m(p, Page.FORCE)
    private var forcePlaceMotion = fsetting("ForcePlaceMotion", 5f, 0.25f, 10f).m(p, Page.FORCE)
    var forcePlaceDmg = dsetting("ForcePlaceDamage", 0.5, 0.1, 10.0).m(p, Page.FORCE)
    private var armorRate = isetting("ForceArmor%", 25, 0, 100).m(p, Page.FORCE)
    private val armorDdos = bsetting("ArmorDdos", false).m(p, Page.FORCE)
    private val ddosMinDamage = fsetting("DdosMinDamage", 1.5f, 0.0f, 10.0f).b(armorDdos).m(p, Page.FORCE)
    private val ddosQueueSize = isetting("DdosQueueSize", 5, 0, 10).b(armorDdos).m(p, Page.FORCE)
    private val ddosDamageStep = fsetting("DdosDamageStep", 0.1f, 0.1f, 5.0f).b(armorDdos).m(p, Page.FORCE)

    //Page Lethal
    private var lethalOverride = bsetting("LethalOverride", true).m(p, Page.LETHAL)
    private var lethalBalance = fsetting("LethalBalance", 0.5f, -5f, 5f, 0.1f).b(lethalOverride).m(p, Page.LETHAL)
    private var lethalMaxDamage = fsetting("LethalMaxDamage", 16f, 0f, 20f, 0.1f).b(lethalOverride).m(p, Page.LETHAL)
    private var antiSurround = bsetting("AntiSurround", false).m(p, Page.LETHAL)
    private var dropsPrio = bsetting("DropsPrio", false).m(p, Page.LETHAL)
    private var chainPop = bsetting("ChainPop", true).m(p, Page.LETHAL)
    private var chainPopFactor = isetting("ChainPopFactor", 6, 1, 8).b(chainPop).m(p, Page.LETHAL)
    private var chainPopRange = isetting("ChainPopRange", 3, 1, 6).b(chainPop).m(p, Page.LETHAL)
    private var chainPopDamage = dsetting("ChainPopDamage", 4.0, 0.1, 20.0).b(chainPop).m(p, Page.LETHAL)
    private var chainPopTime = isetting("ChainPopTime", 185, 1, 1000).b(chainPop).m(p, Page.LETHAL)

    //Page Render
    private var hudState = bsetting("HUDState", false).m(p, Page.RENDER)
    private var renderDamage = bsetting("RenderDamage", true).m(p, Page.RENDER)
    private var motionRender = bsetting("MotionRender", true).m(p, Page.RENDER)
    private var fadeRender = bsetting("FadeRender", false).m(p, Page.RENDER)
    private var fadeAlpha = isetting("FadeAlpha", 80, 0, 255, 1).b(fadeRender).m(p, Page.RENDER)
    private var textSize = fsetting("TextSize", 2.5f, 0.1f, 10f).b(renderDamage).m(p, Page.RENDER)
    private var color = csetting("Color", Color(20, 225, 219)).m(p, Page.RENDER)
    private val filledAlpha = isetting("FilledAlpha", 80, 0, 255).m(p, Page.RENDER)
    private val outlineAlpha = isetting("OutlineAlpha", 200, 0, 255).m(p, Page.RENDER)
    private val movingLength = isetting("MovingLength", 400, 0, 1000).m(p, Page.RENDER)
    private val fadeLength = isetting("FadeLength", 200, 0, 1000).m(p, Page.RENDER)

    private var offsetFacing = arrayOf(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST)
    private var renderQueue = Object2ObjectArrayMap<BlockPos, CrystalFadeRender>().synchronized()
    private var popList = Object2ObjectArrayMap<CrystalChainPop, Long>().synchronized()
    private val attackedCrystalMap = Int2LongMaps.synchronize(Int2LongOpenHashMap())
    private var ddosQueue = ConcurrentLinkedDeque<BlockPos>()
    var renderEnt: EntityLivingBase? = null
    val EntityItem.health: Int
        get() = (this as AccessorEntityItem).melonGetEntityItemHealth()

    private var packetExplodeTimerUtils = TimerUtils()
    private var explodeTimerUtils = TimerUtils()
    private var breakDropsTimer = TimerUtils()
    private var placeTimerUtils = TimerUtils()
    private var calcTimerUtils = TimerUtils()
    private val timeoutTimer = TimerUtils()
    private var fpTimer = TimerUtils()

    var crystalState = AtomicReference<CurrentState>().apply { CurrentState.Waiting }
    private var crystalInteracting: EntityEnderCrystal? = null
    private var lastEntityID = AtomicInteger(-1)
    var rotationInfo = RotationInfo(Vec2f.ZERO)
    private var placeInfo: PlaceInfo? = null
    private var render: BlockPos? = null
    private var canPredictHit = false
    private var isFacePlacing = false
    private var prioNeeded = false
    private var ddosArmor = false
    private var flagged = false
    private var damageCA = 0.0
    private var cSlot = -1

    //RenderNew
    private var lastBlockPos: BlockPos? = null
    private var lastRenderPos: Vec3d? = null
    private var prevPos: Vec3d? = null
    private var currentPos: Vec3d? = null
    private var lastTargetDamage = 0.0
    private var lastUpdateTime = 0L
    private var startTime = 0L
    private var scale = 0.0f

    init {
        onRender3D {
            updateFade(render)
            onRender3D(placeInfo)
        }

        onRender2D {
            onRender2D()
        }

        onPacketSend { event ->
            when (event.packet) {
                is CPacketPlayerDigging -> {
                    val action = event.packet.action
                    if (action == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK || action == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        doAntiSurround(event.packet.position)
                    }
                }

                is CPacketUseEntity -> {
                    world.getEntityByID(event.packet.entityId)?.let {
                        if (event.packet.action == CPacketUseEntity.Action.ATTACK && it is EntityEnderCrystal) {
                            crystalState.set(CurrentState.Breaking)
                            crystalInteracting = it
                        }
                    }
                }

                is CPacketPlayerTryUseItemOnBlock -> {
                    placeInfo?.let {
                        if (event.packet.pos == it.blockPos) {
                            crystalState.set(CurrentState.Placing)
                        }
                    }
                }
            }
        }

        onPacketReceive { event ->
            when (event.packet) {
                is SPacketSpawnObject -> {
                    val packet = event.packet
                    if (predictHitFactor.value > 0) {
                        ArrayList(world.loadedEntityList).forEach {
                            if (it is EntityItem || it is EntityArrow || it is EntityEnderPearl || it is EntitySnowball || it is EntityEgg) {
                                if (it.getDistance(packet.x, packet.y, packet.z) <= 6) {
                                    lastEntityID.set(-1)
                                    canPredictHit = false
                                    event.cancelled = true
                                }
                            }
                        }
                    }
                    if (packet.type == 51 && !event.cancelled) {
                        lastEntityID.getAndUpdate { it.coerceAtLeast(packet.entityID) }
                        val mutableBlockPos = MutableBlockPos()
                        if (checkBreakRange(
                                packet.x,
                                packet.y,
                                packet.z,
                                breakRange.value.toFloat(),
                                wallRange.value,
                                mutableBlockPos
                            )
                        ) {
                            if (packetExplode.value && packetExplodeTimerUtils.tickAndReset(hitDelay.value) && !flagged) {
                                placeInfo?.let {
                                    if (crystalPlaceBoxIntersectsCrystalBox(it.blockPos, packet.x, packet.y, packet.z)
                                        || checkBreakDamage(packet.x, packet.y, packet.z, mutableBlockPos)
                                    ) {
                                        runExplode(packet.entityID) {
                                            doRotate(CurrentState.Breaking)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        lastEntityID.set(-1)
                    }
                }

                is SPacketSoundEffect -> {
                    val placeInfo = placeInfo
                    if (event.packet.getSound() == SoundEvents.ENTITY_EXPERIENCE_BOTTLE_THROW || event.packet.getSound() == SoundEvents.ENTITY_ARROW_SHOOT || event.packet.getSound() == SoundEvents.ENTITY_ITEM_BREAK) {
                        canPredictHit = false
                    }
                    if (event.packet.getSound() === SoundEvents.ENTITY_GENERIC_EXPLODE) {
                        if (placeInfo != null) {
                            placeInfo.let {
                                if (distanceSq(
                                        placeInfo.blockPos.x + 0.5,
                                        placeInfo.blockPos.y + 1.0,
                                        placeInfo.blockPos.z + 0.5,
                                        event.packet.x,
                                        event.packet.y,
                                        event.packet.z
                                    ) <= 144.0
                                ) {
                                    if (packetPlace0.onRemove) {
                                        doPlace(it.blockPos) {
                                            doRotate(CurrentState.Placing)
                                        }
                                        if (debug.value) {
                                            ChatUtil.sendMessage("Debug Remove")
                                        }
                                    }
                                    attackedCrystalMap.clear()
                                }
                            }
                        } else if (player.distanceSqTo(event.packet.x, event.packet.y, event.packet.z) <= 144.0) {
                            attackedCrystalMap.clear()
                        }
                    }
                }

                is SPacketSpawnExperienceOrb -> {
                    lastEntityID.set(-1)
                    canPredictHit = false
                }

                is SPacketSpawnPainting -> {
                    lastEntityID.set(-1)
                    canPredictHit = false
                }

                is SPacketEntityStatus -> {
                    if (event.packet.opCode.toInt() == 35) {
                        val entity = event.packet.getEntity(world)
                        if (EntityUtil.isValid(entity, enemyRange.value.toDouble())) {
                            if (entity is EntityLivingBase) {
                                placeInfo?.let {
                                    if (chainPop.value) {
                                        popList[CrystalChainPop(entity, it.blockPos, damageCA)] =
                                            System.currentTimeMillis()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        onMotion {
            packetPlace0 = (packetPlaceMode.value as PacketPlaceMode)
            switchMode = (switchmode0.value as Switch)
            placeInfo = calcPlaceInfo()
            cSlot = InventoryUtil.findHotbarItem(Items.END_CRYSTAL)
            if (CevBreaker.isEnabled) {
                if (CevBreaker.target != null && CevBreaker.target == renderEnt) {
                    return@onMotion
                }
            }
            if (timeoutTimer.tickAndReset(5L)) {
                updateTimeouts()
            }
            if (prioNeeded && dropsPrio.value && breakDropsTimer.tickAndReset(hitDelay.value)) {
                placeInfo?.let {
                    if (it.dropsItem) {
                        breakCrystal(null, rotate.value)
                    }
                }
            }
            placeInfo?.let { placeInfo ->
                placeInfo.target.let { target ->
                    if (target.isEntityAlive && (!ddosArmor || System.currentTimeMillis() - CombatManager.getHurtTime(
                            target
                        ) !in 450L..500L)
                    ) {
                        doRotate()
                        doBreak()
                        doPlace()
                    }
                }
            }
        }

        safeEventListener<WorldEvent.ClientBlockUpdate>(114514) {
            if (player.distanceSqTo(it.pos) < (placeRange.value.ceilToInt() + 1).sq
                && isResistant(it.oldState) != isResistant(it.newState)
            ) {
                placeInfo = null
            }
        }

        safeEventListener<ConnectionEvent.Disconnect> {
            if (predictHitFactor.value != 0) {
                toggle()
            }
        }

        safeParallelListener<TickEvent.Pre> {
            updateDdosQueue()
            for (target in EntityManager.players) {
                if (EntityUtil.isntValid(target, placeRange.value)) continue
                if (PacketMine.isEnabled && PacketMine.currentPos != null) {
                    val holeInfo = HoleManager.getHoleInfo(target)
                    if ((holeInfo.isHole && holeInfo.surroundPos.contains(PacketMine.currentPos)) || (BlockUtil.canBreak(
                            target.position,
                            false
                        ) && PacketMine.currentPos == target.position)
                    ) {
                        doAntiSurround(PacketMine.currentPos)
                    }
                }
            }
            if (popList.isNotEmpty()) {
                popList.forEach {
                    if (!world.playerEntities.contains(it.key.target)) {
                        popList.remove(it.key, it.value)
                    }
                    it.key.target?.let { e ->
                        if (player.getDistanceSq(e) > enemyRange.value.sq) {
                            popList.remove(it.key, it.value)
                        }
                    }
                }
            }
        }

        safeEventListener<CrystalSpawnEvent> { event ->
            ddosQueue.peekFirst()?.let {
                if (event.crystalDamage.blockPos == it && armorDdos.value) {
                    if (debug.value) {
                        ChatUtil.sendMessage("DDOS")
                    }
                    ddosQueue.pollFirst()
                }
            }
        }
    }

    private fun SafeClientEvent.doRotate(tempState: CurrentState? = null, tempPos: BlockPos? = null) {
        tempState?.let {
            crystalState.set(it)
        }
        val rotation = when (crystalState.get()) {
            CurrentState.Placing -> {
                tempPos?.let {
                    getLegitRotations(it.add(0.5, 0.5, 0.5))
                } ?: placeInfo?.let {
                    getLegitRotations(it.blockPos.add(0.5, 0.5, 0.5))
                }
            }

            CurrentState.Breaking -> {
                placeInfo?.let {
                    getLegitRotations(it.blockPos.add(0.5, 0.5, 0.5))
                } ?: crystalInteracting?.let {
                    getRotationTo(it.positionVector)
                }
            }

            else -> {
                rotationInfo.reset()
                null
            }
        }
        rotation?.let {
            val diff = RotationUtils.calcAngleDiff(it.x, CrystalManager.rotation.x)
            if (rotate.value) {
                rotationInfo.update(rotation)
                if (abs(diff) <= yawSpeed.value) {
                    RotationManager.addRotations(it)
                } else {
                    val clamped = diff.coerceIn(-yawSpeed.value, yawSpeed.value)
                    val newYaw = RotationUtils.normalizeAngle(CrystalManager.rotation.x + clamped)
                    RotationManager.addRotations(newYaw, it.y)
                }
                flagged = rotateDiff.value > 0 && abs(diff) > rotateDiff.value
            }
        }
    }

    fun SafeClientEvent.getLegitRotations(blockPos: BlockPos): Vec2f {
        val eyesPos = CrystalManager.eyePosition
        val diffX = blockPos.getX() - eyesPos.x
        val diffY = blockPos.getY() - eyesPos.y
        val diffZ = blockPos.getZ() - eyesPos.z
        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
        var pitch = -Math.toDegrees(atan2(diffY, diffXZ)).toFloat()
        if (pitch > 90.0f) {
            pitch = 90.0f
        } else if (pitch < -90.0f) {
            pitch = -90.0f
        }
        return Vec2f(
            player.rotationYaw
                    + MathHelper.wrapDegrees(yaw - player.rotationYaw),
            player.rotationPitch + MathHelper
                .wrapDegrees(pitch - player.rotationPitch)
        )
    }

    private fun SafeClientEvent.doAntiSurround(pos: BlockPos?) {
        if (antiSurround.value) {
            if (EntityManager.players.isEmpty()) return
            if (pos == null) return
            for (target in EntityManager.players) {
                if (EntityUtil.isntValid(target, placeRange.value)) continue
                if (target.positionVector.y != pos.getY().toDouble()) continue
                val burrowPos = target.position
                val holeInfo = HoleManager.getHoleInfo(target)
                val finalPos = if (BlockUtil.canBreak(burrowPos, false)) {
                    burrowPos
                } else {
                    pos
                }
                val isBurrowPos = finalPos == burrowPos
                for (facing in offsetFacing) {
                    if (!holeInfo.isHole && !isBurrowPos) continue
                    val placePos = finalPos.offset(facing)
                    if (!getAntiSurroundPos(placePos)) continue
                    if (!world.noCollision(placePos)) continue
                    if (debug.value) {
                        ChatUtil.sendMessage("AntiSurrounding")
                    }
                    if ((switchMode.onSwitch && (player.heldItemMainhand.item == Items.END_CRYSTAL || player.heldItemOffhand.item == Items.END_CRYSTAL)) || ((switchMode.onSpoof || switchMode.onBypassSpoof) && cSlot > 0)) {
                        doPlace(placePos.down()) {
                            doRotate(CurrentState.Placing, placePos.down())
                        }
                        render = placePos.down()
                    }
                    break
                }
            }
        }
    }

    private fun SafeClientEvent.getAntiSurroundPos(posOffset: BlockPos): Boolean {
        return world.isAirBlock(posOffset) && world.isAirBlock(posOffset.up()) && canPlaceCrystal(
            posOffset.down(),
            newPlace.value
        )
    }

    private fun updateTimeouts() {
        val current = System.currentTimeMillis()
        attackedCrystalMap.runSynchronized {
            values.removeIf {
                it < current
            }
        }
    }

    private fun checkPlaceCollision(placeInfo: PlaceInfo): Boolean {
        return EntityManager.entity.asSequence()
            .filterIsInstance<EntityEnderCrystal>()
            .filter { it.isEntityAlive }
            .filter { crystalPlaceBoxIntersectsCrystalBox(placeInfo.blockPos, it) }
            .filterNot { attackedCrystalMap.containsKey(it.entityId) }
            .none()
    }

    private fun SafeClientEvent.doPlace(tempPos: BlockPos? = null, rotationInvoke: (() -> Unit)? = null) {
        runCatching {
            var offhand = false
            if (mc.player.heldItemOffhand.getItem() === Items.END_CRYSTAL) {
                offhand = true
            }
            if (tempPos == null) {
                if (placeInfo != null) {
                    placeInfo?.let {
                        if (checkPlaceCollision(it)) {
                            renderEnt = if (!it.dropsItem) it.target else null
                            render = if (armorDdos.value && ddosQueue.isNotEmpty() && !it.dropsItem) {
                                ddosQueue.peekFirst()
                            } else {
                                it.blockPos
                            }
                        }
                    }
                } else {
                    renderEnt = null
                    render = null
                    popList.clear()
                    crystalState.set(CurrentState.Waiting)
                }
            } else {
                render = tempPos
            }
            if ((renderEnt == null && tempPos == null) || render == null) {
                renderEnt = null
                render = null
                crystalState.set(CurrentState.Waiting)
                return
            }
            rotationInvoke?.invoke() ?: doRotate(CurrentState.Placing)
            if (switchMode.onSwitch && placeInfo != null) {
                if (!offhand && player.inventory.currentItem != cSlot && cSlot > 0) {
                    if (player.heldItemMainhand.getItem() is ItemAppleGold && player.isHandActive) {
                        return
                    }
                    player.inventory.currentItem = cSlot
                    playerController.updateController()
                    return
                }
            }
            if (slowFP.value && isFacePlacing && renderEnt != null) {
                if (!fpTimer.passed(fpDelay.value)) {
                    return
                }
                fpTimer.reset()
            }
            placeInfo?.let { placeInfo ->
                val packet = placePacket(placeInfo, if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND)
                if (checkPlaceCollision(placeInfo)) {
                    if (placeTimerUtils.tickAndReset(placeDelay.value)) {
                        if (switchMode.onSpoof || switchMode.onBypassSpoof) {
                            val slot = player.getCrystalSlot() ?: return
                            if (switchMode.onSpoof) {
                                spoofHotbar(slot) {
                                    connection.sendPacket(packet)
                                }
                            } else if (switchMode.onBypassSpoof) {
                                spoofHotbarBypass(slot) {
                                    connection.sendPacket(packet)
                                }
                            }
                        } else {
                            connection.sendPacket(packet)
                        }
                        if (placeSwing.value) {
                            swingArm()
                        }
                        if (predictHitFactor.value != 0 && renderEnt != null) {
                            if (!canPredictHit) {
                                placeTimerUtils.reset()
                                return
                            }
                            if (player.health + player.absorptionAmount > placeMaxSelf.value && lastEntityID.get() != -1 && canPredictHit) {
                                val syncedId = lastEntityID.get()
                                for (spam in 0 until predictHitFactor.value) {
                                    if (syncedId != -1 && player.getDistance(world.getEntityByID(syncedId)!!) <= breakRange.value) {
                                        runExplode(syncedId + spam + 1) {
                                            doRotate(CurrentState.Breaking)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun placePacket(placeInfo: PlaceInfo, hand: EnumHand): CPacketPlayerTryUseItemOnBlock {
        return CPacketPlayerTryUseItemOnBlock(
            placeInfo.blockPos,
            placeInfo.side,
            hand,
            placeInfo.hitVecOffset.x,
            placeInfo.hitVecOffset.y,
            placeInfo.hitVecOffset.z
        )
    }

    fun SafeClientEvent.getPlaceSide(pos: BlockPos): EnumFacing {
        return if (strictDirection.value) {
            getMiningSide(pos) ?: EnumFacing.UP
        } else {
            EnumFacing.UP
        }
    }

    private fun SafeClientEvent.calcPlaceInfo(): PlaceInfo? {
        val placeInfo: PlaceInfo.Mutable?
        val chainPopPlaceInfo = PlaceInfo.Mutable(player)
        val normal = PlaceInfo.Mutable(player)
        val lethal = PlaceInfo.Mutable(player)
        val drops = PlaceInfo.Mutable(player)
        val safe = PlaceInfo.Mutable(player)
        val targets = targetList.toList()
        val feetPos = CrystalManager.position
        val eyePos = CrystalManager.eyePosition
        if (targets.isEmpty()) return null

        val context = CombatManager.contextSelf ?: return null

        val mutableBlockPos = MutableBlockPos()
        val targetBlocks = rendertions(placeRange.value)
        if (targetBlocks.isEmpty()) return null
        if (cSlot < 0 && player.heldItemOffhand.item != Items.END_CRYSTAL) {
            return null
        }
        for (blockPos in targetBlocks) {
            val placeBox = CrystalUtils.getCrystalPlacingBB(blockPos)
            val crystalX = blockPos.getX() + 0.5
            val crystalY = blockPos.getY() + 1.0
            val crystalZ = blockPos.getZ() + 0.5
            val selfDamage = max(
                context.calcDamage(crystalX, crystalY, crystalZ, false, mutableBlockPos),
                context.calcDamage(crystalX, crystalY, crystalZ, true, mutableBlockPos)
            ).toDouble()
            val collidingDamage = calcCollidingCrystalDamageOld(CombatManager.crystalList, placeBox)
            if (world.isOutsideBuildHeight(blockPos) || !world.worldBorder.contains(blockPos)) continue
            if (feetPos.squareDistanceTo(
                    crystalX,
                    crystalY,
                    crystalZ
                ) > wallRange.value.sq && !world.rayTraceVisible(
                    eyePos,
                    crystalX,
                    crystalY + 1.7,
                    crystalZ,
                    20,
                    mutableBlockPos
                )
            ) continue

            if (player.scaledHealth - selfDamage <= noSuicide.value) continue
            if (player.scaledHealth - collidingDamage <= noSuicide.value) continue
            if (!lethalOverride.value && selfDamage > placeMaxSelf.value) continue

            for ((target, targetPos, targetBox, currentPos) in targets) {
                if (target is EntityItem) {
                    val damageToDrops = calcDamage(
                        targetPos,
                        targetBox,
                        blockPos.x + 0.5,
                        (blockPos.y + 1).toDouble(),
                        blockPos.z + 0.5,
                        MutableBlockPos()
                    ).toDouble()
                    if (damageToDrops - target.health < 0.0) continue
                    drops.update(null, blockPos, selfDamage, damageToDrops, true)
                }
                if (target != player && target is EntityLivingBase) {
                    canPredictHit =
                        target.heldItemMainhand.getItem() != Items.EXPERIENCE_BOTTLE && target.heldItemOffhand.getItem() != Items.EXPERIENCE_BOTTLE || AutoEXP.isDisabled

                    if (targetBox.intersects(placeBox)) continue
                    if (placeBox.intersects(targetPos, currentPos)) continue

                    val targetDamage = calcDamage(
                        target,
                        targetPos,
                        targetBox,
                        blockPos.x + 0.5,
                        (blockPos.y + 1).toDouble(),
                        blockPos.z + 0.5,
                        mutableBlockPos
                    ).toDouble()
                    if (lethalOverride.value && targetDamage - player.totalHealth > lethalBalance.value && selfDamage < lethal.selfDamage && selfDamage <= lethalMaxDamage.value) {
                        lethal.update(target, blockPos, selfDamage, targetDamage)
                    }
                    if (selfDamage > placeMaxSelf.value) continue

                    damageCA = targetDamage
                    if (popList.isNotEmpty() && chainPop.value) {
                        popList.forEach {
                            if (it.key.target != null && it.key.target == target) {
                                if (System.currentTimeMillis() - it.value < chainPopTime.value) {
                                    for (pos in rendertions(placeRange.value)) {
                                        if (targetDamage / chainPopFactor.value > it.key.dmg) continue
                                        if (pos == it.key.targetPos) continue
                                        if (targetDamage > chainPopDamage.value) continue
                                        if (it.key.target.entityBoundingBox.intersects(getCrystalPlacingBB(pos))) continue
                                        if (getCrystalPlacingBB(pos).intersects(targetPos, Vec3d(pos))) continue
                                        if (it.key.target.getDistanceSq(pos) > enemyRange.value.sq) continue
                                        if (player.getDistanceSq(pos) > chainPopRange.value.sq) continue
                                        popList.remove(it.key, it.value)
                                        chainPopPlaceInfo.update(
                                            it.key.target,
                                            pos,
                                            selfDamage,
                                            targetDamage
                                        )
                                    }
                                }
                            }
                        }
                    }
                    val holeInfo = HoleManager.getHoleInfo(target)
                    isFacePlacing = holeInfo.isHole && !ddosArmor

                    val minDamage: Double
                    val balance: Float

                    if (shouldForcePlace(target)) {
                        minDamage = forcePlaceDmg.value
                        balance = forcePlaceBalance.value
                    } else {
                        minDamage = placeMinDmg.value
                        balance = placeBalance.value
                    }

                    if (targetDamage >= minDamage && targetDamage - selfDamage >= balance) {
                        if (targetDamage > normal.targetDamage) {
                            normal.update(target, blockPos, selfDamage, targetDamage)
                        } else if (normal.targetDamage - targetDamage <= safeRange.value
                            && normal.selfDamage - selfDamage >= safeThreshold.value
                        ) {
                            safe.update(target, blockPos, selfDamage, targetDamage)
                        }
                    }
                    if (ModuleManager.getModuleByClass(AutoGG::class.java).isEnabled) {
                        (ModuleManager.getModuleByClass(AutoGG::class.java) as AutoGG).addTargetedPlayer(target.name)
                    }
                }
            }
        }

        if (normal.targetDamage - safe.targetDamage > safeRange.value
            || normal.selfDamage - safe.selfDamage <= safeThreshold.value
        ) {
            safe.clear(player)
        }

        placeInfo = drops.takeValid()
            ?: chainPopPlaceInfo.takeValid()
                    ?: lethal.takeValid()
                    ?: safe.takeValid()
                    ?: normal.takeValid()
        placeInfo?.calcPlacement(this)
        return placeInfo
    }

    private fun SafeClientEvent.shouldForcePlace(entity: EntityLivingBase): Boolean {
        return player.heldItemMainhand.item !is ItemSword
                && (entity.totalHealth <= forceHealth.value
                || entity.realSpeed >= forcePlaceMotion.value
                || entity.getMinArmorRate() <= armorRate.value)
    }

    private fun EntityLivingBase.getMinArmorRate(): Int {
        var minDura = 100

        for (armor in armorInventoryList.toList()) {
            if (!armor.isItemStackDamageable) continue
            val dura = armor.duraPercentage
            if (dura < minDura) {
                minDura = dura
            }
        }

        return minDura
    }

    private fun SafeClientEvent.swingArm() {
        when (swingMode.value) {
            SwingMode.Offhand -> {
                player.swingArm(EnumHand.OFF_HAND)
            }

            SwingMode.Mainhand -> {
                player.swingArm(EnumHand.MAIN_HAND)
            }

            SwingMode.Auto -> {
                player.swingArm(if (player.heldItemMainhand.getItem() == Items.END_CRYSTAL) EnumHand.MAIN_HAND else EnumHand.OFF_HAND)
            }
        }
    }

    private val SafeClientEvent.targetList: Sequence<TargetInfo>
        get() {
            val rangeSq = enemyRange.value.sq
            val ticks = if (motionPredict.value) predictTicks.value else 0
            val list = ObjectArrayList<TargetInfo>().synchronized()
            val eyePos = CrystalManager.eyePosition

            for (target in EntityManager.players) {
                if (target == player) continue
                if (target == mc.renderViewEntity) continue
                if (!target.isEntityAlive) continue
                if (target.posY <= 0.0) continue
                if (target.distanceSqTo(eyePos) > rangeSq) continue
                if (FriendManager.isFriend(target.name)) continue

                list.add(getPredictedTarget(target, ticks))
            }
            if (dropsPrio.value) {
                for (drops in EntityManager.entity) {
                    if (drops !is EntityItem) continue
                    if (player.getDistanceSq(drops) > 16) continue
                    for (target in EntityManager.players) {
                        if (EntityUtil.isntValid(target, placeRange.value)) continue
                        if (player.getDistanceSq(target) > 16) continue
                        list.add(
                            TargetInfo(
                                drops,
                                drops.positionVector,
                                drops.entityBoundingBox,
                                drops.positionVector,
                                drops.positionVector,
                                ExposureSample.getExposureSample(drops.width, drops.height)
                            )
                        )
                    }
                }
            }

            if (list.isNotEmpty()) {
                list.forEach {
                    if (dropsPrio.value) {
                        if (it.entity is EntityItem) {
                            if (it.entity.isNotDead()) {
                                prioNeeded = true
                            } else {
                                prioNeeded = false
                                list.remove(it)
                            }
                            if (player.getDistanceSq(it.entity) > 16) {
                                prioNeeded = false
                                list.remove(it)
                            }
                        }
                    }
                    if (player.getDistance(it.entity) > enemyRange.value || it.entity == player) {
                        list.remove(it)
                    }
                }
            } else {
                prioNeeded = false
            }
            list.sortBy { player.getDistanceSq(it.entity) }
            return list.asSequence()
                .filter { it.entity.isEntityAlive }
                .take(maxTargets.value)
        }

    private fun SafeClientEvent.doBreak() {
        val crystalList = getCrystalList()
        val crystal = getTargetCrystal(crystalList)
            ?: getCrystal(crystalList)
            ?: getFinalCrystal(crystalList)

        crystal?.let {
            if (!flagged && explodeTimerUtils.tickAndReset(hitDelay.value)) {
                breakDirect(it.posX, it.posY, it.posZ, it.entityId)
            }
        }
    }

    private fun SafeClientEvent.breakDirect(x: Double, y: Double, z: Double, entityID: Int) {
        if (player.isWeaknessActive() && !isHoldingTool()) {
            when (antiWeakness.value) {
                AntiWeaknessMode.Off -> {
                    return
                }

                AntiWeaknessMode.Swap -> {
                    val slot = getWeaponSlot() ?: return
                    swapToSlot(slot)
                    connection.sendPacket(attackPacket(entityID))
                    swingArm()
                }

                AntiWeaknessMode.Spoof -> {
                    val slot = getWeaponSlot() ?: return
                    val packet = attackPacket(entityID)
                    spoofHotbar(slot) {
                        connection.sendPacket(packet)
                        swingArm()
                    }
                }
            }
        } else {
            connection.sendPacket(attackPacket(entityID))
            swingArm()
        }

        placeInfo?.let {
            if (packetPlace0.onBreak && crystalPlaceBoxIntersectsCrystalBox(it.blockPos, x, y, z)) {
                doPlace(it.blockPos) {
                    doRotate(CurrentState.Placing, it.blockPos)
                }
                if (debug.value) {
                    ChatUtil.sendMessage("Debug Break")
                }
            }
            player.setLastAttackedEntity(it.target)
        }
        attackedCrystalMap[entityID] = System.currentTimeMillis() + 1000L
    }

    private fun SafeClientEvent.getFinalCrystal(crystalList: List<EntityEnderCrystal>): EntityEnderCrystal? {
        return crystalList
            .filter { checkBreakDamage(it.posX, it.posY, it.posZ, MutableBlockPos()) }
            .minByOrNull { player.getDistanceSq(it) }
    }

    private fun getTargetCrystal( crystalList: List<EntityEnderCrystal>): EntityEnderCrystal? {
        placeInfo?.let { placeInfo ->
            return crystalList.firstOrNull {
                crystalPlaceBoxIntersectsCrystalBox(placeInfo.blockPos, it.posX, it.posY, it.posZ)
            }
        } ?: return null
    }

    private fun SafeClientEvent.getCrystalList(): List<EntityEnderCrystal> {
        val mutableBlockPos = MutableBlockPos()

        return EntityManager.entity.asSequence()
            .filterIsInstance<EntityEnderCrystal>()
            .filter { it.isEntityAlive }
            .filter {
                checkBreakRange(
                    it,
                    breakRange.value,
                    wallRange.value,
                    mutableBlockPos
                )
            }
            .toList()
    }

    private fun SafeClientEvent.getCrystal(crystalList: List<EntityEnderCrystal>): EntityEnderCrystal? {
        val max = BreakInfo.Mutable()
        val safe = BreakInfo.Mutable()
        val lethal = BreakInfo.Mutable()
        val targets = targetList.toList()

        val noSuicide = noSuicide.value
        val mutableBlockPos = MutableBlockPos()
        val context = CombatManager.contextSelf ?: return null

        if (targets.isNotEmpty()) {
            for (crystal in crystalList) {
                val selfDamage = max(
                    context.calcDamage(crystal.posX, crystal.posY, crystal.posZ, false, mutableBlockPos),
                    context.calcDamage(crystal.posX, crystal.posY, crystal.posZ, true, mutableBlockPos)
                )
                if (player.scaledHealth - selfDamage <= noSuicide) continue
                if (!lethalOverride.value && selfDamage > breakMaxSelf.value) continue

                for ((entity, entityPos, entityBox) in targets) {
                    if (entity !is EntityLivingBase) continue
                    val targetDamage = calcDamage(
                        entity,
                        entityPos,
                        entityBox,
                        crystal.posX,
                        crystal.posY,
                        crystal.posZ,
                        mutableBlockPos
                    ).toDouble()
                    if (lethalOverride.value && System.currentTimeMillis() - CombatManager.getHurtTime(entity) > 400L
                        && targetDamage - entity.totalHealth > lethalBalance.value && selfDamage < lethal.selfDamage
                        && selfDamage <= lethalMaxDamage.value
                    ) {
                        lethal.update(crystal, selfDamage, targetDamage)
                    }

                    if (selfDamage > breakMaxSelf.value) continue

                    val minDamage: Double
                    val balance: Float

                    if (shouldForcePlace(entity)) {
                        minDamage = forcePlaceDmg.value
                        balance = forcePlaceBalance.value
                    } else {
                        minDamage = breakMinDmg.value
                        balance = breakBalance.value
                    }

                    if (targetDamage >= minDamage && targetDamage - selfDamage >= balance) {
                        if (targetDamage > max.targetDamage) {
                            max.update(crystal, selfDamage, targetDamage)
                        } else if (max.targetDamage - targetDamage <= safeRange.value
                            && max.selfDamage - selfDamage >= safeThreshold.value
                        ) {
                            safe.update(crystal, selfDamage, targetDamage)
                        }
                    }
                }
            }
        }

        if (max.targetDamage - safe.targetDamage > safeRange.value
            || max.selfDamage - safe.selfDamage <= safeThreshold.value
        ) {
            safe.clear()
        }

        val valid = lethal.takeValid()
            ?: safe.takeValid()
            ?: max.takeValid()

        return valid?.crystal
    }

    private fun SafeClientEvent.runExplode(i: Int, rotationInvoke: (() -> Unit)? = null) {
        try {
            crystalState.set(CurrentState.Breaking)
            rotationInvoke?.invoke()
            val wdnmd = CPacketUseEntity()
            wdnmd.entityId = i
            wdnmd.action = CPacketUseEntity.Action.ATTACK
            connection.sendPacket(wdnmd)
            swingArm()
        } catch (ignored: Exception) {
        }
    }

    private fun onRender2D() {
        try {
            if (renderDamage.value && scale != 0.0f) {
                lastRenderPos?.let { lastPos ->
                    val text = buildString {
                        append("%.1f".format(lastTargetDamage))
                    }

                    val screenPos = ProjectionUtils.toAbsoluteScreenPos(lastPos)
                    var alpha = (255.0f * scale).toInt()
                    var color = if (scale == 1.0f) ColorRGB(255, 255, 255) else ColorRGB(255, 255, 255, alpha)
                    if (!fadeRender.value) {
                        alpha = (255.0f * scale).toInt()
                        color = if (scale == 1.0f) ColorRGB(255, 255, 255) else ColorRGB(255, 255, 255, alpha)
                    } else {
                        if (renderQueue.isNotEmpty()) {
                            renderQueue.forEach { (_: BlockPos, fade: CrystalFadeRender) ->
                                if (lastPos == fade.blockPos) {
                                    alpha = fade.alpha
                                    color = if (fade.alpha == fade.oriAlpha) ColorRGB(255, 255, 255) else ColorRGB(
                                        255,
                                        255,
                                        255,
                                        fade.alpha
                                    )
                                }
                            }
                        }
                    }

                    MainFontRenderer.drawString(
                        text,
                        screenPos.x.toFloat() - MainFontRenderer.getWidth(text, 2.0f) * 0.5f,
                        screenPos.y.toFloat() - MainFontRenderer.getHeight(2.0f) * 0.5f,
                        color,
                        textSize.value
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun onRender3D(placeInfo: PlaceInfo?) {
        val filled = filledAlpha.value > 0
        val outline = outlineAlpha.value > 0
        val flag = filled || outline

        if (!fadeRender.value) {
            if (flag) {
                try {
                    update(placeInfo)
                    scale = if (placeInfo != null) {
                        Easing.OUT_CUBIC.inc(Easing.toDelta(startTime, fadeLength.value))
                    } else {
                        Easing.IN_CUBIC.dec(Easing.toDelta(startTime, fadeLength.value))
                    }

                    prevPos?.let { prevPos ->
                        currentPos?.let { currentPos ->
                            val multiplier = Easing.OUT_QUART.inc(Easing.toDelta(lastUpdateTime, movingLength.value))
                            val motionRenderPos = prevPos.add(currentPos.subtract(prevPos).scale(multiplier.toDouble()))
                            val staticRenderPos = currentPos

                            val finalPos = if (motionRender.value) motionRenderPos else staticRenderPos
                            val box = toRenderBox(finalPos, if (motionRender.value) scale else 1f)
                            val renderer = ESPRenderer()

                            renderer.aFilled = (filledAlpha.value * scale).toInt()
                            renderer.aOutline = (outlineAlpha.value * scale).toInt()
                            renderer.add(box, color.value)
                            renderer.render(false)

                            lastRenderPos = finalPos
                        }
                    }
                } catch (_: Exception) {
                }
            }
        } else {
            if (renderQueue.isNotEmpty()) {
                renderQueue.forEach {
                    it.value.blockPos?.let { pos ->
                        runAsyncThread {
                            if (placeInfo != pos) {
                                if (it.value.alpha > 0) {
                                    it.value.alpha -= 1
                                } else {
                                    renderQueue.remove(it.key, it.value)
                                }
                            } else {
                                lastRenderPos = pos.toVec3d().add(0.5, 0.5, 0.5)
                                if (it.value.alpha < it.value.oriAlpha) {
                                    it.value.alpha += 1
                                }
                            }
                        }
                        val renderer = ESPRenderer()

                        renderer.aFilled = it.value.alpha
                        renderer.aOutline = it.value.alpha
                        renderer.add(pos, color.value)
                        renderer.render(false)
                    }
                }
            }
        }
    }

    private fun toRenderBox(vec3d: Vec3d, scale: Float): AxisAlignedBB {
        val halfSize = 0.5 * scale
        return AxisAlignedBB(
            vec3d.x - halfSize, vec3d.y - halfSize, vec3d.z - halfSize,
            vec3d.x + halfSize, vec3d.y + halfSize, vec3d.z + halfSize
        )
    }

    private fun updateFade(blockPos: BlockPos?) {
        if (fadeRender.value) {
            blockPos?.let {
                if (!renderQueue.containsKey(it)) {
                    runAsyncThread {
                        renderQueue[it] = CrystalFadeRender(it, fadeAlpha.value, fadeAlpha.value)
                    }
                }
            }
        }
    }

    private fun update(placeInfo: PlaceInfo?) {
        val newBlockPos = placeInfo?.blockPos
        if (newBlockPos != lastBlockPos) {
            if (newBlockPos != null) {
                currentPos = placeInfo.blockPos.toVec3dCenter()
                prevPos = lastRenderPos ?: currentPos
                lastUpdateTime = System.currentTimeMillis()
                if (lastBlockPos == null) startTime = System.currentTimeMillis()
            } else {
                lastUpdateTime = System.currentTimeMillis()
                if (lastBlockPos != null) startTime = System.currentTimeMillis()
            }

            lastBlockPos = newBlockPos
        }

        placeInfo?.let {
            lastTargetDamage = it.targetDamage
        }
    }

    private fun SafeClientEvent.checkBreakDamage(
        crystalX: Double,
        crystalY: Double,
        crystalZ: Double,
        mutableBlockPos: MutableBlockPos
    ): Boolean {
        val context = CombatManager.contextSelf ?: return false
        val selfDamage = max(
            context.calcDamage(crystalX, crystalY, crystalZ, false, mutableBlockPos),
            context.calcDamage(crystalX, crystalY, crystalZ, true, mutableBlockPos)
        )
        if (player.scaledHealth - selfDamage <= noSuicide.value) return false
        return targetList.toList().any {
            checkBreakDamage(crystalX, crystalY, crystalZ, selfDamage, it, mutableBlockPos)
        }
    }

    private fun SafeClientEvent.checkBreakDamage(
        crystalX: Double,
        crystalY: Double,
        crystalZ: Double,
        selfDamage: Float,
        targetInfo: TargetInfo,
        mutableBlockPos: MutableBlockPos
    ): Boolean {
        if (targetInfo.entity is EntityLivingBase) {
            val targetDamage = calcDamage(
                targetInfo.entity,
                targetInfo.pos,
                targetInfo.box,
                crystalX,
                crystalY,
                crystalZ,
                mutableBlockPos
            )
            if (lethalOverride.value && targetDamage - targetInfo.entity.totalHealth > lethalBalance.value && targetDamage <= lethalMaxDamage.value) {
                return true
            }

            if (selfDamage > breakMaxSelf.value) return false

            val minDamage: Double
            val balance: Float

            if (shouldForcePlace(targetInfo.entity)) {
                minDamage = forcePlaceDmg.value
                balance = forcePlaceBalance.value
            } else {
                minDamage = breakMinDmg.value
                balance = breakBalance.value
            }

            return targetDamage >= minDamage && targetDamage - selfDamage >= balance
        } else if (targetInfo.entity is EntityItem) {
            return true
        }
        return false
    }

    private fun EntityPlayerSP.isWeaknessActive(): Boolean {
        return this.isPotionActive(MobEffects.WEAKNESS)
                && this.getActivePotionEffect(MobEffects.STRENGTH)?.let {
            it.amplifier <= 0
        } ?: true
    }

    private fun SafeClientEvent.isHoldingTool(): Boolean {
        val item = player.serverSideItem.item
        return item is ItemTool || item is ItemSword
    }

    private fun SafeClientEvent.getWeaponSlot(): HotbarSlot? {
        return player.hotbarSlots.filterByStack {
            val item = it.item
            item is ItemSword || item is ItemTool
        }.maxByOrNull {
            val itemStack = it.stack
            itemStack.attackDamage
        }
    }

    private fun attackPacket(entityID: Int): CPacketUseEntity {
        val packet = CPacketUseEntity()
        packet.action = CPacketUseEntity.Action.ATTACK
        packet.entityId = entityID
        return packet
    }

    private fun SafeClientEvent.rendertions(range: Double): List<BlockPos> {
        val positions = NonNullList.create<BlockPos>()
        positions.addAll(
            CrystalUtil.getSphere(EntityUtil.getPlayerPos(), range, range, false, true, 0)
                .stream()
                .filter { canPlaceCrystal(it, newPlace.value) }
                .collect(Collectors.toList())
        )

        return positions
    }

    private fun SafeClientEvent.canPlaceCrystal(blockPos: BlockPos, newPlace: Boolean): Boolean {
        val boost = blockPos.add(0, 1, 0)
        val boost2 = blockPos.add(0, 2, 0)
        val base = world.getBlockState(blockPos).block
        val b1 = world.getBlockState(boost).block
        val b2 = world.getBlockState(boost2).block
        if (base !== Blocks.BEDROCK && base !== Blocks.OBSIDIAN) return false
        if (b1 !== Blocks.AIR && !isReplaceable(b1)) return false
        if (!newPlace && b2 !== Blocks.AIR) return false
        val box = AxisAlignedBB(
            blockPos.getX().toDouble(),
            blockPos.getY() + 1.0,
            blockPos.getZ().toDouble(),
            blockPos.getX() + 1.0,
            blockPos.getY() + 3.0,
            blockPos.getZ() + 1.0
        )
        for (entity in ArrayList(world.loadedEntityList)) {
            if (entity is EntityEnderCrystal) continue
            if (entity.entityBoundingBox.intersects(box)) return false
        }
        return true
    }

    private fun SafeClientEvent.updateDdosQueue() {
        val target = renderEnt
        val mutableBlockPos = MutableBlockPos()
        ddosArmor =
            armorDdos.value && target != null && (rendertions(placeRange.value).isEmpty() || damageCA < placeMinDmg.value)

        if (target == null || !ddosArmor) {
            ddosQueue.clear()
            return
        }

        val diff = System.currentTimeMillis() - CombatManager.getHurtTime(target)

        if (diff > 500L) {
            if (ddosArmor && ddosQueue.isEmpty() && shouldForcePlace(target)) {
                val last = 0f

                if (last < placeMinDmg.value) {
                    val contextSelf = CombatManager.contextSelf ?: return
                    val feetPos = CrystalManager.position
                    val eyePos = CrystalManager.eyePosition

                    val sequence = rendertions(placeRange.value).asSequence()
                        .filter {
                            calcDamage(
                                target,
                                target.positionVector,
                                target.entityBoundingBox,
                                it.getX() + 0.5,
                                (it.getY() + 1).toDouble(),
                                it.getZ() + 0.5,
                                mutableBlockPos
                            ).toDouble() > ddosMinDamage.value
                        }
                        .filter { canPlaceCrystal(it, newPlace.value) }
                        .filter {
                            player.scaledHealth - max(
                                contextSelf.calcDamage(
                                    it.getX() + 0.5,
                                    (it.getY() + 1).toDouble(),
                                    it.getZ() + 0.5, false, mutableBlockPos
                                ),
                                contextSelf.calcDamage(
                                    it.getX() + 0.5,
                                    (it.getY() + 1).toDouble(),
                                    it.getZ() + 0.5, true, mutableBlockPos
                                )
                            ) > noSuicide.value
                        }
                        .filter {
                            val crystalX = it.x + 0.5
                            val crystalY = it.y + 1.0
                            val crystalZ = it.z + 0.5

                            feetPos.squareDistanceTo(
                                crystalX,
                                crystalY,
                                crystalZ
                            ) <= wallRange.value.sq || world.rayTraceVisible(
                                eyePos,
                                crystalX,
                                crystalY + 1.7,
                                crystalZ,
                                20,
                                mutableBlockPos
                            )
                        }

                    ddosQueue.clear()
                    var lastDamage = Int.MAX_VALUE

                    for (crystalDamage in sequence) {
                        val targetDamage = calcDamage(
                            target,
                            target.positionVector,
                            target.entityBoundingBox,
                            crystalDamage.getX() + 0.5,
                            (crystalDamage.getY() + 1).toDouble(),
                            crystalDamage.getZ() + 0.5,
                            mutableBlockPos
                        ).toDouble()
                        val roundedDamage = (targetDamage / ddosDamageStep.value).roundToInt()
                        if (lastDamage == roundedDamage || lastDamage - roundedDamage < ddosDamageStep.value) continue
                        ddosQueue.addFirst(crystalDamage)
                        lastDamage = roundedDamage

                        if (ddosQueue.size >= ddosQueueSize.value) break
                    }
                }
            }
        } else if (diff > 450L) {
            ddosQueue.clear()
        }
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        cSlot = -1
        lastEntityID.set(-1)
        isFacePlacing = false
        canPredictHit = true
        prioNeeded = false
        flagged = false
        packetExplodeTimerUtils.reset()
        explodeTimerUtils.reset()
        placeTimerUtils.reset()
        breakDropsTimer.reset()
        calcTimerUtils.reset()
        fpTimer.reset()
        crystalState.set(CurrentState.Waiting)
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        popList.clear()
        renderQueue.clear()
        ddosQueue.clear()
        renderEnt = null
        render = null
        prevPos = null
        currentPos = null
        lastRenderPos = null
        lastBlockPos = null
        lastTargetDamage = 0.0
        lastUpdateTime = 0L
        startTime = 0L
        scale = 0.0f
    }

    override fun getHudInfo(): String? {
        if (hudState.value) {
            return when (crystalState.get()) {
                CurrentState.Breaking -> "Breaking"
                CurrentState.Placing -> "Placing"
                else -> "Waiting"
            }
        }
        return if (renderEnt != null) {
            ChatUtil.GREEN + ChatUtil.BOLD + renderEnt!!.name
        } else null
    }

    enum class Page {
        GENERAL, CALCULATION, PLACE, BREAK, FORCE, LETHAL, RENDER
    }

    @Suppress("unused")
    enum class PacketPlaceMode(val onRemove: Boolean, val onBreak: Boolean) {
        Off(false, false), Weak(true, false), Strong(true, true)
    }

    @Suppress("unused")
    enum class Switch(val onSpoof: Boolean, val onSwitch: Boolean, val onBypassSpoof: Boolean) {
        AutoSwitch(false, true, false), PacketSpoof(true, false, false), SpoofBypass(false, false, true), Off(
            false,
            false,
            false
        )
    }

    enum class AntiWeaknessMode {
        Swap, Spoof, Off
    }

    enum class SwingMode {
        Offhand, Mainhand, Auto, Off
    }

    enum class CurrentState {
        Placing, Breaking, Waiting
    }

    private fun getPredictedTarget(entity: EntityPlayer, ticks: Int): TargetInfo {
        val motionX = (entity.posX - entity.lastTickPosX).coerceIn(-0.6, 0.6)
        val motionY = (entity.posY - entity.lastTickPosY).coerceIn(-0.5, 0.5)
        val motionZ = (entity.posZ - entity.lastTickPosZ).coerceIn(-0.6, 0.6)

        val entityBox = entity.entityBoundingBox
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
        val motion = Vec3d(offsetX, offsetY, offsetZ)
        val pos = entity.positionVector

        return TargetInfo(
            entity,
            pos.add(motion),
            targetBox,
            pos,
            motion,
            ExposureSample.getExposureSample(entity.width, entity.height)
        )
    }

    data class TargetInfo(
        val entity: Entity,
        val pos: Vec3d,
        val box: AxisAlignedBB,
        val currentPos: Vec3d,
        val predictMotion: Vec3d,
        val exposureSample: ExposureSample
    )
}