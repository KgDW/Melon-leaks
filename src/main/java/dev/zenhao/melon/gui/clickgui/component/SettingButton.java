package dev.zenhao.melon.gui.clickgui.component;

import dev.zenhao.melon.gui.clickgui.component.Component;
import dev.zenhao.melon.setting.ModeSetting;
import dev.zenhao.melon.setting.Setting;

public abstract class SettingButton<T>
extends Component {
    private Setting<T> value;

    public Setting<T> getValue() {
        return this.value;
    }

    public ModeSetting getAsModeValue() {
        return (ModeSetting)this.value;
    }

    public void setValue(Setting<T> value) {
        this.value = value;
    }
}

