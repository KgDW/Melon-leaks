package dev.zenhao.melon.gui.clickgui.component;

import dev.zenhao.melon.gui.clickgui.Panel;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.setting.ModeSetting;
import dev.zenhao.melon.utils.font.CFontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class ModeButton extends SettingButton<Enum> {
    public ModeButton(ModeSetting value, int width, int height, Panel father) {
        this.width = width;
        this.height = height;
        this.father = father;
        this.setValue(value);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        CFontRenderer font = GuiManager.getINSTANCE().getFont();
        int fontColor = 0x909090;
        Gui.drawRect(x, y, x + width, y + height, -2063597568);
        font.drawString(this.getAsModeValue().getName(), this.x + 3, (int) ((float) (this.y + this.height / 2) - (float) font.getHeight() / 2.0f) + 2, new Color(255, 255, 255).getRGB());
        font.drawString(this.getAsModeValue().getValueAsString(), this.x + this.width - 1 - font.getStringWidth(this.getAsModeValue().getValueAsString()), (int) ((float) (this.y + this.height / 2) - (float) font.getHeight() / 2.0f) + 2, this.isHovered(mouseX, mouseY) ? new Color(255, 255, 255).getRGB() : new Color(155, 155, 155).getRGB());
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!this.isHovered(mouseX, mouseY) || !this.getValue().visible()) {
            return false;
        }
        if (mouseButton == 0) {
            this.getAsModeValue().forwardLoop();
        }
        return true;
    }
}

