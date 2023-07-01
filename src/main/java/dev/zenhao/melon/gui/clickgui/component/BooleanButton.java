package dev.zenhao.melon.gui.clickgui.component;

import dev.zenhao.melon.gui.clickgui.Panel;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.setting.BooleanSetting;
import dev.zenhao.melon.utils.font.CFontRenderer;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class BooleanButton
extends SettingButton<Boolean> {
    public BooleanButton(BooleanSetting value, int width, int height, Panel father) {
        this.width = width;
        this.height = height;
        this.father = father;
        this.setValue(value);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        CFontRenderer font = GuiManager.getINSTANCE().getFont();
        int color = GuiManager.getINSTANCE().isRainbow() ? GuiManager.getINSTANCE().getRainbowColorAdd((long)this.add) : GuiManager.getINSTANCE().getRGB();
        int fontColor = 0x909090;
        Gui.drawRect(x, y, x + width, y + height, -2063597568);
        int c = this.getValue().getValue() ? color : fontColor;
        if (this.isHovered(mouseX, mouseY)) {
            if (GuiManager.getINSTANCE().isRainbow()){
                c = (c & 0x7F7F7F) << 1;
            }else {
                c = new Color(255, 255, 255).getRGB();
            }
        }
        BooleanSetting booleanValue = (BooleanSetting)this.getValue();
        font.drawString(booleanValue.getName(), this.x + 3, (int)((float)(this.y + this.height / 2) - (float)font.getHeight() / 2.0f) + 2, c);
        RenderUtils.drawCircle(this.x + this.width - (this.height / 2D), this.y + this.height / 2D, (this.height - 8) / 2D, new Color(c));
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!this.getValue().visible() || !this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (mouseButton == 0) {
            this.getValue().setValue(!this.getValue().getValue());
        }
        return true;
    }
}

