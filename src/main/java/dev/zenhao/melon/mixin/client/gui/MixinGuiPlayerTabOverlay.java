package dev.zenhao.melon.mixin.client.gui;

import dev.zenhao.melon.module.modules.misc.ExtraTab;
import dev.zenhao.melon.module.modules.render.Animations;
import dev.zenhao.melon.module.modules.render.TabFriends;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Created by 086 on 8/04/2018.
 */
@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay extends Gui {
    /*
    @Shadow
    @Final
    private Minecraft mc;
    @Inject(method = "renderPlayerlist", at = @At("HEAD"))
    public void renderPlayerListPre(int p_renderPlayerlist_1_, Scoreboard p_renderPlayerlist_2_, ScoreObjective p_renderPlayerlist_3_, CallbackInfo ci) {
        final Animations animations = Animations.INSTANCE;

        if (animations.isEnabled() && animations.tab.getValue()) {
            GL11.glPushMatrix();
            float change = System.currentTimeMillis() / (float) animations.tabTime.getValue();

            ScaledResolution sr = new ScaledResolution(mc);
            GL11.glTranslatef(0, sr.getScaledHeight() * (change - 1), 0);
        }
    }

    @Inject(method = "renderPlayerlist", at = @At("RETURN"))
    public void renderPlayerListPost(int p_renderPlayerlist_1_, Scoreboard p_renderPlayerlist_2_, ScoreObjective p_renderPlayerlist_3_, CallbackInfo ci) {
        Animations animations = Animations.INSTANCE;
        if (animations.isEnabled() && animations.tab.getValue()) {
            GL11.glPopMatrix();
        }
    }
     */

    @Redirect(method = {"renderPlayerlist"}, at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;", remap = false))
    public List<NetworkPlayerInfo> subListHook(final List<NetworkPlayerInfo> list, final int fromIndex, final int toIndex) {
        return list.subList(fromIndex, ExtraTab.getINSTANCE().isEnabled() ? Math.min(ExtraTab.getINSTANCE().size.getValue(), list.size()) : toIndex);
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> returnable) {
        if (TabFriends.INSTANCE.isEnabled()) {
            returnable.cancel();
            returnable.setReturnValue(TabFriends.getPlayerName(networkPlayerInfoIn));
        }
    }
}
