package dev.zenhao.melon.module.modules.player

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.player.JumpEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.gui.Notification
import dev.zenhao.melon.manager.HotbarManager.onSpoof
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.entity.PlayerUtil
import dev.zenhao.melon.utils.gl.MelonTessellator
import dev.zenhao.melon.utils.math.RandomUtil
import melon.system.event.safeEventListener
import net.minecraft.block.*
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.nio.FloatBuffer
import java.util.*
import java.util.function.Consumer

@Module.Info(name = "Scaffold", category = Category.PLAYER)
class Scaffold : Module() {
    var renderTarget: MutableList<EntityPlayerSP> = ArrayList()
    var sneakDelay = TimerUtils()
    var PlaceDelay = TimerUtils()
    var sprintDelay = TimerUtils()
    var isTowering = false
    var spoofGround = false
    var rotating = false
    var mode = msetting("TowerMode", TowerMode.Constant)
    var rmode = msetting("RotationMode", RotateMode.Custom)
    var renderMode = msetting("RenderMode", RenderMode.Block)
    var pitchLook = isetting("RotationPitch", 81, 0, 90).m(rmode, RotateMode.Custom)
    var towerDelay = isetting("TowerDelay", 250, 0, 500).m(mode, TowerMode.Hypixel)
    var towerboost = bsetting("TimerBoost", false)
    var timerBypass = bsetting("TimerBypass", true).b(towerboost)
    var timerBoost = fsetting("TimerValue", 1.2f, 1f, 3f).b(towerboost)
    var safe = bsetting("SafeEagle", false)
    var autowalk = bsetting("AutoWalk", false).b(safe)
    var cancelwalk = bsetting("CancelWalk", false).b(safe)
    var ghost = bsetting("GhostSwitch", true)
    var spoofSprint = bsetting("SpoofSprint", false)
    var cancelSprint = bsetting("CancelSprintOnToggle", false)
    var swing = bsetting("PacketSwing", true)
    var lagbackCheck = bsetting("LagBackCheck", true)
    var towerTimer = TimerUtils()
    var packetListReset = TimerUtils()
    var jumpGround = 0.0
    var data: BlockData? = null
    var slot = 0
    var yaw = 0f
    var pitch = 0f
    var currentblock: ItemStack? = null
    var newSlot = -1
    var rotationMode = 1
    var normalPos = 0f
    var normalLookPos = 0f
    var lastPitch = 0f
    var lastYaw = 0f
    override fun getHudInfo(): String {
        return if (rotating) {
            "Placing"
        } else {
            "Waiting..."
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is CPacketPlayer.Position && rotationMode == 1) {
            normalPos++
            if (normalPos > 20) {
                rotationMode = 2
            }
        } else if (event.packet is PositionRotation && rotationMode == 2) {
            normalLookPos++
            if (normalLookPos > 20) {
                rotationMode = 1
            }
        }
        if (event.packet is SPacketPlayerPosLook) {
            if (lagbackCheck.value) {
                jumpGround = 0.0
                mc.player.isSprinting = false
                ChatUtil.sendClientMessage("(LagBackCheck) Potition Reset!", Notification.Type.WARNING)
            }
        }
    }

