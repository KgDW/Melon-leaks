package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractSettingComponent;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.setting.BooleanSetting;
import dev.zenhao.melon.utils.render.RenderUtils;
import java.awt.Color;

public class CheckBox
        extends AbstractSettingComponent<Boolean> {
    private static final int PREFERRED_SIZE = 22;
    private final int preferredSize;

    public CheckBox(BooleanSetting value, int preferredSize) {
        this.preferredSize = preferredSize;
        setValue(value);
        this.setWidth(Window.getFontRenderer().getStringWidth(value.getName()) + this.preferredSize + this.preferredSize / 4);
        this.setHeight(this.preferredSize);
    }

    public CheckBox(BooleanSetting value) {
        this(value, PREFERRED_SIZE);
    }

    @Override
    public void render() {
        RenderUtils.drawRoundedRectangle(this.x, this.y, this.preferredSize, this.preferredSize, 7.0, this.hovered ? Window.SECONDARY_FOREGROUND : Window.TERTIARY_FOREGROUND);
        Color color2 = GuiManager.getINSTANCE().isRainbow() ? new Color(GuiManager.getINSTANCE().getRainbow()) : (this.hovered ? Window.TERTIARY_FOREGROUND : Window.SECONDARY_FOREGROUND);
        if (this.getValue().getValue()) {
            RenderUtils.drawRoundedRectangle(this.x + 3, this.y + 3, this.preferredSize - 6, this.preferredSize - 6, 3.0, color2);
        }
        Color colorHovered = GuiManager.getINSTANCE().isRainbow() ? new Color(GuiManager.getINSTANCE().getRainbow()) : (this.hovered ? Window.TERTIARY_OUTLINE : Window.SECONDARY_OUTLINE);
        RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.preferredSize, this.preferredSize, 7.0, 1.0f, colorHovered);
        Window.getFontRenderer().drawString(this.getValue().getName(), (float)(this.x + this.preferredSize) + (float)this.preferredSize / 2.0f, (float)this.y + (float)this.preferredSize / 2.0f - (float)Window.getFontRenderer().getHeight() / 2.0f, Window.FONT.getRGB());
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        updateHovered(x, y, offscreen);
        if (!this.getValue().visible() || !hovered) {
            return false;
        }
        if (button == 0) {
            this.getValue().setValue(!this.getValue().getValue());
            ((Pane) getFatherPanel()).updateLayout();
        }
        return true;
    }

    @Override
    public boolean isVisible() {
        return this.getValue().visible();
    }
}

