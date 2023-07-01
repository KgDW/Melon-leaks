package dev.zenhao.melon.event.events.client;

import dev.zenhao.melon.event.EventStage;
import dev.zenhao.melon.module.IModule;
import dev.zenhao.melon.setting.Setting;

public class SettingChangeEvent
extends EventStage {
    private IModule iModule;
    private Setting setting;

    public SettingChangeEvent(int stage, IModule iModule) {
        super(stage);
        this.iModule = iModule;
    }

    public SettingChangeEvent(Setting setting) {
        super(2);
        this.setting = setting;
    }

    public IModule getModule() {
        return this.iModule;
    }

    public Setting getSetting() {
        return this.setting;
    }
}

