package dev.zenhao.melon.gui.clickgui;

import dev.zenhao.melon.gui.clickgui.component.Component;
import dev.zenhao.melon.gui.clickgui.component.ModuleButton;
import dev.zenhao.melon.gui.clickgui.component.SettingButton;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.modules.client.ClickGui;
import dev.zenhao.melon.utils.render.FadeUtils;
import dev.zenhao.melon.utils.render.gui.ScalaCalc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GUIRender extends GuiScreen {
    public static ArrayList<Panel> panels = new ArrayList<>();
    static GUIRender INSTANCE;
    private final ScalaCalc oc = new ScalaCalc().setAnimationTime(350).setFadeMode(FadeUtils.FadeMode.FADE_EPS_IN);

    public GUIRender() {
        INSTANCE = this;
        int startX = 5;
        for (Category category : Category.values()) {
            if (category.isHUD() || category == Category.HIDDEN) continue;
            panels.add(new Panel(category, startX, 5, 110, 15));
            startX += 115;
        }
    }

    public static GUIRender getINSTANCE() {
        return INSTANCE;
    }

    public static Panel getPanelByName(String name) {
        Panel getPane = null;
        if (panels != null) {
            for (Panel panel : panels) {
                if (!panel.category.getName().equals(name)) continue;
                getPane = panel;
            }
        }
        return getPane;
    }

    @Override
    public void initGui() {
        super.initGui();
        oc.reset();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseDrag();

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glPushMatrix();
        oc.drawA(sr);
        panels.forEach(panel -> panel.drawScreen(mouseX, mouseY, partialTicks));
        oc.drawB(sr);
        GL11.glPopMatrix();
    }

    @SuppressWarnings("all")
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (Panel panel : panels) {
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
            ClickGui.INSTANCE.disable();
        }
        panels.forEach(panel -> panel.keyTyped(typedChar, keyCode));
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, state));
    }

    public void mouseDrag() {
        int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            panels.forEach(component -> component.y -= 10);
        } else if (dWheel > 0) {
            panels.forEach(component -> component.y += 10);
        }
    }
}

