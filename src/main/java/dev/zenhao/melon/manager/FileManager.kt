package dev.zenhao.melon.manager

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.zenhao.melon.Melon
import dev.zenhao.melon.Melon.Companion.instance
import dev.zenhao.melon.command.commands.comm.BindCommand
import dev.zenhao.melon.gui.alt.utils.Alt
import dev.zenhao.melon.gui.alt.utils.AltSystem
import dev.zenhao.melon.gui.clickgui.GUIRender
import dev.zenhao.melon.gui.clickgui.HUDRender
import dev.zenhao.melon.module.IModule
import dev.zenhao.melon.module.ModuleManager.getHUDByName
import dev.zenhao.melon.module.ModuleManager.getModuleByName
import dev.zenhao.melon.module.ModuleManager.getModules
import dev.zenhao.melon.module.ModuleManager.hUDModules
import dev.zenhao.melon.module.modules.client.NullHUD
import dev.zenhao.melon.module.modules.client.NullModule
import dev.zenhao.melon.setting.*
import dev.zenhao.melon.utils.EncryptionUtils
import dev.zenhao.melon.utils.other.Friend
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import melon.utils.concurrent.threads.defaultScope
import java.awt.Color
import java.io.*
import java.nio.charset.StandardCharsets

object FileManager {
    private val initializedConfig: MutableList<File?> = ArrayList()
    private var CLIENT_FILE: File? = null
    private var FRIEND_FILE: File? = null
    private var GUI_FILE: File? = null
    private var HUD_FILE: File? = null
    private var ALT_FILE: File? = null
    private var mutex = Mutex()

    fun onInit() {
        if (!tryLoad()) {
            deleteFiles()
        }
        checkPath(File(NOTEBOT_PATH))
        checkPath(File(BACKGROUND_PATH))
    }

    private fun deleteFiles() {
        try {
            initializedConfig.forEach { it?.delete() }
            Melon.logger.info("All config files deleted successfully!\n")
        } catch (e: Exception) {
            Melon.logger.error("Error while deleting config files!")
            e.printStackTrace()
        }
    }

    private fun saveClient() {
        try {
            checkFile(CLIENT_FILE)
            val father = JsonObject()
            val stuff = JsonObject()
            stuff.addProperty("CommandPrefix", Melon.commandPrefix.value)
            stuff.addProperty(BindCommand.modifiersEnabled.name, BindCommand.modifiersEnabled.value)
            father.add("Client", stuff)
            val saveJson = PrintWriter(OutputStreamWriter(FileOutputStream(CLIENT_CONFIG), StandardCharsets.UTF_8))
            saveJson.println(gsonPretty!!.toJson(father))
            saveJson.close()
        } catch (e: Exception) {
            Melon.logger.error("Error while saving Client stuff!")
            e.printStackTrace()
        }
    }

    private fun saveFriend() {
        try {
            FRIEND_FILE?.let {
                if (!it.exists()) {
                    it.parentFile.mkdirs()
                    try {
                        it.createNewFile()
                    } catch (ignored: Exception) {
                    }
                }
                val father = JsonObject()
                for (friend in FriendManager.getFriendList()) {
                    val stuff = JsonObject()
                    stuff.addProperty("isFriend", friend.isFriend)
                    father.add(friend.name, stuff)
                }
                val saveJSon = PrintWriter(OutputStreamWriter(FileOutputStream(it), StandardCharsets.UTF_8))
                saveJSon.println(gsonPretty!!.toJson(father))
                saveJSon.close()
            }
        } catch (e: Exception) {
            Melon.logger.error("Error while saving friends!")
            e.printStackTrace()
        }
    }

    private fun saveGUI() {
        try {
            checkFile(GUI_FILE)
            var jsonGui: JsonObject
            val father = JsonObject()
            GUIRender.panels?.let {
                for (panel in it) {
                    jsonGui = JsonObject()
                    jsonGui.addProperty("X", panel.x)
                    jsonGui.addProperty("Y", panel.y)
                    jsonGui.addProperty("Extended", panel.extended)
                    father.add(panel.category.getName(), jsonGui)
                }
            }
            HUDRender.getINSTANCE()?.let {
                it.panels?.let { p ->
                    for (panel in p) {
                        jsonGui = JsonObject()
                        jsonGui.addProperty("X", panel.x)
                        jsonGui.addProperty("Y", panel.y)
                        jsonGui.addProperty("Extended", panel.extended)
                        father.add(panel.category.getName(), jsonGui)
                    }
                }
            }
            val saveJSon = PrintWriter(OutputStreamWriter(FileOutputStream(GUI_CONFIG), StandardCharsets.UTF_8))
            saveJSon.println(gsonPretty!!.toJson(father))
            saveJSon.close()
        } catch (e: Exception) {
            Melon.logger.error("Error while saving GUI config!")
            e.printStackTrace()
        }
    }

