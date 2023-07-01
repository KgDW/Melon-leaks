package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractSettingComponent;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.setting.DoubleSetting;
import dev.zenhao.melon.setting.FloatSetting;
import dev.zenhao.melon.setting.IntegerSetting;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.render.RenderUtils;
import net.minecraft.util.math.MathHelper;

public class Slider<T>
        extends AbstractSettingComponent<T> {

    private static final int PREFERRED_WIDTH = 180;
    private static final int PREFERRED_HEIGHT = 24;
    private boolean changing = false;

    public Slider(Setting<T> value, int width, int height) {
        setValue(value);
        this.setWidth(width);
        this.setHeight(height);
    }

    public Slider(Setting<T> value) {
        this(value, PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }

    @Override
    public void render() {
        int sliderWidth = 4;
        double iwidth = 0.0;
        String displayvalue = "0";

        if (GuiManager.getINSTANCE().isRainbow()) {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 4.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
        } else {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 4.0, 1.0f, this.hovered || this.changing ? Window.SECONDARY_OUTLINE : Window.TERTIARY_OUTLINE);
        }
        if (this.getValue() instanceof DoubleSetting) {
            DoubleSetting doubleValue = (DoubleSetting)this.getValue();
            displayvalue = String.format("%.1f", doubleValue.getValue());
            iwidth = (this.getWidth()-4) * (doubleValue.getValue() - doubleValue.getMin()) / (doubleValue.getMax() - doubleValue.getMin());;
        } else if (this.getValue() instanceof FloatSetting) {
            FloatSetting floatValue = (FloatSetting)this.getValue();
            displayvalue = String.format("%.1f", floatValue.getValue());
            iwidth = (this.getWidth()-4) * (floatValue.getValue() - floatValue.getMin()) / (floatValue.getMax() - floatValue.getMin());
        } else if (this.getValue() instanceof IntegerSetting) {
            IntegerSetting intValue = (IntegerSetting)this.getValue();
            displayvalue = String.valueOf(intValue.getValue());
            iwidth = (this.getWidth()-6) * (double)(intValue.getValue() - intValue.getMin()) / (double)(intValue.getMax() - intValue.getMin());;
        }
        if (GuiManager.getINSTANCE().isRainbow()) {
            RenderUtils.drawRoundedRectangle((double)this.x + iwidth, this.y + 2, sliderWidth, this.getHeight() - 3, 2.0, GuiManager.getINSTANCE().getRainbowColor());
        } else {
            RenderUtils.drawRoundedRectangle((double)this.x + iwidth, this.y + 2, sliderWidth, this.getHeight() - 3, 2.0, this.hovered || this.changing ? Window.TERTIARY_FOREGROUND : Window.SECONDARY_FOREGROUND);
        }
        Window.getFontRenderer().drawString(displayvalue, (float)this.x + (float)this.getWidth() / 2.0f - (float)Window.getFontRenderer().getStringWidth(displayvalue) / 2.0f, (float)this.y + (float)this.getHeight() / 2.0f - (float)Window.getFontRenderer().getHeight() / 4.0f, Window.FONT.getRGB());
    }

    @Override
    public boolean mouseMove(int x, int y, boolean offscreen) {
        this.updateHovered(x, y, offscreen);
        if (!this.getValue().visible()) {
            this.changing = false;
        }
        this.updateValue(x, y);
        return this.changing;
    }

    private void updateValue(int x, int y) {
        if (this.changing) {
            double diff;
            if (this.getValue() instanceof DoubleSetting) {
                DoubleSetting doubleValue = (DoubleSetting)this.getValue();
                diff = doubleValue.getMax() - doubleValue.getMin();
                double val = doubleValue.getMin() + MathHelper.clamp(((double)x - (double)(this.x + 1)) / (double)this.getWidth(), 0.0, 1.0) * diff;
                doubleValue.setValue(val);
            } else if (this.getValue() instanceof FloatSetting) {
                FloatSetting floatValue = (FloatSetting)this.getValue();
                diff = floatValue.getMax() - floatValue.getMin();
                double val = (double) floatValue.getMin() + MathHelper.clamp(((double)x - (double)(this.x + 1)) / (double)this.getWidth(), 0.0, 1.0) * diff;
                floatValue.setValue((float) val);
            } else if (this.getValue() instanceof IntegerSetting) {
                IntegerSetting intValue = (IntegerSetting)this.getValue();
                diff = intValue.getMax() - intValue.getMin();
                double val = (double) intValue.getMin() + MathHelper.clamp(((double)x - (double)(this.x + 1)) / (double)this.getWidth(), 0.0, 1.0) * diff;
                intValue.setValue((int)val);
            }
        }
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        updateHovered(x, y, offscreen);
        if (!this.getValue().visible() || !hovered) {
            return false;
        }
        if (button == 0) {
            this.changing = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int button, int x, int y, boolean offscreen) {
        updateHovered(x, y, offscreen);
        this.changing = false;
        ((dev.zenhao.melon.gui.settingpanel.component.components.Pane) getFatherPanel()).updateLayout();
        return false;
    }

    @Override
    public boolean isVisible() {
        return this.getValue().visible();
    }
}

