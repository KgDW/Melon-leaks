package dev.zenhao.melon.module.modules.misc.nocom

import com.mojang.realmsclient.gui.ChatFormatting
import dev.zenhao.melon.mixin.client.accessor.AccessorChunkProviderClient
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.setting.BooleanSetting
import dev.zenhao.melon.setting.IntegerSetting
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.threads.runAsyncThread
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import melon.events.PacketEvents
import melon.events.RunGameLoopEvent
import melon.system.event.SafeClientEvent
import melon.system.event.safeConcurrentListener
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.runSafe
import net.minecraft.client.multiplayer.ChunkProviderClient
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "NoCom", category = Category.MISC)
object NoCom : Module() {
    private var delay = isetting("Delay", 200, 0, 1000)
    private var loop = isetting("LoopPerTick", 1, 1, 100)
    var scale: IntegerSetting = isetting("PointerScale", 4, 1, 4)
    private var packetType = isetting("PacketType", 1, 1, 3)
    private var resendPlayerPos = bsetting("ResendPlayerPos", false)
    private var logBlock = bsetting("LogBlock", false)
    private var bypassXin = bsetting("BypassXin", false)
    var renderNon: BooleanSetting = bsetting("RenderNon", false)
    private var renderAmount = isetting("RenderAmount", 64, 1, 128).b(renderNon)
    private var loadgui = bsetting("LoadGui", true)
    var couti = 1
    private var renderDistanceDiameter = 0
    private val ChunkProviderClient.loadedChunks: Long2ObjectMap<Chunk>
        get() = (this as AccessorChunkProviderClient).loadedChunks

    fun incCouti() {
        ++couti
    }

    init {
        safeEventListener<PacketEvents.Receive> { event ->
            if (event.packet is SPacketBlockChange) {
                val x = event.packet.blockPosition.getX()
                val z = event.packet.blockPosition.getZ()
                val state = event.packet.blockState
                for (chunk in world.chunkProvider.loadedChunks.values) {
                    if (chunk.x == x / 16 || chunk.z == z / 16) {
                        return@safeEventListener
                    }
                }
                val text =
                    "Player spotted at X: " + ChatFormatting.GREEN + x + ChatFormatting.RESET + " Z: " + ChatFormatting.GREEN + z
                runAsyncThread {
                    dots.add(Dot(x / 16, z / 16, DotType.Spotted))
                }
                ChatUtil.sendMessage(text)
                if (logBlock.value) {
                    ChatUtil.sendMessage(state.block.localizedName)
                }
                GuiScanner.consoleout.add(Cout(couti, text))
                ++couti
                if (GuiScanner.track) {
                    GuiScanner.consoleout.add(Cout(couti, "tracking x $x z $z"))
                    rerun(x, z)
                }
            }
        }

        safeConcurrentListener<RunGameLoopEvent.Tick> {
            if (GuiScanner.neartrack && scannedChunks > 25) {
                scannedChunks = 0
            }
            if (GuiScanner.neartrack && scannedChunks == 0) {
                doNoCom(player.posX.toInt(), player.posZ.toInt())
            }
            if (GuiScanner.neartrack) {
                return@safeConcurrentListener
            }
            if (loadgui.value) {
                mc.displayGuiScreen(GuiScanner)
                loadgui.value = false
            }
            if (!GuiScanner.busy) {
                doNoCom(player.posX.toInt(), player.posZ.toInt())
            } else {
                if (masynax != 0 && masynay != 0) {
                    doNoCom(masynax, masynay)
                }
            }
        }
    }

    private fun SafeClientEvent.doNoCom(x3: Int, y3: Int) {
        runAsyncThread {
            playerPos = BlockPos(player.posX, player.posY - 1, player.posZ)
            if (renderDistanceDiameter == 0) {
                renderDistanceDiameter = 8
            }
            if (time == 0L) {
                time = System.currentTimeMillis()
            }
            if (System.currentTimeMillis() - time > delay.value) {
                for (i in 0 until loop.value) {
                    val x1: Int = getSpiralCoords(count)[0] * renderDistanceDiameter * 16 + x3
                    val z1: Int = getSpiralCoords(count)[1] * renderDistanceDiameter * 16 + y3
                    val position = BlockPos(x1, 0, z1)
                    if (playerPos != null && resendPlayerPos.value) {
                        connection.sendPacket(
                            CPacketPlayerDigging(
                                CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                                playerPos!!,
                                EnumFacing.EAST
                            )
                        )
                    }
                    when (packetType.value) {
                        1 -> {
                            connection.sendPacket(
                                CPacketPlayerDigging(
                                    CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                                    position,
                                    EnumFacing.EAST
                                )
                            )
                        }

                        2 -> {
                            connection.sendPacket(
                                CPacketPlayerDigging(
                                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK,
                                    position,
                                    EnumFacing.EAST
                                )
                            )
                        }
                        3 -> {
                            connection.sendPacket(
                                CPacketPlayerDigging(
                                    CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                    position,
                                    EnumFacing.EAST
                                )
                            )
                        }
                    }
                    if (renderNon.value) {
                        runAsyncThread {
                            dots.add(Dot(x1 / 16, z1 / 16, DotType.Searched))
                        }
                        if (dots.size > renderAmount.value) {
                            if (dots[i].type == DotType.Spotted) continue
                            runAsyncThread {
                                dots.removeAt(i)
                            }
                        }
                    }
                    playerPos = BlockPos(player.posX, player.posY - 1, player.posZ)
                    time = System.currentTimeMillis()
                    count++
                    ++scannedChunks
                }
            }
        }
    }

    private fun getSpiralCoords(n: Int): IntArray {
        var n0 = n
        var x = 0
        var z = 0
        var d = 1
        var lineNumber = 1
        var coords = intArrayOf(0, 0)
        for (i in 0 until n0) {
            if (2 * x * d < lineNumber) {
                x += d
                coords = intArrayOf(x, z)
            } else if (2 * z * d < lineNumber) {
                z += d
                coords = intArrayOf(x, z)
            } else {
                d *= -1
                lineNumber++
                n0++
            }
        }
        return coords
    }

    override fun onEnable() {
        runSafe {
            if (bypassXin.value) {
                val buffer = PacketBuffer(Unpooled.buffer())
                buffer.writeItemStack(player.inventory.getItemStack())
                connection.sendPacket(CPacketCustomPayload("MC|SChunkLoad", buffer))
            }
        }
        playerPos = null
        count = 0
        time = 0
        mc.displayGuiScreen(GuiScanner)
    }

    override fun onDisable() {
        dots.clear()
        playerPos = null
        couti = 1
        count = 0
        time = 0
    }

    enum class DotType {
        Spotted, Searched
    }

    class Dot(var posX: Int, var posY: Int, var type: DotType) {
        var color: Color? = null
        var ticks = 0
    }

    class Cout(var posY: Int, var string: String)

    private var scannedChunks = 0

    var dots = CopyOnWriteArrayList<Dot>()
    private var playerPos: BlockPos? = null
    private var time: Long = 0
    private var count = 0
    private var masynax = 0
    private var masynay = 0

    fun rerun(x: Int, y: Int) {
        dots.clear()
        playerPos = null
        count = 0
        time = 0
        couti = 1
        masynax = x
        masynay = y
    }
}