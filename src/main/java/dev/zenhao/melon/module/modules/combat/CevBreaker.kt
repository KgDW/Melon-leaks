package dev.zenhao.melon.module.modules.combat

import com.mojang.realmsclient.gui.ChatFormatting
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.manager.FriendManager
import dev.zenhao.melon.manager.HotbarManager.onSpoof
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.crystal.CrystalDamageCalculator.calcDamage
import dev.zenhao.melon.module.modules.crystal.CrystalHelper.checkBreakRange
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.BlockEasingRender
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.gl.MelonTessellator
import dev.zenhao.melon.utils.inventory.InventoryUtil
import melon.events.PacketEvents
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import net.minecraft.block.BlockObsidian
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemEndCrystal
import net.minecraft.item.ItemPickaxe
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.function.Consumer

@Module.Info(name = "CevBreaker", category = Category.COMBAT)
object CevBreaker : Module() {
    var oldSlot = 0
    var mode = msetting("CheckMode", CheckMode.Air)
    var packetExplode = bsetting("PacketExplode", false)
    var crystalDead = bsetting("CheckCrystal", false)
    var rotate = bsetting("Rotate", false)
    var packet = bsetting("Packet", false)
    var swing = bsetting("Swing", false)
    var pswing = bsetting("PacketSwing", false).b(swing)
    var range = isetting("Range", 5, 1, 6)
    var color = csetting("Color", Color(207, 19, 220))
    var alpha = isetting("Alpha", 65, 1, 255)
    var blockRenderSmooth = BlockEasingRender(BlockPos(0, 0, 0), 0f, 550f)
    var breakDelay = TimerUtils()
    var updatePos = TimerUtils()
    var lastCrystal: EntityEnderCrystal? = null
    var target: EntityPlayer? = null
    var boost = false
    var flag = false
    var progress = 0
    var firsttime = 0
    var pickaxeitem = 0
    var crystalitem = 0
    var ObiItem = 0
    private var cobi: BlockPos? = null
    private var stage = 0
    private var currentX = 0
    private var currentY = 0
    private var currentZ = 0
    private var lastX = 0
    private var lastY = 0
    private var lastZ = 0
    override fun onWorldRender(event: RenderEvent) {
        if (fullNullCheck()) {
            return
        }
        if (cobi != null) {
            blockRenderSmooth.begin()
            MelonTessellator.drawBBBox(blockRenderSmooth.getFullUpdate(), color.value, alpha.value, 2f, true)
        } else {
            blockRenderSmooth.resetFade()
            blockRenderSmooth.end()
        }
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        blockRenderSmooth.resetFade()
        progress = 0
        firsttime = 0
        flag = false
        cobi = null
        lastCrystal = null
        target = null
        boost = false
        updatePos.reset()
        breakDelay.reset()
        stage = 0
    }

    override fun onLogout() {
        disable()
    }

    override fun onLogin() {
        disable()
    }

    private fun validPosition(Pos: BlockPos, player: EntityPlayer): Boolean {
        return (mc.world.getBlockState(Pos).block !== Blocks.BEDROCK && mc.world.getBlockState(
            Pos.add(
                0,
                1,
                0
            )
        ).block !== Blocks.BEDROCK && mc.world.getBlockState(
            Pos.add(
                0,
                2,
                0
            )
        ).block !== Blocks.BEDROCK && mc.world.getBlockState(
            Pos.add(
                0,
                1,
                0
            )
        ).block === Blocks.AIR && mc.world.getBlockState(
            Pos.add(0, 2, 0)
        ).block === Blocks.AIR && player.position != Pos
                && player.position.up() != Pos
                && !BlockUtil.isIntersected(Pos)
                && !player.entityBoundingBox.intersects(AxisAlignedBB(Pos))
                && validPlace(Pos))
    }

    private fun validPlace(pos: BlockPos): Boolean {
        return !mc.world.isAirBlock(pos.down()) || !mc.world.isAirBlock(pos.north()) || !mc.world.isAirBlock(pos.east()) || !mc.world.isAirBlock(
            pos.west()
        ) || !mc.world.isAirBlock(pos.south())
    }