    @SubscribeEvent
    fun onPacketSend(event: PacketEvent.Send) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is CPacketPlayer) {
            if (mode.value == TowerMode.VerusTest) {
                if (spoofGround) {
                    (event.packet as CPacketPlayer).onGround = true
                    spoofGround = false
                }
            }
        }
    }

    @SubscribeEvent
    fun onJump(event: JumpEvent) {
        if (fullNullCheck()) {
            return
        }
        if (isTowering && (mode.value == TowerMode.VerusTest || mode.value == TowerMode.Hypixel)) {
            event.isCanceled = true
        }
    }

    init {
        safeEventListener<PlayerMotionEvent> {
            newSlot = mc.player.inventory.currentItem
            if (packetListReset.passed(1000)) {
                normalPos = 0f
                normalLookPos = 0f
                lastYaw = mc.player.rotationYaw
                lastPitch = mc.player.rotationPitch
                packetListReset.reset()
            }
            if (isTowering && timerBypass.value) {
                when (rotationMode) {
                    1 -> {
                        if (EntityUtil.isMoving()) {
                            it.setRotation(lastYaw, lastPitch)
                        }
                    }

                    2 -> {
                        it.setRotation(
                            lastYaw + RandomUtil.nextFloat(1f, 3f),
                            lastPitch + RandomUtil.nextFloat(1f, 3f)
                        )
                    }
                }
            }
            var rot = 0f
            if (!renderTarget.contains(mc.player)) {
                renderTarget.add(mc.player)
            }
            if (blockCount <= 0 && getallBlockCount() <= 0) {
                return@safeEventListener
            }
            if (blockCount <= 0) {
                val spoofSlot = bestSpoofSlot
                getBlock(spoofSlot)
            }
            data =
                if (getBlockData(BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ)) == null) getBlockData(
                    BlockPos(
                        mc.player.posX, mc.player.posY - 1.0, mc.player.posZ
                    ).down(1)
                ) else getBlockData(BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ))
            slot = blockSlot
            currentblock = mc.player.inventoryContainer.getSlot(slot + 36).stack
            if (data == null || slot == -1 || blockCount <= 0 || !(EntityUtil.isMoving() || mc.gameSettings.keyBindJump.isKeyDown)) {
                yaw = mc.player.rotationYaw
                pitch = mc.player.rotationPitch
                rotating = false
                isTowering = false
                if (data != null) {
                    if (data!!.blockPos != null) {
                        data!!.blockPos = null
                    }
                }
                return@safeEventListener
            }
            if (mc.world.getBlockState(
                    BlockPos(
                        mc.player.posX,
                        mc.player.posY - 0.5,
                        mc.player.posZ
                    )
                ).block === Blocks.AIR
            ) {
                if (mc.player.movementInput.moveForward > 0.0f) {
                    rot = 180.0f
                    if (mc.player.movementInput.moveStrafe > 0.0f) {
                        rot = -120.0f
                    } else if (mc.player.movementInput.moveStrafe < 0.0f) {
                        rot = 120.0f
                    }
                } else if (mc.player.movementInput.moveForward == 0.0f) {
                    rot = 180.0f
                    if (mc.player.movementInput.moveStrafe > 0.0f) {
                        rot = -90.0f
                    } else if (mc.player.movementInput.moveStrafe < 0.0f) {
                        rot = 90.0f
                    }
                } else if (mc.player.movementInput.moveForward < 0.0f) {
                    if (mc.player.movementInput.moveStrafe > 0.0f) {
                        rot = -45.0f
                    } else if (mc.player.movementInput.moveStrafe < 0.0f) {
                        rot = 45.0f
                    }
                }
                if (PlayerUtil.isAirUnder(mc.player) && mc.gameSettings.keyBindJump.isKeyDown && !PlayerUtil.MovementInput() && mode.value != TowerMode.OFF) {
                    rot = 180.0f
                }
                if (rmode.value == RotateMode.Custom) {
                    yaw = MathHelper.wrapDegrees(mc.player.rotationYaw) - rot
                    pitch = pitchLook.value.toFloat()
                }
            }
            val rotations = BlockInteractionHelper.getLegitRotations(Vec3d(data!!.blockPos!!))
            if (rmode.value == RotateMode.Legit) {
                yaw = rotations[0]
                pitch = rotations[1]
            }
            it.setRotation(yaw, pitch)
            rotating = true
            if (PlayerUtil.isAirUnder(mc.player) && EntityUtil.isOnGround(1.15) && mc.gameSettings.keyBindJump.isKeyDown && !PlayerUtil.MovementInput() && mode.value != TowerMode.OFF) {
                isTowering = true
                if (towerboost.value) {
                    mc.timer.tickLength = 50f / timerBoost.value
                }
                when (mode.value) {
                    TowerMode.Jump -> {
                        fakeJump()
                        mc.player.jump()
                    }

                    TowerMode.VerusTest -> {
                        if (mc.player.ticksExisted % 2 == 1) {
                            mc.player.motionY = 0.5
                            spoofGround = false
                        } else {
                            mc.player.motionY = 0.0
                            mc.player.onGround = true
                            spoofGround = true
                        }
                    }

                    TowerMode.Constant -> {
                        if (mc.player.onGround) {
                            fakeJump()
                            jumpGround = mc.player.posY
                            mc.player.motionY = 0.4001
                        }
                        if (mc.player.posY > jumpGround + 1f) {
                            fakeJump()
                            mc.player.setPosition(mc.player.posX, mc.player.posY, mc.player.posZ)
                            mc.player.movementInput.moveStrafe--
                            mc.player.motionY = 0.4001
                            jumpGround = mc.player.posY
                        }
                    }

                    TowerMode.Hypixel -> {
                        mc.player.motionX = 0.0
                        mc.player.motionZ = 0.0
                        mc.player.jumpMovementFactor = 0f
                        if (towerTimer.passed(towerDelay.value)) {
                            mc.player.motionY = 0.399999986886978 + EntityUtil.getJumpEffect() * 0.1
                            towerTimer.reset()
                        }
                    }

                    TowerMode.Packet -> {
                        if (mc.player.onGround && towerTimer.passed(2)) {
                            fakeJump()
                            mc.player.connection.sendPacket(
                                CPacketPlayer.Position(
                                    mc.player.posX,
                                    mc.player.posY + 0.42,
                                    mc.player.posZ,
                                    false
                                )
                            )
                            mc.player.connection.sendPacket(
                                CPacketPlayer.Position(
                                    mc.player.posX,
                                    mc.player.posY + 0.753,
                                    mc.player.posZ,
                                    false
                                )
                            )
                            mc.player.setPosition(mc.player.posX, mc.player.posY + 1.0, mc.player.posZ)
                            towerTimer.reset()
                        }
                    }

                    TowerMode.OFF -> {}
                }
            } else {
                isTowering = false
            }
            if (EntityUtil.isOnGround(1.15) && mc.gameSettings.keyBindJump.isKeyDown && !PlayerUtil.MovementInput() && mode.value != TowerMode.OFF) {
            } else if (mc.timer.tickLength == timerBoost.value) {
                mc.timer.tickLength = 50f
            }
            if (!mc.gameSettings.keyBindJump.isKeyDown) {
                isTowering = false
                mc.timer.tickLength = 50f
            }
            var eagle = false
            if (blockCount <= 0 && getallBlockCount() <= 0) {
                return@safeEventListener
            }
            if (safe.value && !isTowering) {
                if (autowalk.value) {
                    mc.gameSettings.keyBindForward.pressed = true
                }
                if (mc.world.getBlockState(
                        BlockPos(
                            mc.player.posX,
                            mc.player.posY - 1.0,
                            mc.player.posZ
                        )
                    ).block === Blocks.AIR
                ) {
                    mc.gameSettings.keyBindSneak.pressed = true
                    eagle = true
                } else {
                    mc.gameSettings.keyBindSneak.pressed = false
                    eagle = false
                }
            }
            onSpoof(slot)
            if (safe.value && !eagle) {
                if (ghost.value) {
                    onSpoof(newSlot)
                }
                return@safeEventListener
            }
            if (mc.player.isSprinting && spoofSprint.value && sprintDelay.passed(150)) {
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player,
                        CPacketEntityAction.Action.STOP_SPRINTING
                    )
                )
            }
            if (it.stage == 1) {
                if (mc.playerController.processRightClickBlock(
                        mc.player, mc.world, data!!.blockPos!!, data!!.enumFacing, getVec3d(
                            data!!.blockPos, data!!.enumFacing
                        ), EnumHand.MAIN_HAND
                    ) == EnumActionResult.SUCCESS
                ) {
                    if (mc.player.inventory.getStackInSlot(
                            mc.player.inventory.currentItem
                        ).getItem() is ItemBlock
                    ) {
                        mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem()
                    }
                    if (swing.value) {
                        mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                    } else {
                        mc.player.swingArm(EnumHand.MAIN_HAND)
                    }
                }
            }
            if (ghost.value) {
                onSpoof(newSlot)
            }
            if (!mc.player.isSprinting && spoofSprint.value && sprintDelay.passed(180)) {
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player,
                        CPacketEntityAction.Action.START_SPRINTING
                    )
                )
                if (sprintDelay.passed(350)) {
                    sprintDelay.reset()
                }
            }
        }
    }

    fun fakeJump() {
        mc.player.isAirBorne = true
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        if (safe.value) {
            if (cancelwalk.value) {
                mc.gameSettings.keyBindSneak.pressed = false
                mc.gameSettings.keyBindForward.pressed = false
            }
        }
        lastYaw = mc.player.rotationYaw
        lastPitch = mc.player.rotationPitch
        data = null
        slot = -1
        rotating = false
        isTowering = false
        spoofGround = false
        sneakDelay.reset()
        PlaceDelay.reset()
        sprintDelay.reset()
        renderTarget.clear()
        jumpGround = 0.0
        towerTimer.reset()
        if (cancelSprint.value) {
            mc.player.isSprinting = false
        }
    }

    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        if (safe.value) {
            mc.gameSettings.keyBindSneak.pressed = false
            if (autowalk.value) {
                mc.gameSettings.keyBindForward.pressed = false
            }
        }
        rotating = false
        isTowering = false
        mc.timer.tickLength = 50.0f
        sneakDelay.reset()
        renderTarget.clear()
        jumpGround = 0.0
        towerTimer.reset()
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (fullNullCheck()) {
            return
        }
        val hsBtoRGB = Color.HSBtoRGB(
            floatArrayOf(
                System.currentTimeMillis() % 11520L / 11520.0f
            )[0], 0.5f, 1f
        )
        val r = hsBtoRGB shr 16 and 0xFF
        val g = hsBtoRGB shr 8 and 0xFF
        val b = hsBtoRGB and 0xFF
        val a = BlockPos(mc.player.posX, mc.player.posY - 0.5, mc.player.posZ)
        when (renderMode.value) {
            RenderMode.Block -> {
                if (data != null && data!!.blockPos != null) {
                    MelonTessellator.prepare(GL11.GL_QUADS)
                    MelonTessellator.drawFullBox(a, 1f, r, g, b, 80)
                    MelonTessellator.release()
                }
            }

            RenderMode.Hanabi -> {
                renderTarget.forEach(Consumer { target: EntityPlayerSP ->
                    esp(target, event.partialTicks, 0.5)
                    esp(target, event.partialTicks, 0.4)
                })
            }

            RenderMode.Off -> {}
        }
    }

    override fun onRender2DEvent(event: RenderGameOverlayEvent.Post) {
        if (fullNullCheck()) {
            return
        }
        val sr = ScaledResolution(mc)
        val width = sr.scaledWidth
        val height = sr.scaledHeight
        val middleX = width / 2
        val middleY = height / 2
        val counts = blockCount
        val maxCount = getallBlockCount()
        mc.fontRenderer.drawString(
            (counts + maxCount).toString(),
            middleX + 10 - mc.fontRenderer.getStringWidth((counts + maxCount).toString()) / 2,
            middleY + 21,
            Color(255, 255, 255).rgb
        )
        GL11.glPushMatrix()
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        if (mc.world != null) {
            GlStateManager.pushMatrix()
            GlStateManager.rotate(-30.0f, 0.0f, 1.0f, 0.0f)
            GlStateManager.rotate(165.0f, 1.0f, 0.0f, 0.0f)
            GlStateManager.enableLighting()
            GlStateManager.enableLight(0)
            GlStateManager.enableLight(1)
            GlStateManager.enableColorMaterial()
            GlStateManager.colorMaterial(1032, 5634)
            val n = 0.4f
            val n2 = 0.6f
            val n3 = 0.0f
            GL11.glLight(16384, 4611, setColorBuffer(LIGHT0_POS.x, LIGHT0_POS.y, LIGHT0_POS.z, 0.0))
            GL11.glLight(16384, 4609, setColorBuffer(n2, n2, n2, 1.0f))
            GL11.glLight(16384, 4608, setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f))
            GL11.glLight(16384, 4610, setColorBuffer(n3, n3, n3, 1.0f))
            GL11.glLight(16385, 4611, setColorBuffer(LIGHT1_POS.x, LIGHT1_POS.y, LIGHT1_POS.z, 0.0))
            GL11.glLight(16385, 4609, setColorBuffer(n2, n2, n2, 1.0f))
            GL11.glLight(16385, 4608, setColorBuffer(0.0f, 0.0f, 0.0f, 1.0f))
            GL11.glLight(16385, 4610, setColorBuffer(n3, n3, n3, 1.0f))
            GlStateManager.shadeModel(7424)
            GL11.glLightModel(2899, setColorBuffer(n, n, n, 1.0f))
            GlStateManager.popMatrix()
        }
        GlStateManager.pushMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.clear(256)
        mc.getRenderItem().zLevel = -150.0f
        val renderItem = mc.getRenderItem()
        renderItem.renderItemAndEffectIntoGUI(currentblock, width / 2 - 20, height / 2 + 16)
        mc.getRenderItem().zLevel = 0.0f
        GlStateManager.disableBlend()
        GlStateManager.scale(0.5, 0.5, 0.5)
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.scale(2.0f, 2.0f, 2.0f)
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        GL11.glPopMatrix()
    }

    fun esp(entity: Entity, partialTicks: Float, rad: Double) {
        val points = 90f
        GlStateManager.enableDepth()
        var il = 0.0
        while (il < 4.9E-324) {
            GL11.glPushMatrix()
            GL11.glDisable(3553)
            GL11.glEnable(2848)
            GL11.glEnable(2881)
            GL11.glEnable(2832)
            GL11.glEnable(3042)
            GL11.glBlendFunc(770, 771)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
            GL11.glHint(3153, 4354)
            GL11.glDisable(2929)
            GL11.glLineWidth(3.5f)
            GL11.glBegin(3)
            val x =
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX
            val y =
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY
            val z =
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ
            val speed = 5000f
            var baseHue = (System.currentTimeMillis() % speed.toInt()).toFloat()
            while (baseHue > speed) {
                baseHue -= speed
            }
            baseHue /= speed
            for (i in 0..90) {
                val max = (i.toFloat() + (il * 8).toFloat()) / points
                var hue = max + baseHue
                while (hue > 1) {
                    hue -= 1f
                }
                val pix2 = Math.PI * 2.0
                for (i2 in 0..6) {
                    GlStateManager.color(
                        rainbow(i2 * 100).red.toFloat(),
                        rainbow(i2 * 100).green.toFloat(),
                        rainbow(i2 * 100).red.toFloat(),
                        255f
                    )
                    GL11.glVertex3d(x + rad * Math.cos(i2 * pix2 / 6.0), y, z + rad * Math.sin(i2 * pix2 / 6.0))
                }
            }
            GL11.glEnd()
            GL11.glDepthMask(true)
            GL11.glEnable(2929)
            GL11.glDisable(2848)
            GL11.glDisable(2881)
            GL11.glEnable(2832)
            GL11.glEnable(3553)
            GL11.glPopMatrix()
            GlStateManager.color(255f, 255f, 255f)
            il += 4.9E-324
        }
    }

    val blockSlot: Int
        get() {
            for (i in 0..8) {
                if (!mc.player.inventoryContainer.getSlot(i + 36).hasStack
                    || mc.player.inventoryContainer.getSlot(i + 36).stack
                        .getItem() !is ItemBlock
                ) continue
                return i
            }
            return -1
        }

    fun getBlockData(pos: BlockPos): BlockData? {
        if (isPosSolid(pos.add(0, -1, 0))) {
            return BlockData(pos.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos.add(-1, 0, 0))) {
            return BlockData(pos.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos.add(1, 0, 0))) {
            return BlockData(pos.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos.add(0, 0, 1))) {
            return BlockData(pos.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos.add(0, 0, -1))) {
            return BlockData(pos.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos1 = pos.add(-1, 0, 0)
        if (isPosSolid(pos1.add(0, -1, 0))) {
            return BlockData(pos1.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos1.add(-1, 0, 0))) {
            return BlockData(pos1.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos1.add(1, 0, 0))) {
            return BlockData(pos1.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos1.add(0, 0, 1))) {
            return BlockData(pos1.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos1.add(0, 0, -1))) {
            return BlockData(pos1.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos2 = pos.add(1, 0, 0)
        if (isPosSolid(pos2.add(0, -1, 0))) {
            return BlockData(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos2.add(-1, 0, 0))) {
            return BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos2.add(1, 0, 0))) {
            return BlockData(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos2.add(0, 0, 1))) {
            return BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos2.add(0, 0, -1))) {
            return BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos3 = pos.add(0, 0, 1)
        if (isPosSolid(pos3.add(0, -1, 0))) {
            return BlockData(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos3.add(-1, 0, 0))) {
            return BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos3.add(1, 0, 0))) {
            return BlockData(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos3.add(0, 0, 1))) {
            return BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos3.add(0, 0, -1))) {
            return BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos4 = pos.add(0, 0, -1)
        if (isPosSolid(pos4.add(0, -1, 0))) {
            return BlockData(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos4.add(-1, 0, 0))) {
            return BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos4.add(1, 0, 0))) {
            return BlockData(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos4.add(0, 0, 1))) {
            return BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos4.add(0, 0, -1))) {
            return BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        if (isPosSolid(pos1.add(0, -1, 0))) {
            return BlockData(pos1.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos1.add(-1, 0, 0))) {
            return BlockData(pos1.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos1.add(1, 0, 0))) {
            return BlockData(pos1.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos1.add(0, 0, 1))) {
            return BlockData(pos1.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos1.add(0, 0, -1))) {
            return BlockData(pos1.add(0, 0, -1), EnumFacing.SOUTH)
        }
        if (isPosSolid(pos2.add(0, -1, 0))) {
            return BlockData(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos2.add(-1, 0, 0))) {
            return BlockData(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos2.add(1, 0, 0))) {
            return BlockData(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos2.add(0, 0, 1))) {
            return BlockData(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos2.add(0, 0, -1))) {
            return BlockData(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        if (isPosSolid(pos3.add(0, -1, 0))) {
            return BlockData(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos3.add(-1, 0, 0))) {
            return BlockData(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos3.add(1, 0, 0))) {
            return BlockData(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos3.add(0, 0, 1))) {
            return BlockData(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos3.add(0, 0, -1))) {
            return BlockData(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        if (isPosSolid(pos4.add(0, -1, 0))) {
            return BlockData(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos4.add(-1, 0, 0))) {
            return BlockData(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos4.add(1, 0, 0))) {
            return BlockData(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos4.add(0, 0, 1))) {
            return BlockData(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos4.add(0, 0, -1))) {
            return BlockData(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos5 = pos.add(0, -1, 0)
        if (isPosSolid(pos5.add(0, -1, 0))) {
            return BlockData(pos5.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos5.add(-1, 0, 0))) {
            return BlockData(pos5.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos5.add(1, 0, 0))) {
            return BlockData(pos5.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos5.add(0, 0, 1))) {
            return BlockData(pos5.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos5.add(0, 0, -1))) {
            return BlockData(pos5.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos6 = pos5.add(1, 0, 0)
        if (isPosSolid(pos6.add(0, -1, 0))) {
            return BlockData(pos6.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos6.add(-1, 0, 0))) {
            return BlockData(pos6.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos6.add(1, 0, 0))) {
            return BlockData(pos6.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos6.add(0, 0, 1))) {
            return BlockData(pos6.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos6.add(0, 0, -1))) {
            return BlockData(pos6.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos7 = pos5.add(-1, 0, 0)
        if (isPosSolid(pos7.add(0, -1, 0))) {
            return BlockData(pos7.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos7.add(-1, 0, 0))) {
            return BlockData(pos7.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos7.add(1, 0, 0))) {
            return BlockData(pos7.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos7.add(0, 0, 1))) {
            return BlockData(pos7.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos7.add(0, 0, -1))) {
            return BlockData(pos7.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos8 = pos5.add(0, 0, 1)
        if (isPosSolid(pos8.add(0, -1, 0))) {
            return BlockData(pos8.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos8.add(-1, 0, 0))) {
            return BlockData(pos8.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos8.add(1, 0, 0))) {
            return BlockData(pos8.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos8.add(0, 0, 1))) {
            return BlockData(pos8.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (isPosSolid(pos8.add(0, 0, -1))) {
            return BlockData(pos8.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos9 = pos5.add(0, 0, -1)
        if (isPosSolid(pos9.add(0, -1, 0))) {
            return BlockData(pos9.add(0, -1, 0), EnumFacing.UP)
        }
        if (isPosSolid(pos9.add(-1, 0, 0))) {
            return BlockData(pos9.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (isPosSolid(pos9.add(1, 0, 0))) {
            return BlockData(pos9.add(1, 0, 0), EnumFacing.WEST)
        }
        if (isPosSolid(pos9.add(0, 0, 1))) {
            return BlockData(pos9.add(0, 0, 1), EnumFacing.NORTH)
        }
        return if (isPosSolid(pos9.add(0, 0, -1))) {
            BlockData(pos9.add(0, 0, -1), EnumFacing.SOUTH)
        } else null
    }

    @Deprecated("")
    fun isPosSolid(pos: BlockPos?): Boolean {
        val block = mc.world.getBlockState(pos).block
        return ((block.getMaterial(mc.world.getBlockState(pos)).isSolid || !block.isTranslucent(
            mc.world.getBlockState(
                pos
            )
        ) || block.isOpaqueCube(
            mc.world.getBlockState(pos)
        )
                || block is BlockLadder || block is BlockCarpet || block is BlockSnow
                || block is BlockSkull) && !block.getMaterial(mc.world.getBlockState(pos)).isLiquid
                && block !is BlockContainer)
    }

    val blockCount: Int
        get() {
            var n = 0
            var i = 36
            while (i < 45) {
                if (mc.player.inventoryContainer.getSlot(i).hasStack) {
                    val stack = mc.player.inventoryContainer.getSlot(i).stack
                    val item = stack.getItem()
                    if (stack.getItem() is ItemBlock && isValid(item)) {
                        n += stack.stackSize
                    }
                }
                ++i
            }
            return n
        }

    fun getallBlockCount(): Int {
        var n = 0
        var i = 0
        while (i < 36) {
            if (mc.player.inventoryContainer.getSlot(i).hasStack) {
                val stack = mc.player.inventoryContainer.getSlot(i).stack
                val item = stack.getItem()
                if (stack.getItem() is ItemBlock && isValid(item)) {
                    n += stack.stackSize
                }
            }
            ++i
        }
        return n
    }

    fun isValid(item: Item?): Boolean {
        return item is ItemBlock && !invalidBlocks.contains(item.block)
    }

    fun swap(slot1: Int, hotbarSlot: Int) {
        mc.playerController.windowClick(
            mc.player.inventoryContainer.windowId,
            slot1,
            hotbarSlot,
            ClickType.QUICK_MOVE,
            mc.player
        )
    }

    fun getBlock(hotbarSlot: Int) {
        for (i in 9..44) {
            if (mc.player.inventoryContainer.getSlot(i).hasStack && (mc.currentScreen == null || mc.currentScreen is GuiInventory)) {
                val `is` = mc.player.inventoryContainer.getSlot(i).stack
                if (`is`.getItem() is ItemBlock) {
                    val block = `is`.getItem() as ItemBlock
                    if (isValid(block)) {
                        if (36 + hotbarSlot != i) {
                            swap(i, hotbarSlot)
                        }
                        break
                    }
                }
            }
        }
    }

    val bestSpoofSlot: Int
        get() {
            var spoofSlot = 5
            for (i in 36..44) {
                if (!mc.player.inventoryContainer.getSlot(i).hasStack) {
                    spoofSlot = i - 36
                    break
                }
            }
            return spoofSlot
        }

    enum class RenderMode {
        Off, Hanabi, Block
    }

    enum class RotateMode {
        Custom, Legit
    }

    enum class TowerMode {
        Jump, VerusTest, Constant, Packet, Hypixel, OFF
    }

    class BlockData(var blockPos: BlockPos?, var enumFacing: EnumFacing) {
        var vec: Vec3d? = null

    }

    companion object {
        var invalidBlocks = Arrays.asList(
            Blocks.ENCHANTING_TABLE, Blocks.FURNACE,
            Blocks.CRAFTING_TABLE, Blocks.AIR, Blocks.WEB, Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.SNOW_LAYER
        )
        var INSTANCE = Scaffold()
        var LIGHT0_POS = Vec3d(0.20000000298023224, 1.0, -0.699999988079071).normalize()
        var LIGHT1_POS = Vec3d(-0.20000000298023224, 1.0, 0.699999988079071).normalize()
        var colorBuffer = GLAllocation.createDirectFloatBuffer(16)
        fun rainbow(delay: Int): Color {
            var rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0)
            rainbowState %= 360.0
            return Color.getHSBColor((rainbowState / 360.0f).toFloat(), 0.8f, 0.7f)
        }

        fun getVec3d(pos: BlockPos?, face: EnumFacing): Vec3d {
            var x = pos!!.getX().toDouble() + 0.5
            var y = pos.getY().toDouble() + 0.5
            var z = pos.getZ().toDouble() + 0.5
            if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
                x += randomNumber(0.3, -0.3)
                z += randomNumber(0.3, -0.3)
            } else {
                y += randomNumber(0.3, -0.3)
            }
            if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
                z += randomNumber(0.3, -0.3)
            }
            if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
                x += randomNumber(0.3, -0.3)
            }
            return Vec3d(x, y, z)
        }

        fun randomNumber(max: Double, min: Double): Double {
            return Math.random() * (max - min) + min
        }

        fun setColorBuffer(
            p_setColorBuffer_0_: Double, p_setColorBuffer_2_: Double,
            p_setColorBuffer_4_: Double, p_setColorBuffer_6_: Double
        ): FloatBuffer {
            return setColorBuffer(
                p_setColorBuffer_0_.toFloat(),
                p_setColorBuffer_2_.toFloat(),
                p_setColorBuffer_4_.toFloat(),
                p_setColorBuffer_6_.toFloat()
            )
        }

        fun setColorBuffer(
            p_setColorBuffer_0_: Float,
            p_setColorBuffer_1_: Float,
            p_setColorBuffer_2_: Float,
            p_setColorBuffer_3_: Float
        ): FloatBuffer {
            colorBuffer.clear()
            colorBuffer.put(p_setColorBuffer_0_).put(p_setColorBuffer_1_).put(p_setColorBuffer_2_)
                .put(p_setColorBuffer_3_)
            colorBuffer.flip()
            return colorBuffer
        }
    }
}