package dev.zenhao.melon.module.modules.render;

import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.BooleanSetting;
import dev.zenhao.melon.setting.IntegerSetting;
import dev.zenhao.melon.utils.animations.AnimationFlag;
import dev.zenhao.melon.utils.animations.Easing;

@Module.Info(name = "Animations", category = Category.RENDER)
public class Animations extends Module {
    public static Animations INSTANCE = new Animations();
    public AnimationFlag hotbarAnimation = new AnimationFlag(Easing.OUT_CUBIC, 200.0f);
    public BooleanSetting hotbar = bsetting("Hotbar", true);
    public BooleanSetting inv = bsetting("Inventory", true);
    public IntegerSetting invTime = isetting("InventoryTime", 500, 1, 1000).b(inv);
    public BooleanSetting chat = bsetting("Chat" , true);

    public Animations() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            return;
        }
        float currentPos = mc.player.inventory.currentItem * 20.0f;
        hotbarAnimation.forceUpdate(currentPos, currentPos);
    }

    public float updateHotbar() {
        float currentPos = mc.player.inventory.currentItem * 20f;
        return hotbarAnimation.getAndUpdate(currentPos);
    }
}