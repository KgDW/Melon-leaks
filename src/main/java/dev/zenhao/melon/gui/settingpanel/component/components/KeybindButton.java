package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.utils.render.RenderUtils;
import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.module.Module;

import org.lwjgl.input.Keyboard;

public class KeybindButton
        extends AbstractComponent {

    private static final int PREFERRED_WIDTH = 180;
    private static final int PREFERRED_HEIGHT = 22;
    private boolean listening;
    private final Module module;

    public KeybindButton(int preferredWidth, int preferredHeight, Module module) {
        this.setWidth(preferredWidth);
        this.setHeight(preferredHeight);
        this.module = module;
    }

    public KeybindButton(Module module) {
        this(PREFERRED_WIDTH, PREFERRED_HEIGHT, module);
    }

    @Override
    public boolean keyPressed(char typedChar, int keyCode) {
        if (this.listening) {
            if (keyCode == Keyboard.KEY_BACK) {
                this.module.setBind(0);
            } else {
                this.module.setBind(keyCode);
            }
            this.listening = false;
        }
        return super.keyPressed(typedChar, keyCode);
    }

    @Override
    public void render() {
        RenderUtils.drawRoundedRectangle(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, this.hovered ? Window.SECONDARY_FOREGROUND : Window.TERTIARY_FOREGROUND);
        if (GuiManager.getINSTANCE().isRainbow()) {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
        } else {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, Window.SECONDARY_OUTLINE);
        }
        String text = this.listening ? "Bind | ..." : "Bind | " + (this.module.getBind() == 0 ? "NONE" : Keyboard.getKeyName(this.module.getBind()));
        Window.getFontRenderer().drawString(text, (float)this.x + (float)this.getWidth() / 2.0f - (float)Window.getFontRenderer().getStringWidth(text) / 2.0f, (float)this.y + (float)this.getHeight() / 2.0f - (float)Window.getFontRenderer().getHeight() / 2.0f, Window.FONT.getRGB());
    }

    @Override
    public int getEventPriority() {
        return this.listening ? super.getEventPriority() + 1 : super.getEventPriority();
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        updateHovered(x, y, offscreen);
        if (hovered && button == 0) {
            this.listening = true;
        }
        return this.listening;
    }
}

