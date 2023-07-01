package dev.zenhao.melon.module.modules.extra

import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.manager.FriendManager
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.calcDamage
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.getPredictedTarget
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.BlockEasingRender
import dev.zenhao.melon.utils.entity.CrystalUtil.getSphere
import dev.zenhao.melon.utils.gl.MelonTessellator
import melon.events.RunGameLoopEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.runSafe
import net.minecraft.block.Block
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiCrafting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import kotlin.math.floor
import kotlin.math.pow

@Module.Info(name = "NewBedAura", category = Category.XDDD)
class NewBedAura : Module() {
    private var range = isetting("Range", 5, 1, 8)
    private var distance = isetting("EnemyBedDist", 8, 1, 13)
    private var minDmg = isetting("MinDMG", 4, 0, 20)
    private var maxSelfDmg = isetting("MaxSelfDmg", 4, 0, 20)
    private var prediction = bsetting("Prediction", true)
    private var predictedTicks = isetting("PredictedTicks", 1, 0, 20).b(prediction)
    private var placeDelay = isetting("PlaceDelay", 15, 0, 1000)
    private var clickDelay = isetting("ClickDelay", 15, 0, 1000)
    private var invClickDelay = isetting("InvClickDelay", 5, 0, 1000)
    private var fade = bsetting("FadeMove", false)
    private var color = csetting("Color", Color(255, 198, 206))
    private var alpha = isetting("Alpha", 120, 1, 255)
    private var blockRenderSmooth: BlockEasingRender? = BlockEasingRender(BlockPos(0, 0, 0), 450f, 350f)

    //private val bedAABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5625, 1.0)
    private var blockPos: BlockPos? = null
    private var bedExplodePos: BlockPos? = null
    private var direction: EnumFacing? = null
    private var renderEnt: EntityPlayer? = null
    private var packetTimer: TimerUtils = TimerUtils()
    private var placeTimer: TimerUtils = TimerUtils()
    private var clickTimer: TimerUtils = TimerUtils()
    private var inventoryTimer: TimerUtils = TimerUtils()
    private var yawOffset = 0f
    private var offhand = false

