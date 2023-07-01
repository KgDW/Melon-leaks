package dev.zenhao.melon.module

import dev.zenhao.melon.Melon
import dev.zenhao.melon.event.events.render.RenderEvent
import dev.zenhao.melon.gui.clickgui.guis.HUDEditorScreen
import dev.zenhao.melon.module.hud.huds.*
import dev.zenhao.melon.module.hud.info.*
import dev.zenhao.melon.module.modules.chat.AutoGG
import dev.zenhao.melon.module.modules.chat.ChatNotifier
import dev.zenhao.melon.module.modules.chat.ChatSuffix
import dev.zenhao.melon.module.modules.chat.ChatTimeStamps
import dev.zenhao.melon.module.modules.client.*
import dev.zenhao.melon.module.modules.combat.*
import dev.zenhao.melon.module.modules.crystal.MelonAura2
import dev.zenhao.melon.module.modules.extra.AutoCraftBed
import dev.zenhao.melon.module.modules.extra.NewBedAura
import dev.zenhao.melon.module.modules.extra.SmartOffHand
import dev.zenhao.melon.module.modules.extra.Surround
import dev.zenhao.melon.module.modules.misc.*
import dev.zenhao.melon.module.modules.misc.nocom.NoCom
import dev.zenhao.melon.module.modules.movement.*
import dev.zenhao.melon.module.modules.player.*
import dev.zenhao.melon.module.modules.render.*
import dev.zenhao.melon.module.modules.render.chorus.ChorusDetect
import dev.zenhao.melon.utils.entity.EntityUtil
import dev.zenhao.melon.utils.gl.MelonTessellator
import dev.zenhao.melon.utils.threads.runAsyncThread
import melon.events.render.Render2DEvent
import melon.system.event.AlwaysListening
import melon.system.event.safeEventListener
import melon.system.util.delegate.AsyncCachedValue
import melon.utils.TimeUnit
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import org.lwjgl.opengl.GL11
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import java.util.stream.Collectors

object ModuleManager : AlwaysListening {
    private var moduleList = CopyOnWriteArrayList<IModule>()
    private val modulesDelegate = AsyncCachedValue(5L, TimeUnit.SECONDS) {
        moduleList.distinct().sortedBy { it.moduleName }
    }
    val moduleLists by modulesDelegate

    fun getToggleList(): ArrayList<Module> {
        val toggleList = ArrayList<Module>()
        toggleList.add(NoCom)
        toggleList.add(Freecam)
        toggleList.add(Blink)
        toggleList.add(FakePlayer)
        toggleList.add(AutoStash)
        return toggleList
    }

    fun init() {
        loadModules()
        loadHUDs()
        moduleList.sortWith(Comparator.comparing { it.moduleName.toString() })
        Melon.logger.info("Module Initialised")
    }

