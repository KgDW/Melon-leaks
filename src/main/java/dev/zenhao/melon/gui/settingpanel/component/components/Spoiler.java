package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.gui.settingpanel.layout.GridLayout;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.*;
import dev.zenhao.melon.utils.render.FadeUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class Spoiler
        extends AbstractComponent {

    public final IModule module;
    private final dev.zenhao.melon.gui.settingpanel.component.components.Pane settingsPanel = new dev.zenhao.melon.gui.settingpanel.component.components.Pane(new GridLayout(4));
    private final FadeUtils fadeHeight = new FadeUtils(130L).end();
    public int preferredWidth;
    public int preferredHeight;
    public int lastmousex = 0;
    public int lastmousey = 0;
    private String title;
    private boolean opened = false;

    public Spoiler(IModule module, int preferredWidth, int preferredHeight) {
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.module = module;
        this.setTitle(module.getName());
        setup();
    }

    private void setup() {
        settingsPanel.addComponent(new dev.zenhao.melon.gui.settingpanel.component.components.Label("Keybinding"));
        settingsPanel.addComponent(new dev.zenhao.melon.gui.settingpanel.component.components.KeybindButton((Module) module));
        for (Setting value : this.module.getSettingList()) {
            settingsPanel.addComponent(new dev.zenhao.melon.gui.settingpanel.component.components.Label(value.getName()) {
                @Override
                public boolean isVisible() {
                    return value.visible();
                }
            });
            if (value instanceof BooleanSetting) {
                settingsPanel.addComponent(new dev.zenhao.melon.gui.settingpanel.component.components.CheckBox((BooleanSetting) value).setFatherPanel(settingsPanel));
            } else if (value instanceof IntegerSetting || value instanceof FloatSetting || value instanceof DoubleSetting) {
                settingsPanel.addComponent(new Slider(value).setFatherPanel(settingsPanel));
            } else if (value instanceof ModeSetting) {
                settingsPanel.addComponent(new dev.zenhao.melon.gui.settingpanel.component.components.ComboBox((ModeSetting<?>) value).setFatherPanel(settingsPanel));
            } else if (value instanceof ColorSetting) {
                settingsPanel.addComponent(new dev.zenhao.melon.gui.settingpanel.component.components.ColorSlider((ColorSetting) value).setFatherPanel(settingsPanel));
            } else if (value instanceof StringSetting) {
                settingsPanel.addComponent(new TextField((StringSetting) value).setFatherPanel(settingsPanel));
            } else {
                settingsPanel.addComponent(new dev.zenhao.melon.gui.settingpanel.component.components.Label("Cannot Certification This Setting Pls Report To Dev"));
            }
        }
        settingsPanel.updateSize();
    }

    @Override
    public void render() {
        if (this.module.isEnabled()) {
            RenderUtils.drawRoundedRectangle(this.x, this.y, this.getWidth(), this.preferredHeight, 7.0, Window.ENABLE);
        } else if (this.hovered) {
            RenderUtils.drawRoundedRectangle(this.x, this.y, this.getWidth(), this.preferredHeight, 7.0, Window.SECONDARY_FOREGROUND);
        }
        this.updateBounds();
        if (this.opened) {
            if (fadeHeight.isEnd()) {
                this.settingsPanel.setX(this.getX());
                this.settingsPanel.setY(this.getY() + this.preferredHeight);
                this.settingsPanel.render();
            }
        }
        if (!fadeHeight.isEnd() || this.opened) {
            if (GuiManager.getINSTANCE().isRainbow()) {
                RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.preferredHeight, 7.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
                RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
            } else {
                RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.preferredHeight, 7.0, 1.0f, Window.SECONDARY_OUTLINE);
                RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, Window.SECONDARY_OUTLINE);
            }
        } else {
            if (GuiManager.getINSTANCE().isRainbow()) {
                RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.preferredHeight, 7.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
            } else {
                RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.preferredHeight, 7.0, 1.0f, Window.SECONDARY_OUTLINE);
            }
        }
        Window.getFontRenderer().drawString(this.title, (float) this.x + (float) this.getWidth() / 2.0f - (float) Window.getFontRenderer().getStringWidth(this.title) / 2.0f, (float) this.y + (float) this.preferredHeight / 2.0f - (float) Window.getFontRenderer().getHeight() / 2.0f, Window.FONT.getRGB());
        String description = this.module.description;
        if (this.hovered && !description.equals("")) {
            float widthD = Window.getFontRenderer().getStringWidth(description) + 15;
            float heightD = Window.getFontRenderer().getHeight() + 5;
            float xd = this.lastmousex + 7;
            float yd = (float) this.lastmousey - heightD / 2.0f;
            RenderUtils.drawRoundedRectangle(xd, yd, widthD, heightD, 2.0, new Color(Window.FOREGROUND.getRed(), Window.FOREGROUND.getGreen(), Window.FOREGROUND.getBlue(), 191));
            Window.getFontRenderer().drawCenteredString(description, xd + widthD / 2.0f, yd + heightD / 2.0f, Window.FONT.getRGB());
        }
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
        fadeHeight.reset();
    }

    public dev.zenhao.melon.gui.settingpanel.component.components.Pane getSettingsPanel() {
        return settingsPanel;
    }

    @Override
    public boolean mouseMove(int x, int y, boolean offscreen) {
        this.lastmousey = y;
        this.lastmousex = x;
        this.updateHovered(x, y, offscreen);
        return this.opened && this.settingsPanel.mouseMove(x, y, offscreen);
    }

    protected void updateHovered(int x, int y, boolean offscreen) {
        this.hovered = !offscreen && x >= this.x && y >= this.y && x <= this.x + this.getWidth() && y <= this.y + this.preferredHeight;
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        this.updateHovered(x, y, offscreen);
        if (this.hovered) {
            if (button == 0) {
                if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().world != null) {
                    module.toggle();
                }
                return false;
            } else if (button == 1) {
                this.opened = !this.opened;
                fadeHeight.reset();
                this.settingsPanel.updateLayout();
                this.settingsPanel.updateSize();
                this.updateBounds();
                return true;
            }
        }
        if (!offscreen && x >= this.x && y >= this.y && x <= this.x + this.getWidth() && y <= this.y + this.preferredHeight) {
            if (button == 1) {
                this.settingsPanel.updateLayout();
            }
        }
        return this.opened && this.settingsPanel.mousePressed(button, x, y, offscreen);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updateBounds();
    }

    private void updateBounds() {
        this.setWidth(Math.max(Math.max(Window.getFontRenderer().getStringWidth(this.getTitle()), this.settingsPanel.getWidth()), this.preferredWidth));
        double fade = fadeHeight.getFade(FadeUtils.FadeMode.FADE_ONE);
        if (this.opened) {
            this.setHeight(this.preferredHeight + (int) (this.settingsPanel.getHeight() * fade));
        } else {
            this.setHeight(this.preferredHeight + (int) (this.settingsPanel.getHeight() * (1 - fade)));
        }
    }

    @Override
    public boolean mouseReleased(int button, int x, int y, boolean offscreen) {
        updateHovered(x, y, offscreen);
        return this.opened && this.settingsPanel.mouseReleased(button, x, y, offscreen);
    }

    @Override
    public boolean mouseWheel(int change) {
        return this.opened && this.settingsPanel.mouseWheel(change);
    }

    @Override
    public boolean keyPressed(char c, int key) {
        return this.opened && this.settingsPanel.keyPressed(c, key);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(Math.max(width, this.settingsPanel.getWidth()));
    }

    @Override
    public boolean isVisible() {
        return !this.module.isHidden();
    }
}

