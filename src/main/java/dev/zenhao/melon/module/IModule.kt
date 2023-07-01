package dev.zenhao.melon.module

import com.mojang.realmsclient.gui.ChatFormatting
import dev.zenhao.melon.event.events.client.SettingChangeEvent
import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.module.hud.huds.ShowArrayList
import dev.zenhao.melon.module.modules.client.Colors
import dev.zenhao.melon.module.modules.client.CustomFont
import dev.zenhao.melon.notification.HudNotification.StackNotificationManager
import dev.zenhao.melon.notification.HudNotification.notifications.NormalStackNotification
import dev.zenhao.melon.notification.NotificationType
import dev.zenhao.melon.setting.*
import dev.zenhao.melon.utils.Wrapper
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.font.CFontRenderer
import melon.events.PacketEvents
import melon.events.Render3DEvent
import melon.events.RunGameLoopEvent
import melon.events.render.Render2DEvent
import melon.system.event.ListenerOwner
import melon.system.event.SafeClientEvent
import melon.system.event.safeEventListener
import melon.system.util.IDRegistry
import melon.system.util.color.ColorRGB
import melon.system.util.interfaces.Alias
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import java.awt.Color

abstract class IModule : ListenerOwner() {
    val settingList = ArrayList<Setting<*>>()
    var remainingAnimation = 0.0f

    @JvmField
    var moduleName: String? = null
    var isEnabled = false

    @JvmField
    var description: String? = null

    @JvmField
    var category: Category? = null
    private var bind = 0

    @JvmField
    var isHUD = false

    @JvmField
    var x = 0

    @JvmField
    var y = 0

    @JvmField
    var width = 0

    @JvmField
    var height = 0
    private var hidden = false
    private val idRegistry = IDRegistry()
    val id = idRegistry.register()

    fun enable() {
        ShowArrayList.toggleList.remove(this, ShowArrayList.toggleList[this])
        remainingAnimation = 0.0f
        isEnabled = true
        if (Colors.INSTANCE.chat.value) {
            ChatUtil.NoSpam.sendMessage(ChatFormatting.AQUA.toString() + moduleName + ChatFormatting.WHITE + " is" + ChatFormatting.GREEN + " Enabled!")
        }
        StackNotificationManager.add(
            NormalStackNotification(
                NotificationType.SUCCESS,
                ChatFormatting.AQUA.toString() + moduleName + ChatFormatting.WHITE + " is" + ChatFormatting.GREEN + " Enabled!",
                9
            )
        )
        onEnable()
        MinecraftForge.EVENT_BUS.register(this)
        subscribe()
    }

    fun disable() {
        ShowArrayList.toggleList[this] = System.currentTimeMillis() + (ShowArrayList.animationSpeed.value * 100f).toInt()
        remainingAnimation = 0.0f
        isEnabled = false
        if (Colors.INSTANCE.chat.value) {
            ChatUtil.NoSpam.sendMessage(ChatFormatting.AQUA.toString() + moduleName + ChatFormatting.WHITE + " is" + ChatFormatting.RED + " Disabled!")
        }
        StackNotificationManager.add(
            NormalStackNotification(
                NotificationType.DISABLE,
                ChatFormatting.AQUA.toString() + moduleName + ChatFormatting.WHITE + " is" + ChatFormatting.RED + " Disabled!",
                9
            )
        )
        onDisable()
        MinecraftForge.EVENT_BUS.unregister(this)
        unsubscribe()
    }

    fun safeDisable() {
        if (isEnabled) {
            disable()
        }
    }

    inline fun onRender3D(crossinline event: SafeClientEvent.(Render3DEvent) -> Unit) {
        safeEventListener<Render3DEvent> {
            if (isEnabled) {
                event.invoke(this, it)
            }
        }
    }

    inline fun onRender2D(crossinline event: SafeClientEvent.(Render2DEvent.Absolute) -> Unit) {
        safeEventListener<Render2DEvent.Absolute> {
            if (isEnabled) {
                event.invoke(this, it)
            }
        }
    }

    inline fun onRender2DMc(crossinline event: SafeClientEvent.(Render2DEvent.Mc) -> Unit) {
        safeEventListener<Render2DEvent.Mc> {
            if (isEnabled) {
                event.invoke(this, it)
            }
        }
    }

    inline fun onPacketSend(crossinline event: SafeClientEvent.(PacketEvents.Send) -> Unit) {
        safeEventListener<PacketEvents.Send> {
            if (isEnabled) {
                event.invoke(this, it)
            }
        }
    }

    inline fun onPacketReceive(crossinline event: SafeClientEvent.(PacketEvents.Receive) -> Unit) {
        safeEventListener<PacketEvents.Receive> {
            if (isEnabled) {
                event.invoke(this, it)
            }
        }
    }

