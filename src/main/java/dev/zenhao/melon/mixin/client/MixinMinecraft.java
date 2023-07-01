package dev.zenhao.melon.mixin.client;

import dev.zenhao.melon.Melon;
import dev.zenhao.melon.command.commands.module.ConfigCommand;
import dev.zenhao.melon.event.events.gui.GuiScreenEvent;
import dev.zenhao.melon.manager.FileManager;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.player.MultiTask;
import dev.zenhao.melon.utils.Wrapper;
import dev.zenhao.melon.utils.verify.Auth;
import melon.events.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {Minecraft.class})
public abstract class MixinMinecraft {
    @Shadow
    public WorldClient world;
    @Shadow
    public EntityPlayerSP player;
    @Shadow
    public PlayerControllerMP playerController;
    @Shadow
    public GuiScreen currentScreen;
    @Shadow
    public GameSettings gameSettings;
    @Shadow
    public boolean skipRenderWorld;
    @Shadow
    public SoundHandler soundHandler;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo info) {
        GuiScreenEvent.Closed screenEvent = new GuiScreenEvent.Closed(Wrapper.getMinecraft().currentScreen);
        MinecraftForge.EVENT_BUS.post(screenEvent);
        GuiScreenEvent.Displayed screenEvent1 = new GuiScreenEvent.Displayed(guiScreenIn);
        MinecraftForge.EVENT_BUS.post(screenEvent1);
        guiScreenIn = screenEvent1.getScreen();

        if (guiScreenIn == null && this.world == null) {
            guiScreenIn = new GuiMainMenu();
        } else if (guiScreenIn == null && this.player.getHealth() <= 0.0F) {
            guiScreenIn = new GuiGameOver(null);
        }

        GuiScreen old = this.currentScreen;
        GuiOpenEvent event = new GuiOpenEvent(guiScreenIn);

        if (MinecraftForge.EVENT_BUS.post(event)) return;

        guiScreenIn = event.getGui();
        if (old != null && guiScreenIn != old) {
            old.onGuiClosed();
        }

        this.currentScreen = guiScreenIn;

        if (guiScreenIn != null) {
            Minecraft.getMinecraft().setIngameNotInFocus();
            KeyBinding.unPressAllKeys();
            ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            guiScreenIn.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
            this.skipRenderWorld = false;
        } else {
            this.soundHandler.resumeSounds();
            Minecraft.getMinecraft().setIngameFocus();
        }

        info.cancel();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Timer;updateTimer()V", shift = At.Shift.BEFORE))
    public void runGameLoop$Inject$INVOKE$updateTimer(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("melonRunGameLoop");
        RunGameLoopEvent.Start.INSTANCE.post();
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", ordinal = 0, shift = At.Shift.AFTER))
    public void runGameLoopTick(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("melonRunGameLoop");
        RunGameLoopEvent.Tick.INSTANCE.post();
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 0, shift = At.Shift.BEFORE))
    public void runGameLoop$Inject$INVOKE$endStartSection(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.endStartSection("melonRunGameLoop");
        RunGameLoopEvent.Render.INSTANCE.post();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isFramerateLimitBelowMax()Z", shift = At.Shift.BEFORE))
    public void runGameLoop$Inject$INVOKE$isFramerateLimitBelowMax(CallbackInfo ci) {
        Wrapper.getMinecraft().profiler.startSection("melonRunGameLoop");
        RunGameLoopEvent.End.INSTANCE.post();
        Wrapper.getMinecraft().profiler.endSection();
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    public void runTick$Inject$HEAD(CallbackInfo ci) {
        TickEvent.Pre.INSTANCE.post();
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    public void runTick$Inject$RETURN(CallbackInfo ci) {
        TickEvent.Post.INSTANCE.post();
    }

    @Redirect(method = {"run"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"))
    public void displayCrashReport(Minecraft minecraft, CrashReport crashReport) {
        this.save();
    }

    @Inject(method = {"shutdown"}, at = @At(value = "HEAD"))
    public void shutdown(CallbackInfo info) {
        try {
            if (Auth.socket != null && !Auth.socket.isClosed()) {
                Auth.socket.close();
            }
        } catch (Exception ignored) {
        }
        this.save();
    }

    public void save() {
        Melon.logger.warn("Saving Melon configuration please wait...");
        FileManager.saveAll(ConfigCommand.org);
        Melon.logger.warn("Configuration saved!");
    }

    @Redirect(method = {"sendClickBlockToController"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    public boolean isHandActiveWrapper(EntityPlayerSP playerSP) {
        return (!ModuleManager.getModuleByClass(MultiTask.class).isEnabled() && playerSP.isHandActive());
    }

    @Redirect(method = {"rightClickMouse"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z", ordinal = 0), require = 1)
    public boolean isHittingBlockHook(PlayerControllerMP playerControllerMP) {
        return (!ModuleManager.getModuleByClass(MultiTask.class).isEnabled() && playerControllerMP.getIsHittingBlock());
    }
}

