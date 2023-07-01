package melon.system.config

import dev.zenhao.melon.Melon
import melon.client.managers.FriendsManager
import melon.client.managers.UUIDManager
import java.io.File
import java.io.FileWriter
import java.io.IOException

object ConfigUtils {

    fun loadAll(): Boolean {
        var success = ConfigManager.loadAll()
//        success = MacroManager.loadMacros() && success // Macro
//        success = WaypointManager.loadWaypoints() && success // Waypoint
        success = FriendsManager.load() && success // Friends
        success = UUIDManager.load() && success // UUID Cache

        return success
    }

    fun saveAll(): Boolean {
        var success = ConfigManager.saveAll()
//        success = MacroManager.saveMacros() && success // Macro
//        success = WaypointManager.saveWaypoints() && success // Waypoint
        success = FriendsManager.save() && success // Friends
        success = UUIDManager.save() && success // UUID Cache

        return success
    }

    fun isPathValid(path: String): Boolean {
        return try {
            File(path).canonicalPath
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun fixEmptyJson(file: File, isArray: Boolean = false) {
        var empty = false

        if (!file.exists()) {
            file.createNewFile()
            empty = true
        } else if (file.length() <= 8) {
            val string = file.readText()
            empty =
                string.isBlank() ||
                    string.all {
                        it == '[' ||
                            it == ']' ||
                            it == '{' ||
                            it == '}' ||
                            it == ' ' ||
                            it == '\n' ||
                            it == '\r'
                    }
        }

        if (empty) {
            try {
                FileWriter(file, false).use { it.write(if (isArray) "[]" else "{}") }
            } catch (exception: IOException) {
                Melon.logger.warn("Failed fixing empty json", exception)
            }
        }
    }
}
