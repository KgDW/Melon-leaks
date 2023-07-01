package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.manager.EntityManager
import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.combat.HoleSnap
import dev.zenhao.melon.module.modules.crystal.MelonAura2.getLegitRotations
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.inventory.HotbarSlot
import dev.zenhao.melon.utils.ticklength.setTimer
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import melon.events.EntityAddToWorldEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.onMainThread
import melon.utils.concurrent.threads.runSafe
import melon.utils.entity.EntityUtils.spoofSneak
import melon.utils.extension.fastPosDirection
import melon.utils.extension.synchronized
import melon.utils.inventory.slot.firstBlock
import melon.utils.inventory.slot.firstItem
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.world.noCollision
import net.minecraft.entity.passive.EntityChicken
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemShulkerBox
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.server.SPacketSpawnMob
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

@Module.Info(name = "AutoStash", category = Category.MISC)
object AutoStash : Module() {
    private var autoJump = bsetting("AutoJump", true)
    private var autoScale = bsetting("AutoScale", false)

    private var buildingState = BuildingState.Waiting
    private var chickenList = Object2ObjectArrayMap<EntityChicken, Int>().synchronized()
    private var offsetFacing = arrayOf(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST)
    private var obiStack = 0
    private var chestSlot: HotbarSlot? = null
    private var hopperSlot: HotbarSlot? = null
    private var obiSlot: HotbarSlot? = null
    private var eggSlot: HotbarSlot? = null
    private var seedSlot: HotbarSlot? = null
    private var shulkerSlot = 0
    private var shulkerName = ""
    private var pointer = BlockPos.ORIGIN

    override fun onEnable() {
        runSafe {
            mc.timer.setTimer(1f)
            pointer = if (autoScale.value) mc.objectMouseOver.blockPos else mc.objectMouseOver.blockPos.up()
            buildingState = BuildingState.Waiting
            chickenList.clear()
            shulkerName = ""
            if (player.heldItemMainhand.item !is ItemShulkerBox) {
                ChatUtil.sendMessage("Please Confirm You Are Holding A ShulkerBox!")
                disable()
                return
            } else {
                shulkerSlot = playerController.currentPlayerItem
                shulkerName = player.heldItemMainhand.displayName
            }
            obiStack = 0
            chestSlot =
                player.hotbarSlots.firstBlock(Blocks.CHEST) ?: player.hotbarSlots.firstBlock(Blocks.TRAPPED_CHEST)
            hopperSlot = player.hotbarSlots.firstBlock(Blocks.HOPPER)
            obiSlot = player.hotbarSlots.firstBlock(Blocks.OBSIDIAN)
            eggSlot = player.hotbarSlots.firstItem(Items.EGG)
            seedSlot = player.hotbarSlots.firstItem(Items.WHEAT_SEEDS)
            if (chestSlot == null || hopperSlot == null || obiSlot == null || eggSlot == null || seedSlot == null) {
                ChatUtil.sendMessage("Material Was Not Enough! Disabled!")
                disable()
                return@runSafe
            }

            eggSlot?.let {
                if (it.stack.stackSize < 16) {
                    ChatUtil.sendMessage("Egg Size < 16, Disabled!")
                    disable()
                    return@runSafe
                }
            }

            seedSlot?.let {
                if (it.stack.stackSize < 64) {
                    ChatUtil.sendMessage("Seeds Size < 64, Disabled!")
                    disable()
                    return@runSafe
                }
            }

            obiSlot?.let {
                if (it.stack.stackSize < 12) {
                    ChatUtil.sendMessage("Obsidian Size < 12, Disabled!")
                    disable()
                    return@runSafe
                }
            }

            buildingState = BuildingState.Chest
        }
    }

