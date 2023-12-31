package melon.client.managers

import dev.zenhao.melon.Melon
import dev.zenhao.melon.utils.JsonUtils
import dev.zenhao.melon.utils.java.resolve
import melon.client.utils.PlayerProfile
import melon.system.manager.Manager
import melon.system.util.io.ConnectionUtils
import melon.utils.Wrapper
import melon.utils.extension.synchronized
import java.util.*

object UUIDManager : Manager() {
    private val cache = (Melon.Companion.CachePath.CACHE resolve "uuid.txt").toFile()

    private val nameProfileMap = LinkedHashMap<String, PlayerProfile>().synchronized()
    private val uuidNameMap = LinkedHashMap<UUID, PlayerProfile>().synchronized()

    fun getOrRequest(nameOrUUID: String): PlayerProfile? {
        return Wrapper.minecraft.connection?.playerInfoMap?.let { playerInfoMap ->
            val infoMap = ArrayList(playerInfoMap)
            val isUUID = isUUID(nameOrUUID)
            val withOutDashes = removeDashes(nameOrUUID)

            infoMap.find {
                isUUID && removeDashes(it.gameProfile.id.toString()).equals(withOutDashes, ignoreCase = true)
                    || !isUUID && it.gameProfile.name.equals(nameOrUUID, ignoreCase = true)
            }?.gameProfile?.let {
                PlayerProfile(it.id, it.name)
            }
        } ?: requestProfile(nameOrUUID)
    }

    fun getByString(stringIn: String?) = stringIn?.let { string ->
        fixUUID(string)?.let { getByUUID(it) } ?: getByName(string)
    }

    fun getByUUID(uuid: UUID?) = uuid?.let {
        uuidNameMap.getOrPut(uuid) {
            getOrRequest(uuid.toString())?.also { profile ->
                // If UUID already present in nameUuidMap but not in uuidNameMap (user changed name)
                nameProfileMap[profile.name]?.let { uuidNameMap.remove(it.uuid) }
                nameProfileMap[profile.name] = profile
            } ?: return null
        }.also {
            trimMaps()
        }
    }

    fun getByName(name: String?) = name?.let {
        nameProfileMap.getOrPut(name.lowercase()) {
            getOrRequest(name)?.also { profile ->
                // If UUID already present in uuidNameMap but not in nameUuidMap (user changed name)
                uuidNameMap[profile.uuid]?.let { nameProfileMap.remove(it.name) }
                uuidNameMap[profile.uuid] = profile
            } ?: return null
        }.also {
            trimMaps()
        }
    }

    private fun trimMaps() {
        while (nameProfileMap.size > 1000) {
            nameProfileMap.remove(nameProfileMap.keys.first())?.also {
                uuidNameMap.remove(it.uuid)
            }
        }
    }

    private fun requestProfile(nameOrUUID: String): PlayerProfile? {
        val isUUID = isUUID(nameOrUUID)
        val response = if (isUUID) requestProfileFromUUID(nameOrUUID) else requestProfileFromName(nameOrUUID)

        return if (response.isNullOrBlank()) {
            Melon.logger.error("Response is null or blank, internet might be down")
            null
        } else {
            try {
                val jsonElement = JsonUtils.parser.parse(response)
                if (isUUID) {
                    val name = jsonElement.asJsonArray.last().asJsonObject["name"].asString
                    PlayerProfile(UUID.fromString(nameOrUUID), name)
                } else {
                    val id = jsonElement.asJsonObject["id"].asString
                    val name = jsonElement.asJsonObject["name"].asString
                    PlayerProfile(fixUUID(id)!!, name) // let it throw a NPE if failed to parse the string to UUID
                }
            } catch (e: Exception) {
                Melon.logger.error("Failed parsing profile", e)
                null
            }
        }
    }

    private fun requestProfileFromUUID(uuid: String): String? {
        return request("https://api.mojang.com/user/profiles/${removeDashes(uuid)}/names")
    }

    private fun requestProfileFromName(name: String): String? {
        return request("https://api.mojang.com/users/profiles/minecraft/$name")
    }

    private fun request(url: String): String? {
        return ConnectionUtils.requestRawJsonFrom(url) {
            Melon.logger.error("Failed requesting from Mojang API", it)
        }
    }

    fun load(): Boolean {
        JsonUtils.fixEmptyJson(cache)

        return try {
            val profileList = ArrayList<PlayerProfile>()

            cache.forEachLine {
                runCatching {
                    val split = it.split(':')
                    profileList.add(PlayerProfile(UUID.fromString(split[1]), split[0]))
                }
            }

            uuidNameMap.clear()
            nameProfileMap.clear()
            uuidNameMap.putAll(profileList.associateBy { it.uuid })
            nameProfileMap.putAll(profileList.associateBy { it.name })

            Melon.logger.info("UUID cache loaded")
            true
        } catch (e: Exception) {
            Melon.logger.warn("Failed loading UUID cache", e)
            false
        }
    }

    fun save(): Boolean {
        return try {
            cache.bufferedWriter().use { writer ->
                uuidNameMap.values.forEach {
                    writer.append(it.name)
                    writer.append(':')
                    writer.append(it.uuid.toString())
                    writer.newLine()
                }
            }

            Melon.logger.info("UUID cache saved")
            true
        } catch (e: Exception) {
            Melon.logger.warn("Failed saving UUID cache", e)
            false
        }
    }

    private fun fixUUID(string: String): UUID? {
        if (isUUID(string)) return UUID.fromString(string)
        if (string.length < 32) return null
        val fixed = insertDashes(string)
        return if (isUUID(fixed)) UUID.fromString(fixed)
        else null
    }

    private val uuidRegex = "[a-z0-9].{7}-[a-z0-9].{3}-[a-z0-9].{3}-[a-z0-9].{3}-[a-z0-9].{11}".toRegex()

    private fun isUUID(string: String) = uuidRegex.matches(string)

    private fun removeDashes(string: String) = string.replace("-", "")

    private fun insertDashes(string: String) = StringBuilder(string)
        .insert(8, '-')
        .insert(13, '-')
        .insert(18, '-')
        .insert(23, '-')
        .toString()
}
