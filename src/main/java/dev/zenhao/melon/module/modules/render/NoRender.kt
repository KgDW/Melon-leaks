package dev.zenhao.melon.module.modules.render

import dev.zenhao.melon.event.events.client.PacketEvent
import dev.zenhao.melon.event.events.render.*
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.utils.chat.ChatUtil
import net.minecraft.block.BlockSnow
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.*
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityEnchantmentTable
import net.minecraft.tileentity.TileEntityEnderChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Created by 086 on 4/02/2018.
 * Updated by S-B99 on 14/12/19
 */
@Module.Info(name = "NoRender", category = Category.RENDER, description = "Ignore entity spawn packets")
class NoRender : Module() {
    @JvmField
    var skylight: Setting<*> = msetting("SkyLight", Skylight.ALL)

    @JvmField
    var nohurtCam = bsetting("NoHurtCam", true)
    val BlockLayer: Setting<Boolean> = bsetting("BlockLayer", true)
    var AntiChatCrash = bsetting("AntiChatCrashing", true)
    val mob: Setting<Boolean> = bsetting("Mob", false)
    val sand: Setting<Boolean> = bsetting("Sand", false)
    val gentity: Setting<Boolean> = bsetting("GEntity", false)
    val `object`: Setting<Boolean> = bsetting("Object", false)
    val xp: Setting<Boolean> = bsetting("XP", false)
    val paint: Setting<Boolean> = bsetting("Paintings", false)
    val fire: Setting<Boolean> = bsetting("Fire", true)
    val explosion: Setting<Boolean> = bsetting("Explosions", true)
    var skylightupdate = bsetting("SkylightUpdate", true)
    var totemPops: Setting<Boolean> = bsetting("Totem", false)
    var table = bsetting("EnchantmentTable", false)
    var Chest = bsetting("Chest", false)
    var enderChest = bsetting("EnderChest", false)
    var banner = bsetting("Banner", false)
    private var chatLag = bsetting("ChatLag", true)

    @SubscribeEvent
    fun oao(event: RenderBlockOverlayEvent) {
        if (fire.value && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) event.isCanceled = true
    }

    @SubscribeEvent
    fun onReceive(event: ClientChatReceivedEvent) {
        if (AntiChatCrash.value && event.message.toString().contains("\${jndi:ldap")) {
            ChatUtil.sendMessage("[AntiCrash] Canceled The Crash Chat Event!")
            event.isCanceled = true
        }
        if (event.message.unformattedText.contains("嘁圁堁") && chatLag.value) {
            event.isCanceled = true
            ChatUtil.sendMessage("ChatLag Patched!")
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderOverlayEvent) {
        event.isCanceled = true
    }

    @SubscribeEvent
    fun RenderLight(event: RenderLightEvent) {
        if (skylightupdate.value) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun totemPop(event: RenderTotemPopEvent) {
        if (totemPops.value) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun banner(event: RenderBannerEvent) {
        if (banner.value) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun enderChest(event: RenderEnderChestEvent) {
        if (enderChest.value) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun enderChest(event: RenderChestEvent) {
        if (Chest.value) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun enchantmentTable(event: RenderEnchantmentTableEvent) {
        if (table.value) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun BlockLayer(event: RenderLiquidVisionEvent) {
        if (BlockLayer.value) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun awa(event: PacketEvent.Receive) {
        val packet = event.packet
        if (packet is SPacketSpawnMob && mob.value || packet is SPacketSpawnGlobalEntity && gentity.value || packet is SPacketSpawnObject && `object`.value || packet is SPacketSpawnExperienceOrb && xp.value || packet is SPacketSpawnObject && sand.value || packet is SPacketExplosion && explosion.value || packet is SPacketSpawnPainting && paint.value) event.isCanceled =
            true
    }

    fun tryReplaceEnchantingTable(tileEntity: TileEntity): Boolean {
        if (table.value && tileEntity is TileEntityEnchantmentTable) {
            val blockState = Blocks.SNOW_LAYER.defaultBlockState.withProperty(BlockSnow.LAYERS, 7)
            mc.world.setBlockState(tileEntity.getPos(), blockState)
            mc.world.markTileEntityForRemoval(tileEntity)
            return true
        }
        return false
    }

    fun tryReplaceEnderChest(tileEntity: TileEntity): Boolean {
        if (enderChest.value && tileEntity is TileEntityEnderChest) {
            val blockState = Blocks.SNOW_LAYER.defaultBlockState.withProperty(BlockSnow.LAYERS, 7)
            mc.world.setBlockState(tileEntity.getPos(), blockState)
            mc.world.markTileEntityForRemoval(tileEntity)
            return true
        }
        return false
    }

    fun tryReplaceChest(tileEntity: TileEntity): Boolean {
        if (enderChest.value && tileEntity is TileEntityChest) {
            val blockState = Blocks.SNOW_LAYER.defaultBlockState.withProperty(BlockSnow.LAYERS, 7)
            mc.world.setBlockState(tileEntity.getPos(), blockState)
            mc.world.markTileEntityForRemoval(tileEntity)
            return true
        }
        return false
    }

    fun setInstance() {
        INSTANCE = this
    }

    enum class Skylight {
        NONE, WORLD, ENTITY, ALL
    }

    companion object {
        @JvmField
        var INSTANCE = NoRender()
    }
}