package dev.zenhao.melon.gui.clickgui;

import dev.zenhao.melon.gui.clickgui.component.Component;
import dev.zenhao.melon.gui.clickgui.component.ModuleButton;
import dev.zenhao.melon.gui.clickgui.component.SettingButton;
import dev.zenhao.melon.gui.clickgui.guis.HUDEditorScreen;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.HUDModule;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.utils.TimerUtils;
import dev.zenhao.melon.utils.Wrapper;
import dev.zenhao.melon.utils.font.CFontRenderer;
import dev.zenhao.melon.utils.font.FontUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Panel {
    public int x;
    public int y;
    public int width;
    public int height;
    public Category category;
    public boolean extended;
    public String categoryName;
    public List<ModuleButton> Elements = new ArrayList<>();
    boolean dragging;
    boolean isHUD;
    int x2;
    int y2;
    TimerUtils panelTimerUtils = new TimerUtils();
    CFontRenderer font;

    public Panel(Category category, int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.extended = true;
        this.dragging = false;
        this.category = category;
        this.isHUD = category.isHUD();
        this.font = FontUtils.LemonMilk;
        this.setup();
    }

    public void setup() {
        try {
            categoryName = category.getName();
            for (IModule m : ModuleManager.getAllIModules()) {
                if (m.category != this.category) continue;
                this.Elements.add(new ModuleButton(m, this.width, this.height, this));
            }
        } catch (Exception ignored) {
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.dragging) {
            this.x = this.x2 + mouseX;
            this.y = this.y2 + mouseY;
        }
        int panelColor = -2063597568;
        int GradientIntensity = GuiManager.getINSTANCE().getGradientIntensity();
        int startY = this.y + this.height + 2;
        if (!this.Elements.isEmpty()) {
            int zhe = 0;
            for (ModuleButton button : this.Elements) {
                if (this.extended ? !this.panelTimerUtils.passed((long) ++zhe * 20L) : this.panelTimerUtils.passed((long) (this.Elements.size() - ++zhe) * 20L))
                    continue;
                button.solvePos();
                button.y = startY;
                button.setAdd((float) ((startY - this.y) / button.height) * (float) GradientIntensity);
                button.render(mouseX, mouseY, partialTicks);
                int settingY = startY - 1;
                startY += button.height + 1;
                int zhe1 = 0;
                for (Component component : this.toIsVisibleList(button.settings)) {
                    ++zhe1;
                    if (!button.isExtended ? button.buttonTimerUtils.passed((long) (this.toIsVisibleList(button.settings).size() - zhe1) * 15L) : !button.buttonTimerUtils.passed((long) (++zhe1) * 15L))
                        continue;
                    if (component instanceof SettingButton && !((SettingButton<?>) component).getValue().visible())
                        continue;
                    component.solvePos();
                    component.y = startY;
                    component.setAdd((float) ((startY - this.y) / component.height) * (float) GradientIntensity);
                    component.render(mouseX, mouseY, partialTicks);
                    startY += component.height;
                }
                ++startY;
                if (!button.module.isHUD || !(Wrapper.mc.currentScreen instanceof HUDEditorScreen)) continue;
                HUDModule hud = (HUDModule) button.module;
                if (hud.isEnabled()) {
                    Gui.drawRect(hud.x, hud.y, hud.x + hud.width, hud.y + hud.height, panelColor);
                    hud.onRender();
                }
            }
        }
        RenderUtils.drawHalfRoundedRectangle(this.x, this.y + 2f, this.width , this.height, 5, RenderUtils.HalfRoundedDirection.Top, new Color(0, 0, 0, 255));
        RenderUtils.drawHalfRoundedRectangle(this.x, startY - 1.5f - (this.extended ? 0 : 2), this.width, this.height, 5, RenderUtils.HalfRoundedDirection.Bottom, new Color(0, 0, 0, 255));
        font.drawString(categoryName, (float) this.x + (width / 2F) - (font.getStringWidth(categoryName) / 2F), (float) this.y + (float) this.height / 2.0f - (float) font.getHeight() / 2.0f, -1052689);
    }

    public List<Component> toIsVisibleList(List<Component> toChangeList) {
        return toChangeList.stream().filter(obj -> {
            if (obj instanceof SettingButton) {
                return ((SettingButton<?>) obj).getValue().visible();
            }
            return true;
        }).collect(Collectors.toList());
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovered(mouseX, mouseY).test(this)) {
            this.x2 = this.x - mouseX;
            this.y2 = this.y - mouseY;
            this.dragging = true;
            if (!this.isHUD) {
                Collections.swap(GUIRender.panels, 0, GUIRender.panels.indexOf(this));
            } else {
                Collections.swap(HUDRender.getINSTANCE().panels, 0, HUDRender.getINSTANCE().panels.indexOf(this));
            }
            return true;
        }
        if (mouseButton == 1 && this.isHovered(mouseX, mouseY).test(this)) {
            this.extended = !this.extended;
            this.panelTimerUtils.reset();
            return true;
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0) {
            this.dragging = false;
        }
        for (Component component : this.Elements) {
            component.mouseReleased(mouseX, mouseY, state);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        for (Component component : this.Elements) {
            component.keyTyped(typedChar, keyCode);
        }
    }

    public Predicate<Panel> isHovered(int mouseX, int mouseY) {
        return c -> mouseX >= Math.min(c.x, c.x + c.width) && mouseX <= Math.max(c.x, c.x + c.width) && mouseY >= Math.min(c.y, c.y + c.height) && mouseY <= Math.max(c.y, c.y + c.height);
    }
}

