package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.Window;
import dev.zenhao.melon.gui.settingpanel.component.AbstractSettingComponent;
import dev.zenhao.melon.manager.GuiManager;
import dev.zenhao.melon.setting.ModeSetting;
import dev.zenhao.melon.utils.render.FadeUtils;
import dev.zenhao.melon.utils.render.RenderUtils;
import org.lwjgl.opengl.GL11;

public class ComboBox
        extends AbstractSettingComponent<Enum> {
    private static final int PREFERRED_WIDTH = 180;
    private static final int PREFERRED_HEIGHT = 22;
    private final int preferredWidth;
    private final int preferredHeight;
    private final FadeUtils fadeHeight = new FadeUtils(170L).end();
    private boolean hoveredExtended;
    private boolean opened;
    private int mouseY;
    private int angleArrow = 0;

    public ComboBox(ModeSetting value, int width, int height) {
        this.preferredWidth = width;
        this.preferredHeight = height;
        this.setValue(value);
        this.setWidth(preferredWidth);
        this.updateHeight();
    }

    public ComboBox(ModeSetting value) {
        this(value, PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }


    private void updateHeight() {
        if (this.opened) {
            int heightF = (int) ((this.preferredHeight * (this.getAsModeValue().getModes().length - 1)) * fadeHeight.getFade(FadeUtils.FadeMode.FADE_ONE));
            this.setHeight(this.preferredHeight + heightF + 4);
        } else {
            int heightF = (int) ((this.preferredHeight * (this.getAsModeValue().getModes().length - 1)) * (1 - fadeHeight.getFade(FadeUtils.FadeMode.FADE_ONE)));
            this.setHeight(this.preferredHeight + heightF);
        }
    }

    @Override
    public void render() {
        this.updateHeight();
        RenderUtils.drawRoundedRectangle(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, Window.TERTIARY_FOREGROUND);
        if (this.hovered) {
            RenderUtils.drawRoundedRectangle(this.x, this.y, this.getWidth(), this.preferredHeight, 7.0, Window.SECONDARY_FOREGROUND);
        } else if (this.hoveredExtended) {
            int offset = this.preferredHeight + 4;
            int modeLength = this.getAsModeValue().getModes().length;
            for (int i = 0; i < modeLength; ++i) {
                if (i == getSelectedIndex()) continue;
                int height = this.preferredHeight;
                if ((this.getAsModeValue().getIndexValue() != 0 ? i == 0 : i == 1) || (getSelectedIndex() == modeLength - 1 ? i == modeLength - 2 : i == modeLength - 1)) {
                    ++height;
                }
                if (this.mouseY >= this.getY() + offset && this.mouseY <= this.getY() + offset + this.preferredHeight) {
                    RenderUtils.drawRoundedRectangle(this.x, this.y + offset, this.getWidth(), this.preferredHeight, 7.0, Window.SECONDARY_FOREGROUND);
                    break;
                }
                offset += height;
            }
        }
        RenderUtils.drawRoundedRectangle(this.x + this.getWidth() - this.preferredHeight, this.y, this.preferredHeight, this.getHeight(), 7.0, this.hovered || this.opened ? Window.TERTIARY_FOREGROUND : Window.SECONDARY_FOREGROUND);
        GL11.glPushMatrix();
        {
            double y1, x1, y2, x2, y3, x3;
            if (this.angleArrow >= 360) {
                this.angleArrow = 0;
            }
            if (this.opened) {
                if (this.angleArrow < 180) {
                    this.angleArrow += 18;
                } else if (this.angleArrow > 180) {
                    if (this.angleArrow == 189) {
                        this.angleArrow -= 18;
                    } else {
                        this.angleArrow += 36;
                    }
                }
            } else {
                if (this.angleArrow != 0) {
                    this.angleArrow += 18;
                }
            }
            GL11.glTranslated(this.x + this.getWidth() - (this.preferredHeight / 2.0), this.y + (this.preferredHeight / 2.0), 0);
            GL11.glRotated(angleArrow, 0, 0, 1);

            x1 = -(double) this.preferredHeight / 4.0;
            y1 = -(double) this.preferredHeight / 4.0;
            x2 = 0;
            y2 = (double) this.preferredHeight / 4.0;
            x3 = (double) this.preferredHeight / 4.0;
            y3 = -(double) this.preferredHeight / 4.0;
            if (GuiManager.getINSTANCE().isRainbow()) {
                RenderUtils.drawTriangle(x1, y1, x2, y2, x3, y3, GuiManager.getINSTANCE().getRainbowColor());
            } else {
                RenderUtils.drawTriangle(x1, y1, x2, y2, x3, y3, Window.FOREGROUND);
            }
        }
        GL11.glPopMatrix();
        String text = this.getAsModeValue().getValueAsString();
        Window.getFontRenderer().drawString(text, this.x + 4, (float) this.y + (float) this.preferredHeight / 2.0f - (float) Window.getFontRenderer().getHeight() / 2.0f, Window.FONT.getRGB());
        int offset = this.preferredHeight + 8;
        for (int i = 0; i < this.getAsModeValue().getModes().length; ++i) {
            if ((offset + this.preferredHeight - 8) > getHeight()) break;
            if (i == getSelectedIndex()) continue;
            Window.getFontRenderer().drawString(this.getAsModeValue().getModesAsStrings()[i], this.x + 4, (float) (this.y + offset) + (float) Window.getFontRenderer().getHeight() / 2.0f, Window.FONT.getRGB());
            offset += this.preferredHeight;
        }
        if (GuiManager.getINSTANCE().isRainbow()) {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, RenderUtils.GradientDirection.LeftToRight, GuiManager.getINSTANCE().getRainbowColor(), GuiManager.getINSTANCE().getAddRainbowColor());
        } else {
            RenderUtils.drawRoundedRectangleOutline(this.x, this.y, this.getWidth(), this.getHeight(), 7.0, 1.0f, this.hovered && !this.opened ? Window.TERTIARY_OUTLINE : Window.SECONDARY_OUTLINE);
        }
    }

    protected void updateHovered(int x, int y, boolean offscreen) {
        this.hovered = !offscreen && x >= this.x && y >= this.y && x <= this.x + this.getWidth() && y <= this.y + this.preferredHeight;
        this.hoveredExtended = !offscreen && x >= this.x && y >= this.y && x <= this.x + this.getWidth() && y <= this.y + this.getHeight();
        this.mouseY = y;
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        this.updateHovered(x, y, offscreen);
        if (button != 0) {
            return false;
        }
        if (this.hovered) {
            fadeHeight.reset();
            this.setOpened(!this.opened);
            this.updateHeight();
            return true;
        }
        if (this.hoveredExtended && this.opened) {
            fadeHeight.reset();
            int offset = this.y + this.preferredHeight + 4;
            for (int i = 0; i < this.getAsModeValue().getModes().length; ++i) {
                if (i == getSelectedIndex()) continue;
                if (y >= offset && y <= offset + this.preferredHeight) {
                    this.setSelectedChecked(i);
                    this.setOpened(false);
                    break;
                }
                offset += this.preferredHeight;
            }
            this.updateHovered(x, y, offscreen);
            return true;
        }
        return false;
    }

    private void setSelectedChecked(int i) {
        if (this.getValue().visible()) {
            this.getAsModeValue().setValueByIndex(i);
            ((Pane) getFatherPanel()).updateLayout();
        }
    }

    private int getSelectedIndex() {
        return this.getAsModeValue().getIndexValue();
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
        this.updateHeight();
    }

    @Override
    public boolean isVisible() {
        return this.getValue().visible();
    }
}

