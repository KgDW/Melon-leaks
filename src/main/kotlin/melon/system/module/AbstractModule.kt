package melon.system.module

import melon.system.config.setting.AbstractSetting
import melon.system.config.setting.SettingRegister
import melon.system.config.setting.type.BindSetting
import melon.system.config.setting.type.BooleanSetting
import melon.system.config.setting.type.EnumSetting
import dev.zenhao.melon.utils.Bind
import dev.zenhao.melon.utils.FALSE_BLOCK
import dev.zenhao.melon.utils.chat.ChatUtil
import melon.system.config.basic.AbstractConfig
import melon.system.event.ListenerOwner
import melon.system.util.IDRegistry
import melon.system.util.interfaces.Alias
import melon.system.util.interfaces.DisplayEnum
import melon.system.util.interfaces.Nameable
import net.minecraft.client.Minecraft

@Suppress("UNCHECKED_CAST")
open class AbstractModule(
    override val name: String,
    override val alias: Array<String> = emptyArray(),
    val category: Category,
    val description: String,
    val modulePriority: Int = -1,
    var alwaysListening: Boolean = false,
    visible: Boolean = true,
    val alwaysEnabled: Boolean = false,
    val enabledByDefault: Boolean = false,
    private val config: AbstractConfig<out Nameable>
) : ListenerOwner(), Nameable, Alias, SettingRegister<Nameable>, Comparable<AbstractModule> {

    val id = idRegistry.register()

    private val enabled = BooleanSetting("Enabled", false, FALSE_BLOCK).also(::addSetting)
    val bind =
        BindSetting(
                "Bind",
                Bind(),
                { !alwaysEnabled },
                {
                    when (onHold.value) {
                        OnHold.OFF -> if (it) toggle()
                        OnHold.ENABLE -> toggle(it)
                        OnHold.DISABLE -> toggle(!it)
                    }
                }
            )
            .also(::addSetting)
    private val onHold = EnumSetting("On Hold", OnHold.OFF).also(::addSetting)
    private val visible = BooleanSetting("Visible", visible).also(::addSetting)
    private val default =
        BooleanSetting("Default", false, { settingList.isNotEmpty() }).also(::addSetting)

    private enum class OnHold(override val displayName: String) : DisplayEnum {
        OFF("Off"),
        ENABLE("Enable"),
        DISABLE("Disable")
    }

    val fullSettingList
        get() = config.getSettings(this)
    val settingList: List<AbstractSetting<*>>
        get() =
            fullSettingList.filter {
                it != bind && it != enabled && it != enabled && it != visible && it != default
            }

    val isEnabled: Boolean get() = enabled.value || alwaysEnabled
    val isDisabled: Boolean get() = !isEnabled
    val chatName: String get() = "[${name}]"
    val isVisible: Boolean
        get() = visible.value

    private fun addSetting(setting: AbstractSetting<*>) {
        (config as AbstractConfig<Nameable>).addSettingToConfig(this, setting)
    }

    internal fun postInit() {
        enabled.value = enabledByDefault || alwaysEnabled
        if (alwaysListening) {
            subscribe()
        }
    }

    fun toggle(state: Boolean) {
        enabled.value = state
    }

    fun toggle() {
        enabled.value = !enabled.value
    }

    fun enable() {
        enabled.value = true
    }

    fun disable() {
        enabled.value = false
    }

    open fun isActive(): Boolean {
        return isEnabled || alwaysListening
    }

    open fun getHudInfo(): String {
        return ""
    }

    protected fun onEnable(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            if (input) {
                block(input)
            }
        }
    }

    protected fun onDisable(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input ->
            if (!input) {
                block(input)
            }
        }
    }

    protected fun onToggle(block: (Boolean) -> Unit) {
        enabled.valueListeners.add { _, input -> block(input) }
    }

    override fun <S : AbstractSetting<*>> Nameable.setting(setting: S): S {
        (config as AbstractConfig<Nameable>).addSettingToConfig(this, setting)
        return setting
    }

    override fun compareTo(other: AbstractModule): Int {
        val result = this.modulePriority.compareTo(other.modulePriority)
        if (result != 0) return result
        return this.id.compareTo(other.id)
    }

    init {
        enabled.consumers.add { prev, input ->
            val enabled = alwaysEnabled || input

            if (prev != input && !alwaysEnabled) {
                //ModuleToggleEvent(this).post()
            }

            if (enabled || alwaysListening) {
                subscribe()
            } else {
                unsubscribe()
            }

            enabled
        }

        default.valueListeners.add { _, it ->
            if (it) {
                settingList.forEach { it.resetValue() }
                default.value = false
                ChatUtil.sendNoSpamRawChatMessage("$chatName $defaultMessage!")
            }
        }
    }

    protected companion object {
        const val defaultMessage = "Set to defaults"

        private val idRegistry = IDRegistry()
        val mc: Minecraft = Minecraft.getMinecraft()
    }
}