    private fun SafeClientEvent.onCalc() {
        val entities = CopyOnWriteArrayList(world.playerEntities).stream()
            .filter { e: EntityPlayer -> !FriendManager.isFriend(e.name) && e !== player && e.health > 0.0f && !e.isDead }
            .collect(
                Collectors.toCollection { ArrayList() }
            )
        if (entities == null) {
            blockPos = null
            return
        }
        val sphereBlocks = getSphere(
            player.position, range.value.toDouble(), range.value.toDouble(),
            false,
            true,
            0
        )
        val bedPos = canPlaceBed(entities, sphereBlocks)
        var d = minDmg.value.toDouble()
        for (entity in entities) {
            if (entity == null) {
                blockPos = null
            }
            for (pos in bedPos) {
                if (entity!!.getDistanceSq(pos.blockPos) > distance.value.toDouble().pow(2.0)) continue
                for (i in pos.canPlaceDirection.indices) {
                    val boost2 = pos.blockPos.add(0, 1, 0).offset(pos.canPlaceDirection[i])
                    val predictTarget = if (prediction.value) getPredictedTarget(
                        entity,
                        predictedTicks.value
                    ) else Vec3d(0.0, 0.0, 0.0)
                    val d2 = calcDamage(
                        entity,
                        entity.positionVector.add(predictTarget),
                        entity.entityBoundingBox,
                        boost2.x.toDouble() + 0.5,
                        boost2.y.toDouble() + 0.5,
                        boost2.z.toDouble() + 0.5,
                        MutableBlockPos()
                    ).toDouble()
                    if (d2 < pos.selfDamage[i] && d2 <= (entity.health + entity.absorptionAmount).toDouble() || d2 < d) continue
                    if (d2 < minDmg.value) continue
                    d = d2
                    blockPos = pos.blockPos
                    direction = pos.canPlaceDirection[i]
                    renderEnt = entity
                }
            }
        }
        if (d == minDmg.value.toDouble() || renderEnt == null) {
            blockPos = null
            return
        }
    }

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            onCalc()
            offhand = player.heldItemOffhand.getItem() === Items.BED
            var bedSlot =
                if (player.heldItemMainhand.getItem() === Items.BED) player.inventory.currentItem else -1
            if (bedSlot == -1) {
                for (l in 0..8) {
                    if (player.inventory.getStackInSlot(l).getItem() !== Items.BED) continue
                    bedSlot = l
                    break
                }
            }
            if (bedSlot == -1 && !offhand) {
                if (mc.currentScreen !is GuiContainer) {
                    for (i in 9..36) {
                        if (player.inventory.getStackInSlot(i).getItem() !== Items.BED) continue
                        if (inventoryTimer.tickAndReset(invClickDelay.value)) {
                            if (mc.currentScreen !is GuiCrafting) {
                                playerController.windowClick(
                                    player.inventoryContainer.windowId,
                                    i,
                                    0,
                                    ClickType.QUICK_MOVE,
                                    player
                                )
                                playerController.updateController()
                            }
                        }
                        break
                    }
                }
                bedSlot =
                    if (player.heldItemMainhand.getItem() === Items.BED) player.inventory.currentItem else -1
                if (bedSlot == -1) {
                    for (l in 0..8) {
                        if (player.inventory.getStackInSlot(l).getItem() !== Items.BED) continue
                        bedSlot = l
                        break
                    }
                }
                if (bedSlot == -1 && !offhand) {
                    return@safeEventListener
                }
            }
            if (blockPos != null) {
                bedExplodePos = blockPos
                if (direction != null) {
                    when (direction) {
                        EnumFacing.EAST -> {
                            RotationManager.addRotations(-91.0f, player.rotationPitch)
                            yawOffset = -91f
                            //event.setRotation(-91.0f, player.rotationPitch)
                        }

                        EnumFacing.NORTH -> {
                            RotationManager.addRotations(179.0f, player.rotationPitch)
                            yawOffset = 179f
                            //event.setRotation(179.0f, player.rotationPitch)
                        }

                        EnumFacing.WEST -> {
                            RotationManager.addRotations(89.0f, player.rotationPitch)
                            yawOffset = 89f
                            //event.setRotation(89.0f, player.rotationPitch)
                        }

                        else -> {
                            RotationManager.addRotations(-1.0f, player.rotationPitch)
                            yawOffset = -1f
                            //event.setRotation(-1.0f, player.rotationPitch)
                        }
                    }
                }
                val vec =
                    blockPos?.let {
                        Vec3d(it).add(0.5, 0.5, 0.5).add(Vec3d(EnumFacing.DOWN.getDirectionVec()).scale(0.5))
                    }
                val f = (vec!!.x - blockPos!!.getX().toDouble()).toFloat()
                val f1 = (vec.y - blockPos!!.getY().toDouble()).toFloat()
                val f2 = (vec.z - blockPos!!.getZ().toDouble()).toFloat()
                var sneak = false
                if (player.isSneaking) {
                    sneak = true
                    connection.sendPacket(
                        CPacketEntityAction(
                            player,
                            CPacketEntityAction.Action.STOP_SNEAKING
                        )
                    )
                }
                try {
                    if (world.getBlockState(blockPos!!.up().offset(direction!!)).block === Blocks.BED) {
                        connection.sendPacket(CPacketAnimation(if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND))
                        CPacketPlayerTryUseItemOnBlock(
                            blockPos!!.up().offset(direction!!),
                            EnumFacing.UP,
                            if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND,
                            0.0f,
                            0.0f,
                            0.0f
                        )
                        if (clickTimer.tickAndReset(clickDelay.value)) {
                            connection.sendPacket(
                                CPacketPlayerTryUseItemOnBlock(
                                    blockPos!!.up().offset(direction!!),
                                    EnumFacing.UP,
                                    if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND,
                                    0.0f,
                                    0.0f,
                                    0.0f
                                )
                            )
                        }
                    }
                    connection.sendPacket(CPacketAnimation(if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND))
                    if (placeTimer.tickAndReset(placeDelay.value)) {
                        spoofHotbar(bedSlot) {
                            connection.sendPacket(
                                CPacketPlayerTryUseItemOnBlock(
                                    blockPos!!,
                                    EnumFacing.UP,
                                    if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND,
                                    f,
                                    f1,
                                    f2
                                )
                            )
                        }
                        connection.sendPacket(CPacketAnimation(if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND))
                        connection.sendPacket(
                            CPacketPlayerTryUseItemOnBlock(
                                blockPos!!.up(),
                                EnumFacing.UP,
                                if (offhand) EnumHand.OFF_HAND else EnumHand.MAIN_HAND,
                                f,
                                f1,
                                f2
                            )
                        )
                    }
                } catch (_: Exception) {
                }
                if (sneak) {
                    connection.sendPacket(
                        CPacketEntityAction(
                            player,
                            CPacketEntityAction.Action.START_SNEAKING
                        )
                    )
                }
            }
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        runSafe {
            if (direction == null || blockPos == null) {
                return
            }
            val render = direction?.let { blockPos!!.up().offset(it) }
            val renderPos = AxisAlignedBB(
                render!!.x.toDouble(),
                render.y.toDouble(),
                render.z.toDouble(),
                (render.x.toDouble() + 1),
                render.y.toDouble() + 0.5625,
                (render.z + 1).toDouble()
            )
            blockRenderSmooth!!.updatePos(BlockPos(renderPos.center.x, renderPos.center.y, renderPos.center.z))
            blockRenderSmooth!!.begin()
            MelonTessellator.drawBBBox(
                if (fade.value) blockRenderSmooth!!.getFullUpdate() else renderPos,
                color.value,
                alpha.value,
                2f,
                true
            )
            //MelonTessellator.prepare(GL11.GL_QUADS)
            //MelonTessellator.drawFullBox(BlockPos(xPos, yPos, zPos), 2f, c.rgb)
            //MelonTessellator.drawFullBox(renderPos.center.add(0, (0.5625).toInt(), 0), 2f, c.rgb)
            //MelonTessellator.release()
            GlStateManager.pushMatrix()
            try {
                if (!fade.value) {
                    MelonTessellator.glBillboardDistanceScaled(
                        renderPos.center.x.toFloat() + 0.5f,
                        renderPos.center.y.toFloat() + 0.5f,
                        renderPos.center.z.toFloat() + 0.5f,
                        player,
                        1.0f
                    )
                } else {
                    MelonTessellator.glBillboardDistanceScaled(
                        blockRenderSmooth!!.getFullUpdate().center.x.toFloat() + 0.5f,
                        blockRenderSmooth!!.getFullUpdate().center.y.toFloat() + 0.5f,
                        blockRenderSmooth!!.getFullUpdate().center.z.toFloat() + 0.5f,
                        player,
                        1.0f
                    )
                }
                val damage = if (fade.value) {
                    calcDamage(
                        renderEnt!!,
                        renderEnt!!.positionVector,
                        renderEnt!!.entityBoundingBox,
                        renderPos.center.x + 0.5,
                        renderPos.center.y + 0.5,
                        renderPos.center.z + 0.5,
                        MutableBlockPos()
                    )
                } else {
                    calcDamage(
                        renderEnt!!,
                        renderEnt!!.positionVector,
                        renderEnt!!.entityBoundingBox,
                        blockRenderSmooth!!.getFullUpdate().center.x + 0.5,
                        blockRenderSmooth!!.getFullUpdate().center.y + 0.5,
                        blockRenderSmooth!!.getFullUpdate().center.z + 0.5,
                        MutableBlockPos()
                    )
                }
                val damage2 = if (fade.value) {
                    calcDamage(
                        player,
                        player.positionVector,
                        player.entityBoundingBox,
                        renderPos.center.x + 0.5,
                        renderPos.center.y + 0.5,
                        renderPos.center.z + 0.5,
                        MutableBlockPos()
                    )
                } else {
                    calcDamage(
                        player,
                        player.positionVector,
                        player.entityBoundingBox,
                        blockRenderSmooth!!.getFullUpdate().center.x + 0.5,
                        blockRenderSmooth!!.getFullUpdate().center.y + 0.5,
                        blockRenderSmooth!!.getFullUpdate().center.z + 0.5,
                        MutableBlockPos()
                    )
                }
                val damageText =
                    (if (floor(damage.toDouble()) == damage.toDouble()) Integer.valueOf(damage.toInt()) else String.format(
                        "%.1f",
                        damage
                    )).toString() + ""
                val damageText2 =
                    (if (floor(damage2.toDouble()) == damage2.toDouble()) Integer.valueOf(damage2.toInt()) else String.format(
                        "%.1f",
                        damage2
                    )).toString() + ""
                GlStateManager.disableDepth()
                GlStateManager.translate(
                    -(fontRenderer.getStringWidth("$damageText/$damageText2").toDouble() / 2.0),
                    0.0,
                    0.0
                )
                //GlStateManager.scale(0.7, 0.7, 0.7)
                fontRenderer.drawStringWithShadow("\u00a7b$damageText/$damageText2", 0f, 10f, -5592406)
                GlStateManager.enableDepth()
            } catch (exception: Exception) {
                // empty catch block
            }
            GlStateManager.popMatrix()
        }
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        blockPos = null
        packetTimer.reset()
        clickTimer.reset()
        inventoryTimer.reset()
    }

    private fun SafeClientEvent.canPlaceBed(
        entityPlayerList: List<EntityPlayer>,
        blockPosList: List<BlockPos>
    ): List<BedSaver> {
        val bedSaverList = ArrayList<BedSaver>()
        val list = ArrayList<EnumFacing>()
        val damage = ArrayList<Double>()
        for (pos in blockPosList) {
            var x = false
            for (entityPlayer in entityPlayerList) {
                if (entityPlayer.getDistanceSq(pos) > distance.value.toDouble().pow(2.0)) continue
                x = true
                break
            }
            if (!x) continue
            for (facing in EnumFacing.HORIZONTALS) {
                var selfDmg = 0.0
                val side = pos.offset(facing)
                val boost = pos.add(0, 1, 0)
                val boost2 = pos.add(0, 1, 0).offset(facing)
                val boostBlock = world.getBlockState(boost).block
                val boostBlock2 = world.getBlockState(boost2).block
                if (boostBlock !== Blocks.AIR && boostBlock !== Blocks.BED || boostBlock2 !== Blocks.AIR && boostBlock2 !== Blocks.BED || !world.getBlockState(
                        side
                    ).material.isOpaque || !world.getBlockState(side).isFullCube || !world.getBlockState(pos).material.isOpaque || !world.getBlockState(
                        pos
                    ).isFullCube || calcDamage(
                        player,
                        player.positionVector,
                        player.entityBoundingBox,
                        boost2.x.toDouble() + 0.5,
                        boost2.y.toDouble() + 0.5,
                        boost2.z.toDouble() + 0.5,
                        MutableBlockPos()
                    ).also {
                        selfDmg = it.toDouble()
                    } > maxSelfDmg.value.toDouble() || selfDmg >= (player.health + player.absorptionAmount + 2.0f).toDouble()
                ) continue
                list.add(facing)
                damage.add(selfDmg)
            }
            if (list.isEmpty()) continue
            bedSaverList.add(BedSaver(pos, list, damage))
            list.clear()
            damage.clear()
        }
        return bedSaverList
    }

    class BedSaver(var blockPos: BlockPos, canPlaceDirection: List<EnumFacing>?, selfDamage: List<Double>?) {
        var canPlaceDirection: List<EnumFacing>
        var selfDamage: List<Double>

        init {
            this.canPlaceDirection = canPlaceDirection?.let { ArrayList(it) }!!
            this.selfDamage = selfDamage?.let { ArrayList(it) }!!
        }
    }

    companion object {
        val blocks: List<Block>
            get() = listOf(
                Blocks.OBSIDIAN,
                Blocks.BEDROCK,
                Blocks.COMMAND_BLOCK,
                Blocks.BARRIER,
                Blocks.ENCHANTING_TABLE,
                Blocks.ENDER_CHEST,
                Blocks.END_PORTAL_FRAME,
                Blocks.BEACON,
                Blocks.ANVIL
            )
    }
}