package dev.zenhao.melon.module.modules.client;

import dev.zenhao.melon.command.commands.module.ConfigCommand;
import dev.zenhao.melon.gui.clickgui.GUIRender;
import dev.zenhao.melon.gui.clickgui.guis.ClickGuiScreen;
import dev.zenhao.melon.manager.FileManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;

@Module.Info(name = "ClickGUI", category = Category.CLIENT, keyCode = 22, visible = false)
public class ClickGui extends Module {
    public static ClickGui INSTANCE;
    ClickGuiScreen screen;

    @Override
    public void onInit() {
        INSTANCE = this;
        this.setGUIScreen(new ClickGuiScreen());
    }

    @Override
    public void onEnable() {
        if (!fullNullCheck() && !(mc.currentScreen instanceof ClickGuiScreen)) {
            GUIRender.getINSTANCE().initGui();
            mc.displayGuiScreen(this.screen);
        }
    }

    @Override
    public void onDisable() {
        if (!fullNullCheck() && mc.currentScreen instanceof ClickGuiScreen) {
            mc.displayGuiScreen(null);
        }
        FileManager.saveAll(ConfigCommand.org);
    }

    private void setGUIScreen(ClickGuiScreen screen) {
        this.screen = screen;
    }
}

