package dev.zenhao.melon.module.modules.combat

import dev.zenhao.melon.manager.HotbarManager.spoofHotbar
import dev.zenhao.melon.manager.RotationManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.math.RotationUtils.getRotationTo
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import melon.utils.block.notBlock
import melon.utils.concurrent.threads.runSafe
import melon.utils.extension.fastPos
import melon.utils.extension.position
import melon.utils.extension.positionRotation
import melon.utils.inventory.slot.firstBlock
import melon.utils.inventory.slot.hotbarSlots
import melon.utils.math.vector.toVec3d
import melon.utils.math.vector.toVec3dCenter
import melon.utils.player.breakCrystal
import melon.utils.world.noCollision
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.util.math.BlockPos

@Module.Info(name = "Burrow", category = Category.COMBAT, description = "Selffill urself in ur mom's pussy")
object Burrow : Module() {
    private val rotate: Setting<Boolean> = bsetting("Rotate", true)
    private var invalid = bsetting("Invalid", true)
    private var originalPos: BlockPos? = null
    private var velocityTime = 0L

    override fun onEnable() {
        runSafe {
            velocityTime = 0L
            originalPos = BlockPos(player)
            if (!world.notBlock(
                    player,
                    Blocks.OBSIDIAN
                ) || (originalPos != null && !world.noCollision(originalPos!!))
            ) {
                disable()
                return
            }
        }
    }

    override fun onDisable() {
        runSafe {
            player.isSneaking = false
            originalPos = null
        }
    }

    init {
        safeEventListener<PacketEvents.Receive>(true) {
            when (it.packet) {
                is SPacketExplosion -> {
                    if (it.packet.y > 2.0 && velocityTime <= System.currentTimeMillis()) {
                        velocityTime = System.currentTimeMillis() + 3000L
                    }
                }
            }
        }

        onLoop {
            val slot =
                player.hotbarSlots.firstBlock(Blocks.OBSIDIAN) ?: player.hotbarSlots.firstBlock(Blocks.ENDER_CHEST)
            if (slot == null) {
                disable()
                return@onLoop
            }
            if (originalPos == null) return@onLoop
            originalPos?.let {
                if (world.notBlock(player, Blocks.AIR) || !world.isAirBlock(it.up())) {
                    ChatUtil.sendMessage("Prevented Burrow While In Block!")
                    disable()
                    return@onLoop
                }
                breakCrystal(null, rotate.value)
                if (player.onGround) {
                    connection.sendPacket(
                        if (rotate.value) positionRotation(0.41999998688698) else position(
                            0.41999998688698
                        )
                    )
                    connection.sendPacket(if (rotate.value) positionRotation(0.7500019) else position(0.7500019))
                    connection.sendPacket(if (rotate.value) positionRotation(0.9999962) else position(0.9999962))
                    connection.sendPacket(
                        if (rotate.value) positionRotation(1.17000380178814) else position(
                            1.17000380178814
                        )
                    )
                } else {
                    RotationManager.addRotations(getRotationTo(it.toVec3d()))
                }
                spoofHotbar(slot) {
                    connection.sendPacket(fastPos(it))
                    if (player.onGround) {
                        if (invalid.value) {
                            var boost = (player.posY - 3) * -1
                            if (player.posY >= 65) {
                                boost -= boost - player.posY
                            }
                            connection.sendPacket(position(-8 + boost))
                        } else {
                            connection.sendPacket(position(1.2426308013947485))
                            if (velocityTime > System.currentTimeMillis()) {
                                connection.sendPacket(position(10.340088003576279))
                                connection.sendPacket(position(-7.0))
                            } else {
                                connection.sendPacket(position(5.3400880035762786))
                                connection.sendPacket(position(-3.0))
                            }
                        }
                    }
                }
                disable()
            }
        }
    }
}