package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.block.BreakingUtil.Companion.hotbarSlots
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.inventory.HotbarSlot
import melon.events.RunGameLoopEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.onMainThread
import melon.utils.entity.EntityUtils.spoofSneak
import melon.utils.extension.fastPosDirectionDown
import melon.utils.inventory.slot.firstBlock
import melon.utils.math.vector.toVec3d
import melon.utils.world.noCollision
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockLiquid
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Blocks
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

/**
 * Created by hub on 10/12/2019
 * Updated by zenhao on 2/2/2023
 */
@Module.Info(
    name = "DispenserMeta",
    category = Category.COMBAT,
    description = "Do not use with any AntiGhostBlock Mod!"
)
object DispenserMeta : Module() {
    private val rotate = bsetting("Rotate", false)
    private val placeStage = isetting("PlaceStage", 0, 0, 1)
    private val faceStage = isetting("FaceStage", 0, 0, 1)
    private val offsetFacing =
        arrayOf(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP)
    private var stage = 0
    private var placeTarget: BlockPos? = null
    private var shulkerSlot = -1
    override fun onDisable() {
        if (fullNullCheck()) {
            return
        }
        mc.player.isSneaking = false
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            disable()
            return
        }
        stage = 0
        placeTarget = null
        shulkerSlot = -1
        for (i in 0..8) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.getItem() !is ItemBlock) continue
            val block = (stack.getItem() as ItemBlock).block
            if (BlockInteractionHelper.shulkerList.contains(block)) {
                shulkerSlot = i
                break
            }
        }
        if (shulkerSlot < 0) {
            ChatUtil.sendMessage("No Shulker Found!")
            disable()
        }
        if (mc.objectMouseOver == null) {
            ChatUtil.NoSpam.sendMessage("[Dispenser32k] Not a valid place target, disabling.")
            disable()
            return
        }
        placeTarget = mc.objectMouseOver.blockPos.up()
        stage = 0
    }

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            val obiSlot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN) ?: return@safeEventListener
            val dispenserSlot = player.hotbarSlots.firstBlock(Blocks.DISPENSER) ?: return@safeEventListener
            val redstoneSlot = player.hotbarSlots.firstBlock(Blocks.REDSTONE_BLOCK) ?: return@safeEventListener
            val hopperSlot = player.hotbarSlots.firstBlock(Blocks.HOPPER) ?: return@safeEventListener
            if (placeTarget == null) {
                ChatUtil.sendMessage("PlacePos Not Found!")
                disable()
                return@safeEventListener
            }
            when (stage) {
                0 -> {
                    placeBlock(obiSlot, placeTarget!!)
                    placeBlock(dispenserSlot, placeTarget!!.add(0, 1, 0))
                    connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                    connection.sendPacket(
                        fastPosDirectionDown(
                            placeTarget!!.add(0, 1, 0),
                            EnumHand.MAIN_HAND,
                            0f,
                            0f,
                            0f
                        )
                    )
                    stage = 1
                    return@safeEventListener
                }

                1 -> {
                    if (mc.currentScreen !is GuiContainer) {
                        return@safeEventListener
                    }
                    playerController.windowClick(player.openContainer.windowId, 1, shulkerSlot, ClickType.SWAP, player)
                    player.closeScreen()
                    stage = 2
                    return@safeEventListener
                }

                2 -> {
                    for (facing in offsetFacing) {
                        val placePos = placeTarget!!.up().offset(facing)
                        if (!world.noCollision(placePos)
                            || !world.isAirBlock(placePos)
                            || placePos == placeTarget!!.up().offset(player.horizontalFacing.getOpposite())
                            || placePos.getY() < placeTarget!!.getY()
                        ) continue
                        placeBlock(
                            redstoneSlot,
                            placeTarget!!.up(),
                            if (facing == EnumFacing.UP) EnumFacing.DOWN else facing
                        )
                        break
                    }
                    stage = 3
                    return@safeEventListener
                }

                3 -> {
                    val block =
                        world.getBlockState(placeTarget!!.offset(player.horizontalFacing.getOpposite()).up()).block
                    if (block is BlockAir || block is BlockLiquid) {
                        return@safeEventListener
                    }
                    placeBlock(
                        hopperSlot,
                        placeTarget!!,
                        player.horizontalFacing.getOpposite()
                    )
                    connection.sendPacket(
                        CPacketEntityAction(
                            player,
                            CPacketEntityAction.Action.STOP_SNEAKING
                        )
                    )
                    connection.sendPacket(
                        fastPosDirectionDown(
                            placeTarget!!.offset(player.horizontalFacing.getOpposite()),
                            EnumHand.MAIN_HAND,
                            0f,
                            0f,
                            0f
                        )
                    )
                    stage = 4
                    return@safeEventListener
                }

                4 -> {
                    if (mc.currentScreen !is GuiContainer) {
                        return@safeEventListener
                    }
                    if ((mc.currentScreen as GuiContainer?)!!.inventorySlots.getSlot(0).stack.isEmpty) {
                        return@safeEventListener
                    }
                    playerController.windowClick(
                        player.openContainer.windowId,
                        0,
                        shulkerSlot,
                        ClickType.SWAP,
                        player
                    )
                    disable()
                }
            }
        }
    }

    private fun SafeClientEvent.placeBlock(slot: HotbarSlot, pos: BlockPos, facing: EnumFacing = EnumFacing.DOWN) {
        val neighbour = when (placeStage.value) {
            0 -> pos.offset(facing)
            1 -> if (facing == EnumFacing.DOWN) pos.offset(facing) else pos
            else -> pos.offset(facing)
        }
        val opposite = when (faceStage.value) {
            0 -> facing.getOpposite()
            1 -> if (facing == EnumFacing.DOWN) facing.getOpposite() else facing
            else -> facing.getOpposite()
        }
        if (rotate.value) {
            RotationManager.addRotationsNew(neighbour)
        }
        onMainThread {
            player.spoofSneak {
                if (!world.noCollision(neighbour)) {
                    disable()
                    return@onMainThread
                }
                player.inventory.currentItem = slot.hotbarSlot
                playerController.processRightClickBlock(
                    player,
                    world,
                    neighbour,
                    opposite,
                    neighbour.toVec3d(),
                    EnumHand.MAIN_HAND
                )
            }
            connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }
    }
}