    init {
        onPacketSend { event ->
            when (event.packet) {
                is CPacketPlayerTryUseItem -> {
                    if (player.heldItemMainhand.item == Items.WHEAT_SEEDS) {
                        for (chicken in EntityManager.entity) {
                            if (chicken !is EntityChicken) continue
                            if (chickenList.containsKey(chicken)) {
                                chickenList[chicken] = chickenList[chicken]!!.minus(1).coerceIn(0, 63)
                            }
                        }
                    }
                }
            }
        }

        safeEventListener<EntityAddToWorldEvent> {
            if (it.entity is EntityChicken) {
                ChatUtil.sendMessage("Chicken Was Found!")
                chickenList[it.entity] = 63
            }
        }

        onLoop {
            when (buildingState) {
                BuildingState.Chest -> {
                    chestSlot?.let {
                        placeBlock(it, pointer)
                        buildingState = BuildingState.Hopper
                        ChatUtil.sendMessage("Updated To ${buildingState.name}")
                        return@onLoop
                    }
                    return@onLoop
                }

                BuildingState.Hopper -> {
                    hopperSlot?.let {
                        placeBlock(it, pointer.up())
                        buildingState = BuildingState.Obsidian
                        ChatUtil.sendMessage("Updated To ${buildingState.name}")
                        return@onLoop
                    }
                    return@onLoop
                }

                BuildingState.Obsidian -> {
                    obiSlot?.let {
                        for (facing in offsetFacing) {
                            val placePos = pointer.up().offset(facing)
                            if (!world.noCollision(placePos) || !world.isAirBlock(placePos)) continue
                            placeBlock(it, placePos, facing)
                            obiStack++
                            if (!world.noCollision(placePos.up()) || !world.isAirBlock(placePos.up())) continue
                            placeBlock(it, placePos.up())
                            obiStack++
                            break
                        }
                        if (obiStack >= 18) {
                            buildingState = BuildingState.Egg
                            ChatUtil.sendMessage("Updated To ${buildingState.name}")
                            return@onLoop
                        }
                        return@onLoop
                    }
                    return@onLoop
                }

                BuildingState.Egg -> {
                    if (player.getDistanceSq(pointer.up()) > 1) {
                        if (HoleSnap.isDisabled) {
                            HoleSnap.enable()
                        }
                        return@onLoop
                    }
                    eggSlot?.let {
                        spoofHotbar(it) {
                            RotationManager.addRotations(getLegitRotations(pointer.up()))
                            connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        }
                    }
                    if (chickenList.isNotEmpty()) {
                        buildingState = BuildingState.Seeds
                        ChatUtil.sendMessage("Updated To ${buildingState.name}")
                        return@onLoop
                    }
                }

                BuildingState.Seeds -> {
                    seedSlot?.let { seedSlot ->
                        chickenList.forEach { (caixukun: EntityChicken, feededAmount: Int) ->
                            if (feededAmount > 0) {
                                if (player.heldItemMainhand.item != Items.WHEAT_SEEDS) {
                                    player.inventory.currentItem = seedSlot.hotbarSlot
                                    playerController.updateController()
                                }
                                RotationManager.addRotations(getLegitRotations(BlockPos(caixukun)))
                                mc.timer.setTimer(10f)
                                connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                            } else {
                                mc.timer.setTimer(1f)
                                buildingState = BuildingState.Shulker
                                ChatUtil.sendMessage("Updated To ${buildingState.name}")
                                return@onLoop
                            }
                        }
                    }
                }

                BuildingState.Shulker -> {
                    chickenList.forEach { (caixukun: EntityChicken, _: Int) ->
                        RotationManager.addRotations(getLegitRotations(BlockPos(caixukun)))
                        connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))
                        if (caixukun.customNameTag == shulkerName) {
                            chickenList.remove(caixukun)
                        }
                        buildingState = BuildingState.Finished
                        ChatUtil.sendMessage("Updated To ${buildingState.name}")
                        return@onLoop
                    }
                }

                BuildingState.Finished -> {
                    ChatUtil.sendMessage("Have A Great Time!")
                    disable()
                    return@onLoop
                }

                else -> {
                    ChatUtil.sendMessage("???")
                }
            }
        }
    }

    override fun getHudInfo(): String {
        return buildingState.name
    }

    private fun SafeClientEvent.placeBlock(slot: HotbarSlot, pos: BlockPos, facing: EnumFacing = EnumFacing.UP) {
        if (!world.isAirBlock(pos)) {
            ChatUtil.sendMessage("??")
            return
        }
        onMainThread {
            if (player.posY < pos.getY() && autoJump.value) {
                player.spoofSneak {
                    obiSlot?.let {
                        RotationManager.addRotations(player.rotationYaw, 89.5f)
                        if (player.onGround) {
                            player.jump()
                        }
                        spoofHotbar(it) {
                            connection.sendPacket(fastPosDirection(BlockPos(player)))
                        }
                    }
                }
            }

            RotationManager.addRotations(getLegitRotations(pos))
            player.spoofSneak {
                spoofHotbar(slot) {
                    connection.sendPacket(fastPosDirection(pos, facing))
                }
            }

            connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }
    }

    enum class BuildingState {
        Chest, Hopper, Obsidian, Egg, Seeds, Shulker, Finished, Waiting
    }
}