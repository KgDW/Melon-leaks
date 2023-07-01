package dev.zenhao.melon.module.modules.render;

import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.FloatSetting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Info(name = "CustomFov" , category = Category.RENDER)
public class CustomFov extends Module{
    public FloatSetting fov = fsetting("Fov" , 130f, 70f, 200f);
    @SubscribeEvent
    public void onFov(EntityViewRenderEvent.FOVModifier event) {
        event.setFOV(fov.getValue());
    }
}
