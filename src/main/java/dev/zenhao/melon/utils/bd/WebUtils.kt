package dev.zenhao.melon.utils.bd

import dev.zenhao.melon.Melon
import melon.system.util.io.ConnectionUtils
import melon.system.util.io.readText

object WebUtils {
    fun requestRawJsonFrom(url: String, catch: (Exception) -> Unit = { it.printStackTrace() }): String? {
        return ConnectionUtils.runConnection(url, { connection ->
            connection.requestMethod = "GET"
            connection.inputStream.use { it.readText() }
        }, catch)
    }

    fun request(url: String): String? {
        return requestRawJsonFrom(url) {
            Melon.logger.error("Failed requesting from Discord!", it)
        }
    }
}