package dev.zenhao.melon.utils.render.gui;

import dev.zenhao.melon.utils.render.FadeUtils;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

public class ScalaCalc {
    public final FadeUtils fadeUtils = new FadeUtils(700);
    public double percent;
    public FadeUtils.FadeMode fadeMode = FadeUtils.FadeMode.FADE_ONE;

    public ScalaCalc() {
        reset();
    }

    public void drawA(ScaledResolution sr) {
        GL11.glPushMatrix();
        this.percent = fadeUtils.getFade(fadeMode);
        GL11.glTranslated(sr.getScaledWidth() / 2.0, sr.getScaledHeight() / 2.0, 0);
        GL11.glScaled(percent, percent, 0);
        GL11.glTranslated(-sr.getScaledWidth() / 2.0, -sr.getScaledHeight() / 2.0, 0);
    }

    public void drawB(ScaledResolution sr) {
        GL11.glTranslated(sr.getScaledWidth() / 2.0, sr.getScaledHeight() / 2.0, 0);
        GL11.glScaled(1 / percent, 1 / percent, 0);
        GL11.glTranslated(-sr.getScaledWidth() / 2.0, -sr.getScaledHeight() / 2.0, 0);
        GL11.glTranslated(sr.getScaledWidth() / 2.0, sr.getScaledHeight() / 2.0, 0);
        GL11.glScaled(percent, percent, 0);
        GL11.glTranslated(-sr.getScaledWidth() / 2.0, -sr.getScaledHeight() / 2.0, 0);
        GL11.glPopMatrix();
    }

    public void reset() {
        fadeUtils.reset();
    }

    public ScalaCalc setAnimationTime(long ms) {
        this.fadeUtils.setLength(ms);
        return this;
    }

    public ScalaCalc setFadeMode(FadeUtils.FadeMode fadeMode) {
        this.fadeMode = fadeMode;
        return this;
    }
}