    init {
        safeEventListener<PacketEvents.Receive> { event ->
            if (event.packet is SPacketSoundEffect) {
                if (event.packet.getCategory() == SoundCategory.BLOCKS && event.packet.getSound() === SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    ArrayList(mc.world.loadedEntityList).forEach(Consumer { e: Entity ->
                        if (e is EntityEnderCrystal) {
                            if (lastCrystal == e) {
                                if (lastCrystal!!.getDistance(event.packet.x, event.packet.y, event.packet.z) <= 6.0f) {
                                    lastCrystal!!.setDead()
                                }
                            }
                        }
                    })
                }
            }
            if (event.packet is SPacketSpawnObject) {
                if (event.packet.type == 51 && !event.cancelled && boost && packetExplode.value) {
                    PacketExplode(event.packet.entityID)
                }
            }
        }
    }

    fun SafeClientEvent.PacketExplode(i: Int) {
        try {
            if (canHitCrystal(lastCrystal!!.positionVector, cobi, target)) {
                val wdnmd = CPacketUseEntity(lastCrystal!!)
                wdnmd.entityId = i
                wdnmd.action = CPacketUseEntity.Action.ATTACK
                player.connection.sendPacket(wdnmd)
            }
        } catch (ignored: Exception) {
        }
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            if (lastCrystal != null) {
                if (lastCrystal!!.isDead) {
                    boost = false
                }
            }
            val entity = findClosestTarget()
            oldSlot = mc.player.inventory.currentItem
            pickaxeitem = InventoryUtil.findHotbarBlock(ItemPickaxe::class.java)
            crystalitem = InventoryUtil.findHotbarBlock(ItemEndCrystal::class.java)
            ObiItem = InventoryUtil.findHotbarBlock(BlockObsidian::class.java)
            if (updatePos.passed(250)) {
                lastX = currentX
                lastY = currentY
                lastZ = currentZ
                updatePos.reset()
            }
            if (pickaxeitem == -1 || crystalitem == -1 || ObiItem == -1) {
                ChatUtil.sendMessage(ChatFormatting.WHITE.toString() + "Not enough Material")
                disable()
                return@safeEventListener
            }
            if (entity != null) {
                target = entity
                if (entity.getDistance(mc.player) > range.value) {
                    cobi = null
                    stage = 0
                    return@safeEventListener
                }
                val obiUpExtend = BlockPos(entity).add(0.0, 3.0, 0.0)
                val obi = BlockPos(entity).add(-1.0, 1.0, 0.0)
                val obi2 = BlockPos(entity).add(0.0, 1.0, 1.0)
                val obi3 = BlockPos(entity).add(0.0, 1.0, -1.0)
                val obi4 = BlockPos(entity).add(1.0, 1.0, 0.0)
                val obiUp = BlockPos(entity).add(0.0, 2.0, 0.0)
                if (validPosition(obiUpExtend, entity) && mc.world.isAirBlock(obiUpExtend.down()) && checkAirBlock(
                        obiUpExtend,
                        false
                    )
                ) {
                    cobi = obiUpExtend
                    stage = 1
                } else if (validPosition(obi, entity) && checkAirBlock(obi, true)) {
                    cobi = obi
                    stage = 0
                } else if (validPosition(obi2, entity) && checkAirBlock(obi2, true)) {
                    cobi = obi2
                    stage = 0
                } else if (validPosition(obi3, entity) && checkAirBlock(obi3, true)) {
                    cobi = obi3
                    stage = 0
                } else if (validPosition(obi4, entity) && checkAirBlock(obi4, true)) {
                    cobi = obi4
                    stage = 0
                } else if (validPosition(obiUp, entity) && checkAirBlock(obiUp, false)) {
                    cobi = obiUp
                    stage = 1
                }
                if (cobi == null || mc.world.getBlockState(cobi!!.up(2)).block != Blocks.AIR || mc.world.getBlockState(
                        cobi!!.up()
                    ).block != Blocks.AIR
                ) {
                    disable()
                    return@safeEventListener
                }
                if (lastCrystal != null && !lastCrystal!!.isDead) {
                    if (!checkBreakRange(lastCrystal!!, 5f, 8f, MutableBlockPos())) {
                        onSpoof(oldSlot)
                        //ChatUtil.sendMessage("[§c§lAutoCev§f] Disabled Due To Crystal Unbreakable!");
                        //disable();
                        return@safeEventListener
                    }
                }
                if (lastCrystal != null && crystalDead.value) {
                    if (!lastCrystal!!.isDead) {
                        onSpoof(oldSlot)
                        return@safeEventListener
                    }
                }
                blockRenderSmooth.updatePos(cobi!!)
                currentX = cobi!!.x
                currentY = cobi!!.y
                currentZ = cobi!!.z
                when (progress) {
                    0 -> {
                        if (firsttime < 1) {
                            flag = true
                        }
                        onSpoof(ObiItem)
                        if (mc.world.isAirBlock(cobi!!.down()) && stage == 0) {
                            BlockUtil.placeBlock(cobi!!.down(), EnumHand.MAIN_HAND, rotate.value, packet.value)
                        }
                        if (mc.world.isAirBlock(cobi!!)) {
                            BlockUtil.placeBlock(cobi, EnumHand.MAIN_HAND, rotate.value, packet.value)
                        }
                        onSpoof(oldSlot)
                        ++progress
                    }

                    1 -> {
                        if (firsttime < 1 || lastX != currentX || lastZ != currentZ || lastY != currentY) {
                            mc.player.connection.sendPacket(
                                CPacketPlayerDigging(
                                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                                    cobi!!,
                                    BlockUtil.getRayTraceFacing(cobi)
                                )
                            )
                        }
                        onSpoof(crystalitem)
                        mc.player.connection.sendPacket(
                            CPacketPlayerTryUseItemOnBlock(
                                cobi!!,
                                EnumFacing.UP,
                                EnumHand.MAIN_HAND,
                                0.5f,
                                1f,
                                0.5f
                            )
                        )
                        onSpoof(oldSlot)
                        ++progress
                    }

                    2 -> {
                        onSpoof(pickaxeitem)
                        mc.player.connection.sendPacket(
                            CPacketPlayerDigging(
                                CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                cobi!!,
                                BlockUtil.getRayTraceFacing(cobi)
                            )
                        )
                        onSpoof(oldSlot)
                        ++progress
                    }

                    3 -> {
                        var n5 = 0
                        if (mc.world.isAirBlock(cobi!!)) {
                            for (entity3 in ArrayList(mc.world.loadedEntityList)) {
                                if (entity.getDistance(entity3) <= range.value) {
                                    if (entity3 !is EntityEnderCrystal) {
                                        continue
                                    }
                                    if (breakDelay.passed(50)) {
                                        mc.player.connection.sendPacket(CPacketUseEntity(entity3))
                                        breakDelay.reset()
                                    }
                                    lastCrystal = entity3
                                    boost = true
                                    if (swing.value && !pswing.value) {
                                        mc.player.swingArm(EnumHand.MAIN_HAND)
                                    } else if (swing.value && pswing.value) {
                                        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                                    }
                                    ++n5
                                }
                                if (n5 != 0 && !flag) {
                                    break
                                }
                            }
                        }
                        progress = 0
                        ++firsttime
                    }
                }
            } else {
                ChatUtil.sendMessage("[§c§lAutoCev§f] Disabled Due To No Target!")
                disable()
            }
        }
    }

    fun checkAirBlock(pos: BlockPos, down: Boolean): Boolean {
        val horizontal = (!mc.world.isAirBlock(pos.north())
                || !mc.world.isAirBlock(pos.east())
                || !mc.world.isAirBlock(pos.west())
                || !mc.world.isAirBlock(pos.south()))
        return if (down) {
            horizontal || !mc.world.isAirBlock(pos.down())
        } else horizontal
    }

    fun SafeClientEvent.canHitCrystal(crystal: Vec3d, pos: BlockPos?, player: EntityPlayer?): Boolean {
        val healthSelf = mc.player.health + mc.player.absorptionAmount
        if (mc.player.isDead || healthSelf <= 0.0f || player!!.isDead) return false
        val minDamage = 0.2
        val target = calcDamage(
            player,
            player.positionVector,
            player.entityBoundingBox,
            crystal.x,
            crystal.y,
            crystal.z,
            MutableBlockPos()
        ).toDouble()
        return if (mode.value == CheckMode.Damage) {
            target < minDamage
        } else {
            mc.world.isAirBlock(pos!!)
        }
    }

    enum class CheckMode {
        Air, Damage
    }

    fun findClosestTarget(): EntityPlayer? {
        if (mc.world.playerEntities.isEmpty()) {
            return null
        }
        var closestTarget: EntityPlayer? = null
        for (target in ArrayList(mc.world.playerEntities)) {
            if (target === mc.player) {
                continue
            }
            if (FriendManager.isFriend(target.name)) {
                continue
            }
            if (closestTarget != null && mc.player.getDistance(target) > mc.player.getDistance(closestTarget)) {
                continue
            }
            closestTarget = target
        }
        return closestTarget
    }
}