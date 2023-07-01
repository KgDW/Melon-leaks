package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.modules.crystal.CrystalHelper
import dev.zenhao.melon.setting.BooleanSetting
import dev.zenhao.melon.setting.IntegerSetting
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.Wrapper
import dev.zenhao.melon.utils.animations.sq
import dev.zenhao.melon.utils.block.BlockInteractionHelper
import dev.zenhao.melon.utils.vector.VectorUtils.toBlockPos
import melon.events.RunGameLoopEvent
import melon.system.event.safeEventListener
import melon.utils.block.BlockUtil.getNeighbor
import melon.utils.inventory.slot.firstBlock
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.player.getTarget
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumHand

@Module.Info(name = "AutoWeb", category = Category.COMBAT)
object AutoWeb : Module() {
    private var spoofRotations: Setting<Boolean> = bsetting("Rotate", false)
    private var inside: BooleanSetting = bsetting("Inside", false)
    private var strictDirection = bsetting("StrictDirection", false)
    private var range: IntegerSetting = isetting("Range", 4, 1, 6)
    private var predictTicks: IntegerSetting = isetting("PredictTicks", 8, 0, 20)
    private var delay: IntegerSetting = isetting("Delay", 25, 0, 500)
    private var timerDelay = TimerUtils()
    private var target: EntityPlayer? = null

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        timerDelay.reset()
    }

    override fun getHudInfo(): String? {
        return if (target != null) {
            target!!.name
        } else null
    }

    init {
        safeEventListener<RunGameLoopEvent.Tick> {
            target = getTarget(range.value)
            val webSlot = player.hotbarSlots.firstBlock(Blocks.WEB)
            if (target != null) {
                val targetDistance = if (predictTicks.value > 0) target!!.positionVector.add(
                    CrystalHelper.getPredictedTarget(
                        target!!,
                        predictTicks.value
                    )
                ) else target!!.positionVector
                if (!Wrapper.getWorld()
                        .getBlockState(targetDistance.toBlockPos()).material.isReplaceable || webSlot == null
                ) {
                    return@safeEventListener
                }
                var needSneak = false
                val blockBelow = world.getBlockState(targetDistance.toBlockPos()).block
                if (BlockInteractionHelper.blackList.contains(blockBelow) || BlockInteractionHelper.shulkerList.contains(
                        blockBelow
                    )
                ) {
                    needSneak = true
                }
                if (needSneak) {
                    player.connection.sendPacket(
                        CPacketEntityAction(
                            player,
                            CPacketEntityAction.Action.START_SNEAKING
                        )
                    )
                }
                if (player.getDistanceSq(targetDistance.toBlockPos()) > range.value.sq) {
                    return@safeEventListener
                }
                val placePos =
                    getNeighbor(targetDistance.toBlockPos(), strictDirection.value) ?: return@safeEventListener
                val placePosDown =
                    getNeighbor(targetDistance.toBlockPos().down(), strictDirection.value) ?: return@safeEventListener
                if (timerDelay.tickAndReset(delay.value)) {
                    spoofHotbar(webSlot) {
                        if (inside.value) {
                            if (spoofRotations.value) {
                                RotationManager.addRotations(targetDistance.toBlockPos())
                            }
                            player.connection.sendPacket(
                                CPacketPlayerTryUseItemOnBlock(
                                    placePos.blockPos,
                                    placePos.face,
                                    EnumHand.MAIN_HAND,
                                    0.5f,
                                    1f,
                                    0.5f
                                )
                            )
                            //BlockUtil.placeBlock(targetDistance.toBlockPos(), EnumHand.MAIN_HAND, spoofRotations.value, packet.value)
                        }
                        if (spoofRotations.value) {
                            RotationManager.addRotations(targetDistance.toBlockPos().down())
                        }
                        player.connection.sendPacket(
                            CPacketPlayerTryUseItemOnBlock(
                                placePosDown.blockPos,
                                placePosDown.face,
                                EnumHand.MAIN_HAND,
                                0.5f,
                                1f,
                                0.5f
                            )
                        )
                        //BlockUtil.placeBlock(targetDistance.toBlockPos().down(), EnumHand.MAIN_HAND, spoofRotations.value, packet.value)
                    }
                }
                //onSpoof(playerHotbarSlot)
                if (needSneak) {
                    player.connection.sendPacket(
                        CPacketEntityAction(
                            player,
                            CPacketEntityAction.Action.STOP_SNEAKING
                        )
                    )
                }
            }
        }
    }
}