    private fun loadModules() {
        //Chat
        registerModule(AutoGG())
        registerModule(ChatSuffix())
        registerModule(ChatTimeStamps())
        registerModule(ChatNotifier())
        //Client
        registerModule(ClickGui())
        registerModule(Colors())
        registerModule(CustomFont())
        registerModule(NewCustomFont)
        registerModule(HUDEditor())
        //registerModule(new NullAura());
        registerModule(NullModule())
        registerModule(SettingPanel())
        registerModule(SettingPanelColor())
        //Render
        registerModule(CameraClip())
        registerModule(StorageESP)
        registerModule(SkyColor())
        registerModule(CustomFov())
        registerModule(ArmourHUD())
        registerModule(BreakESP)
        registerModule(TabFriends())
        registerModule(ViewModel())
        registerModule(NoRender())
        registerModule(Brightness())
        registerModule(Animations())
        registerModule(HoleESP())
        registerModule(Nametags())
        registerModule(PearlViewer())
        registerModule(PortalESP())
        registerModule(ShulkerPreview())
        registerModule(PopChams())
        registerModule(NewChunks())
        registerModule(CrystalChams)
        registerModule(Skeleton())
        registerModule(ESP())
        registerModule(ArmorHide())
        registerModule(ChorusDetect())
        registerModule(ExplosionChams)
        registerModule(AutoEsu)
        //Combat
        registerModule(Anti32kTotem)
        registerModule(Aura32K())
        registerModule(Auto32GAY())
        registerModule(AutoEXP)
        registerModule(AutoReplenish())
        registerModule(AutoTotem)
        registerModule(AutoTrap())
        registerModule(AutoWeb)
        registerModule(Burrow)
        registerModule(Criticals())
        registerModule(CevBreaker)
        registerModule(DispenserMeta)
        registerModule(Fastuse())
        registerModule(HoleFiller())
        registerModule(KillAura())
        registerModule(PistonCrystal())
        registerModule(Pull32k())
        registerModule(SelfWeb())
        registerModule(TotemPopCounter())
        registerModule(EzBow())
        registerModule(AutoCity)
        registerModule(AntiBurrow())
        registerModule(HoleSnap)
        registerModule(HopperNuker())
        registerModule(AntiWeb())
        registerModule(HoleKicker)
        //Player
        registerModule(LiquidInteract())
        registerModule(Reach())
        registerModule(Freecam)
        registerModule(AutoArmour)
        registerModule(Blink)
        registerModule(ChestStealer())
        registerModule(PacketMine)
        registerModule(MultiTask())
        registerModule(NoEntityTrace())
        registerModule(LowOffHand())
        registerModule(NoFall())
        registerModule(PacketCancel())
        registerModule(PingSpoof())
        registerModule(Scaffold())
        registerModule(Timer)
        registerModule(NoRotate)
        //Misc
        registerModule(AntiAim())
        registerModule(ServerCrasher())
        registerModule(NoteBot())
        registerModule(EntityDeSync())
        registerModule(PacketEat())
        registerModule(ExtraTab())
        registerModule(XCarry())
        registerModule(AutoReconnect())
        registerModule(AutoRespawn())
        registerModule(AutoWither())
        registerModule(FakePlayer)
        registerModule(Nuker())
        registerModule(MCP)
        registerModule(MCF())
        registerModule(Spammer())
        registerModule(AirPlace)
        registerModule(NoCom)
        registerModule(MountBypass)
        registerModule(PacketAnalyzer)
        registerModule(Disabler)
        registerModule(AutoStash)
        //Movement
        registerModule(FlyFactor())
        registerModule(Velocity())
        registerModule(NoSlowDown)
        registerModule(BoatFly)
        registerModule(EntitySpeed())
        registerModule(GuiMove())
        registerModule(AntiVoid())
        registerModule(ElytraPlus)
        registerModule(FastSwim())
        registerModule(Jesus())
        registerModule(PacketFlyRewrite)
        registerModule(Speed)
        registerModule(Sprint())
        registerModule(Step)
        registerModule(Strafe())
        registerModule(ReverseStep())
        registerModule(Flight())
        registerModule(AutoWalk())
        registerModule(Phase())
        //XDDD
        registerModule(Surround())
        registerModule(SmartOffHand())
        registerModule(NewBedAura())
        registerModule(AutoCraftBed())
        //SEXY
        registerModule(MelonAura2)
        getModules().sortedWith(Comparator.comparing { it.moduleName!! })
    }

    private fun loadHUDs() {
        //_root_ide_package_.dev.zenhao.melon.module.ModuleManager.registerModule(ShowArrayList())
        registerModule(WaterMark())
        registerModule(Player())
        registerModule(Ping())
        registerModule(FPS())
        registerModule(TPS())
        registerModule(CoordsHUD())
        registerModule(Server())
        registerModule(Obsidian())
        registerModule(HoleHud())
        registerModule(Friends())
        registerModule(TextRadar())
        registerModule(SpeedHud())
        registerModule(Ram())
        registerModule(Totem())
        registerModule(Crystal())
        registerModule(Exp())
        registerModule(Gap())
        registerModule(Welcomer())
        registerModule(StackNotificationHUD())
        registerModule(NewArrayList())
        registerModule(ShowArrayList)
        registerModule(ActiveModules)
        getModules().sortedWith(Comparator.comparing { it.moduleName!! })
    }

    fun onKey(event: InputUpdateEvent) {
        moduleList.forEach {
            if (it.isEnabled) {
                it.onKey(event)
            }
        }
    }

