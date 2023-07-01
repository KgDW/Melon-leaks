package dev.zenhao.melon.gui.clickgui.component;

import dev.zenhao.melon.gui.clickgui.Panel;
import dev.zenhao.melon.setting.StringSetting;
import dev.zenhao.melon.utils.KeyboardUtilsJava;
import dev.zenhao.melon.utils.TimerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class TextButton extends SettingButton<String> {
    TimerUtils timerUtils = new TimerUtils();
    private String TypeDir = "";
    private boolean typing;

    public TextButton(StringSetting value, int width, int height, Panel father) {
        this.width = width;
        this.height = height;
        this.father = father;
        setValue(value);
    }

    public static String removeLastChar(String str) {
        String output = "";
        if (str != null && str.length() > 0) {
            output = str.substring(0, str.length() - 1);
        }
        return output;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        Gui.drawRect(x, y, x + width, y + height, -2063597568);
        Minecraft.getMinecraft().fontRenderer.drawString(getValue().getValue() + (this.typing ? "..." : ""), x + width / 2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(getValue().getValue() + (this.typing ? "..." : "")) / 2, y + height / 2 - Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT / 2, new Color(191, 255, 0).getRGB());
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!getValue().visible() || !isHovered(mouseX, mouseY)) {
            return false;
        }
        typing = mouseButton == 0;
        return true;
    }

    @Override
    public void keyTyped(char c, int key) {
        if (this.typing) {
            if (key == 211 || key == 14) {
                if (this.timerUtils.passed(700L)) {
                    this.TypeDir = removeLastChar(this.TypeDir);
                    this.timerUtils.reset();
                } else {
                    this.TypeDir = removeLastChar(this.TypeDir);
                }
            } else if (key == 28) {
                this.typing = false;
            } else if (!String.valueOf(c).contains("\u0000")) {
                this.TypeDir = this.TypeDir + c;
            } else if (KeyboardUtilsJava.isCtrlDown() && KeyboardUtilsJava.isDown(47)) {
                this.TypeDir = this.TypeDir + KeyboardUtilsJava.getClipboardString();
            }
            this.update();
        }
    }

    public void changeValue(String newValue) {
        getValue().setValue(newValue);
    }

    public void update() {
        if (getValue().visible()) changeValue(TypeDir);
    }
}