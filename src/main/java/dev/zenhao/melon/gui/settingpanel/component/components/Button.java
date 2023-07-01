package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.gui.settingpanel.component.ActionEventListener;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.utils.render.RenderUtils;

public class Button
        extends AbstractComponent {
    private static final int PREFERRED_WIDTH = 180;
    private static final int PREFERRED_HEIGHT = 22;
    private String title;
    private final int preferredWidth;
    private final int preferredHeight;
    private ActionEventListener listener;

    public Button(String title, int preferredWidth, int preferredHeight) {
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.setWidth(preferredWidth);
        this.setHeight(preferredHeight);
        this.setTitle(title);
    }

    public Button(String title) {
        this(title, PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }

    @Override
    public void render() {
        RenderUtils.drawRoundedRectangle(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, this.hovered ? Window.SECONDARY_FOREGROUND : Window.TERTIARY_FOREGROUND);
        if (GuiManager.getINSTANCE().isRainbow()) {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
        } else {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, Window.SECONDARY_OUTLINE);
        }
        Window.getFontRenderer().drawString(this.title, (float)this.x + (float)this.getWidth() / 2.0f - (float)Window.getFontRenderer().getStringWidth(this.title) / 2.0f, (float)this.y + (float)this.getHeight() / 2.0f - (float)Window.getFontRenderer().getHeight() / 2.0f, Window.FONT.getRGB());
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        if (button == 0) {
            if (this.hovered && this.listener != null) {
                this.listener.onActionEvent();
                return true;
            }
        }
        return super.mousePressed(button, x, y, offscreen);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.setWidth(Math.max(Window.getFontRenderer().getStringWidth(title), this.preferredWidth));
        this.setHeight(Math.max(Window.getFontRenderer().getHeight() * 5 / 4, this.preferredHeight));
    }

    public ActionEventListener getOnClickListener() {
        return this.listener;
    }

    public void setOnClickListener(ActionEventListener listener) {
        this.listener = listener;
    }
}

