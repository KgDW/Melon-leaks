package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.event.events.block.BlockEvent
import dev.zenhao.melon.manager.CrystalManager
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.HotbarManager.spoofHotbarBypass
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.combat.CevBreaker
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.animations.Easing
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.block.BlockUtil
import dev.zenhao.melon.utils.block.BreakingUtil.Companion.calcBreakTime
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.inventory.findBestTool
import dev.zenhao.melon.utils.math.RotationUtils
import dev.zenhao.melon.utils.vector.Vec2f
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.system.util.color.ColorRGB
import melon.utils.block.getBlock
import melon.utils.graphics.ESPRenderer
import melon.utils.math.VectorUtils.toViewVec
import melon.utils.math.scale
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Blocks
import net.minecraft.init.Enchantments
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent

@Module.Info(name = "PacketMine", category = Category.PLAYER, description = "Better Mining")
object PacketMine : Module() {
    private var spoofMining = bsetting("SpoofMine", true)
    private var spoofBypass = bsetting("SpoofBypass", false).b(spoofMining)
    private var packetOnly = bsetting("PacketOnly", false)
    private var continous = bsetting("ContinuousMine", false).b(packetOnly)
    private var resendClick = bsetting("ResendClick", false).b(packetOnly)
    private var autoResend = bsetting("AutoResend", false).b(packetOnly).b(resendClick)
    private var cancelNonSafe = bsetting("CancelNotSafe", false).b(packetOnly).b(resendClick)
    private var safeTicks = isetting("SafeTicks", 3, 0, 20).b(cancelNonSafe).b(packetOnly).b(resendClick)
    private var pauseStopPacket = bsetting("PauseStopPacket", false).b(packetOnly)
    private var spoofSwing = bsetting("SpoofSwing", false)
    private var alwaysSwing = bsetting("AlwaysSwing", false).b(spoofSwing)
    private var noanim = bsetting("NoAnim", false)
    private var ground = bsetting("SetGround", false)
    private var rotateCheck = bsetting("RotateCheck", false)
    private var rotateRange = fsetting("RotateRange", 10f, 0f, 180f, 1f).b(rotateCheck)
    private var rotate = bsetting("Rotate", false)
    private var render = bsetting("Render", true)
    private val renderer = ESPRenderer().apply { aFilled = 31; aOutline = 233 }
    private var groundMining = false
    private var currentBlockState: IBlockState? = null
    private var startTime = System.currentTimeMillis()
    private var facing: EnumFacing? = null
    var currentPos: BlockPos? = null
    private var flagged = false
    private var flagTime = 0L
    private var flaggedTimes = 0
    private var flagRemoveTimer = TimerUtils()
    private var mineTime = 0f
    private var multiplier = 0f
    private var waitedTicks = 0

    @Suppress("unused")
    private enum class SwapMode {
        Spoof,
        Bypass,
        Off
    }

    private fun checkBreakBox(box: AxisAlignedBB, eyePos: Vec3d, sight: Vec3d): Boolean {
        return !rotateCheck.value
                || box.calculateIntercept(eyePos, sight) != null
                || rotateRange.value != 0.0f && checkRotationDiff(
            RotationUtils.getRotationTo(eyePos, box.center),
            rotateRange.value
        )
    }

    private fun checkRotationDiff(rotation: Vec2f, range: Float): Boolean {
        val serverSide = CrystalManager.rotation
        return RotationUtils.calcAbsAngleDiff(rotation.x, serverSide.x) <= range
                && RotationUtils.calcAbsAngleDiff(rotation.y, serverSide.y) <= range
    }

    private fun SafeClientEvent.equipBestTool(blockState: IBlockState?): Int {
        var max = 0.0
        var bestSlot = 0
        for (i in 0..8) {
            var eff: Int
            var speed = 0f
            val stack = player.inventory.getStackInSlot(i)
            if (stack.isEmpty || stack.getDestroySpeed(blockState!!)
                    .also { speed = it } <= 1.0f || (speed.toDouble() + if (EnchantmentHelper.getEnchantmentLevel(
                        Enchantments.EFFICIENCY,
                        stack
                    )
                        .also { eff = it } > 0
                ) eff + 1.0 else 0.0).toFloat().also { speed = it }.toDouble() <= max
            ) continue
            max = speed.toDouble()
            bestSlot = i
        }
        return bestSlot
    }

