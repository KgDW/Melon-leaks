package dev.zenhao.melon.module.modules.player;

import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.setting.Setting;

@Module.Info(name = "LowOffHand", category = Category.PLAYER)
public class LowOffHand extends Module {
    private final Setting<Float> offhandHeight = fsetting("Height",0.5f,0.1f,1);

    @Override
    public void onUpdate() {
        LowOffHand.mc.entityRenderer.itemRenderer.equippedProgressOffHand = this.offhandHeight.getValue();
    }

    @Override
    public String getHudInfo() {
        return "[" + this.offhandHeight.getValue().toString() + "]";
    }
}
