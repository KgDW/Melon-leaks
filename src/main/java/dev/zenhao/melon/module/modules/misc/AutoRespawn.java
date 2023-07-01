package dev.zenhao.melon.module.modules.misc;

import dev.zenhao.melon.event.events.gui.GuiScreenEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.chat.ChatUtil;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by 086 on 9/04/2018.
 * Updated 16 November 2019 by hub
 */
@Module.Info(name = "AutoRespawn", description = "Automatically respawn after dying", category = Category.MISC)
public class AutoRespawn extends Module {

    private final Setting<Boolean> respawn = bsetting("Respawn", true);
    private final Setting<Boolean> deathCoords = bsetting("DeadCoords", true);
    private final Setting<Boolean> antiGlitchScreen = bsetting("AntiGlitchScreen", true);

    @SubscribeEvent
    public void listener(GuiScreenEvent.Displayed event) {

        if (!(event.getScreen() instanceof GuiGameOver)) {
            return;
        }

        if (deathCoords.getValue() && mc.player.getHealth() <= 0) {
            ChatUtil.NoSpam.sendMessage(String.format("You died at x %d y %d z %d", (int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ));
        }

        if (respawn.getValue() || (antiGlitchScreen.getValue() && mc.player.getHealth() > 0)) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }

    }

}