    private fun SafeClientEvent.sendMinePacket(action: Int, pos: BlockPos? = currentPos, face: EnumFacing? = facing) {
        if (pos != null && face != null) {
            when (action) {
                1 -> connection.sendPacket(
                    CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                        pos,
                        face
                    )
                )

                2 -> connection.sendPacket(
                    CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        pos,
                        face
                    )
                )

                3 -> connection.sendPacket(
                    CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                        pos,
                        face
                    )
                )
            }
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: ClientDisconnectionFromServerEvent?) {
        currentPos = null
        facing = null
    }

    init {
        onPacketReceive {
            if (it.packet is SPacketBlockChange && packetOnly.value && continous.value && currentPos != null && it.packet.blockPosition == currentPos) {
                val newBlockState = it.packet.blockState
                val packetPos = it.packet.blockPosition
                val current = world.getBlock(packetPos)
                val new = newBlockState.block
                if (new != Blocks.AIR) {
                    if (player.getDistanceSq(it.packet.blockPosition) <= 36) {
                        if (new != current) {
                            facing?.let { facing ->
                                BlockEvent(
                                    packetPos,
                                    facing
                                ).post()
                                ChatUtil.sendMessage("Retry Mining!")
                            }
                        }
                    }
                }
            }
        }

        onPacketSend { event ->
            when (event.packet) {
                is CPacketPlayerDigging -> {
                    when (event.packet.action) {
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK -> {
                            if (currentPos != null && BlockUtil.canBreak(currentPos, false)) {
                                if (event.packet.position == currentPos) {
                                    if (cancelNonSafe.value && multiplier < 1f && waitedTicks < safeTicks.value) {
                                        event.cancelled = true
                                    }
                                }
                            }
                        }

                        CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK -> {
                            flaggedTimes++
                        }

                        else -> {}
                    }
                }
            }
        }

        safeEventListener<BlockEvent> { event ->
            try {
                currentPos?.let { pos ->
                    if (!BlockUtil.canBreak(pos, false)) {
                        currentPos = null
                        return@safeEventListener
                    }
                    if (pos == event.pos) {
                        if (!packetOnly.value) {
                            return@safeEventListener
                        } else {
                            if (resendClick.value) {
                                val eyePos = CrystalManager.position.add(0.0, player.getEyeHeight().toDouble(), 0.0)
                                val sight = eyePos.add(CrystalManager.rotation.toViewVec().scale(8.0))
                                if (!checkBreakBox(AxisAlignedBB(pos), eyePos, sight)) {
                                    return@safeEventListener
                                }
                                if (cancelNonSafe.value && multiplier < 1f) {
                                    return@safeEventListener
                                }
                                resendMine(pos)
                            }
                        }
                    }
                }
                currentPos = event.pos
                facing = event.facing
                startTime = System.currentTimeMillis()
                currentPos?.let { pos ->
                    currentBlockState = world.getBlockState(pos)
                    mineTime = calcBreakTime(player, pos)
                    if (BlockUtil.canBreak(pos, false)) {
                        mineBlock()
                    }
                }
            } catch (ignored: Exception) {
            }
        }

        onMotion {
            try {
                if (ModuleManager.getModuleByClass(CevBreaker::class.java).isEnabled) {
                    currentPos = null
                    return@onMotion
                }
                currentPos?.let { pos ->
                    if (flagged && flagTime != 0L && System.currentTimeMillis() - flagTime > (mineTime - 180)) {
                        flagged = false
                    }
                    if (mineTime != 0f && flagRemoveTimer.tickAndReset(mineTime) && flaggedTimes > 0) {
                        flaggedTimes -= MathHelper.clamp(
                            if ((packetOnly.value && !pauseStopPacket.value) || !packetOnly.value) 2 else 1, 0, 114514
                        )
                    }
                    if (BlockUtil.canBreak(pos, false) && player.getDistanceSq(pos) <= 5.5.sq) {
                        multiplier =
                            Easing.OUT_CUBIC.inc(Easing.toDelta(startTime, calcBreakTime(player, pos)))
                        if (multiplier >= 1f) {
                            waitedTicks += 1
                        } else {
                            waitedTicks = 0
                        }
                        if (resendClick.value && autoResend.value && packetOnly.value) {
                            if (!flagged) {
                                if (multiplier >= 1 && flaggedTimes in 0..4) {
                                    resendMine(pos)
                                    ChatUtil.sendMessage("Start Mining! ($flaggedTimes)")
                                    flagged = true
                                }
                            }
                            return@onMotion
                        }
                        if (isFinished(pos) && spoofMining.value && !packetOnly.value) {
                            spoofMine(currentBlockState!!)
                        } else if (!spoofMining.value) {
                            if (!packetOnly.value) {
                                sendMinePacket(2)
                            }
                        }
                        if (spoofSwing.value && alwaysSwing.value) {
                            connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                        }
                        if (rotate.value) {
                            if (System.currentTimeMillis() - startTime > mineTime - 150) {
                                RotationManager.addRotationsNew(pos)
                            }
                        }
                    }
                    if (ground.value) {
                        if (BlockUtil.canBreak(pos, false) && player.getDistanceSq(pos) <= 36) {
                            it.isOnGround = true
                            groundMining = true
                        } else {
                            groundMining = false
                        }
                    } else {
                        groundMining = false
                    }
                }
            } catch (ignored: Exception) {
            }
        }

        onRender3D {
            currentPos?.let { pos ->
                if (render.value && BlockUtil.canBreak(pos, true)) {
                    val multiplier = Easing.OUT_CUBIC.inc(Easing.toDelta(startTime, calcBreakTime(player, pos)))
                    val box = AxisAlignedBB(pos).scale(multiplier.toDouble())
                    val color = if (world.isAirBlock(pos)) ColorRGB(32, 255, 32) else ColorRGB(255, 32, 32)

                    renderer.add(box, color)
                    renderer.render(true)
                }
            }
        }
    }

    private fun SafeClientEvent.resendMine(pos: BlockPos) {
        if (rotate.value) {
            if (System.currentTimeMillis() - startTime > mineTime - 150) {
                RotationManager.addRotationsNew(pos)
            }
        }
        if (spoofMining.value) {
            sendMinePacket(3)
            spoofMine(world.getBlockState(pos))
        } else {
            sendMinePacket(3)
            sendMinePacket(2)
        }
        flagTime = System.currentTimeMillis()
    }

    private fun SafeClientEvent.mineBlock() {
        if (spoofSwing.value) {
            connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }
        sendMinePacket(1)
        if (noanim.value) {
            sendMinePacket(2)
        }
        if ((packetOnly.value && !pauseStopPacket.value) || !packetOnly.value) {
            sendMinePacket(3)
        }
    }

    private fun isFinished(pos: BlockPos): Boolean {
        return BlockUtil.canBreak(pos, false) && System.currentTimeMillis() - startTime > mineTime
    }

    private fun SafeClientEvent.spoofMine(blockState: IBlockState, pos: BlockPos = currentPos!!) {
        if (!spoofBypass.value) {
            spoofHotbar(equipBestTool(blockState)) {
                sendMinePacket(2, pos)
            }
        } else {
            findBestTool(blockState)?.let {
                spoofHotbarBypass(it) {
                    sendMinePacket(2, pos)
                }
            }
        }
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        currentPos = null
        facing = null
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        currentPos = null
        facing = null
    }

    override fun getHudInfo(): String {
        val state: String = if (groundMining) {
            "Bypassing"
        } else {
            "Normal"
        }
        return state + if (packetOnly.value) " | Packet" else " | Instant"
    }

}