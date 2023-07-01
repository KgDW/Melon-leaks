package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.inventory.InventoryUtil.findHotbarBlock
import melon.utils.extension.fastPos
import melon.events.Render3DEvent
import melon.events.RunGameLoopEvent
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.onMainThreadSafe
import melon.utils.graphics.ESPRenderer
import melon.system.util.color.ColorRGB
import melon.utils.inventory.slot.firstBlock
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.math.vector.toBlockPos
import melon.utils.player.getTarget
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "AutoTrap", category = Category.COMBAT)
class AutoTrap : Module() {
    private var offsetsDefault = arrayOf(
        Vec3d(0.0, 0.0, -1.0),
        Vec3d(1.0, 0.0, 0.0),
        Vec3d(0.0, 0.0, 1.0),
        Vec3d(-1.0, 0.0, 0.0),
        Vec3d(0.0, 1.0, -1.0),
        Vec3d(1.0, 1.0, 0.0),
        Vec3d(0.0, 1.0, 1.0),
        Vec3d(-1.0, 1.0, 0.0),
        Vec3d(0.0, 2.0, -1.0),
        Vec3d(1.0, 2.0, 0.0),
        Vec3d(0.0, 2.0, 1.0),
        Vec3d(-1.0, 2.0, 0.0),
        Vec3d(0.0, 3.0, -1.0),
        Vec3d(0.0, 3.0, 1.0),
        Vec3d(1.0, 3.0, 0.0),
        Vec3d(-1.0, 3.0, 0.0),
        Vec3d(0.0, 3.0, 0.0)
    )
    private var offsetsFace = arrayOf(
        Vec3d(0.0, 0.0, -1.0),
        Vec3d(1.0, 0.0, 0.0),
        Vec3d(0.0, 0.0, 1.0),
        Vec3d(-1.0, 0.0, 0.0),
        Vec3d(0.0, 1.0, -1.0),
        Vec3d(1.0, 1.0, 0.0),
        Vec3d(0.0, 1.0, 1.0),
        Vec3d(-1.0, 1.0, 0.0),
        Vec3d(0.0, 2.0, -1.0),
        Vec3d(0.0, 3.0, -1.0),
        Vec3d(0.0, 3.0, 1.0),
        Vec3d(1.0, 3.0, 0.0),
        Vec3d(-1.0, 3.0, 0.0),
        Vec3d(0.0, 3.0, 0.0)
    )
    private var offsetsFeet = arrayOf(
        Vec3d(0.0, 0.0, -1.0),
        Vec3d(0.0, 1.0, -1.0),
        Vec3d(0.0, 2.0, -1.0),
        Vec3d(1.0, 2.0, 0.0),
        Vec3d(0.0, 2.0, 1.0),
        Vec3d(-1.0, 2.0, 0.0),
        Vec3d(0.0, 3.0, -1.0),
        Vec3d(0.0, 3.0, 1.0),
        Vec3d(1.0, 3.0, 0.0),
        Vec3d(-1.0, 3.0, 0.0),
        Vec3d(0.0, 3.0, 0.0),
        Vec3d(0.0, 4.0, 0.0)
    )
    private var offsetsExtra = arrayOf(
        Vec3d(0.0, 0.0, -1.0),
        Vec3d(1.0, 0.0, 0.0),
        Vec3d(0.0, 0.0, 1.0),
        Vec3d(-1.0, 0.0, 0.0),
        Vec3d(0.0, 1.0, -1.0),
        Vec3d(1.0, 1.0, 0.0),
        Vec3d(0.0, 1.0, 1.0),
        Vec3d(-1.0, 1.0, 0.0),
        Vec3d(0.0, 2.0, -1.0),
        Vec3d(1.0, 2.0, 0.0),
        Vec3d(0.0, 2.0, 1.0),
        Vec3d(-1.0, 2.0, 0.0),
        Vec3d(0.0, 3.0, -1.0),
        Vec3d(0.0, 3.0, 0.0),
        Vec3d(0.0, 4.0, 0.0)
    )
    private var placeMode = msetting("Mode", Mode.Normal)
    private var rotate: Setting<Boolean> = bsetting("Rotate", false)
    private var strictDirection = bsetting("StrictDirection", false)
    private var enemyRange = isetting("EnemyRange", 4, 0, 6)
    private var placeRange = isetting("PlaceRange", 4, 0, 6)
    private var placeDelay = isetting("PlaceDelay", 10, 0, 1000)
    private var renderer = ESPRenderer()
    private var placeTimer = TimerUtils()
    private var lastTargetName = ""
    private var offsetStep = -1
    private var firstRun = true
    private var targetPos: BlockPos? = null

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        firstRun = true
        if (findHotbarBlock(Blocks.OBSIDIAN) == -1) {
            disable()
            return
        }
        placeTimer.reset()
        offsetStep = -1
    }

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            try {
                val target = getTarget(enemyRange.value)
                val slot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)
                if (target == null) {
                    disable()
                    return@safeEventListener
                }
                if (slot == null) {
                    ChatUtil.sendMessage("[$moduleName] No Obsidian in hotbar, Disabled!")
                    disable()
                    return@safeEventListener
                }
                if (firstRun) {
                    firstRun = false
                    lastTargetName = target.name
                } else if (lastTargetName != target.name) {
                    lastTargetName = target.name
                    offsetStep = -1
                }
                val placeTarget = CopyOnWriteArrayList<Vec3d>()
                when (placeMode.value) {
                    Mode.Normal -> Collections.addAll(placeTarget, *offsetsDefault)
                    Mode.Extra -> Collections.addAll(placeTarget, *offsetsExtra)
                    Mode.Feet -> Collections.addAll(placeTarget, *offsetsFeet)
                    else -> Collections.addAll(placeTarget, *offsetsFace)
                }
                if (offsetStep >= placeTarget.size) {
                    offsetStep = -1
                    return@safeEventListener
                }
                val offsetPos = BlockPos(placeTarget[offsetStep++])
                targetPos = target.positionVector.toBlockPos().down().add(offsetPos)
                var shouldTryPlace = world.getBlockState(targetPos!!).material.isReplaceable
                for (entity in world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB(targetPos!!))) {
                    if (entity !is EntityItem && entity !is EntityXPOrb) {
                        shouldTryPlace = false
                        break
                    }
                }
                if (player.getDistanceSq(targetPos!!) > placeRange.value.sq) return@safeEventListener
                if (BlockUtil.isIntersected(targetPos)) return@safeEventListener
                if (shouldTryPlace && targetPos != null && placeTimer.tickAndReset(placeDelay.value)) {
                    if (rotate.value) {
                        RotationManager.addRotations(targetPos!!)
                    }
                    spoofHotbar(slot) {
                        player.connection.sendPacket(fastPos(targetPos!!, strictDirection.value))
                    }
                    onMainThreadSafe {
                        val blockState = Blocks.OBSIDIAN.getStateForPlacement(
                            world,
                            targetPos!!,
                            EnumFacing.UP,
                            0.5f,
                            1f,
                            0.5f,
                            0,
                            player,
                            EnumHand.MAIN_HAND
                        )
                        val soundType = blockState.block.getSoundType(blockState, world, targetPos!!, player)
                        world.playSound(
                            player,
                            targetPos!!,
                            soundType.placeSound,
                            SoundCategory.BLOCKS,
                            (soundType.getVolume() + 1.0f) / 2.0f,
                            soundType.getPitch() * 0.8f
                        )
                    }
                }
                //offsetStep++
            } catch (ignored: Exception) {
            }
        }

        safeEventListener<Render3DEvent> {
            if (targetPos != null && world.isAirBlock(targetPos!!)) {
                renderer.add(targetPos!!, ColorRGB(231, 248, 0, 70))
                renderer.render(true)
            }
        }
    }

    enum class Mode {
        Extra, Normal, Feet
    }
}