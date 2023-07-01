package dev.zenhao.melon.module.modules.render;

import com.mojang.authlib.GameProfile;
import dev.zenhao.melon.event.events.client.PacketEvent;
import dev.zenhao.melon.event.events.render.RenderEvent;
import dev.zenhao.melon.module.Category;
import dev.zenhao.melon.module.Module;
import dev.zenhao.melon.setting.BooleanSetting;
import dev.zenhao.melon.setting.IntegerSetting;
import dev.zenhao.melon.setting.Setting;
import dev.zenhao.melon.utils.gl.MelonTessellator;
import dev.zenhao.melon.utils.render.TotemPopCham;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.GL_QUADS;

@Module.Info(name = "PopChams", description = "Renders when someone pops", category = Category.RENDER)
public class PopChams extends Module {
    public static PopChams INSTANCE = new PopChams();
    public BooleanSetting self = bsetting("Self", false);
    public IntegerSetting rF = isetting("RedFill", 255, 0, 255);
    public IntegerSetting gF = isetting("GreenFill", 26, 0, 255);
    public IntegerSetting bF = isetting("BlueFill", 42, 0, 255);
    public IntegerSetting aF = isetting("AlphaFill", 42, 0, 255);
    public IntegerSetting fadestart = isetting("FadeStart", 200, 0, 3000);
    public Setting<Double> fadetime = dsetting("FadeTime", 0.5D, 0.0D, 2.0D);
    public BooleanSetting onlyOneEsp = bsetting("OnlyOneEsp", true);
    EntityOtherPlayerMP player;
    ModelPlayer playerModel;
    Long startTime;
    double alphaFill;

    public static Color newAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static void glColor(Color color) {
        GL11.glColor4f((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F, (float) color.getAlpha() / 255.0F);
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) != null && (this.self.getValue() || packet.getEntity(mc.world).getEntityId() != mc.player.getEntityId())) {
                GameProfile profile = new GameProfile(mc.player.getUniqueID(), "");
                (this.player = new EntityOtherPlayerMP(mc.world, profile)).copyLocationAndAnglesFrom(packet.getEntity(mc.world));
                this.playerModel = new ModelPlayer(0.0F, false);
                this.startTime = System.currentTimeMillis();
                this.playerModel.bipedHead.showModel = false;
                this.playerModel.bipedBody.showModel = false;
                this.playerModel.bipedLeftArmwear.showModel = false;
                this.playerModel.bipedLeftLegwear.showModel = false;
                this.playerModel.bipedRightArmwear.showModel = false;
                this.playerModel.bipedRightLegwear.showModel = false;
                this.alphaFill = (double) this.aF.getValue();
            }
        }
    }

    @Override
    public void onWorldRender(RenderEvent event) {
        if (this.onlyOneEsp.getValue()) {
            if (this.player == null || mc.world == null || mc.player == null) {
                return;
            }

            GL11.glLineWidth(1.0F);
            Color fillColorS = new Color(this.rF.getValue(), this.bF.getValue(), this.gF.getValue(), this.aF.getValue());
            int fillA = fillColorS.getAlpha();
            long time = System.currentTimeMillis() - this.startTime - ((Number) this.fadestart.getValue()).longValue();
            if (System.currentTimeMillis() - this.startTime > ((Number) this.fadestart.getValue()).longValue()) {
                double normal = this.normalize((double) time, 0.0D, ((Number) this.fadetime.getValue()).doubleValue());
                normal = MathHelper.clamp(normal, 0.0D, 1.0D);
                normal = -normal + 1.0D;
                fillA *= (int) normal;
            }

            Color fillColor = newAlpha(fillColorS, fillA);
            if (this.player != null && this.playerModel != null) {
                MelonTessellator.INSTANCE.prepare(GL_QUADS);
                GL11.glPushAttrib(1048575);
                GL11.glEnable(2881);
                GL11.glEnable(2848);
                if (this.alphaFill > 1.0D) {
                    this.alphaFill -= this.fadetime.getValue();
                }

                Color fillFinal = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int) this.alphaFill);

                glColor(fillFinal);
                GL11.glPolygonMode(1032, 6914);
                TotemPopCham.renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0F);
                GL11.glPolygonMode(1032, 6913);
                TotemPopCham.renderEntity(this.player, this.playerModel, this.player.limbSwing, this.player.limbSwingAmount, 1.0F);
                GL11.glPolygonMode(1032, 6914);
                GL11.glPopAttrib();
                MelonTessellator.release();
            }
        } else if (!onlyOneEsp.getValue()) {
            new TotemPopCham(this.player, this.playerModel, this.startTime, this.alphaFill);
        }
    }

    public double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}
 
