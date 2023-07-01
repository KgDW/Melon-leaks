package dev.zenhao.melon.gui.clickgui;

import dev.zenhao.melon.gui.clickgui.component.Component;
import dev.zenhao.melon.gui.clickgui.component.ModuleButton;
import dev.zenhao.melon.gui.clickgui.component.SettingButton;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.ModuleManager;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;

public class HUDRender
        extends GuiScreen {
    static HUDRender INSTANCE;
    public ArrayList<Panel> panels = new ArrayList<>();

    public HUDRender() {
        INSTANCE = this;
        this.setup();
    }

    public static HUDRender getINSTANCE() {
        return INSTANCE;
    }

    public static Panel getPanelByName(String name) {
        Panel getPane = null;
        if (INSTANCE.panels != null) {
            for (Panel panel : INSTANCE.panels) {
                if (!panel.category.getName().equals(name)) continue;
                getPane = panel;
            }
        }
        return getPane;
    }

    public void setup() {
        int startX = 5;
        for (Category category : Category.values()) {
            if (!category.isHUD()) continue;
            this.panels.add(new Panel(category, startX, 5, 110, 15));
            startX += 120;
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseDrag();
        for (int i = this.panels.size() - 1; i >= 0; --i) {
            this.panels.get(i).drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Panel panel : this.panels) {
            if (panel.mouseClicked(mouseX, mouseY, mouseButton)) {
                return;
            }
            if (!panel.extended) continue;
            for (ModuleButton part : panel.Elements) {
                if (part.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return;
                }
                if (!part.isExtended) continue;
                for (Component component : part.settings) {
                    if (component instanceof SettingButton && !((SettingButton) component).getValue().visible() || !component.mouseClicked(mouseX, mouseY, mouseButton))
                        continue;
                    return;
                }
            }
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            ModuleManager.getModuleByName("HUDEditor").disable();
        }
        for (Panel panel : this.panels) {
            panel.keyTyped(typedChar, keyCode);
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        for (Panel panel : this.panels) {
            panel.mouseReleased(mouseX, mouseY, state);
        }
    }

    public void mouseDrag() {
        int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            this.panels.forEach(component -> component.y -= 10);
        } else if (dWheel > 0) {
            this.panels.forEach(component -> component.y += 10);
        }
    }
}

