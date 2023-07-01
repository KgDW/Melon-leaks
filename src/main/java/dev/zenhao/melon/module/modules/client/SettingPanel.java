package dev.zenhao.melon.module.modules.client;

import dev.zenhao.melon.command.commands.module.ConfigCommand;
import dev.zenhao.melon.gui.settingpanel.MelonSettingPanel;
import dev.zenhao.melon.manager.FileManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;

@Module.Info(name = "SettingPanel", category = Category.CLIENT, keyCode = 54, visible = false)
public class SettingPanel
        extends Module {
    public static SettingPanel INSTANCE;
    MelonSettingPanel screen;

    @Override
    public void onInit() {
        INSTANCE = this;
        this.setGUIScreen(new MelonSettingPanel());
    }

    @Override
    public void onEnable() {
        if (SettingPanel.mc.player != null && !(SettingPanel.mc.currentScreen instanceof MelonSettingPanel)) {
            mc.displayGuiScreen(this.screen);
        }
    }

    @Override
    public void onDisable() {
        if (SettingPanel.mc.currentScreen instanceof MelonSettingPanel) {
            mc.displayGuiScreen(null);
        }
        FileManager.saveAll(ConfigCommand.org);
    }

    public void setGUIScreen(MelonSettingPanel screen) {
        this.screen = screen;
    }
}

