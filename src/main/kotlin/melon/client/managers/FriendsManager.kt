package melon.client.managers

import com.google.gson.reflect.TypeToken
import dev.zenhao.melon.Melon
import dev.zenhao.melon.utils.JsonUtils
import dev.zenhao.melon.utils.java.resolve
import melon.client.utils.PlayerProfile
import melon.system.manager.Manager
import melon.utils.extension.synchronized
import java.io.FileWriter

internal object FriendsManager : Manager() {
    var friends = HashMap<String, Friend>().synchronized()
    val file = (Melon.DIRECTORY resolve "friends.json").toFile()

    fun hasFriend() = friends.isNotEmpty()

    fun isFriend(name: String): Boolean = friends.contains(name.lowercase())

    fun addFriend(name: String, level: Int = 1) = UUIDManager.getByName(name)?.let {
        friends[it.name.lowercase()] = Friend(level, it)
        true
    } ?: false

    fun setFriendLevel(name: String, level: Int): Boolean {
        return friends[name.lowercase()]?.let {
            it.level = level
            true
        } ?: false
    }

    fun removeFriend(name: String) = friends.remove(name.lowercase())

    fun clearFriend() {
        friends.clear()
    }

    fun load(): Boolean {
        JsonUtils.fixEmptyJson(file)

        return try {
            val friendFile: FriendFile = JsonUtils.gson.fromJson(file.readText(), object : TypeToken<FriendFile>() {}.type)
            friends.clear()
            friends.putAll(friendFile.friends.associateBy { it.profile.name.lowercase() })
            Melon.logger.info("Friend loaded")
            true
        } catch (e: Exception) {
            Melon.logger.warn("Failed loading friends", e)
            false
        }
    }

    fun save(): Boolean {
        return try {
            FileWriter(file, false).buffered().use {
                JsonUtils.gson.toJson(FriendFile(friends.values.toMutableSet()), it)
            }
            Melon.logger.info("Friends saved")
            true
        } catch (e: Exception) {
            Melon.logger.warn("Failed saving friends", e)
            false
        }
    }

    data class Friend(
        var level: Int,
        val profile: PlayerProfile
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Friend) return false

            if (level != other.level) return false
            if (profile != other.profile) return false

            return true
        }

        override fun hashCode(): Int = profile.hashCode()
    }

    data class FriendFile(val friends: MutableSet<Friend> = LinkedHashSet<Friend>().synchronized()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FriendFile) return false

            if (friends != other.friends) return false

            return true
        }

        override fun hashCode(): Int {
            return friends.hashCode()
        }
    }
}