    private fun registerModule(module: IModule) {
        try {
            runAsyncThread {
                moduleList.add(module)
                modulesDelegate.update()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            System.err.println("Couldn't initiate module " + module.javaClass.simpleName + "! Err: " + e.javaClass.simpleName + ", message: " + e.message)
        }
    }

    @JvmStatic
    val modulesForRender: List<IModule>
        get() = getModules().stream()
            .filter { it.isEnabled }
            .filter { it.isShownOnArrayStatic }
            .collect(Collectors.toList())

    @JvmStatic
    val allIModules: List<IModule>
        get() = moduleList

    @JvmStatic
    fun getModules(): List<IModule> {
        return moduleList.stream()
            .filter { it is Module }
            .collect(Collectors.toList())
    }

    @JvmStatic
    val hUDModules: List<IModule>
        get() = moduleList.stream()
            .filter { it is HUDModule }
            .collect(Collectors.toList())

    @JvmStatic
    fun getModuleByName(targetName: String?): IModule {
        for (iModule in allIModules) {
            if (!iModule.moduleName.equals(targetName, ignoreCase = true)) continue
            return iModule
        }
        //XG42.logger.fatal("Module " + targetName + " is not exist.Please check twice!");
        return NullModule()
    }

    @JvmStatic
    fun getModuleByClass(targetName: Class<*>): IModule {
        for (iModule in allIModules) {
            if (iModule.javaClass != targetName) continue
            return iModule
        }
        //XG42.logger.fatal("Module " + targetName + " is not exist.Please check twice!");
        return NullModule()
    }

    @JvmStatic
    fun getHUDByName(targetName: String?): HUDModule {
        for (iModule in hUDModules) {
            if (!iModule.moduleName.equals(targetName, ignoreCase = true)) continue
            return iModule as HUDModule
        }
        //XG42.logger.fatal("HUD " + targetName + " is not exist.Please check twice!");
        return NullHUD()
    }

    fun onBind(bind: Int) {
        if (bind == 0) {
            return
        }
        moduleList.forEach {
            if (getModuleByClass(it.javaClass) == getModuleByClass(
                    AutoEXP::class.java
                )
            ) {
                if (!AutoEXP.toggleMend.value) {
                    return@forEach
                }
            }
            if (it.getBind() == bind) {
                it.toggle()
            }
        }
    }

    fun onUpdate() {
        moduleList.forEach(Consumer { mod: IModule ->
            if (mod.isEnabled) {
                mod.onUpdate()
            }
        })
    }

    fun onLogin() {
        moduleList.forEach(Consumer { mod: IModule ->
            if (mod.isEnabled) {
                mod.onLogin()
            }
        })
    }

    fun onLogout() {
        moduleList.forEach(Consumer { mod: IModule ->
            if (mod.isEnabled) {
                mod.onLogout()
            }
        })
    }

    fun onRender(event: RenderGameOverlayEvent.Post) {
        moduleList.forEach {
            if (it.isEnabled) {
                it.onRender2DEvent(event)
            }
        }
        //onRenderHUD()
    }

    init {
        safeEventListener<Render2DEvent.Mc> {
            onRenderHUD()
        }
    }

    private fun onRenderHUD() {
        if (Minecraft.getMinecraft().currentScreen !is HUDEditorScreen) {
            hUDModules.forEach {
                if (it.isEnabled) {
                    it.onRender()
                }
            }
        }
    }

    private fun getInterpolatedPos(entity: Entity?, ticks: Float): Vec3d {
        return Vec3d(
            entity!!.lastTickPosX,
            entity.lastTickPosY,
            entity.lastTickPosZ
        ).add(EntityUtil.getInterpolatedAmount(entity, ticks.toDouble()))
    }

    fun onWorldRender(event: RenderWorldLastEvent) {
        Minecraft.getMinecraft().profiler.startSection("melon")
        Minecraft.getMinecraft().profiler.startSection("setup")
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        GlStateManager.disableDepth()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_POINT_SMOOTH)
        GlStateManager.glLineWidth(1f)
        GlStateManager.shadeModel(GL11.GL_SMOOTH)
        val renderPos = getInterpolatedPos(Minecraft.getMinecraft().getRenderViewEntity(), event.partialTicks)
        val e = RenderEvent(MelonTessellator, renderPos)
        e.resetTranslation()
        Minecraft.getMinecraft().profiler.endSection()
        moduleList.forEach {
            if (it.isEnabled) {
                Minecraft.getMinecraft().profiler.startSection(it.moduleName!!)
                it.onWorldRender(e)
                Minecraft.getMinecraft().profiler.endSection()
            }
        }
        Minecraft.getMinecraft().profiler.startSection("release")
        GlStateManager.glLineWidth(1f)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_POINT_SMOOTH)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.enableCull()
        MelonTessellator.releaseGL()
        Minecraft.getMinecraft().profiler.endSection()
    }
}