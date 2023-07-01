package dev.zenhao.melon.gui.settingpanel.layout;

import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;

import java.util.Map;

public class Layout {
    private Map<AbstractComponent, int[]> componentLocations;
    private int maxHeight;
    private int maxWidth;

    Layout(Map<AbstractComponent, int[]> componentLocations, int maxHeight, int maxWidth) {
        this.componentLocations = componentLocations;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    public Map<AbstractComponent, int[]> getComponentLocations() {
        return this.componentLocations;
    }

    public void setComponentLocations(Map<AbstractComponent, int[]> componentLocations) {
        this.componentLocations = componentLocations;
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMaxWidth() {
        return this.maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
}

