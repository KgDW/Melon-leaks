package dev.zenhao.melon.gui.settingpanel;

import dev.zenhao.melon.Melon;
import dev.zenhao.melon.gui.settingpanel.component.components.Button;
import dev.zenhao.melon.gui.settingpanel.component.components.Pane;
import dev.zenhao.melon.gui.settingpanel.component.components.ScrollPane;
import dev.zenhao.melon.gui.settingpanel.component.components.Spoiler;
import dev.zenhao.melon.gui.settingpanel.layout.GridLayout;
import dev.zenhao.melon.gui.settingpanel.utils.SettingPanelUtils;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.utils.BackgroundEffect.FakeMeteor.MeteorSystem;
import dev.zenhao.melon.utils.Wrapper;
import dev.zenhao.melon.utils.BackgroundEffect.particle.ParticleSystem;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.zenhao.melon.utils.render.FadeUtils;
import dev.zenhao.melon.utils.render.gui.ScalaCalc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class MelonSettingPanel
        extends GuiScreen {
    private final Window window = new Window(Melon.MOD_NAME, 50, 50, 740, 370);
    private final HashMap<Category, Pane> categoryPanel = new HashMap<>();
    private final Pane currentCategory = new Pane(new GridLayout(1));

    private final ScalaCalc oc = new ScalaCalc().setAnimationTime(350).setFadeMode(FadeUtils.FadeMode.FADE_EPS_IN);
    private final ParticleSystem particleSystem = new ParticleSystem(100);
    private final MeteorSystem meteorSystem = new MeteorSystem(50);

    public MelonSettingPanel() {
        Pane buttonPane = new Pane(new GridLayout(1));

        HashMap<Category, List<Module>> moduleCategoryMap = new HashMap<>();
        for (IModule module : ModuleManager.getAllIModules()) {
            if (!(module instanceof Module)) continue;
            if (!moduleCategoryMap.containsKey(module.category) && !module.category.isHidden()) {
                moduleCategoryMap.put(module.category, new ArrayList<>());
            }
            if (module.category.isHidden() || module.isHidden()) continue;
            moduleCategoryMap.get(module.category).add((Module) module);
        }

        ArrayList<Spoiler> spoilerList = new ArrayList<>();

        for (Map.Entry<Category,  List<Module>> moduleCategoryListEntry : moduleCategoryMap.entrySet()) {
            Pane spoilerPane = new Pane(new GridLayout(1));
            for (Module module : moduleCategoryListEntry.getValue()) {
                Spoiler spoiler = new Spoiler(module, 360, 28);
                spoilerList.add(spoiler);
                spoilerPane.addComponent(spoiler);
            }
            this.categoryPanel.put(moduleCategoryListEntry.getKey(), spoilerPane);
        }
        for (Category moduleCategory : this.categoryPanel.keySet()) {
            Button button = new Button(moduleCategory.toString(), 70, 17);
            buttonPane.addComponent(button);
            button.setOnClickListener(() -> this.setCurrentCategory(moduleCategory));
        }
        int maxWidth = Integer.MIN_VALUE;
        for (Spoiler spoiler : spoilerList) {
            maxWidth = Math.max(maxWidth, spoiler.getSettingsPanel().getWidth() + buttonPane.getWidth());
        }
        for (Spoiler spoiler : spoilerList) {
            spoiler.preferredWidth = maxWidth - buttonPane.getWidth();
            spoiler.setWidth(maxWidth - buttonPane.getWidth());
        }
        this.window.setWidth(36 + maxWidth);
        this.currentCategory.setWidth(window.getWidth() - buttonPane.getWidth() - 2);
        ScrollPane scrollPane = new ScrollPane(new GridLayout(1));
        scrollPane.addComponent(this.currentCategory);
        scrollPane.updateLayout();
        this.window.setContentPane(buttonPane);
        this.window.setSpoilerPane(scrollPane);
        if (this.categoryPanel.keySet().size() > 0) {
            this.setCurrentCategory(this.categoryPanel.keySet().iterator().next());
        }
    }

    public void updateWindowSize(List<Spoiler> spoilerList, int buttonPanelWidth){
        int maxWidth = Integer.MIN_VALUE;
        for (Spoiler spoiler : spoilerList) {
            maxWidth = Math.max(maxWidth, spoiler.getSettingsPanel().getWidth() + buttonPanelWidth);
        }
        for (Spoiler spoiler : spoilerList) {
            spoiler.preferredWidth = maxWidth - buttonPanelWidth;
            spoiler.setWidth(maxWidth - buttonPanelWidth);
        }
        this.window.setWidth(28 + maxWidth);
        this.currentCategory.setWidth(window.getWidth() - buttonPanelWidth - 2);
    }

    public void initGui() {
        if (OpenGlHelper.shadersSupported && Wrapper.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            if (Wrapper.getMinecraft().entityRenderer.getShaderGroup() != null) {
                Wrapper.getMinecraft().entityRenderer.getShaderGroup().deleteShaderGroup();
            }
            Wrapper.getMinecraft().entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
        oc.reset();
    }

    public void onGuiClosed() {
        if (Wrapper.getMinecraft().entityRenderer.getShaderGroup() != null) {
            Wrapper.getMinecraft().entityRenderer.getShaderGroup().deleteShaderGroup();
        }
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClearDepth(1.0);
        GL11.glClear(16640);
        try {
            Point point = SettingPanelUtils.calculateMouseLocation();
            int mX = (int) ((double) point.x);
            int mY = (int) ((double) point.y);
            this.window.mouseReleased(1, mX, mY);
            this.window.mouseReleased(0, mX, mY);
        }catch (Exception ignore){}
    }

    private void setCurrentCategory(Category category) {
        this.currentCategory.clearComponents();
        this.currentCategory.addComponent(this.categoryPanel.get(category));
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawDefaultBackground();
        //this.particleSystem.tick(3);
        this.meteorSystem.setRainbow(GuiManager.INSTANCE.isRainbow());
        this.meteorSystem.tick();
        Point point = SettingPanelUtils.calculateMouseLocation();
        int mX = (int)((double)point.x);
        int mY = (int)((double)point.y);
        this.window.mouseMoved(mX, mY);
        //this.particleSystem.render();
        this.meteorSystem.render();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glPushMatrix();
        oc.drawA(sr);
        this.window.render();
        oc.drawB(sr);
        GL11.glPopMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.window.mouseMoved(mouseX, mouseY);
        this.window.mousePressed(mouseButton, mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.window.mouseMoved(mouseX, mouseY);
        this.window.mouseReleased(state, mouseX, mouseY);
        super.mouseReleased(mouseX, mouseY, state);
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        this.window.mouseMoved(mouseX, mouseY);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int eventDWheel = Mouse.getEventDWheel();
        this.window.mouseWheel(eventDWheel);
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.window.keyPressed(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }
}