    private fun saveHUD() {
        try {
            checkFile(HUD_FILE)
            val father = JsonObject()
            for (module in hUDModules) {
                val jsonModule = JsonObject()
                jsonModule.addProperty("Enable", module.isEnabled)
                jsonModule.addProperty("HUDPosX", module.x)
                jsonModule.addProperty("HUDPosY", module.y)
                jsonModule.addProperty("Bind", module.getBind())
                if (module.settingList.isNotEmpty()) {
                    for (setting in module.settingList) {
                        if (setting is StringSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as String)
                        }
                        if (setting is ColorSetting) {
                            jsonModule.addProperty(setting.getName(), (setting.value as Color).rgb)
                        }
                        if (setting is BooleanSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Boolean)
                        }
                        if (setting is IntegerSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Int)
                        }
                        if (setting is FloatSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Float)
                        }
                        if (setting is DoubleSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Double)
                        }
                        if (setting !is ModeSetting<*>) continue
                        jsonModule.addProperty(setting.name, setting.valueAsString)
                    }
                }
                module.onConfigSave()
                father.add(module.moduleName, jsonModule)
            }
            val saveJSon = PrintWriter(OutputStreamWriter(FileOutputStream(HUD_CONFIG), StandardCharsets.UTF_8))
            saveJSon.println(gsonPretty!!.toJson(father))
            saveJSon.close()
        } catch (e: Exception) {
            Melon.logger.error("Error while saving HUD config!")
            e.printStackTrace()
        }
    }

    private fun saveModule(bbtt: Boolean) {
        try {
            val father = JsonObject()
            for (module in getModules()) {
                val moduleFile =
                    File(if (bbtt) "Melon/config/2b2t/modules/" + module.moduleName + ".json" else "Melon/config/modules/" + module.moduleName + ".json")
                checkFile(moduleFile)
                val jsonModule = JsonObject()
                jsonModule.addProperty("Enable", module.isEnabled)
                jsonModule.addProperty("Bind", module.getBind())
                if (module.settingList.isNotEmpty()) {
                    for (setting in module.settingList) {
                        if (setting is StringSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as String)
                        }
                        if (setting is ColorSetting) {
                            jsonModule.addProperty(setting.getName(), (setting.value as Color).rgb)
                        }
                        if (setting is BooleanSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Boolean)
                        }
                        if (setting is IntegerSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Int)
                        }
                        if (setting is FloatSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Float)
                        }
                        if (setting is DoubleSetting) {
                            jsonModule.addProperty(setting.getName(), setting.value as Double)
                        }
                        if (setting !is ModeSetting<*>) continue
                        jsonModule.addProperty(setting.name, setting.valueAsString)
                    }
                }
                val saveJSon = PrintWriter(OutputStreamWriter(FileOutputStream(moduleFile), StandardCharsets.UTF_8))
                saveJSon.println(gsonPretty!!.toJson(jsonModule))
                saveJSon.close()
                module.onConfigSave()
                father.add(module.moduleName, jsonModule)
            }
        } catch (e: Exception) {
            Melon.logger.error("Error while saving module config!")
            e.printStackTrace()
        }
    }

    private fun saveAlt() {
        try {
            checkFile(ALT_FILE)
            val father = JsonObject()
            for (alt in AltSystem.getAlts()) {
                val stuff = JsonObject()
                stuff.addProperty("Pass", EncryptionUtils.Encrypt(alt.password, Melon.ALT_Encrypt_Key))
                father.add(alt.username, stuff)
            }
            ALT_FILE?.let {
                val saveJSon = PrintWriter(OutputStreamWriter(FileOutputStream(it), StandardCharsets.UTF_8))
                saveJSon.println(gsonPretty!!.toJson(father))
                saveJSon.close()
            }
        } catch (e: Exception) {
            Melon.logger.error("Error while saving alt!")
            e.printStackTrace()
        }
    }

    private fun loadClient() {
        CLIENT_FILE?.let {
            if (it.exists()) {
                try {
                    val loadJson =
                        BufferedReader(InputStreamReader(FileInputStream(it), StandardCharsets.UTF_8))
                    val guiJason = jsonParser.parse(loadJson) as JsonObject
                    loadJson.close()
                    for ((key, value) in guiJason.entrySet()) {
                        if (key != "Client") continue
                        val json = value as JsonObject
                        trySetClient(json)
                    }
                } catch (e: IOException) {
                    Melon.logger.error("Error while loading Client stuff!")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadFriend() {
        FRIEND_FILE?.let {
            if (it.exists()) {
                try {
                    val loadJson = BufferedReader(InputStreamReader(FileInputStream(it), StandardCharsets.UTF_8))
                    val friendJson = jsonParser.parse(loadJson) as JsonObject
                    loadJson.close()
                    instance.friendManager!!.friends.clear()
                    for ((name, value) in friendJson.entrySet()) {
                        if (name == null) continue
                        val nmsl = value as JsonObject
                        var isFriend = false
                        try {
                            isFriend = nmsl["isFriend"].asBoolean
                        } catch (e: Exception) {
                            Melon.logger.error("Can't set friend value for $name, unfriended!")
                        }
                        instance.friendManager!!.friends.add(Friend(name, isFriend))
                    }
                } catch (e: IOException) {
                    Melon.logger.error("Error while loading friends!")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadGUI() {
        GUI_FILE?.let {
            if (it.exists()) {
                try {
                    val loadJson = BufferedReader(InputStreamReader(FileInputStream(it), StandardCharsets.UTF_8))
                    val guiJson = jsonParser.parse(loadJson) as JsonObject
                    loadJson.close()
                    for ((key, value) in guiJson.entrySet()) {
                        var panel = GUIRender.getPanelByName(key as String)
                        if (panel == null) {
                            panel = HUDRender.getPanelByName(key)
                        }
                        if (panel == null) continue
                        val jsonGui = value as JsonObject
                        panel.x = jsonGui["X"].asInt
                        panel.y = jsonGui["Y"].asInt
                        panel.extended = jsonGui["Extended"].asBoolean
                    }
                } catch (e: IOException) {
                    Melon.logger.error("Error while loading GUI config!")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadHUD() {
        HUD_FILE?.let {
            if (it.exists()) {
                try {
                    val loadJson =
                        BufferedReader(InputStreamReader(FileInputStream(it), StandardCharsets.UTF_8))
                    val moduleJason = jsonParser.parse(loadJson) as JsonObject
                    loadJson.close()
                    for ((key, value) in moduleJason.entrySet()) {
                        val module = getHUDByName(key as String)
                        if (module is NullHUD) continue
                        val jsonMod = value as JsonObject
                        val enabled = jsonMod["Enable"].asBoolean
                        if (module.isEnabled && !enabled) {
                            module.disable()
                        }
                        if (module.isDisabled && enabled) {
                            module.enable()
                        }
                        module.x = jsonMod["HUDPosX"].asInt
                        module.y = jsonMod["HUDPosY"].asInt
                        if (module.settingList.isNotEmpty()) {
                            trySet(module, jsonMod)
                        }
                        module.onConfigLoad()
                        module.setBind(jsonMod["Bind"].asInt)
                    }
                } catch (e: IOException) {
                    Melon.logger.info("Error while loading module config")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadModule(bbtt: Boolean) {
        try {
            for (module in getModules()) {
                val modulefile =
                    File(if (bbtt) "Melon/config/2b2t/modules/" + module.moduleName + ".json" else "Melon/config/modules/" + module.moduleName + ".json")
                if (!modulefile.exists()) continue
                defaultScope.launch {
                    runBlocking {
                        mutex.withLock {
                            val loadJson =
                                BufferedReader(InputStreamReader(FileInputStream(modulefile), StandardCharsets.UTF_8))
                            val moduleJason = jsonParser.parse(loadJson) as JsonObject
                            loadJson.close()
                            for ((_, _) in moduleJason.entrySet()) {
                                val modul = getModuleByName(module.moduleName)
                                if (modul is NullModule) continue
                                val enabled = moduleJason["Enable"].asBoolean
                                if (modul.isEnabled && !enabled) {
                                    modul.disable()
                                }
                                if (modul.isDisabled && enabled) {
                                    modul.enable()
                                }
                                if (modul.settingList.isNotEmpty()) {
                                    trySet(modul, moduleJason)
                                }
                                modul.onConfigLoad()
                                modul.setBind(moduleJason["Bind"].asInt)
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Melon.logger.info("Error while loading module config")
            e.printStackTrace()
        }
    }

    private fun loadAlt() {
        ALT_FILE?.let {
            if (it.exists()) {
                try {
                    val loadJson =
                        BufferedReader(InputStreamReader(FileInputStream(it), StandardCharsets.UTF_8))
                    val altJson = jsonParser.parse(loadJson) as JsonObject
                    loadJson.close()
                    for ((key, value) in altJson.entrySet()) {
                        if (key == null) continue
                        val jsonalt = value as JsonObject
                        var pass: String? = ""
                        try {
                            pass = EncryptionUtils.Decrypt(jsonalt["Pass"].asString, Melon.ALT_Encrypt_Key)
                        } catch (e: Exception) {
                            Melon.logger.error("Can't set Password for $key!")
                        }
                        AltSystem.getAlts().add(Alt(key, pass))
                    }
                } catch (e: IOException) {
                    Melon.logger.error("Error while loading Alt!")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun tryLoad(): Boolean {
        try {
            CLIENT_FILE = File(CLIENT_CONFIG)
            initializedConfig.add(CLIENT_FILE)
            FRIEND_FILE = File(FRIEND_CONFIG)
            initializedConfig.add(FRIEND_FILE)
            GUI_FILE = File(GUI_CONFIG)
            initializedConfig.add(GUI_FILE)
            HUD_FILE = File(HUD_CONFIG)
            initializedConfig.add(HUD_FILE)
            ALT_FILE = File(ALT_CONFIG)
            initializedConfig.add(ALT_FILE)
        } catch (e: Exception) {
            Melon.logger.error("Config files aren't exist or are broken!")
            return false
        }
        return true
    }

    private fun checkFile(file: File?) {
        try {
            file?.let {
                if (!it.exists()) {
                    it.parentFile.mkdirs()
                    it.createNewFile()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkPath(path: File) {
        try {
            if (!path.exists()) {
                path.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun trySet(mods: IModule, jsonMod: JsonObject) {
        try {
            for (value in mods.settingList) {
                tryValue(mods.moduleName!!, value, jsonMod)
            }
        } catch (e: Exception) {
            Melon.logger.error("Cant set value for " + (if (mods.isHUD) "HUD " else " module ") + mods.moduleName + "!")
        }
    }

    private fun tryValue(name: String, setting: Setting<*>, jsonMod: JsonObject) {
        try {
            if (setting is StringSetting) {
                val sValue = jsonMod[setting.getName()].asString
                setting.value = sValue
            }
            if (setting is ColorSetting) {
                val rgba = jsonMod[setting.getName()].asInt
                setting.value = Color(rgba, true)
            }
            if (setting is BooleanSetting) {
                val bValue = jsonMod[setting.getName()].asBoolean
                setting.value = bValue
            }
            if (setting is DoubleSetting) {
                val dValue = jsonMod[setting.getName()].asDouble
                setting.value = dValue
            }
            if (setting is IntegerSetting) {
                val iValue = jsonMod[setting.getName()].asInt
                setting.value = iValue
            }
            if (setting is FloatSetting) {
                val fValue = jsonMod[setting.getName()].asFloat
                setting.value = fValue
            }
            if (setting is ModeSetting<*>) {
                setting.setValueByString(jsonMod[setting.name].asString)
            }
        } catch (e: Exception) {
            Melon.logger.error("Cant set value for " + name + ",loaded default! Value name: " + setting.name)
        }
    }

    private fun trySetClient(json: JsonObject) {
        try {
            Melon.commandPrefix.value = json["CommandPrefix"].asString
            BindCommand.modifiersEnabled.setValue(json[BindCommand.modifiersEnabled.name].asBoolean)
        } catch (e: Exception) {
            Melon.logger.error("Error while setting Client!")
        }
    }

    // Default
    const val D = Melon.MOD_NAME
    const val CONFIG_PATH = "$D/config/"
    const val HUD_CONFIG = CONFIG_PATH + "/" + Melon.MOD_NAME + "-HUDModule.json"
    const val MODULE_CONFIG = "$CONFIG_PATH/modules/"
    const val NOTEBOT_PATH = "$D/notebot/"
    const val BACKGROUND_PATH = "$D/background/"
    const val SPAMMER = "$D/Spammer.txt"
    const val packetSendFile = "$D/PacketSend.txt"
    const val packetReceiveFile = "$D/PacketReceive.txt"
    const val esuImageFile = "$D/esu/EsuImageConfig.txt"
    const val ALT_CONFIG = D + "/" + Melon.MOD_NAME + "-Alt.json"
    const val CLIENT_CONFIG = D + "/" + Melon.MOD_NAME + "-Client.json"
    const val FRIEND_CONFIG = D + "/" + Melon.MOD_NAME + "-Friend.json"
    const val GUI_CONFIG = D + "/" + Melon.MOD_NAME + "-Gui.json"
    private var gsonPretty = GsonBuilder().setPrettyPrinting().create()
    private var jsonParser = JsonParser()

    @JvmStatic
    fun saveAll(bbtt: Boolean) {
        saveClient()
        saveFriend()
        saveGUI()
        saveHUD()
        saveModule(bbtt)
        saveAlt()
    }

    @JvmStatic
    fun loadAll(bbtt: Boolean) {
        loadClient()
        loadFriend()
        loadGUI()
        loadHUD()
        loadModule(bbtt)
        loadAlt()
    }
}