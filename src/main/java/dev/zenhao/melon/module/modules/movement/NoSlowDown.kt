package dev.zenhao.melon.module.modules.movement

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.TimerUtils
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.gui.GuiScreenOptionsSounds
import net.minecraft.client.gui.GuiVideoSettings
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.settings.KeyBinding
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import kotlin.math.floor

@Module.Info(
    name = "NoSlowDown",
    category = Category.MOVEMENT,
    description = "Prevents being slowed down when using an item or going through cobwebs"
)
object NoSlowDown : Module() {
    private var guiMove: Setting<Boolean> = bsetting("GuiMove", true)
    var soulSand: Setting<Boolean> = bsetting("SoulSand", true)
    private var strict: Setting<Boolean> = bsetting("Strict", true)
    private var superStrict: Setting<Boolean> = bsetting("SuperStrict", false)
    private var sneakPacket: Setting<Boolean> = bsetting("SneakPacket", false)
    private var packetBypass = bsetting("PacketBypass", false)
    private var packetDelay = isetting("PacketDelay", 25,0,500).b(packetBypass)
    private var packetTimer = TimerUtils()
    private var sneaking = false
    private var spoofing = false
    private var keys = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindLeft,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSprint
    )

    init {
        safeEventListener<PacketEvents.Receive> {
            if (it.packet is SPacketPlayerPosLook && packetBypass.value && player.isSneaking) {
                packetTimer.reset()
                connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                spoofing = false
            }
        }

        safeEventListener<PacketEvents.Send> {
            if (it.packet is CPacketEntityAction && packetBypass.value && player.isHandActive) {
                if (it.packet.action.equals(CPacketEntityAction.Action.START_SNEAKING)) {
                    if (packetTimer.tickAndReset(packetDelay.value) && !spoofing) {
                        connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                        spoofing = true
                    }
                } else if (it.packet.action.equals(CPacketEntityAction.Action.STOP_SNEAKING)) {
                    if (packetTimer.tickAndReset(packetDelay.value) && spoofing) {
                        connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))
                        spoofing = false
                    }
                }
            }
        }
    }

    override fun onUpdate() {
        if (fullNullCheck()) {
            return
        }
        if (guiMove.value) {
            if (mc.currentScreen is GuiOptions || mc.currentScreen is GuiVideoSettings || mc.currentScreen is GuiScreenOptionsSounds || mc.currentScreen is GuiContainer || mc.currentScreen is GuiIngameMenu) {
                for (bind in keys) {
                    KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()))
                }
            } else if (mc.currentScreen == null) {
                for (bind in keys) {
                    if (Keyboard.isKeyDown(bind.getKeyCode())) continue
                    KeyBinding.setKeyBindState(bind.getKeyCode(), false)
                }
            }
        }
        if (sneaking && !mc.player.isHandActive && sneakPacket.value) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
            sneaking = false
        }
    }

    @SubscribeEvent
    fun onWorldEvent(event: EntityJoinWorldEvent?) {
        if (sneakPacket.value && sneaking && !mc.player.isHandActive) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
            sneaking = false
        }
    }

    @SubscribeEvent
    fun onUseItem(event: RightClickItem) {
        val item = mc.player.getHeldItem(event.hand).getItem()
        if (sneakPacket.value && !sneaking) {
            if (item is ItemFood || item is ItemBow || item is ItemPotion) {
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player,
                        CPacketEntityAction.Action.START_SNEAKING
                    )
                )
                sneaking = true
            }
        }
    }

    @SubscribeEvent
    fun onInput(event: InputUpdateEvent) {
        if (fullNullCheck()) {
            return
        }
        if (mc.player.isHandActive && !mc.player.isRiding) {
            event.movementInput.moveStrafe *= 5.0f
            event.movementInput.moveForward *= 5.0f
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Send) {
        if (fullNullCheck()) {
            return
        }
        if (event.packet is CPacketPlayer && strict.value && mc.player.isHandActive && !mc.player.isRiding) {
            mc.player.connection.sendPacket(
                CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                    BlockPos(floor(mc.player.posX), floor(mc.player.posY), floor(mc.player.posZ)),
                    EnumFacing.DOWN
                )
            )
        }
        if (event.stage == 1) {
            if (event.packet is CPacketPlayerTryUseItem || event.packet is CPacketPlayerTryUseItemOnBlock) {
                val item = mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem()
                if (superStrict.value && (item is ItemFood || item is ItemBow || item is ItemPotion)) {
                    mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
                }
            }
        }
    }
}