    inline fun onMotion(crossinline event: SafeClientEvent.(PlayerMotionEvent) -> Unit) {
        safeEventListener<PlayerMotionEvent> {
            if (isEnabled) {
                event.invoke(this, it)
            }
        }
    }

    inline fun onLoop(crossinline event: SafeClientEvent.(RunGameLoopEvent.Tick) -> Unit) {
        safeEventListener<RunGameLoopEvent.Tick> {
            if (isEnabled) {
                event.invoke(this, it)
            }
        }
    }

    fun bindsetting(name: String?, keyboard: Int): BindSetting {
        val value = BindSetting(name, this, keyboard)
        settingList.add(value)
        return value
    }

    fun bsetting(name: String?, defaultValue: Boolean): BooleanSetting {
        val value = BooleanSetting(name, this, defaultValue)
        settingList.add(value)
        return value
    }

    fun isetting(name: String?, defaultValue: Int, minValue: Int, maxValue: Int, modifyValue: Int = 0): IntegerSetting {
        val value = IntegerSetting(name!!, this, defaultValue, minValue, maxValue, modifyValue)
        settingList.add(value)
        return value
    }

    fun fsetting(
        name: String?,
        defaultValue: Float,
        minValue: Float,
        maxValue: Float,
        modifyValue: Float = 0f
    ): FloatSetting {
        val value = FloatSetting(name!!, this, defaultValue, minValue, maxValue, modifyValue)
        settingList.add(value)
        return value
    }

    fun dsetting(
        name: String?,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        modifyValue: Double = 0.0
    ): DoubleSetting {
        val value = DoubleSetting(name!!, this, defaultValue, minValue, maxValue, modifyValue)
        settingList.add(value)
        return value
    }

    fun isetting(name: String, defaultValue: Int, minValue: Int, maxValue: Int): IntegerSetting {
        val value = IntegerSetting(name, this, defaultValue, minValue, maxValue, 0)
        settingList.add(value)
        return value
    }

    fun fsetting(name: String, defaultValue: Float, minValue: Float, maxValue: Float): FloatSetting {
        val value = FloatSetting(name, this, defaultValue, minValue, maxValue, 0f)
        settingList.add(value)
        return value
    }

    fun dsetting(name: String, defaultValue: Double, minValue: Double, maxValue: Double): DoubleSetting {
        val value = DoubleSetting(name, this, defaultValue, minValue, maxValue, 0.0)
        settingList.add(value)
        return value
    }

    fun msetting(name: String, modes: Enum<*>): ModeSetting<*> {
        val value: ModeSetting<*> = ModeSetting(name, this, modes)
        settingList.add(value)
        return value
    }

    fun ssetting(name: String, defaultValue: String): StringSetting {
        val value = StringSetting(name, this, defaultValue)
        settingList.add(value)
        return value
    }

    fun csetting(name: String?, defaultValue: Color): ColorSetting {
        val value = ColorSetting(name, this, defaultValue)
        settingList.add(value)
        return value
    }

    fun csetting(name: String?, defaultValue: ColorRGB): ColorSetting {
        val value = ColorSetting(name, this, Color(defaultValue.r, defaultValue.g, defaultValue.b))
        settingList.add(value)
        return value
    }

    val isDisabled: Boolean
        get() = !isEnabled

    open fun onConfigLoad() {}
    open fun onConfigSave() {}
    open fun onEnable() {}
    open fun onDisable() {}
    open fun onUpdate() {}
    open fun onLogout() {}
    open fun onLogin() {}
    open fun onRender() {}
    open fun onRender2DEvent(event: RenderGameOverlayEvent.Post) {}
    open fun onWorldRender(event: RenderEvent) {}
    open fun getHudInfo(): String? {
        return null
    }

    open fun getName(): String? {
        return moduleName
    }

    open fun getBind(): Int {
        return bind
    }

    fun setBind(bind: Int) {
        this.bind = bind
    }

    fun toggle() {
        val event = SettingChangeEvent(if (!isEnabled) 1 else 0, this)
        MinecraftForge.EVENT_BUS.post(event)
        if (event.isCanceled) {
            return
        }
        isEnabled = !isEnabled
        if (isEnabled) {
            enable()
        } else {
            disable()
        }
    }

    @Suppress("unused")
    fun onKey(event: InputUpdateEvent) {
    }

    fun setEnable(toggled: Boolean) {
        isEnabled = toggled
    }

    val font: CFontRenderer
        get() = CustomFont.getHUDFont()

    fun isHidden(): Boolean {
        return hidden || category!!.isHidden
    }

    val isShownOnArrayStatic: Boolean
        get() = Module.INSTANCEModule.isShownOnArray

    companion object {
        @JvmField
        val mc: Minecraft = Wrapper.mc

        @JvmField
        val fontRenderer: FontRenderer = mc.fontRenderer
    }
}