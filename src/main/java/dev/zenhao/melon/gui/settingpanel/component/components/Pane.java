package dev.zenhao.melon.gui.settingpanel.component.components;

import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.gui.settingpanel.layout.ILayoutManager;
import dev.zenhao.melon.gui.settingpanel.layout.Layout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Pane
        extends AbstractComponent {
    protected List<AbstractComponent> components = new ArrayList<>();
    protected Map<AbstractComponent, int[]> componentLocations = new HashMap<>();
    protected Layout layout;
    private ILayoutManager layoutManager;

    public Pane(ILayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void render() {
        if (this.isSizeChanged()) {
            this.updateLayout();
            this.resetSizeChanged();
        }
        this.updateComponentLocation();
        this.components.stream().filter(AbstractComponent::isVisible).forEach(AbstractComponent::render);
    }

    @Override
    public boolean isSizeChanged() {
        for (AbstractComponent component : this.components) {
            if (!component.isSizeChanged()) continue;
            return true;
        }
        return super.isSizeChanged();
    }

    private void resetSizeChanged() {
        this.components.forEach(abstractComponent -> abstractComponent.setSizeChanged(false));
    }

    protected void updateComponentLocation() {
        this.components.stream().filter(AbstractComponent::isVisible).forEach(abstractComponent -> {
            int[] ints = this.componentLocations.get(abstractComponent);
            if (ints == null) {
                this.updateLayout();
                this.updateComponentLocation();
                return;
            }
            abstractComponent.setX(this.getX() + ints[0]);
            abstractComponent.setY(this.getY() + ints[1]);
        });
    }

    public void updateLayout() {
        this.updateLayout(this.getWidth(), this.getHeight(), true);
    }

    protected void updateLayout(int width, int height, boolean changeHeight) {
        this.layout = this.layoutManager.buildLayout(this.components, width, height);
        this.componentLocations = this.layout.getComponentLocations();
        if (changeHeight) {
            this.setHeight(this.layout.getMaxHeight());
        }
    }

    public void addComponent(AbstractComponent component) {
        this.components.add(component);
        this.updateLayout(super.getWidth(), super.getHeight(), true);
    }

    public List<AbstractComponent> getComponent() {
        return this.components;
    }

    public void removeComponent(AbstractComponent component) {
        this.components.remove(component);
        this.updateLayout(super.getWidth(), super.getHeight(), true);
    }

    @Override
    public boolean mouseMove(int x, int y, boolean offscreen) {
        boolean[] consumed = new boolean[]{false};
        this.components.stream().filter(AbstractComponent::isVisible).sorted(Comparator.comparingInt(AbstractComponent::getEventPriority)).forEach(component -> {
            if (!consumed[0] && component.mouseMove(x, y, offscreen)) {
                consumed[0] = true;
            }
        });
        super.mouseMove(x, y, offscreen);
        return consumed[0];
    }

    @Override
    public boolean mousePressed(int button, int x, int y, boolean offscreen) {
        boolean[] consumed = new boolean[]{false};
        this.components.stream().filter(AbstractComponent::isVisible).sorted(Comparator.comparingInt(AbstractComponent::getEventPriority)).forEach(component -> {
            if (!consumed[0] && component.mousePressed(button, x, y, offscreen)) {
                consumed[0] = true;
            }
        });
        super.mousePressed(button, x, y, offscreen);
        return consumed[0];
    }

    @Override
    public boolean mouseReleased(int button, int x, int y, boolean offscreen) {
        boolean[] consumed = new boolean[]{false};
        this.components.stream().filter(AbstractComponent::isVisible).sorted(Comparator.comparingInt(AbstractComponent::getEventPriority)).forEach(component -> {
            if (!consumed[0] && component.mouseReleased(button, x, y, offscreen)) {
                consumed[0] = true;
            }
        });
        super.mouseReleased(button, x, y, offscreen);
        return consumed[0];
    }

    @Override
    public boolean keyPressed(char c, int key) {
        boolean[] consumed = new boolean[]{false};
        this.components.stream().filter(AbstractComponent::isVisible).sorted(Comparator.comparingInt(AbstractComponent::getEventPriority)).forEach(component -> {
            if (!consumed[0] && component.keyPressed(c, key)) {
                consumed[0] = true;
            }
        });
        super.keyPressed(c, key);
        return consumed[0];
    }

    @Override
    public int getWidth() {
        if (super.getWidth() <= 0) {
            this.updateSize();
        }
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        if (super.getHeight() <= 0) {
            this.updateSize();
        }
        return super.getHeight();
    }

    public void updateSize() {
        this.components.stream().filter(abstractComponent -> abstractComponent instanceof  Pane).forEach(abstractComponent -> ((Pane) abstractComponent).updateSize());
        int[] optimalDiemension = this.layoutManager.getOptimalDiemension(this.components, Integer.MAX_VALUE);
        this.setWidth(optimalDiemension[0]);
        this.setHeight(optimalDiemension[1]);
    }

    public void clearComponents() {
        this.components.clear();
        this.updateLayout(super.getWidth(), super.getHeight(), true);
    }
}

