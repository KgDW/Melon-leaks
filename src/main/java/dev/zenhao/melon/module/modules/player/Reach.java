package dev.zenhao.melon.module.modules.player;

import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;

@Module.Info(name = "Reach", category = Category.PLAYER)
public class Reach extends Module {
    public Setting<Float> range = fsetting("Range", 6, 1, 10);
    public static Reach INSTANCE;

    public static boolean isEnable() {
        return INSTANCE.isEnabled();
    }

    @Override
    public String getHudInfo() {
        return this.range.getValue().toString();
    }

    public static float getReach() {
        return INSTANCE.range.getValue();
    }

    public Reach() {
        INSTANCE = this;
    }
}
