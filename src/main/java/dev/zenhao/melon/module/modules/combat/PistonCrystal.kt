package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.manager.HotbarManager.onSpoof
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.BlockEasingRender
import dev.zenhao.melon.utils.block.BlockUtils
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.entity.CrystalUtil
import dev.zenhao.melon.utils.entity.TargetUtils
import dev.zenhao.melon.utils.inventory.InventoryUtil.pickItem
import dev.zenhao.melon.utils.threads.runAsyncThread
import melon.events.RunGameLoopEvent
import melon.system.event.safeEventListener
import melon.utils.player.getTarget
import net.minecraft.block.Block
import net.minecraft.block.BlockEmptyDrops
import net.minecraft.block.BlockObsidian
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "PistonCrystal", category = Category.COMBAT, description = "Holecamper.EZ")
class PistonCrystal : Module() {
    private var range = isetting("Range", 5, 1, 6)
    private var delay1 = isetting("ChangeDelay", 5, 0, 20)
    private var thread = isetting("Thread", 1, 0, 10)
    private var min: Setting<Int> = isetting("MinDamage", 5, 0, 10)
    private var blockRenderSmooth = BlockEasingRender(BlockPos(0, 0, 0), 450f, 250f)
    private var attackDelay = TimerUtils()
    private var attackable: CopyOnWriteArrayList<PA?>? = CopyOnWriteArrayList()
    private var progress = 0
    private var oldSlot = 0
    private var pa: PA? = null

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        progress = 0
        attackable!!.clear()
        attackDelay.reset()
    }

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            oldSlot = mc.player.inventory.currentItem
            val pistonItem = pickItem(33, false)
            val cryst = pickItem(426, false)
            val redstoneBlock = pickItem(152, false)
            val redstoneTorch = pickItem(76, false)
            if (pistonItem == -1 || cryst == -1 || redstoneBlock == -1 && redstoneTorch == -1) {
                ChatUtil.sendMessage("\u00A77[Melon] \u00A74Item Not Found ")
                toggle()
            }
            val player = getTarget(range.value)?: return@safeEventListener
            val range = range.value
            if (attackable == null) {
                return@safeEventListener
            }
            if (attackable!!.isEmpty() || mc.player.ticksExisted % 1.coerceAtLeast(
                    delay1.value / 1.coerceAtLeast(
                        thread.value
                    )
                ) == 0) {
                runAsyncThread {
                    for (dx in -range..range) {
                        for (dy in -range..range) {
                            for (dz in -range..range) {
                                val pos = BlockPos(mc.player).add(dx, dy, dz)
                                if (player.getDistanceSq(pos) > range * range) continue
                                var b = false
                                for (off in pistonoff) {
                                    if (mc.world.getBlockState(pos.add(off)).block is BlockObsidian) {
                                        b = true
                                        break
                                    }
                                    if (mc.world.getBlockState(pos.add(off)).block is BlockEmptyDrops) {
                                        b = true
                                        break
                                    }
                                }
                                if (!b) continue
                                val damage = CrystalUtil.getDamage(Vec3d(pos).add(0.5, 0.0, 0.5), player)
                                if (damage < min.value) continue
                                pa = PA(pos, damage)
                                if (!pa!!.canPA()) continue
                                if (!attackable!!.contains(pa)) {
                                    attackable!!.add(pa)
                                }
                                break
                            }
                        }
                    }
                }
                attackable!!.sortWith { a: PA?, b: PA? ->
                    if (a == null && b == null) return@sortWith 0
                    b!!.damage.compareTo(a!!.damage)
                }
            }
            if (attackable!!.isNotEmpty()) {
                attackable!![0]!!.updatePA()
            }
        }

        safeEventListener<PlayerMotionEvent> {
            for (et in ArrayList(mc.world.loadedEntityList)) {
                if (et != null) {
                    if (et is EntityEnderCrystal) {
                        if (et.getDistance(mc.player) > range.value) continue
                        if (attackDelay.tickAndReset(55)) {
                            mc.player.connection.sendPacket(CPacketUseEntity(et))
                            mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                            attackable!!.clear()
                        }
                    }
                }
            }
        }
    }

    fun rotateHantaigawa(f: EnumFacing?): EnumFacing? {
        when (f) {
            EnumFacing.WEST -> return EnumFacing.EAST
            EnumFacing.EAST -> return EnumFacing.WEST
            EnumFacing.SOUTH -> return EnumFacing.NORTH
            EnumFacing.NORTH -> return EnumFacing.SOUTH
            EnumFacing.UP -> return EnumFacing.DOWN
            EnumFacing.DOWN -> return EnumFacing.UP
            else -> {}
        }
        return null
    }

    inner class PA(var pos: BlockPos, var damage: Double) {
        private var crystal: BlockPos? = null
        private var power: BlockPos? = null
        private var pistonFacing: EnumFacing? = null
        private var piston: BlockPos? = null
        private var stage = 0

        init {
            blockRenderSmooth.updatePos(pos)
        }

        fun canPA(): Boolean {
            val pist = .5
            for (f in EnumFacing.values()) {
                val crypos = pos.offset(f)
                //check
                if (!mc.world.isAirBlock(crypos)) continue
                if (!mc.world.isAirBlock(crypos.offset(EnumFacing.UP))) continue
                if (!TargetUtils.canAttack(
                        mc.player.positionVector.add(0.0, mc.player.getEyeHeight().toDouble(), 0.0),
                        Vec3d(crypos).add(.5, 1.7, .5)
                    )
                ) continue
                if (mc.world.getBlockState(crypos.offset(EnumFacing.DOWN)).block !is BlockObsidian && mc.world.getBlockState(
                        crypos.offset(EnumFacing.DOWN)
                    ).block !is BlockEmptyDrops
                ) continue
                if (!mc.world.checkNoEntityCollision(Block.FULL_BLOCK_AABB.offset(crypos))) continue
                if (mc.player.getDistanceSq(
                        crypos.getX().toDouble() + 0.5,
                        crypos.getY().toDouble() + 0.5,
                        crypos.getZ().toDouble() + 0.5
                    ) >= 64.0
                ) continue
                //check2
                crystal = crypos
                pistonFacing = rotateHantaigawa(f)
                if (pistonFacing == EnumFacing.DOWN) continue
                if (!mc.world.isAirBlock(crypos.offset(pistonFacing!!))) continue
                for (off in pistonoff) {
                    val pispos = crystal!!.add(off)
                    if (pispos == crypos) continue
                    if (crypos.offset(EnumFacing.UP) == pispos) continue
                    if (crypos.offset(pistonFacing!!) == pispos) continue
                    val sfac = EnumFacing.getDirectionFromEntityLiving(pispos, mc.player)
                    if (sfac.getAxis() == EnumFacing.Axis.Y) {
                        if (pistonFacing != sfac) continue
                    }
                    if (pistonFacing!!.getAxis() == EnumFacing.Axis.Y) {
                        if (pistonFacing != sfac) continue
                    }
                    power = null
                    if (mc.world.isBlockPowered(pispos)) {
                        if (BlockUtils.isPlaceable(pispos, 0.0, true) == null) continue
                    } else {
                        for (fa in EnumFacing.values()) {
                            val powpos = pispos.offset(fa)
                            if (pispos == powpos) continue
                            if (pispos.offset(pistonFacing!!) == powpos) continue
                            if (crypos == powpos) continue
                            if (crypos.offset(EnumFacing.UP) == powpos) continue
                            if (mc.player.getDistanceSq(
                                    powpos.getX().toDouble() + 0.5,
                                    powpos.getY().toDouble() + 0.5,
                                    powpos.getZ().toDouble() + 0.5
                                ) >= 64.0
                            ) continue
                            if (BlockUtils.isPlaceable(powpos, 0.0, true) == null) continue
                            if (pistonFacing!!.getDirectionVec()
                                    .getX() > 0 && powpos.getX() - pist > crypos.getX()
                            ) continue
                            if (pistonFacing!!.getDirectionVec()
                                    .getY() > 0 && powpos.getY() - pist > crypos.getY()
                            ) continue
                            if (pistonFacing!!.getDirectionVec()
                                    .getZ() > 0 && powpos.getZ() - pist > crypos.getZ()
                            ) continue
                            if (pistonFacing!!.getDirectionVec()
                                    .getX() < 0 && powpos.getX() + pist < crypos.getX()
                            ) continue
                            if (pistonFacing!!.getDirectionVec()
                                    .getY() < 0 && powpos.getY() + pist < crypos.getY()
                            ) continue
                            if (pistonFacing!!.getDirectionVec()
                                    .getZ() < 0 && powpos.getZ() + pist < crypos.getZ()
                            ) continue
                            if (!mc.world.isAirBlock(powpos)) continue
                            power = powpos
                        }
                        if (power == null) continue
                    }
                    if (mc.player.getDistanceSq(
                            pispos.getX().toDouble() + 0.5,
                            pispos.getY().toDouble() + 0.5,
                            pispos.getZ().toDouble() + 0.5
                        ) >= 64.0
                    ) continue
                    if (!mc.world.checkNoEntityCollision(Block.FULL_BLOCK_AABB.offset(pispos))) continue
                    if (pistonFacing!!.getDirectionVec().getX() > 0 && pispos.getX() - pist > crypos.getX()) continue
                    if (pistonFacing!!.getDirectionVec().getY() > 0 && pispos.getY() - pist > crypos.getY()) continue
                    if (pistonFacing!!.getDirectionVec().getZ() > 0 && pispos.getZ() - pist > crypos.getZ()) continue
                    if (pistonFacing!!.getDirectionVec().getX() < 0 && pispos.getX() + pist < crypos.getX()) continue
                    if (pistonFacing!!.getDirectionVec().getY() < 0 && pispos.getY() + pist < crypos.getY()) continue
                    if (pistonFacing!!.getDirectionVec().getZ() < 0 && pispos.getZ() + pist < crypos.getZ()) continue
                    if (!mc.world.isAirBlock(pispos)) continue
                    if (!mc.world.isAirBlock(pispos.offset(pistonFacing!!))) continue
                    if (pispos.getY() < crystal!!.getY() && pistonFacing!!.getAxis() != EnumFacing.Axis.Y) continue
                    piston = pispos
                    return true
                }
            }
            return false
        }

        fun updatePA() {
            val pistonItem = pickItem(33, false)
            val redstoneBlock = pickItem(152, false)
            val redstoneTorch = pickItem(76, false)
            val cryst = pickItem(426, false)
            when (pistonFacing) {
                EnumFacing.SOUTH -> {
                    RotationManager.addRotations(180f, 0f)
                }

                EnumFacing.NORTH -> {
                    RotationManager.addRotations(0f, 0f)
                }

                EnumFacing.EAST -> {
                    RotationManager.addRotations(90f, 0f)
                }

                EnumFacing.WEST -> {
                    RotationManager.addRotations(-90f, 0f)
                }

                EnumFacing.UP, EnumFacing.DOWN ->
                    RotationManager.addRotations(mc.player.rotationYaw, 90f)

                else -> {}
            }
            if (stage == 2) {
                onSpoof(pistonItem)
                BlockUtils.doPlace(BlockUtils.isPlaceable(piston, 0.0, false), true)
                if (power != null) {
                    onSpoof(redstoneBlock)
                    onSpoof(redstoneTorch)
                    BlockUtils.doPlace(BlockUtils.isPlaceable(power, 0.0, false), true)
                }
                onSpoof(pistonItem)
                BlockUtils.doPlace(BlockUtils.isPlaceable(piston, 0.0, false), true)
                onSpoof(cryst)
                CrystalUtil.placeCrystal(crystal)
                if (power != null) {
                    onSpoof(redstoneBlock)
                    onSpoof(redstoneTorch)
                    BlockUtils.doPlace(BlockUtils.isPlaceable(power, 0.0, false), true)
                }
                onSpoof(oldSlot)
            }
            if (stage == 3) {
                onSpoof(cryst)
                if (piston != null) {
                    mc.world.setBlockToAir(piston!!)
                }
                if (power != null) {
                    mc.world.setBlockToAir(power!!)
                }
                onSpoof(oldSlot)
            }
            stage++
        }
    }

    companion object {
        val pistonoff = arrayOf( /*y = -1*/
            BlockPos(-1, -1, -1),
            BlockPos(0, -1, -1),
            BlockPos(1, -1, -1),
            BlockPos(-1, -1, 0),
            BlockPos(0, -1, 0),
            BlockPos(1, -1, 0),
            BlockPos(-1, -1, 1),
            BlockPos(0, -1, 1),
            BlockPos(1, -1, 1),  /*y = 0*/
            BlockPos(-1, 0, -1),
            BlockPos(0, 0, -1),
            BlockPos(1, 0, -1),
            BlockPos(-1, 0, 0),
            BlockPos(0, 0, 0),
            BlockPos(1, 0, 0),
            BlockPos(-1, 0, 1),
            BlockPos(0, 0, 1),
            BlockPos(1, 0, 1),  /*y = 1*/
            BlockPos(-1, 1, -1),
            BlockPos(0, 1, -1),
            BlockPos(1, 1, -1),
            BlockPos(-1, 1, 0),
            BlockPos(0, 1, 0),
            BlockPos(1, 1, 0),
            BlockPos(-1, 1, 1),
            BlockPos(0, 1, 1),
            BlockPos(1, 1, 1)
        )
    }
}