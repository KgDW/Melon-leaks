package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractSettingComponent;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.setting.StringSetting;
import dev.zenhao.melon.utils.KeyboardUtilsJava;
import dev.zenhao.melon.utils.TimerUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;

public class TextField extends AbstractSettingComponent<String> {
    private static final int PREFERRED_WIDTH = 180;
    private static final int PREFERRED_HEIGHT = 22;
    private final int preferredWidth;
    private final int preferredHeight;
    TimerUtils timerUtils = new TimerUtils();
    private boolean typing;
    private String TypeDir = "";

    public TextField(StringSetting value, int preferredWidth, int preferredHeight) {
        this.preferredWidth = preferredWidth;
        this.preferredHeight = preferredHeight;
        this.setValue(value);
        this.setWidth(preferredWidth);
        this.setHeight(preferredHeight);
    }

    public TextField(StringSetting value) {
        this(value, PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public void render() {
        RenderUtils.drawRoundedRectangle(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, this.hovered ? Window.TERTIARY_FOREGROUND : Window.SECONDARY_FOREGROUND);
        if (GuiManager.getINSTANCE().isRainbow()) {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
        } else {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, this.hovered ? Window.TERTIARY_OUTLINE : Window.SECONDARY_OUTLINE);
        }
        Minecraft.getMinecraft().fontRenderer.drawString(this.getValue().getValue() + (this.typing ? "..." : ""), this.x + this.getWidth() / 2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(this.getValue().getValue() + (this.typing ? "..." : "")) / 2, this.y + this.getHeight() / 2 - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2, Window.FONT.getRGB());
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
            if (this.hovered) {
                this.typing = !this.typing;
                this.TypeDir = this.getValue().getValue();
                this.update();
                return true;
            }
        }
        return this.typing;
    }

    @Override
    public boolean keyPressed(char c, int key) {
        if (this.typing) {
            if (key == 211 || key == 14) {
                if (this.timerUtils.passed(700L)) {
                    this.TypeDir = TextField.removeLastChar(this.TypeDir);
                    this.timerUtils.reset();
                } else {
                    this.TypeDir = TextField.removeLastChar(this.TypeDir);
                }
            } else if (key == 28) {
                this.typing = false;
                ((dev.zenhao.melon.gui.settingpanel.component.components.Pane) getFatherPanel()).updateLayout();
            } else if (!String.valueOf(c).contains("\u0000")) {
                this.TypeDir = this.TypeDir + c;
            } else if (KeyboardUtilsJava.isCtrlDown() && KeyboardUtilsJava.isDown(47)) {
                this.TypeDir = this.TypeDir + KeyboardUtilsJava.getClipboardString();
            }
            this.update();
        }
        return this.typing;
    }

    public void changeValue(String newValue) {
        this.getValue().setValue(newValue);
    }

    public void update() {
        if (this.getValue().visible()) this.changeValue(this.TypeDir);
    }

    public void setTitle(String title) {
        this.setWidth(Math.max(Window.getFontRenderer().getStringWidth(title), this.preferredWidth));
        this.setHeight(Math.max(Window.getFontRenderer().getHeight() * 5 / 4, this.preferredHeight));
    }

    @Override
    public boolean isVisible() {
        return this.getValue().visible();
    }
}

