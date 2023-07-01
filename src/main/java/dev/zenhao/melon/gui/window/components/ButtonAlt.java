package dev.zenhao.melon.gui.window.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.gui.settingpanel.component.ActionEventListener;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.utils.render.FadeUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

public class ButtonAlt extends AbstractComponent {
    private final FadeUtils fadeUtils = new FadeUtils(470L);
    private final int preferredWidth;
    private final int preferredHeight;
    private String title;
    private ActionEventListener listener;

    public ButtonAlt(String title, int preferredWidth, int preferredHeight) {
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.setWidth(preferredWidth);
        this.setHeight(preferredHeight);
        this.setTitle(title);
        this.fadeUtils.reset();
    }

    public void reset() {
        this.fadeUtils.reset();
    }

    @Override
    public void render() {
        double offset = (60 + this.getWidth()) * fadeUtils.getFade(FadeUtils.FadeMode.FADE_OUT);
        RenderUtils.drawRoundedRectangle((-offset) + this.x, this.y, this.getWidth(), this.getHeight(), 3.0, this.hovered ? Window.SECONDARY_FOREGROUND : Window.TERTIARY_FOREGROUND);
        RenderUtils.drawRoundedRectangleOutline((float) ((-offset) + this.x), this.y, this.getWidth(), this.getHeight(), 3.0f, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
        Minecraft.getMinecraft().fontRenderer.drawString(this.title, (int) ((-offset) + this.x + this.getWidth() / 2.0f - Window.getFontRenderer().getStringWidth(this.title) / 2.0f), (int) (this.y + this.getHeight() / 2.0f - Window.getFontRenderer().getHeight() / 2.0), Window.FONT.getRGB());
    }

    @Override
    public boolean mouseMove(int x, int y, boolean offscreen) {
        this.updateHovered(x, y, offscreen);
        return false;
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        if (button == 0) {
            this.updateHovered(x, y, offscreen);
            if (this.hovered && this.listener != null) {
                this.listener.onActionEvent();
                return true;
            }
        }
        return false;
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
