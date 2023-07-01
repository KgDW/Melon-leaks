package dev.zenhao.melon.gui.clickgui.component;

import dev.zenhao.melon.gui.clickgui.Panel;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.module.HUDModule;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.module.modules.client.Colors;
import dev.zenhao.melon.setting.*;
import dev.zenhao.melon.utils.TimerUtils;
import dev.zenhao.melon.utils.font.CFontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton extends Component {
    public List<Component> settings = new ArrayList<>();
    public IModule module;
    public TimerUtils buttonTimerUtils = new TimerUtils();
    int x2;
    int y2;
    int fade = 0;
    boolean dragging;

    public ModuleButton(IModule module, int width, int height, Panel father) {
        this.module = module;
        this.width = width;
        this.height = height;
        this.father = father;
        this.setup();
    }

    public void setup() {
        try {
            for (Setting<?> value : this.module.getSettingList()) {
                if (value instanceof BooleanSetting) {
                    this.settings.add(new BooleanButton((BooleanSetting) value, this.width, this.height, this.father));
                }
                if (value instanceof BindSetting) {
                    this.settings.add(new BindButton<>(this.module, this.width, this.height, this.father));
                }
                if (value instanceof IntegerSetting || value instanceof FloatSetting || value instanceof DoubleSetting) {
                    this.settings.add(new NumberSlider<>(value, this.width, this.height, this.father));
                }
                if (value instanceof ModeSetting) {
                    this.settings.add(new ModeButton((ModeSetting<?>) value, this.width, this.height, this.father));
                }
                if (value instanceof StringSetting) {
                    this.settings.add(new TextButton((StringSetting) value, this.width, this.height, this.father));
                }
                if (!(value instanceof ColorSetting)) continue;
                this.settings.add(new ColorPicker((ColorSetting) value, this.father, this.width, this.height, 50));
            }
            this.settings.add(new BindButton<>(this.module, this.width, this.height, this.father));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        CFontRenderer font = GuiManager.getINSTANCE().getFont();
        if (this.dragging) {
            ((HUDModule) this.module).onDragging(mouseX, mouseY);
        }
        this.solveHUDPos(mouseX, mouseY);
        int color = GuiManager.getINSTANCE().isRainbow() ? GuiManager.getINSTANCE().getRainbowColorAdd((long) this.add) : GuiManager.getINSTANCE().getRGB();
        int fontColor = new Color(255, 255, 255).getRGB();
        int newColor;
        if (this.isHovered(mouseX, mouseY)) {
            //TODO 更新渐变颜色
            fade = fade < 255 ? fade + 5 : 255;
            if (fade > 255) {
                fade = 255;
            }
            color = (color & 0x7F7F7F) << 1;
        } else {
            fade = 0;
        }
        newColor = mix(Color.BLACK, Colors.getINSTANCE().fadeColor.getValue(), fade / 255F).getRGB();
        Gui.drawRect(x, y - 1, x + width, y + height + 1, newColor);
        //Gui.drawRect(this.x, this.y - 1, this.x + this.width, this.y + this.height + 1, newColor);
        int finalColor = color;
        font.drawString(module.getName(), this.x + 2, (int) ((float) (this.y + this.height / 2) - (float) font.getHeight() / 2.0f) + 2, this.module.isEnabled() ? finalColor : fontColor);

        GL11.glPushMatrix();
        GL11.glTranslated((double) (this.x + this.width) - (double) this.height / 2.0 - 3.0, (double) this.y + (double) this.height / 2.0, 0.0);
        GL11.glPopMatrix();
    }

    private Color mix(Color from, Color to, Float ratio) {
        float rationSelf = 1.0f - ratio;
        return new Color(
                (int) (from.getRed() * rationSelf + to.getRed() * ratio),
                (int) (from.getGreen() * rationSelf + to.getGreen() * ratio),
                (int) (from.getBlue() * rationSelf + to.getBlue() * ratio),
                (int) (from.getAlpha() * rationSelf + to.getAlpha() * ratio)
        );
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.module.isHUD && mouseButton == 0 && this.isHoveredHUD(mouseX, mouseY)) {
            this.x2 = this.module.x - mouseX;
            this.y2 = this.module.y - mouseY;
            this.dragging = true;
            return true;
        }
        if (!this.isHovered(mouseX, mouseY)) {
            return false;
        }
        if (mouseButton == 0) {
            this.module.toggle();
        } else if (mouseButton == 1) {
            this.isExtended = !this.isExtended;
            this.buttonTimerUtils.reset();
        }
        return true;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (state == 0 && this.module.isHUD) {
            ((HUDModule) this.module).onMouseRelease();
            this.dragging = false;
        }
        for (dev.zenhao.melon.gui.clickgui.component.Component setting : this.settings) {
            setting.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        for (Component setting : this.settings) {
            setting.keyTyped(typedChar, keyCode);
        }
    }

    public void solveHUDPos(int mouseX, int mouseY) {
        if (this.module.isHUD && this.dragging) {
            this.module.x = this.x2 + mouseX;
            this.module.y = this.y2 + mouseY;
        }
        if (this.module.isHUD && !this.dragging) {
            if (Math.min(this.module.x, this.module.x + this.module.width) < 0) {
                this.module.x = this.module.x < this.module.x + this.module.width ? 0 : -this.module.width;
            }
            if (Math.max(this.module.x, this.module.x + this.module.width) > this.mc.displayWidth / 2) {
                this.module.x = this.module.x < this.module.x + this.module.width ? this.mc.displayWidth / 2 - this.module.width : this.mc.displayWidth / 2;
            }
            if (Math.min(this.module.y, this.module.y + this.module.height) < 0) {
                this.module.y = this.module.y < this.module.y + this.module.height ? 0 : -this.module.height;
            }
            if (Math.max(this.module.y, this.module.y + this.module.height) > this.mc.displayHeight / 2) {
                this.module.y = this.module.y < this.module.y + this.module.height ? this.mc.displayHeight / 2 - this.module.height : this.mc.displayHeight / 2;
            }
        }
    }

    public boolean isHoveredHUD(int mouseX, int mouseY) {
        return mouseX >= Math.min(this.module.x, this.module.x + this.module.width) && mouseX <= Math.max(this.module.x, this.module.x + this.module.width) && mouseY >= Math.min(this.module.y, this.module.y + this.module.height) && mouseY <= Math.max(this.module.y, this.module.y + this.module.height);
    }
}

