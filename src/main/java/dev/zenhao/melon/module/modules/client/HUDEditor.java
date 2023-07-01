package dev.zenhao.melon.module.modules.client;

import dev.zenhao.melon.command.commands.module.ConfigCommand;
import dev.zenhao.melon.gui.clickgui.guis.HUDEditorScreen;
import dev.zenhao.melon.manager.FileManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import net.minecraft.client.gui.GuiScreen;

@Module.Info(name = "HUDEditor", category = Category.CLIENT, keyCode = 41, visible = false)
public class HUDEditor
        extends Module {
    public static HUDEditor INSTANCE;
    HUDEditorScreen screen;

    @Override
    public void onInit() {
        INSTANCE = this;
        this.setGUIScreen(new HUDEditorScreen());
    }

    @Override
    public void onEnable() {
        if (HUDEditor.mc.player != null && !(HUDEditor.mc.currentScreen instanceof HUDEditorScreen)) {
            mc.displayGuiScreen(this.screen);
        }
    }

    @Override
    public void onDisable() {
        if (HUDEditor.mc.currentScreen instanceof HUDEditorScreen) {
            mc.displayGuiScreen(null);
        }
        FileManager.saveAll(ConfigCommand.org);
    }

    private void setGUIScreen(HUDEditorScreen screen) {
        this.screen = screen;
    }
}

