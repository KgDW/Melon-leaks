package dev.zenhao.melon.gui.clickgui.guis;

import dev.zenhao.melon.gui.clickgui.HUDRender;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.client.HUDEditor;
import dev.zenhao.melon.utils.Wrapper;
import dev.zenhao.melon.utils.BackgroundEffect.particle.ParticleSystem;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class HUDEditorScreen
        extends GuiScreen {
    private final ParticleSystem particleSystem = new ParticleSystem(100);

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void initGui() {
        if (GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Blur) || GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Both)) {
            if (Wrapper.getMinecraft().entityRenderer.getShaderGroup() != null) {
                Wrapper.getMinecraft().entityRenderer.getShaderGroup().deleteShaderGroup();
            }
            Wrapper.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
    }

    public void onGuiClosed() {
        if (Wrapper.getMinecraft().entityRenderer.getShaderGroup() != null) {
            Wrapper.getMinecraft().entityRenderer.getShaderGroup().deleteShaderGroup();
        }
        if (ModuleManager.getModuleByClass(HUDEditor.class).isEnabled()) {
            ModuleManager.getModuleByClass(HUDEditor.class).disable();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Shadow) || GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Both)) {
            this.drawDefaultBackground();
        }
        if (this.mc.player == null) {
            Gui.drawRect(0, 0, 9999, 9999, new Color(0, 0, 0, 255).getRGB());
        }
        if (GuiManager.getINSTANCE().isParticle()) {
            this.particleSystem.tick(10);
            this.particleSystem.render();
        }
        HUDRender.getINSTANCE().drawScreen(mouseX, mouseY, partialTicks);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        HUDRender.getINSTANCE().mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void keyTyped(char typedChar, int keyCode) {
        HUDRender.getINSTANCE().keyTyped(typedChar, keyCode);
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {
        HUDRender.getINSTANCE().mouseReleased(mouseX, mouseY, state);
    }
}

