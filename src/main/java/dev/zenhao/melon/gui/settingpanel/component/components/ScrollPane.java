package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.gui.settingpanel.layout.ILayoutManager;
import dev.zenhao.melon.utils.render.GLWindowView;
import dev.zenhao.melon.utils.render.RenderUtils;
import java.awt.Color;

public class ScrollPane extends Pane {
    private static final double SCROLL_AMOUNT = 0.25;
    private int scrollOffset = 0;
    private int realHeight;

    public ScrollPane(ILayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public void updateLayout() {
        this.updateLayout(this.getWidth(), Integer.MAX_VALUE, true);
    }

    @Override
    protected void updateLayout(int width, int height, boolean changeHeight) {
        super.updateLayout(width, height, false);
        this.realHeight = this.layout.getMaxHeight();
        this.validateOffset();
    }

    @Override
    public void render() {
        GLWindowView.start(this.x, this.y, this.getWidth(), this.getHeight());

        super.render();

        GLWindowView.end();

        int maxY = this.realHeight - this.getHeight();
        if (maxY > 0) {
            int sliderHeight = (int)((double)this.getHeight() / (double)this.realHeight * (double)this.getHeight());
            int sliderWidth = 3;
            RenderUtils.drawRoundedRectangle(this.x + this.getWidth() - sliderWidth, (double)this.y + (double)(this.getHeight() - sliderHeight) * ((double)this.scrollOffset / (double)maxY), sliderWidth, sliderHeight, 1.0, new Color(255, 255, 255, 255/2));
        }
    }

    @Override
    protected void updateComponentLocation() {
        for (AbstractComponent component : this.components) {
            if (!component.isVisible()) continue;
            int[] ints = this.componentLocations.get(component);
            if (ints == null) {
                this.updateLayout();
                this.updateComponentLocation();
                return;
            }
            component.setX(this.x + ints[0]);
            component.setY(this.y + ints[1] - this.scrollOffset);
        }
    }

    @Override
    public boolean mouseWheel(int change) {
        if (hovered){
            this.scrollOffset = (int)((double)this.scrollOffset - (double)change * SCROLL_AMOUNT);
            this.validateOffset();
        }
        return super.mouseWheel(change);
    }

    private void validateOffset() {
        if (this.scrollOffset > this.realHeight - this.getHeight()) {
            this.scrollOffset = this.realHeight - this.getHeight();
        }
        if (this.scrollOffset < 0) {
            this.scrollOffset = 0;
        }
    }

    @Override
    public boolean mouseMove(int x, int y, boolean offscreen) {
        return super.mouseMove(x, y, offscreen || x < this.x || y < this.y || x > this.x + this.getWidth() || y > this.y + this.getHeight());
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        return super.mousePressed(button, x, y, offscreen || x < this.x || y < this.y || x > this.x + this.getWidth() || y > this.y + this.getHeight());
    }

    @Override
    public boolean mouseReleased(int button, int x, int y, boolean offscreen) {
        return super.mouseReleased(button, x, y, offscreen || x < this.x || y < this.y || x > this.x + this.getWidth() || y > this.y + this.getHeight());
    }
}

