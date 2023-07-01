package dev.zenhao.melon.module.modules.movement;

import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Info(name = "AutoWalk", category = Category.MOVEMENT)
public class AutoWalk extends Module {
    @SubscribeEvent
    public void onUpdateInput(InputUpdateEvent event) {
        if (fullNullCheck()) {
            return;
        }
        event.getMovementInput().moveForward = 1.0f;
    }
}