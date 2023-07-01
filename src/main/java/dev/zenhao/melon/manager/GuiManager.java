package dev.zenhao.melon.manager;

import dev.zenhao.melon.module.modules.client.Colors;
import dev.zenhao.melon.module.modules.client.CustomFont;
import dev.zenhao.melon.utils.Rainbow;
import dev.zenhao.melon.utils.color.ColorUtils;
import dev.zenhao.melon.utils.font.CFontRenderer;

import java.awt.*;

public class GuiManager {
    public static GuiManager INSTANCE;

    public GuiManager() {
        INSTANCE = this;
    }

    public static GuiManager getINSTANCE() {
        return INSTANCE;
    }

    public static int getBGColor(int alpha) {
        return new Color(64, 64, 64, alpha).getRGB();
    }

    public Colors getColorINSTANCE() {
        return Colors.getINSTANCE();
    }

    public boolean isNull() {
        return Colors.getINSTANCE() == null;
    }

    public int getNormalRed() {
        return this.isNull() ? 255 : this.getColorINSTANCE().color.getValue().getRed();
    }

    public int getNormalGreen() {
        return this.isNull() ? 0 : this.getColorINSTANCE().color.getValue().getGreen();
    }

    public int getNormalBlue() {
        return this.isNull() ? 0 : this.getColorINSTANCE().color.getValue().getBlue();
    }

    public int getNormalRGB() {
        return new Color(this.getNormalRed(), this.getNormalGreen(), this.getNormalBlue()).getRGB();
    }

    public CFontRenderer getFont() {
        return CustomFont.getCGFont();
    }

    public boolean isRainbow() {
        return !this.isNull() && this.getColorINSTANCE().rainbow.getValue();
    }

    public int getGradientIntensity() {
        return this.getColorINSTANCE().GradientIntensity.getValue();
    }

    public int getRainbow() {
        return Rainbow.getRainbow(this.getColorINSTANCE().rainbowSpeed.getValue(), this.getColorINSTANCE().rainbowSaturation.getValue(), this.getColorINSTANCE().rainbowBrightness.getValue());
    }

    public Color getRainbowColor() {
        return new Color(Rainbow.getRainbow(this.getColorINSTANCE().rainbowSpeed.getValue(), this.getColorINSTANCE().rainbowSaturation.getValue(), this.getColorINSTANCE().rainbowBrightness.getValue()));
    }

    public int getAddRainbow() {
        return Rainbow.getRainbow(this.getColorINSTANCE().rainbowSpeed.getValue(), this.getColorINSTANCE().rainbowSaturation.getValue(), this.getColorINSTANCE().rainbowBrightness.getValue(), (long) this.getColorINSTANCE().GradientIntensity.getValue() * 5L);
    }

    public Color getAddRainbowColor() {
        return new Color(this.getAddRainbow());
    }

    public int getRainbowColorAdd(long add, int alpha) {
        Color c = new Color(Rainbow.getRainbow(this.getColorINSTANCE().rainbowSpeed.getValue(), this.getColorINSTANCE().rainbowSaturation.getValue(), this.getColorINSTANCE().rainbowBrightness.getValue(), add));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha).getRGB();
    }

    public int getRainbowColorAdd(long add) {
        return this.getRainbowColorAdd(add, 255);
    }

    public int getRainbowRed() {
        return ColorUtils.getRed(this.getRainbow());
    }

    public int getRainbowGreen() {
        return ColorUtils.getGreen(this.getRainbow());
    }

    public int getRainbowBlue() {
        return ColorUtils.getBlue(this.getRainbow());
    }

    public int getRed() {
        return this.isRainbow() ? this.getRainbowRed() : this.getNormalRed();
    }

    public int getGreen() {
        return this.isRainbow() ? this.getRainbowGreen() : this.getNormalGreen();
    }

    public int getBlue() {
        return this.isRainbow() ? this.getRainbowBlue() : this.getNormalBlue();
    }

    public int getRGB() {
        return new Color(this.getRed(), this.getGreen(), this.getBlue()).getRGB();
    }

    public Color getColor() {
        return new Color(this.getRed(), this.getGreen(), this.getBlue());
    }

    public boolean isParticle() {
        return !this.isNull() && this.getColorINSTANCE().particle.getValue();
    }

    public boolean isSettingRect() {
        return !this.isNull() && this.getColorINSTANCE().setting.getValue() == Colors.SettingViewType.RECT;
    }

    public boolean isSettingSide() {
        return !this.isNull() && this.getColorINSTANCE().setting.getValue() == Colors.SettingViewType.SIDE;
    }

    public Background getBackground() {
        try {
            switch ((Colors.Background) this.getColorINSTANCE().background.getValue()) {
                case SHADOW: {
                    return Background.Shadow;
                }
                case BLUR: {
                    return Background.Blur;
                }
                case BOTH: {
                    return Background.Both;
                }
            }
            return Background.None;
        } catch (Exception ignored) {
            return Background.None;
        }
    }

    public enum Background {
        Shadow,
        Blur,
        Both,
        None

    }
}

