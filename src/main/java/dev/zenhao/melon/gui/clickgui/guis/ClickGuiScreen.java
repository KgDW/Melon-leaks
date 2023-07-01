package dev.zenhao.melon.gui.clickgui.guis;

import dev.zenhao.melon.gui.clickgui.GUIRender;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.client.ClickGui;
import dev.zenhao.melon.utils.BackgroundEffect.FakeMeteor.MeteorSystem;
import dev.zenhao.melon.utils.Wrapper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class ClickGuiScreen extends GuiScreen {
    //private final ParticleSystem particleSystem = new ParticleSystem(100);
    private final MeteorSystem meteorSystem = new MeteorSystem(30);

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        if (GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Blur) || GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Both)) {
            if (Wrapper.getMinecraft().entityRenderer.getShaderGroup() != null) {
                Wrapper.getMinecraft().entityRenderer.getShaderGroup().deleteShaderGroup();
            }
            Wrapper.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
    }

    @Override
    public void onGuiClosed() {
        if (Wrapper.getMinecraft().entityRenderer.getShaderGroup() != null) {
            Wrapper.getMinecraft().entityRenderer.getShaderGroup().deleteShaderGroup();
        }
        if (ModuleManager.getModuleByClass(ClickGui.class).isEnabled()) {
            ModuleManager.getModuleByClass(ClickGui.class).disable();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Shadow) || GuiManager.getINSTANCE().getBackground().equals(GuiManager.Background.Both)) {
            this.drawDefaultBackground();
        }
        if (this.mc.player == null) {
            Gui.drawRect(0, 0, 9999, 9999, new Color(0, 0, 0, 255).getRGB());
        }
        if (GuiManager.getINSTANCE().isParticle()) {
            //this.particleSystem.tick(10);
            //this.particleSystem.render();
            this.meteorSystem.setRainbow(GuiManager.INSTANCE.isRainbow());
            this.meteorSystem.tick();
            this.meteorSystem.render();
        }
        GUIRender.getINSTANCE().drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        GUIRender.getINSTANCE().mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        GUIRender.getINSTANCE().keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        GUIRender.getINSTANCE().mouseReleased(mouseX, mouseY, state);
    }

}

