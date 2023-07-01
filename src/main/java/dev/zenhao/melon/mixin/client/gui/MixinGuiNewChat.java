package dev.zenhao.melon.mixin.client.gui;

import dev.zenhao.melon.module.ModuleManager;
import dev.zenhao.melon.module.modules.render.Animations;
import dev.zenhao.melon.utils.animations.EaseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class MixinGuiNewChat extends Gui {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    public abstract int getLineCount();

    @Shadow
    @Final
    private List<ChatLine> drawnChatLines;

    @Shadow
    public abstract boolean getChatOpen();

    @Shadow
    public abstract float getChatScale();

    @Shadow
    public abstract int getChatWidth();

    @Shadow
    private int scrollPos;

    @Shadow
    private boolean isScrolled;

    /**
     * @author fdp client
     * @reason no
     */
    @Overwrite
    public void drawChat(int updateCounter) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.drawnChatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;
            if (k > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int l = MathHelper.ceil((float) this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                int i1;
                int j1;
                int l1;
                for (i1 = 0; i1 + this.scrollPos < this.drawnChatLines.size() && i1 < i; ++i1) {
                    ChatLine chatline = this.drawnChatLines.get(i1 + this.scrollPos);
                    if (chatline != null) {
                        j1 = updateCounter - chatline.getUpdatedCounter();
                        if (j1 < 200 || flag) {
                            double d0 = (double) j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 *= 10.0D;
                            d0 = MathHelper.clamp(d0, 0.0D, 1.0D);
                            d0 *= d0;
                            l1 = (int) (255.0D * d0);
                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int) ((float) l1 * f);
                            ++j;

                            if (l1 > 3) {
                                GL11.glPushMatrix();

                                int i2 = 0;
                                int j2 = -i1 * 9;

                                if (ModuleManager.getModuleByClass(Animations.class).isEnabled() && Animations.INSTANCE.chat.getValue() && !flag) {
                                    if (j1 <= 20) {
                                        GL11.glTranslatef((float) (-(l + 4) * EaseUtils.INSTANCE.easeInExpo(1 - ((j1 + mc.timer.renderPartialTicks) / 20.0))), 0F, 0F);
                                    }
                                    if (j1 >= 180) {
                                        GL11.glTranslatef((float) (-(l + 4) * EaseUtils.INSTANCE.easeInCubic(((j1 + mc.timer.renderPartialTicks) - 180) / 20.0)), 0F, 0F);
                                    }
                                }

                                drawRect(-2, j2 - 9, l + 4, j2, l1 / 2 << 24);
                                GlStateManager.enableBlend();
                                mc.fontRenderer.drawStringWithShadow(chatline.getChatComponent().getFormattedText(), (float) i2, (float) (j2 - 8), 16777215 + (l1 << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();

                                GL11.glPopMatrix();
                            }
                        }
                    }
                }

                if (flag) {
                    i1 = this.mc.fontRenderer.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * i1 + k;
                    j1 = j * i1 + j;
                    int j3 = this.scrollPos * j1 / k;
                    int k1 = j1 * j1 / l2;
                    if (l2 != j1) {
                        l1 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (l1 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (l1 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }
}
