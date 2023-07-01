package dev.zenhao.melon.module.modules.misc

import dev.zenhao.melon.manager.FileManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.chat.ChatUtil
import kotlinx.coroutines.launch
import melon.events.PacketEvents
import melon.system.event.safeEventListener
import melon.utils.concurrent.threads.defaultScope
import melon.utils.concurrent.threads.mainScope
import net.minecraft.network.Packet
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList

@Module.Info(name = "PacketAnalyzer", category = Category.MISC)
object PacketAnalyzer : Module() {
    private val print = bsetting("Print", false)
    private val record = bsetting("Record", false)
    private val cpacket = bsetting("CPacket", true)
    private val spacket = bsetting("SPacket", false)
    private val packetSendFile = File(FileManager.packetSendFile)
    private val packetReceiveFile = File(FileManager.packetReceiveFile)
    private val packetSendList = CopyOnWriteArrayList<Packet<*>>()
    private val packetReceiveList = CopyOnWriteArrayList<Packet<*>>()

    init {
        mainScope.launch {
            while (true) {
                if (packetSendList.isNotEmpty()) {
                    packetSendList.forEach {
                        writeFile(packetSendFile, it.toString())
                        packetSendList.remove(it)
                    }
                }
                if (packetReceiveList.isNotEmpty()) {
                    packetReceiveList.forEach {
                        writeFile(packetReceiveFile, it.toString())
                        packetReceiveList.remove(it)
                    }
                }
            }
        }

        safeEventListener<PacketEvents.Send> {
            if (cpacket.value) {
                defaultScope.launch {
                    packetSendList.add(it.packet)
                    if (print.value) {
                        ChatUtil.sendMessage(it.packet.toString())
                    }
                }
            }
        }

        safeEventListener<PacketEvents.Receive> {
            if (spacket.value) {
                defaultScope.launch {
                    packetReceiveList.add(it.packet)
                    if (print.value) {
                        ChatUtil.sendMessage(it.packet.toString())
                    }
                }
            }
        }
    }

    override fun onEnable() {
        if (fullNullCheck()) {
            return
        }
        try {
            if (!packetSendFile.exists()) {
                packetSendFile.parentFile.mkdirs()
                packetSendFile.createNewFile()
            }
            if (!packetReceiveFile.exists()) {
                packetReceiveFile.parentFile.mkdirs()
                packetReceiveFile.createNewFile()
            }
        } catch (ignored: Exception) {
        }
    }

    private fun writeFile(file: File?, msg: String?) {
        try {
            if (file != null && file.exists() && record.value) {
                val saveJSon = PrintWriter(OutputStreamWriter(FileOutputStream(file, true), StandardCharsets.UTF_8))
                saveJSon.println(msg)
                saveJSon.close()
            }
        } catch (ignored: Exception) {
        }
    }
}