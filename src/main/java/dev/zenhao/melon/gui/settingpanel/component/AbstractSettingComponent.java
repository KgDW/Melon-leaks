package dev.zenhao.melon.gui.settingpanel.component;

import dev.zenhao.melon.setting.ModeSetting;
import dev.zenhao.melon.setting.Setting;

public abstract class AbstractSettingComponent<T> extends AbstractComponent {
    private Setting<T> value;

    private AbstractComponent fatherPanel;

    public Setting<T> getValue() {
        return this.value;
    }

    public ModeSetting getAsModeValue() {
        return (ModeSetting)this.value;
    }

    public void setValue(Setting<T> value) {
        this.value = value;
    }

    public AbstractComponent getFatherPanel() {
        return fatherPanel;
    }

    public AbstractComponent setFatherPanel(AbstractComponent fatherPanel) {
        this.fatherPanel = fatherPanel;
        return this;
    }
}
