package dev.zenhao.melon.gui.settingpanel.layout;

import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;
import dev.zenhao.melon.gui.settingpanel.layout.Layout;
import dev.zenhao.melon.gui.settingpanel.component.AbstractComponent;

import java.util.List;

public interface ILayoutManager {
    int[] getOptimalDiemension(List<AbstractComponent> var1, int var2);

    Layout buildLayout(List<AbstractComponent> var1, int var2, int var3);
}

